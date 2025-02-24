package com.hazcom.sso.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final RelyingPartyRegistrationRepository relyingPartyRegistrationRepository;

    public SecurityConfig(RelyingPartyRegistrationRepository relyingPartyRegistrationRepository) {
        this.relyingPartyRegistrationRepository = relyingPartyRegistrationRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setDefaultTargetUrl("https://app.maxcomsc.com/maxcomsc");
        successHandler.setAlwaysUseDefaultTargetUrl(true);

        http
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
            );

        return http.build();
    }
}
