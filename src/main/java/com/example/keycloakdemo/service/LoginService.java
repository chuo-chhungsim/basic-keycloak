package com.example.keycloakdemo.service;

import com.example.keycloakdemo.config.KeycloakProperties;
import com.example.keycloakdemo.exception.AuthenticationException;
import com.example.keycloakdemo.exception.KeycloakException;
import com.example.keycloakdemo.model.request.LoginRequest;
import com.example.keycloakdemo.model.respose.LoginResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final KeycloakProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LoginResponse login(LoginRequest request) {
        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token",
                properties.getServerUrl(), properties.getRealm());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", properties.getClientId());
        if (properties.getClientSecret() != null && !properties.getClientSecret().isEmpty()) {
            body.add("client_secret", properties.getClientSecret());
        }
        body.add("username", request.getUsername());
        body.add("password", request.getPassword());

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                return new LoginResponse(
                        jsonNode.get("access_token").asText(),
                        jsonNode.has("refresh_token") ? jsonNode.get("refresh_token").asText() : null,
                        jsonNode.has("token_type") ? jsonNode.get("token_type").asText() : "Bearer",
                        jsonNode.has("expires_in") ? jsonNode.get("expires_in").asLong() : null,
                        jsonNode.has("refresh_expires_in") ? jsonNode.get("refresh_expires_in").asLong() : null
                );
            }

            throw new KeycloakException("Unexpected response from Keycloak", HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (HttpClientErrorException e) {
            log.error("Keycloak authentication error: {}", e.getResponseBodyAsString(), e);
            
            HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
            if (status == HttpStatus.UNAUTHORIZED || status == HttpStatus.BAD_REQUEST) {
                String errorMessage = extractErrorMessage(e.getResponseBodyAsString());
                throw new AuthenticationException("Authentication failed: " + errorMessage);
            }
            
            throw new KeycloakException("Keycloak error: " + e.getMessage(), status);
        } catch (Exception e) {
            log.error("Unexpected error during login", e);
            throw new KeycloakException("Failed to authenticate: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String extractErrorMessage(String responseBody) {
        try {
            if (responseBody != null && !responseBody.isEmpty()) {
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                if (jsonNode.has("error_description")) {
                    return jsonNode.get("error_description").asText();
                }
                if (jsonNode.has("error")) {
                    return jsonNode.get("error").asText();
                }
            }
        } catch (Exception e) {
            log.debug("Could not parse error response", e);
        }
        return "Invalid credentials";
    }
}
