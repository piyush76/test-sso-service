package com.hazcom.sso.service;

import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {
    private final SAMLUserDetailsService userDetailsService;
    private final Map<String, UserDetails> sessionStore = new ConcurrentHashMap<>();

    public SessionService(SAMLUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public void createSession(Authentication authentication) {
        if (authentication instanceof Saml2Authentication) {
            Saml2Authentication saml2Auth = (Saml2Authentication) authentication;
            UserDetails userDetails = userDetailsService.extractUserDetails(saml2Auth);
            sessionStore.put(saml2Auth.getName(), userDetails);
        }
    }

    public UserDetails getSessionData(String userId) {
        return sessionStore.get(userId);
    }

    public void invalidateSession(String userId) {
        sessionStore.remove(userId);
        SecurityContextHolder.clearContext();
    }

    public boolean isSessionValid(String userId) {
        return sessionStore.containsKey(userId);
    }
}
