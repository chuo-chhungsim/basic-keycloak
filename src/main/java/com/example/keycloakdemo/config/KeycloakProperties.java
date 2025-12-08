package com.example.keycloakdemo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {
    private String serverUrl;
    private String adminRealm;
    private String realm;
    private String clientId;
    private String clientSecret;
}





