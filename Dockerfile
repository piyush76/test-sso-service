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

# Generate SAML certificates during build
RUN mkdir -p src/main/resources/saml && \
    keytool -genkeypair \
    -alias hazcom \
    -keyalg RSA \
    -keysize 2048 \
    -storetype PKCS12 \
    -keystore src/main/resources/saml/keystore.jks \
    -validity 3650 \
    -storepass changeit \
    -keypass changeit \
    -dname "CN=Hazcom SSO Service, OU=IT, O=Hazcom, L=Unknown, ST=Unknown, C=US" && \
    keytool -exportcert \
    -alias hazcom \
    -keystore src/main/resources/saml/keystore.jks \
    -storepass changeit \
    -file src/main/resources/saml/public.cer \
    -rfc && \
    keytool -importkeystore \
    -srckeystore src/main/resources/saml/keystore.jks \
    -srcstorepass changeit \
    -srckeypass changeit \
    -srcalias hazcom \
    -destkeystore src/main/resources/saml/keystore.p12 \
    -deststoretype PKCS12 \
    -deststorepass changeit \
    -destkeypass changeit && \
    openssl pkcs12 -in src/main/resources/saml/keystore.p12 \
    -nodes \
    -nocerts \
    -passin pass:changeit \
    | openssl pkcs8 -topk8 -nocrypt > src/main/resources/saml/private.key && \
    chmod 600 src/main/resources/saml/private.key && \
    chmod 644 src/main/resources/saml/public.cer

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
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Expose application port
EXPOSE 8080

# Set JVM and application options
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV SPRING_CONFIG_LOCATION=classpath:/application.yml

# Start the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
