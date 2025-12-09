package com.example.keycloakdemo.service.impl;

import com.example.keycloakdemo.exception.KeycloakException;
import com.example.keycloakdemo.model.entity.AppUser;
import com.example.keycloakdemo.model.request.CreateUserRequest;
import com.example.keycloakdemo.repository.UserRepository;
import com.example.keycloakdemo.service.KeycloakUserService;
import com.example.keycloakdemo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final KeycloakUserService keycloakUserService;

    @Override
    @Transactional
    public AppUser createUser(CreateUserRequest request) {
        // Check if user already exists in database
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new KeycloakException("Username already exists", HttpStatus.CONFLICT);
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new KeycloakException("Email already exists", HttpStatus.CONFLICT);
        }

        // Create user in Keycloak first
        String keycloakUserId = keycloakUserService.createUser(request);
        log.info("User created in Keycloak with ID: {}", keycloakUserId);

        // Create user in database
        AppUser appUser = new AppUser();
        appUser.setUsername(request.getUsername());
        appUser.setEmail(request.getEmail());
        appUser.setFirstName(request.getFirstName());
        appUser.setLastName(request.getLastName());
        appUser.setKeycloakUserId(keycloakUserId);
        appUser.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);

        AppUser savedUser = userRepository.save(appUser);
        log.info("User saved to database with ID: {}", savedUser.getId());
        
        return savedUser;
    }

    @Override
    public Optional<AppUser> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<AppUser> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<AppUser> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<Object> getUserByKeycloakId(String subject) {
        return userRepository.findByKeycloakUserId(subject)
                .map(user -> (Object) user);
    }

    @Override
    public List<AppUser> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public AppUser updateUser(UUID id, CreateUserRequest request) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new KeycloakException("User not found", HttpStatus.NOT_FOUND));

        // Update fields
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new KeycloakException("Email already exists", HttpStatus.CONFLICT);
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new KeycloakException("User not found", HttpStatus.NOT_FOUND));
        
        userRepository.delete(user);
        log.info("User deleted from database with ID: {}", id);
    }
}
