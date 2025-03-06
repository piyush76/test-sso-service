#!/bin/sh
set -e

# Default values
KEYSTORE_PATH=${1:-"/app/saml/keystore.jks"}
KEYSTORE_PASSWORD=${SAML_KEYSTORE_PASSWORD:-"changeit"}
PRIVATE_KEY_PASSWORD=${SAML_PRIVATE_KEY_PASSWORD:-"changeit"}
PRIVATE_KEY_ALIAS=${SAML_PRIVATE_KEY_ALIAS:-"hazcom"}
CERT_PATH=${2:-"/app/saml/public.cer"}
PRIVATE_KEY_PATH=${3:-"/app/saml/private.key"}
PKCS12_PATH=${4:-"/app/saml/keystore.p12"}

# Create directory if it doesn't exist
mkdir -p $(dirname "$KEYSTORE_PATH")

# Generate keystore and private key
keytool -genkeypair \
  -alias "$PRIVATE_KEY_ALIAS" \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore "$KEYSTORE_PATH" \
  -validity 3650 \
  -storepass "$KEYSTORE_PASSWORD" \
  -keypass "$PRIVATE_KEY_PASSWORD" \
  -dname "CN=Hazcom SSO Service, OU=IT, O=Hazcom, L=Unknown, ST=Unknown, C=US"

# Export certificate
keytool -exportcert \
  -alias "$PRIVATE_KEY_ALIAS" \
  -keystore "$KEYSTORE_PATH" \
  -storepass "$KEYSTORE_PASSWORD" \
  -file "$CERT_PATH" \
  -rfc

# Export private key
keytool -importkeystore \
  -srckeystore "$KEYSTORE_PATH" \
  -srcstorepass "$KEYSTORE_PASSWORD" \
  -srckeypass "$PRIVATE_KEY_PASSWORD" \
  -srcalias "$PRIVATE_KEY_ALIAS" \
  -destkeystore "$PKCS12_PATH" \
  -deststoretype PKCS12 \
  -deststorepass "$KEYSTORE_PASSWORD" \
  -destkeypass "$PRIVATE_KEY_PASSWORD"

openssl pkcs12 -in "$PKCS12_PATH" \
  -nodes \
  -nocerts \
  -passin pass:"$KEYSTORE_PASSWORD" \
  | openssl pkcs8 -topk8 -nocrypt > "$PRIVATE_KEY_PATH"

# Set proper permissions
chmod 600 "$PRIVATE_KEY_PATH"
chmod 644 "$CERT_PATH"

# Clean up temporary PKCS12 file
rm -f "$PKCS12_PATH"
