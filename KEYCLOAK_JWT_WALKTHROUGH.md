# Spring Boot + Keycloak + JWT Complete Walkthrough 🔐

This guide explains how Spring Boot integrates with Keycloak for JWT-based authentication and authorization.

## 📋 Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [How It Works](#how-it-works)
3. [Setup Steps](#setup-steps)
4. [Code Explanation](#code-explanation)
5. [Testing the Flow](#testing-the-flow)
6. [Common Scenarios](#common-scenarios)

---

## 🏗️ Architecture Overview

```
┌─────────────┐         ┌──────────────┐         ┌─────────────┐
│   Client    │────────▶│ Spring Boot  │────────▶│  Keycloak   │
│  (Browser/  │  JWT    │   App (8081) │  Validates │  Server     │
│   Postman)  │  Token  │              │  JWT     │   (8080)    │
└─────────────┘         └──────────────┘         └─────────────┘
                              │
                              │ Validates & Extracts
                              │ Roles/Claims
                              ▼
                        ┌──────────────┐
                        │   Protected  │
                        │   Resources  │
                        └──────────────┘
```

### Two Main Flows:

1. **Login Flow** (Password Grant):
   - User sends credentials → Spring Boot → Keycloak
   - Keycloak validates → Returns JWT tokens
   - Client stores tokens

2. **Resource Access Flow** (JWT Validation):
   - Client sends request with JWT token
   - Spring Security validates token with Keycloak
   - Extracts roles/claims from JWT
   - Grants/denies access based on roles

---

## 🔄 How It Works

### 1. **Login Endpoint** (`POST /api/auth/login`)

**Flow:**
```
Client → LoginController → LoginService → Keycloak (Password Grant) → Returns JWT
```

**What happens:**
- User provides username/password
- `LoginService` uses Keycloak Admin Client with `PASSWORD` grant type
- Keycloak validates credentials
- Returns `AccessTokenResponse` containing:
  - `accessToken` (JWT)
  - `refreshToken`
  - `expiresIn`
  - `tokenType` ("Bearer")

**Code Location:** `LoginController.java` → `LoginService.java`

### 2. **JWT Validation** (OAuth2 Resource Server)

**Flow:**
```
Client Request (with JWT) → Spring Security Filter → Validates with Keycloak → Extracts Roles → Authorizes
```

**What happens:**
1. Client includes JWT in `Authorization: Bearer <token>` header
2. Spring Security's `OAuth2ResourceServer` intercepts request
3. Validates JWT signature with Keycloak's public key (fetched from `issuer-uri`)
4. `JwtAuthenticationConverter` extracts roles from JWT claims
5. Maps Keycloak roles to Spring Security `GrantedAuthority`
6. Security rules check if user has required role

**Configuration:**
- `application.properties`: `spring.security.oauth2.resourceserver.jwt.issuer-uri`
- `SecurityConfig.java`: OAuth2 Resource Server configuration

---

## 🛠️ Setup Steps

### Step 1: Start Keycloak

```bash
docker run --name keycloak -p 8080:8080 \
  -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:26.1.0 start-dev
```

### Step 2: Create Realm

1. Go to http://localhost:8080
2. Login with `admin/admin`
3. Create realm: `demo`

### Step 3: Create Client for Admin Operations

**Purpose:** Service account for creating users (admin operations)

1. In `demo` realm → **Clients** → **Create client**
2. Settings:
   - **Client ID:** `spring-admin`
   - **Client authentication:** ON (Confidential)
   - **Service accounts roles:** ON
3. **Credentials** tab → Copy **Client secret**
4. **Service account roles** → Assign `realm-management` → `manage-users`

### Step 4: Create Client for User Login

**Purpose:** Public client for users to login and get JWT tokens

1. In `demo` realm → **Clients** → **Create client**
2. Settings:
   - **Client ID:** `spring-app` (or any name)
   - **Client authentication:** OFF (Public client)
   - **Standard flow:** ON
   - **Direct access grants:** ON (for password grant)
   - **Valid redirect URIs:** `http://localhost:8081/*`
3. Save

### Step 5: Create Roles

1. **Realm roles** → **Create role**
   - Create: `admin`, `user`

### Step 6: Create Test User

1. **Users** → **Create new user**
   - Username: `testuser`
   - Email: `test@example.com`
   - **Credentials** tab → Set password: `password123`
   - **Role mapping** → Assign `user` role

### Step 7: Configure Application

Edit `application.properties`:

```properties
# Keycloak server
keycloak.server-url=http://localhost:8080
keycloak.admin-realm=master
keycloak.realm=demo
keycloak.client-id=spring-admin
keycloak.client-secret=YOUR_CLIENT_SECRET_HERE

# JWT Validation (OAuth2 Resource Server)
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/demo

# App runs on 8081 (Keycloak on 8080)
server.port=8081
```

**Important:** The `issuer-uri` tells Spring Security where to fetch Keycloak's public keys to validate JWTs.

---

## 💻 Code Explanation

### 1. **SecurityConfig.java** - The Heart of JWT Validation

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );
        // ...
    }
}
```

**What this does:**
- Enables OAuth2 Resource Server
- Configures JWT validation
- Custom converter extracts Keycloak roles

### 2. **JWT Authentication Converter**

```java
@Bean
public Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
    return jwt -> {
        // Extract realm_access.roles from JWT
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        Collection<String> roles = (Collection<String>) realmAccess.get("roles");
        
        // Convert to Spring Security authorities: ROLE_admin, ROLE_user
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toList());
    };
}
```

**What this does:**
- Reads `realm_access.roles` from JWT
- Converts to Spring Security format: `ROLE_admin`, `ROLE_user`
- Spring Security uses these for authorization checks

### 3. **JWT Structure (Keycloak)**

When you decode a Keycloak JWT, it looks like:

```json
{
  "sub": "user-uuid",
  "email": "test@example.com",
  "realm_access": {
    "roles": ["user", "admin"]
  },
  "resource_access": {
    "spring-app": {
      "roles": ["client-role"]
    }
  },
  "iss": "http://localhost:8080/realms/demo",
  "exp": 1234567890
}
```

### 4. **LoginService.java** - Getting JWT Tokens

```java
public LoginResponse login(LoginRequest request) {
    Keycloak keycloak = KeycloakBuilder.builder()
        .serverUrl(properties.getServerUrl())
        .realm(properties.getRealm())
        .grantType(OAuth2Constants.PASSWORD)  // Password grant
        .clientId(properties.getClientId())
        .username(request.getUsername())
        .password(request.getPassword())
        .build();
    
    AccessTokenResponse tokenResponse = keycloak.tokenManager().getAccessToken();
    return new LoginResponse(tokenResponse.getToken(), ...);
}
```

**What this does:**
- Uses Keycloak Admin Client with `PASSWORD` grant
- Authenticates user with Keycloak
- Returns JWT tokens

---

## 🧪 Testing the Flow

### Step 1: Login (Get JWT Token)

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ...",
  "tokenType": "Bearer",
  "expiresIn": 300,
  "refreshExpiresIn": 1800
}
```

