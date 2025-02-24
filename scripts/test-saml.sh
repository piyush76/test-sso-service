#!/bin/bash

# SAML Authentication Test Script
echo "Starting SAML Authentication Tests..."

# Setup test environment
mkdir -p src/test/resources/saml
cp ~/attachments/7d51b2b2-9265-456d-8734-61e1f7c22dd5/phs-saml.xml src/test/resources/saml/sample-response.xml

# Generate test certificates
keytool -genkeypair -alias hazcom -keyalg RSA -keysize 2048 \
  -keystore src/main/resources/saml/keystore.jks \
  -validity 365 -storepass changeit \
  -dname "CN=Hazcom SSO Test, OU=Development, O=Hazcom, L=Test, ST=Test, C=US"

# Start service
docker-compose down
docker-compose up -d

# Wait for service to start
echo "Waiting for service to start..."
sleep 10

# Test 1: Basic Authentication Flow
echo "Test 1: Testing Basic Authentication Flow"
curl -v http://localhost:8080/login 2>&1 | grep "Location"

# Test 2: SAML Response Validation
echo "Test 2: Testing SAML Response Validation"
curl -X POST -H "Content-Type: application/xml" \
  --data-binary @src/test/resources/saml/sample-response.xml \
  http://localhost:8080/login/saml2/sso/hazcom

# Test 3: Error Cases
echo "Test 3: Testing Error Cases"

# Test 3.1: Expired Response
cp src/test/resources/saml/sample-response.xml src/test/resources/saml/expired-response.xml
sed -i 's/NotOnOrAfter="2024-12-03T17:54:00.493Z"/NotOnOrAfter="2024-12-03T16:54:00.493Z"/' \
  src/test/resources/saml/expired-response.xml

echo "Testing expired SAML response..."
curl -X POST -H "Content-Type: application/xml" \
  --data-binary @src/test/resources/saml/expired-response.xml \
  http://localhost:8080/login/saml2/sso/hazcom

# Test 3.2: Invalid Signature
cp src/test/resources/saml/sample-response.xml src/test/resources/saml/invalid-sig-response.xml
sed -i 's/<DigestValue>.*<\/DigestValue>/<DigestValue>invalid<\/DigestValue>/' \
  src/test/resources/saml/invalid-sig-response.xml

echo "Testing invalid signature..."
curl -X POST -H "Content-Type: application/xml" \
  --data-binary @src/test/resources/saml/invalid-sig-response.xml \
  http://localhost:8080/login/saml2/sso/hazcom

# View logs
echo "Test Results from logs:"
docker-compose logs --tail 100
