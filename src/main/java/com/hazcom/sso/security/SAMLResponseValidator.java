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

        // Validate email format for SSO Circle responses
        String email = authentication.getName();
        if (!email.contains("@")) {
            throw new Saml2AuthenticationException(new Saml2Error("invalid_user", "Invalid email format from SSO Circle"));
        }

        // Additional SSO Circle specific validations
        if (authentication.getAuthorities().isEmpty()) {
            throw new Saml2AuthenticationException(new Saml2Error("invalid_attributes", "No roles/authorities found in SAML response"));
        }
    }
}