### Step 2: Use JWT Token to Access Protected Resource

```bash
curl -X GET http://localhost:8081/api/users \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ..."
```

**What happens:**
1. Spring Security extracts token from `Authorization` header
2. Validates signature with Keycloak's public key
3. Extracts roles from `realm_access.roles`
4. Checks if user has required role (from `SecurityConfig`)
5. Allows/denies request

### Step 3: Test Role-Based Access

**User with `user` role:**
```bash
# This works (has 'user' role)
curl -X GET http://localhost:8081/api/users \
  -H "Authorization: Bearer <token>"
```

**User with `admin` role:**
```bash
# This works (has 'admin' role)
curl -X GET http://localhost:8081/admin/users \
  -H "Authorization: Bearer <token>"
```

---

## 📝 Common Scenarios

### Scenario 1: Public Endpoint (No Auth Required)

```java
.requestMatchers("/api/auth/login").permitAll()
```

### Scenario 2: Role-Based Access

```java
.requestMatchers("/admin/**").hasRole("admin")
.requestMatchers("/api/users/**").hasAnyRole("admin", "user")
```

### Scenario 3: Method-Level Security

```java
@PreAuthorize("hasRole('admin')")
public void deleteUser(String id) { ... }
```

### Scenario 4: Extract User Info from JWT

```java
@GetMapping("/me")
public Map<String, Object> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
    return Map.of(
        "sub", jwt.getSubject(),
        "email", jwt.getClaim("email"),
        "roles", jwt.getClaim("realm_access")
    );
}
```

---

## 🔍 Key Concepts

### 1. **OAuth2 Resource Server**
- Spring Security component that validates JWT tokens
- Fetches public keys from Keycloak's `/.well-known/jwks_uri`
- Validates token signature and expiration

### 2. **JWT Claims**
- `sub`: User ID
- `realm_access.roles`: Realm roles
- `resource_access`: Client-specific roles
- `exp`: Expiration time
- `iss`: Issuer (Keycloak realm)

### 3. **Grant Types**
- **Password Grant**: User login (username/password → JWT)
- **Client Credentials**: Service account (client_id/secret → JWT)
- **Authorization Code**: Browser-based OAuth flow

### 4. **Role Mapping**
- Keycloak roles: `admin`, `user`
- Spring Security roles: `ROLE_admin`, `ROLE_user`
- Converter adds `ROLE_` prefix automatically

---

## 🐛 Troubleshooting

### Issue: "Invalid token" or 401 Unauthorized

**Causes:**
- Token expired
- Wrong issuer URI
- Token not in `Authorization: Bearer <token>` format
- Keycloak server not running

**Solution:**
- Check `issuer-uri` matches Keycloak realm
- Verify token hasn't expired
- Check Keycloak logs

### Issue: "Access Denied" (403)

**Causes:**
- User doesn't have required role
- Role not mapped correctly in converter

**Solution:**
- Check user's roles in Keycloak
- Verify role mapping in `jwtGrantedAuthoritiesConverter()`
- Check SecurityConfig authorization rules

### Issue: Login fails

**Causes:**
- Client doesn't have "Direct access grants" enabled
- Wrong client ID
- User credentials incorrect

**Solution:**
- Enable "Direct access grants" in Keycloak client
- Verify client ID in `application.properties`
- Check user exists and password is correct

---

## 📚 Summary

**The Complete Flow:**

1. **User Login:**
   - POST `/api/auth/login` with credentials
   - Spring Boot → Keycloak (password grant)
   - Returns JWT token

2. **Access Protected Resource:**
   - Include JWT in `Authorization` header
   - Spring Security validates token with Keycloak
   - Extracts roles from JWT
   - Authorizes based on roles

3. **Key Components:**
   - `LoginController` + `LoginService`: Get JWT tokens
   - `SecurityConfig`: Configure JWT validation
   - `OAuth2ResourceServer`: Validates tokens automatically
   - `JwtAuthenticationConverter`: Maps Keycloak roles to Spring Security

**That's it!** You now have a complete Spring Boot + Keycloak + JWT setup! 🎉
