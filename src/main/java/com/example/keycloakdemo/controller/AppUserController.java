package com.example.keycloakdemo.controller;

import com.example.keycloakdemo.model.entity.AppUser;
import com.example.keycloakdemo.service.LoginService;
import com.example.keycloakdemo.service.UserService;
import com.example.keycloakdemo.model.request.CreateUserRequest;
import com.example.keycloakdemo.model.request.LoginRequest;
import com.example.keycloakdemo.model.respose.CreateUserResponse;
import com.example.keycloakdemo.model.respose.LoginResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AppUserController {
    private final LoginService loginService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = loginService.login(request);
        return ResponseEntity.ok(response);
    }
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/create-user")
    public ResponseEntity<CreateUserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        AppUser user = userService.createUser(request);
        CreateUserResponse response = new CreateUserResponse(
                user.getId().toString(), 
                "User created successfully"
        );
        log.info("User created successfully: {}", response);
        return ResponseEntity.created(URI.create("/api/v1/auth/users/" + user.getId()))
                .body(response);
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", jwt.getSubject());
        userInfo.put("email", jwt.getClaim("email"));
        userInfo.put("username", jwt.getClaim("preferred_username"));
        userInfo.put("realmRoles", jwt.getClaim("realm_access"));
        
        // Try to find user in database by Keycloak ID
        userService.getUserByKeycloakId(jwt.getSubject())
                .ifPresent(userObj -> {
                    if (userObj instanceof AppUser user) {
                        userInfo.put("appUserId", user.getId());
                        userInfo.put("firstName", user.getFirstName());
                        userInfo.put("lastName", user.getLastName());
                    }
                });
        
        return ResponseEntity.ok(userInfo);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        Map<String, Object> response = new HashMap<>();
        response.put("users", userService.getAllUsers());
        response.put("count", userService.getAllUsers().size());
        return ResponseEntity.ok(response);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/users/{id}")
    public ResponseEntity<AppUser> getUserById(@PathVariable java.util.UUID id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/users/{id}")
    public ResponseEntity<AppUser> updateUser(
            @PathVariable java.util.UUID id,
            @Valid @RequestBody CreateUserRequest request) {
        AppUser updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable java.util.UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
