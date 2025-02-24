package com.hazcom.sso.security;

import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationException;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.saml2.core.Saml2Error;
import org.springframework.stereotype.Component;

@Component
public class SAMLResponseValidator {

    public void validateResponse(Saml2Authentication authentication) {
        if (!authentication.isAuthenticated()) {
            throw new Saml2AuthenticationException(new Saml2Error("invalid_response", "SAML response validation failed"));
        }

        // Additional custom validations can be added here
        if (!authentication.getName().contains("@")) {
            throw new Saml2AuthenticationException(new Saml2Error("invalid_user", "Invalid user identifier format"));
        }
    }
}
