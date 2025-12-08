package com.example.keycloakdemo.controller;

import com.example.keycloakdemo.service.KeycloakUserService;
import com.example.keycloakdemo.service.LoginService;
import com.example.keycloakdemo.model.request.CreateUserRequest;
import com.example.keycloakdemo.model.request.LoginRequest;
import com.example.keycloakdemo.model.respose.LoginResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AppUserController {
    private final LoginService loginService;
    private final KeycloakUserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = loginService.login(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createUser(@Valid @RequestBody CreateUserRequest request) {
        String userId = userService.createUser(request);
        return ResponseEntity.created(URI.create("/api/users/" + userId))
                .body(Map.of("id", userId));
    }
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", jwt.getSubject());
        userInfo.put("email", jwt.getClaim("email"));
        userInfo.put("username", jwt.getClaim("preferred_username"));
//        userInfo.put("realmRoles", jwt.getClaim("realm_access"));
//        userInfo.put("allClaims", jwt.getClaims());
        return ResponseEntity.ok(userInfo);
    }
}
