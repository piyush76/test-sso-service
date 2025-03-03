#!/bin/sh
set -e

# Generate keystore and private key
keytool -genkeypair \
  -alias hazcom \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore /app/saml/keystore.jks \
  -validity 3650 \
  -storepass ${SAML_KEYSTORE_PASSWORD:-changeit} \
  -keypass ${SAML_PRIVATE_KEY_PASSWORD:-changeit} \
  -dname "CN=Hazcom SSO Service, OU=IT, O=Hazcom, L=Unknown, ST=Unknown, C=US"

# Export certificate
keytool -exportcert \
  -alias hazcom \
  -keystore /app/saml/keystore.jks \
  -storepass ${SAML_KEYSTORE_PASSWORD:-changeit} \
  -file /app/saml/public.cer \
  -rfc

# Export private key in PKCS8 format
keytool -importkeystore \
  -srckeystore /app/saml/keystore.jks \
  -srcstorepass ${SAML_KEYSTORE_PASSWORD:-changeit} \
  -srckeypass ${SAML_PRIVATE_KEY_PASSWORD:-changeit} \
  -srcalias hazcom \
  -destkeystore /app/saml/keystore.p12 \
  -deststoretype PKCS12 \
  -deststorepass ${SAML_KEYSTORE_PASSWORD:-changeit} \
  -destkeypass ${SAML_PRIVATE_KEY_PASSWORD:-changeit}

openssl pkcs12 -in /app/saml/keystore.p12 \
  -nodes \
  -nocerts \
  -passin pass:${SAML_KEYSTORE_PASSWORD:-changeit} \
  | openssl pkcs8 -topk8 -nocrypt > /app/saml/private.key

chmod 600 /app/saml/private.key
chmod 644 /app/saml/public.cer
