# SAML Authentication Testing Guide

## Prerequisites
1. Access to customer's IdP test environment
2. SAML metadata file (provided in `phs-saml.xml`)
3. Test user credentials from the IdP
4. SSL certificates for local development

## Test Environment Setup

### 1. Configure Environment Variables
```bash
# Copy example env file
cp .env.example .env

# Update with test IdP details
SAML_IDP_METADATA_URL=https://sts.windows.net/2e319086-9a26-46a3-865f-615bed576786/federationmetadata/2007-06/federationmetadata.xml
SAML_SP_ENTITY_ID=https://app.maxcomsc.com/maxcomsc
SAML_SP_ACS_URL=https://app.maxcomsc.com/maxcomsc/login/saml2/sso/hazcom
```

### 2. Setup SAML Certificates
```bash
# Generate test certificates (for development only)
openssl req -x509 -newkey rsa:4096 -keyout src/main/resources/saml/private.key -out src/main/resources/saml/public.cer -days 365 -nodes
```

### 3. Start the Service
```bash
# Build and run with Docker Compose
docker-compose up -d

# View logs
docker-compose logs -f
```

## Test Cases

### 1. SSO Login Flow

#### 1.1 Initial Setup
1. Configure environment:
   ```bash
   # Copy and edit environment variables
   cp .env.example .env
   
   # Update environment variables for Azure AD test environment
   cat << EOF > .env
   # SAML Configuration for Azure AD Test Environment
   SAML_IDP_METADATA_URL=https://sts.windows.net/2e319086-9a26-46a3-865f-615bed576786/federationmetadata/2007-06/federationmetadata.xml
   SAML_SP_ENTITY_ID=https://app.maxcomsc.com/maxcomsc
   SAML_SP_ACS_URL=https://app.maxcomsc.com/maxcomsc/login/saml2/sso/hazcom
   
   # Keystore Configuration
   SAML_KEYSTORE_PATH=/app/saml/keystore.jks
   SAML_KEYSTORE_PASSWORD=changeit
   SAML_PRIVATE_KEY_ALIAS=hazcom
   SAML_PRIVATE_KEY_PASSWORD=changeit
   
   # Session Configuration
   SESSION_TIMEOUT=3900
   SESSION_COOKIE_NAME=HAZCOM_SESSION
   
   # Logging Configuration
   LOGGING_LEVEL_ROOT=DEBUG
   LOGGING_LEVEL_COM_HAZCOM=DEBUG
   LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY=DEBUG
   EOF
   
   # Generate test keystore
   keytool -genkeypair -alias hazcom -keyalg RSA -keysize 2048 \
     -keystore src/main/resources/saml/keystore.jks \
     -validity 365 \
     -storepass changeit \
     -dname "CN=Hazcom SSO Test, OU=Development, O=Hazcom, L=Test, ST=Test, C=US"
   ```

2. Start the service:
   ```bash
   docker-compose up -d
   docker-compose logs -f
   ```

#### 1.2 Test Authentication Flow
1. Access the service:
   ```bash
   curl -v http://localhost:8080/login
   ```
   Expected: 302 redirect to Azure AD login

2. Test with sample user:
   - Username: testuser1@providence.org
   - Verify redirect to: https://sts.windows.net/2e319086-9a26-46a3-865f-615bed576786/
   - After successful login, check redirect back to ACS URL

3. Monitor SAML flow:
   ```bash
   # View debug logs
   docker-compose logs -f | grep "SAML"
   ```

### 2. SAML Response Validation
Test the following scenarios using the Azure AD IdP:

#### 2.1 Valid Response Test
Verify these elements from the sample SAML response:
- Issuer: https://sts.windows.net/2e319086-9a26-46a3-865f-615bed576786/
- Authentication Method: http://schemas.microsoft.com/ws/2008/06/identity/authenticationmethod/x509
- NotBefore: Present and valid
- NotOnOrAfter: Present and valid (sample shows 65 minutes validity)
- Signature: Valid XML signature with RSA-SHA256
- Subject NameID Format: urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress

#### 2.2 Error Scenarios
Test the following error cases:
- Expired SAML response (wait for NotOnOrAfter to pass)
- Invalid signature (modify the response content)
- Missing required attributes
- Wrong audience value
- Invalid NotBefore time
- Incorrect issuer URL

### 3. User Attribute Verification
From the sample SAML file, verify the following attributes using test user (testuser1@providence.org):

