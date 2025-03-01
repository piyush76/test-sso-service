package com.hazcom.sso.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.hazcom.sso.service.SAMLUserDetailsService;
import com.hazcom.sso.service.SessionService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final SAMLUserDetailsService userDetailsService;
    private final SessionService sessionService;

    public AuthController(SAMLUserDetailsService userDetailsService, SessionService sessionService) {
        this.userDetailsService = userDetailsService;
        this.sessionService = sessionService;
    }

    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal Saml2Authentication authentication) {
        if (authentication != null) {
            UserDetails userDetails = userDetailsService.extractUserDetails(authentication);
            sessionService.createSession(authentication);
            return ResponseEntity.ok(userDetails);
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
