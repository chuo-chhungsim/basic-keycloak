package com.example.keycloakdemo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

public class AuthenticationException extends ErrorResponseException {

    public AuthenticationException(String message) {
        super(HttpStatus.UNAUTHORIZED);
        getBody().setTitle("Authentication Failed");
        getBody().setDetail(message);
    }
}

