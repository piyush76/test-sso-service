# Hazcom SSO Service Configuration Guide

## Environment Variables

The SSO service uses environment variables for configuration. These can be set in the `.env` file or through Docker environment variables.

### Required Environment Variables

#### SAML Configuration
- `SAML_IDP_METADATA_URL`: URL of the Identity Provider metadata
- `SAML_SP_ENTITY_ID`: Service Provider entity ID
- `SAML_SP_ACS_URL`: Assertion Consumer Service URL
- `SAML_KEYSTORE_PATH`: Path to the SAML keystore
- `SAML_KEYSTORE_PASSWORD`: Keystore password
- `SAML_PRIVATE_KEY_ALIAS`: Private key alias in the keystore
- `SAML_PRIVATE_KEY_PASSWORD`: Private key password

#### Session Configuration
- `SESSION_TIMEOUT`: Session timeout in seconds (default: 3600)
- `SESSION_COOKIE_NAME`: Session cookie name (default: HAZCOM_SESSION)

#### Logging Configuration
- `LOGGING_LEVEL_ROOT`: Root logging level (default: INFO)
- `LOGGING_LEVEL_COM_HAZCOM`: Application logging level (default: DEBUG)
- `LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY`: Security logging level (default: DEBUG)

#### Cache Configuration
- `SPRING_CACHE_TYPE`: Cache type (default: caffeine)
- `SPRING_CACHE_CAFFEINE_SPEC`: Caffeine cache specification

## Multi-tenant Configuration

1. Create a tenant-specific configuration file in the `config` directory
2. Name it `tenant-{tenantId}.yml`
3. Follow the format in `tenant-example.yml`
4. Mount the configuration directory when running with Docker

## Running with Docker Compose

```bash
# Start the service
docker-compose up -d

# View logs
docker-compose logs -f

# Stop the service
docker-compose down
```

## Security Notes

1. Never commit sensitive values in the `.env` file
2. Use a secure secrets manager in production
3. Rotate SAML certificates regularly
4. Keep keystore files secure and separate for each tenant
