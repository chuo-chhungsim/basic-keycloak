package com.example.keycloakdemo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

public class KeycloakException extends ErrorResponseException {

    public KeycloakException(String message, HttpStatus status) {
        super(status);
        getBody().setTitle("Keycloak Error");
        getBody().setDetail(message);
    }
}

