package com.hazcom.sso.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.hazcom.sso.service.SAMLUserDetailsService;

@RestController
public class AuthController {

    private final SAMLUserDetailsService userDetailsService;

    public AuthController(SAMLUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/auth/user")
    public UserDetails currentUser(@AuthenticationPrincipal Saml2Authentication authentication) {
        return userDetailsService.extractUserDetails(authentication);
    }
}
