package com.example.keycloakdemo.service;

import com.example.keycloakdemo.model.entity.AppUser;
import com.example.keycloakdemo.model.request.CreateUserRequest;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    AppUser createUser(@Valid CreateUserRequest request);

    void deleteUser(UUID id);

    Optional<Object> getUserByKeycloakId(String subject);

    AppUser updateUser(UUID id, @Valid CreateUserRequest request);

    Optional<AppUser> getUserById(UUID id);

    List<AppUser> getAllUsers();
}