Expected Attributes:
- Email (http://schemas.xmlsoap.org/ws/2005/05/identity/claims/mail): testuser1@providence.org
- Name (http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name): testuser1@providence.org
- Given Name (http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname): test
- Surname (http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname): user
- Display Name (http://schemas.xmlsoap.org/ws/2005/05/identity/claims/displayName): user1, test
- Tenant ID (http://schemas.microsoft.com/identity/claims/tenantid): 2e319086-9a26-46a3-865f-615bed576786
- Object ID (http://schemas.microsoft.com/identity/claims/objectidentifier): 3ea77e9d-3b79-433e-b1da-caa12646e573

### 4. Session Management
Test the following:
- Session creation after successful login
- Session timeout (configured in .env)
- Session invalidation on logout
- Session cookie security attributes

### 5. Test Result Documentation

#### 5.1 Record Test Execution
Use the provided template in `docs/test-results-template.md` to document your test results. This template includes:
- Test environment details
- Authentication flow verification
- SAML response validation
- User attribute verification
- Session management checks
- Security compliance validation

#### 5.2 Error Handling and Debugging
1. Update .env file:
   ```
   LOGGING_LEVEL_ROOT=DEBUG
   LOGGING_LEVEL_COM_HAZCOM=TRACE
   LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY=TRACE
   ```

2. Monitor specific components:
   ```bash
   # SAML Response validation
   docker-compose logs -f | grep "SAMLResponseValidator"
   
   # Authentication process
   docker-compose logs -f | grep "SAMLUserDetailsService"
   
   # Session management
   docker-compose logs -f | grep "SessionManagement"
   ```

#### 5.2 Test Error Scenarios
1. Invalid Metadata:
   ```bash
   # Test with invalid metadata URL
   sed -i 's/SAML_IDP_METADATA_URL=.*/SAML_IDP_METADATA_URL=https://invalid-url/' .env
   docker-compose up -d
   ```
   Expected: Error logs showing metadata fetch failure

2. Certificate Issues:
   ```bash
   # Rename certificate to simulate missing cert
   mv src/main/resources/saml/keystore.jks src/main/resources/saml/keystore.jks.bak
   docker-compose up -d
   ```
   Expected: Error logs about missing keystore

3. Test with Sample SAML Response:
   ```bash
   # Create a test directory
   mkdir -p src/test/resources/saml
   
   # Copy the sample SAML response
   cp ~/attachments/7d51b2b2-9265-456d-8734-61e1f7c22dd5/phs-saml.xml src/test/resources/saml/sample-response.xml
   
   # Test validation with sample response
   curl -X POST -H "Content-Type: application/xml" \
     --data-binary @src/test/resources/saml/sample-response.xml \
     http://localhost:8080/login/saml2/sso/hazcom
   ```
   
   Expected validation results:
   - Valid signature verification
   - NotBefore: 2024-12-03T16:49:00.493Z
   - NotOnOrAfter: 2024-12-03T17:54:00.493Z
   - Valid test user attributes
   
4. Test Error Cases:
   ```bash
   # Test expired response
   sed -i 's/NotOnOrAfter="2024-12-03T17:54:00.493Z"/NotOnOrAfter="2024-12-03T16:54:00.493Z"/' \
     src/test/resources/saml/sample-response.xml
   
   # Test invalid signature
   sed -i 's/<DigestValue>.*<\/DigestValue>/<DigestValue>invalid<\/DigestValue>/' \
     src/test/resources/saml/sample-response.xml
   
   # Test missing attributes
   sed -i '/<AttributeStatement>/,/<\/AttributeStatement>/d' \
     src/test/resources/saml/sample-response.xml
   ```
   Expected: Detailed validation errors in logs

#### 5.3 Verify Security Compliance
1. Check signature validation:
   ```bash
   docker-compose logs -f | grep "SignatureValidation"
   ```
   Expected: Logs showing signature verification steps

2. Monitor session security:
   ```bash
   # Check session creation and expiration
   docker-compose logs -f | grep "SessionManagement"
   ```
   Expected: Logs showing secure session handling

3. Verify SSL/TLS:
   ```bash
   # Test HTTPS enforcement
   curl -v http://localhost:8080/login
   ```
   Expected: Redirect to HTTPS

## Debugging

### Enable Debug Logging
```yaml
# application.yml
logging:
  level:
    org.springframework.security: DEBUG
    org.springframework.security.saml2: TRACE
    com.hazcom: DEBUG
```

### Common Issues and Solutions

1. Certificate Issues
```
Error: Unable to validate signature
Solution: 
- Verify the Azure AD certificate in the SAML response matches the one in your keystore
- Check certificate expiration (current cert valid until 2025-03-18)
- Ensure certificate path and permissions are correct
```

2. Metadata Issues
```
Error: Unable to fetch IdP metadata
Solution: 
- Verify Azure AD metadata URL is accessible: https://sts.windows.net/2e319086-9a26-46a3-865f-615bed576786/federationmetadata/2007-06/federationmetadata.xml
- Check network connectivity to Azure AD
- Verify tenant ID in metadata URL matches your Azure AD tenant
```

3. Attribute Mapping Issues
```
Error: Required attribute not found
Solution: 
- Verify attribute names match Azure AD claims:
  - Email: http://schemas.xmlsoap.org/ws/2005/05/identity/claims/mail
  - Name: http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name
  - Given Name: http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname
  - Surname: http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname
- Check Azure AD attribute release policy
```

4. Session Issues
```
Error: Session not maintained after successful authentication
Solution:
- Check session timeout (default 65 minutes to match SAML assertion validity)
- Verify session cookie settings (secure, httpOnly)
- Ensure all required session attributes are stored
```

## Security Compliance Checklist

- [ ] SAML response signature validation
- [ ] Response expiration check
- [ ] Secure session management
- [ ] HTTPS enforcement
- [ ] Proper error handling
- [ ] No sensitive data exposure in logs

## Test Results Documentation

Document test results in the following format:

```markdown
## Test Case: [Name]
- Date: [Test Date]
- Tester: [Name]
- Environment: [Dev/Stage/Prod]

### Steps Performed
1. [Step 1]
2. [Step 2]

### Expected Result
[Description]

### Actual Result
[Description]

### Status
[Pass/Fail]

### Notes
[Additional observations]
```
