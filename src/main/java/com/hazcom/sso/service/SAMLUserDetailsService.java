package com.hazcom.sso.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SAMLUserDetailsService {
    private final UserManagementService userManagementService;
    
    public SAMLUserDetailsService(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    public UserDetails extractUserDetails(Saml2Authentication authentication) {
        String email = authentication.getName();
        
        if (!userManagementService.validateUserExists(email)) {
            userManagementService.logFailedAuthentication(email, "User not found in Hazcom database");
            throw new UsernameNotFoundException("User not found in Hazcom database");
        }
        
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
