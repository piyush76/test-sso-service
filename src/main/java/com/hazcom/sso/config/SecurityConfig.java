package com.hazcom.sso.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final RelyingPartyRegistrationRepository relyingPartyRegistrationRepository;

    @Value("${spring.security.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${spring.security.cors.allowed-methods}")
    private String allowedMethods;

    @Value("${spring.security.cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${spring.security.cors.allow-credentials}")
    private boolean allowCredentials;

    public SecurityConfig(RelyingPartyRegistrationRepository relyingPartyRegistrationRepository) {
        this.relyingPartyRegistrationRepository = relyingPartyRegistrationRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setDefaultTargetUrl("https://app.maxcomsc.com/maxcomsc");
        successHandler.setAlwaysUseDefaultTargetUrl(true);

        http
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
                config.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
                config.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
                config.setAllowCredentials(allowCredentials);
                return config;
            }))
            .csrf().disable()
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/error", "/saml2/service-provider-metadata/**").permitAll()
                .anyRequest().authenticated()
            )
            .saml2Login(saml2 -> saml2
                .relyingPartyRegistrationRepository(relyingPartyRegistrationRepository)
                .loginProcessingUrl("/login/saml2/sso/{registrationId}")
                .successHandler(successHandler)
            )
            .saml2Logout(saml2 -> saml2
                .logoutUrl("/logout/saml2/slo")
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/login?expired")
            );

        return http.build();
    }
}
