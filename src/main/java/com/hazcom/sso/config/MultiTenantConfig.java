package com.hazcom.sso.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.Map;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "saml")
public class MultiTenantConfig {
    private Map<String, IdpConfig> idpConfigurations;

    @Data
    public static class IdpConfig {
        private String entityId;
        private String metadataUrl;
        private String assertionConsumerServiceUrl;
        private String singleLogoutUrl;
    }
}
