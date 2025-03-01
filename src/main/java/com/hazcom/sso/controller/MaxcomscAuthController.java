package com.hazcom.sso.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.hazcom.sso.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;

@RestController
@RequestMapping("/api/auth")
public class MaxcomscAuthController {
    private final SessionService sessionService;

    public MaxcomscAuthController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof Saml2Authentication) {
            Saml2Authentication saml2Auth = (Saml2Authentication) auth;
            return ResponseEntity.ok(Map.of(
                "username", saml2Auth.getName(),
                "authorities", saml2Auth.getAuthorities()
            ));
        }
        return ResponseEntity.status(401).body("Not authenticated");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            sessionService.invalidateSession(auth.getName());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
}
