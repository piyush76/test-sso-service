# Use Eclipse Temurin JDK 17 Alpine AS base image
FROM eclipse-temurin:17-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Install necessary tools
RUN apk add --no-cache curl dos2unix openssl

# Copy Maven wrapper files first
COPY .mvn/ .mvn/
COPY mvnw mvnw.cmd ./
RUN chmod +x mvnw && \
    dos2unix mvnw

# Copy POM file separately to cache dependencies
COPY pom.xml ./
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src/ src/

# Build the application
RUN ./mvnw clean package -DskipTests -B

# Create the final image
FROM eclipse-temurin:17-jre-alpine

# Add non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Install OpenSSL and keytool
RUN apk add --no-cache openssl

# Set working directory
WORKDIR /app

# Create SAML directory and set permissions
RUN mkdir -p /app/saml && \
    chown -R spring:spring /app && \
    chmod 755 /app/saml

# Copy certificate generation script
COPY scripts/generate-certs.sh /app/
RUN chmod +x /app/generate-certs.sh

# Copy the built artifact
COPY --from=builder /app/target/*.jar app.jar
RUN chown spring:spring /app/app.jar

# Switch to non-root user
USER spring:spring

# Expose application port
EXPOSE 8080

# Set JVM and application options
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV SPRING_CONFIG_LOCATION=classpath:/application.yml

# Generate certificates and start the application
ENTRYPOINT ["/bin/sh", "-c", "/app/generate-certs.sh && java $JAVA_OPTS -jar app.jar"]
