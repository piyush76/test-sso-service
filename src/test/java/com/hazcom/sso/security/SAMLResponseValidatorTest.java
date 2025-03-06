package com.hazcom.sso.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SAMLResponseValidatorTest {

    private final SAMLResponseValidator validator = new SAMLResponseValidator();

    @Test
    void validateResponse_ValidAuthentication_NoException() {
        Saml2Authentication auth = mock(Saml2Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("testuser@example.com");
        when(auth.getAuthorities()).thenReturn(Collections.singleton(new SimpleGrantedAuthority("USER")));
        
        assertDoesNotThrow(() -> validator.validateResponse(auth));
    }

    @Test
    void validateResponse_NotAuthenticated_ThrowsException() {
        Saml2Authentication auth = mock(Saml2Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        
        Saml2AuthenticationException exception = assertThrows(
            Saml2AuthenticationException.class,
            () -> validator.validateResponse(auth)
        );
        assertEquals("SAML response validation failed", exception.getMessage());
    }

    @Test
    void validateResponse_InvalidEmailFormat_ThrowsException() {
        Saml2Authentication auth = mock(Saml2Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("invalid-email");
        
        Saml2AuthenticationException exception = assertThrows(
            Saml2AuthenticationException.class,
            () -> validator.validateResponse(auth)
        );
        assertEquals("Invalid email format from SSO Circle", exception.getMessage());
    }

    @Test
    void validateResponse_NoAuthorities_ThrowsException() {
        Saml2Authentication auth = mock(Saml2Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("testuser@example.com");
        when(auth.getAuthorities()).thenReturn(Collections.emptySet());
        
        Saml2AuthenticationException exception = assertThrows(
            Saml2AuthenticationException.class,
            () -> validator.validateResponse(auth)
        );
        assertEquals("No roles/authorities found in SAML response", exception.getMessage());
    }
}
