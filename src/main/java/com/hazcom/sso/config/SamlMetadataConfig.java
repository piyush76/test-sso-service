package com.hazcom.sso.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.core.io.ClassPathResource;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import org.springframework.util.Base64Utils;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Configuration
public class SamlMetadataConfig {
    
    @Bean
    public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() throws Exception {
        String metadataUrl = "https://sts.windows.net/2e319086-9a26-46a3-865f-615bed576786/federationmetadata/2007-06/federationmetadata.xml";
        
        // Load signing certificate and private key from classpath
        ClassPathResource certResource = new ClassPathResource("saml/public.cer");
        ClassPathResource keyResource = new ClassPathResource("saml/private.key");
        
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate;
        try (InputStream is = certResource.getInputStream()) {
            certificate = (X509Certificate) certFactory.generateCertificate(is);
        }
        
        // Load private key from PEM file
        String privateKeyPEM;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(keyResource.getInputStream()))) {
            privateKeyPEM = reader.lines()
                .filter(line -> !line.startsWith("-----BEGIN") && !line.startsWith("-----END"))
                .collect(Collectors.joining());
        }
        
        byte[] encodedKey = Base64Utils.decodeFromString(privateKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedKey);
        RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        
        Saml2X509Credential signingCredential = Saml2X509Credential.signing(privateKey, certificate);
        
        var registration = RelyingPartyRegistrations
            .fromMetadataLocation(metadataUrl)
            .registrationId("hazcom")
            .signingX509Credentials(c -> c.add(signingCredential))
            .entityId("https://app.maxcomsc.com/maxcomsc")
            .assertionConsumerServiceLocation("https://app.maxcomsc.com/maxcomsc/login/saml2/sso/hazcom")
            .build();
            
        return new InMemoryRelyingPartyRegistrationRepository(registration);
    }
}
