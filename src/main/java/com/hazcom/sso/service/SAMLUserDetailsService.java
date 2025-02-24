package com.hazcom.sso.service;

import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.stereotype.Service;

@Service
public class SAMLUserDetailsService {

    public UserDetails extractUserDetails(Saml2Authentication authentication) {
        String email = authentication.getName();
        
        // Extract attributes from SAML assertion
        var attributes = authentication.getAuthorities().stream()
            .filter(a -> a.getAuthority().startsWith("SAML2_ATTR_"))
            .map(a -> a.getAuthority().substring("SAML2_ATTR_".length()))
            .toList();
        
        return User.builder()
            .username(email)
            .password("") // No password needed for SAML auth
            .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
            .build();
    }
}
