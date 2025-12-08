package com.example.keycloakdemo.service;

import com.example.keycloakdemo.config.KeycloakProperties;
import com.example.keycloakdemo.exception.KeycloakException;
import com.example.keycloakdemo.model.request.CreateUserRequest;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakUserService {

    private final Keycloak keycloak;
    private final KeycloakProperties properties;

    public String createUser(CreateUserRequest request) {
        RealmResource realm = keycloak.realm(properties.getRealm());
        UsersResource users = realm.users();

        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(request.getEnabled() == null || request.getEnabled());
        user.setEmailVerified(true); // Set email as verified to avoid "account not fully set up" error

        Response response = users.create(user);
        try {
            int status = response.getStatus();
            if (status == 201) {
                URI location = response.getLocation();
                String path = location != null ? location.getPath() : null;
                String id = path != null ? path.substring(path.lastIndexOf('/') + 1) : null;

                if (id != null) {
                    CredentialRepresentation cred = new CredentialRepresentation();
                    cred.setType(CredentialRepresentation.PASSWORD);
                    cred.setTemporary(false);
                    cred.setValue(request.getPassword());

                    users.get(id).resetPassword(cred);
                }
                return id;
            }
            if (status == 409) {
                throw new KeycloakException("User already exists", HttpStatus.CONFLICT);
            }
            throw new KeycloakException("Failed to create user. HTTP status: " + status, HttpStatus.valueOf(status));
        } catch (KeycloakException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating user", e);
            throw new KeycloakException("Failed to create user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            response.close();
        }
    }
}





