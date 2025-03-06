package com.hazcom.sso.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthenticationExceptionHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, 
                                      HttpServletResponse response,
                                      AuthenticationException exception) throws IOException {
        response.sendRedirect("/error?message=" + exception.getMessage());
    }
}
