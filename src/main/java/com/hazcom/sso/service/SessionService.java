package com.hazcom.sso.service;

import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {
    private final Map<String, Object> sessionStore = new ConcurrentHashMap<>();

    public void createSession(Authentication authentication) {
        if (authentication instanceof Saml2Authentication) {
            Saml2Authentication saml2Auth = (Saml2Authentication) authentication;
            String userId = saml2Auth.getName();
            sessionStore.put(userId, saml2Auth.getAuthorities());
        }
    }

    public Object getSessionData(String userId) {
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
