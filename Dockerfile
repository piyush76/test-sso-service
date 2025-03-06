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

# Copy source code and scripts
COPY src/ src/
COPY scripts/ scripts/

# Make script executable and generate certificates
RUN chmod +x scripts/generate-certs.sh && \
    scripts/generate-certs.sh \
    "/app/src/main/resources/saml/keystore.jks" \
    "/app/src/main/resources/saml/public.cer" \
    "/app/src/main/resources/saml/private.key" \
    "/app/src/main/resources/saml/keystore.p12"

# Build the application
RUN ./mvnw clean package -DskipTests -B

# Create the final image
FROM eclipse-temurin:17-jre-alpine

# Add non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Set working directory
WORKDIR /app

# Copy the built artifact and SAML resources
COPY --from=builder /app/target/*.jar app.jar
COPY --from=builder /app/src/main/resources/saml /app/src/main/resources/saml

# Create and set permissions for logs directory
RUN mkdir -p /logs && \
    chown -R spring:spring /app /logs && \
    chmod 755 /logs

# Switch to non-root user
USER spring:spring

# Expose application port
EXPOSE 8080

# Set JVM and application options
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV SPRING_CONFIG_LOCATION=classpath:/application.yml

# Start the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
