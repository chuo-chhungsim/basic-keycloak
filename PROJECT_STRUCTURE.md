# Project Structure 📁

## Directory Layout

```
src/main/java/com/example/keycloakdemo/
├── KeycloakDemoApplication.java          # Main Spring Boot application
│
├── config/                                # Configuration classes
│   ├── KeycloakAdminConfig.java          # Keycloak admin client bean
│   ├── KeycloakProperties.java            # Keycloak configuration properties (Lombok)
│   └── SecurityConfig.java               # Spring Security + JWT configuration
│
├── service/                               # Business logic layer
│   ├── KeycloakUserService.java          # User management service (Lombok)
│   └── LoginService.java                 # Authentication service (Lombok)
│
└── web/                                   # Web/Controller layer
    ├── dto/                              # Data Transfer Objects
    │   ├── CreateUserRequest.java        # User creation DTO (Lombok)
    │   ├── LoginRequest.java             # Login DTO (Lombok)
    │   └── LoginResponse.java            # Login response DTO (Lombok)
    │
    ├── LoginController.java              # Login endpoint (Lombok)
    ├── UserController.java               # User management endpoints (Lombok)
    └── UserInfoController.java           # Current user info endpoint (Lombok)
```

## Code Style & Patterns

### ✅ Consistent Use of Lombok
- All DTOs use `@Data`
- All services use `@RequiredArgsConstructor`
- All controllers use `@RequiredArgsConstructor`
- Configuration properties use `@Data`

### ✅ Package Organization
- **config/** - Spring configuration beans
- **service/** - Business logic services
- **web/** - REST controllers
- **web/dto/** - Request/Response DTOs

### ✅ Naming Conventions
- Controllers: `*Controller`
- Services: `*Service`
- DTOs: `*Request`, `*Response`
- Config: `*Config`, `*Properties`

## Key Components

### Configuration Layer (`config/`)
- **KeycloakAdminConfig**: Creates Keycloak admin client bean
- **KeycloakProperties**: Maps `application.properties` to Java object
- **SecurityConfig**: Configures JWT validation and role mapping

### Service Layer (`service/`)
- **KeycloakUserService**: Manages user CRUD operations via Keycloak Admin API
- **LoginService**: Handles user authentication and token generation

### Web Layer (`web/`)
- **LoginController**: `POST /api/auth/login` - User login endpoint
- **UserController**: `POST /api/users` - Create user endpoint
- **UserInfoController**: `GET /api/user/me` - Get current user info

## API Endpoints

### Public Endpoints
- `POST /api/auth/login` - Login and get JWT token
- `GET /swagger-ui.html` - Swagger UI
- `GET /v3/api-docs/**` - OpenAPI docs

### Protected Endpoints (Require JWT)
- `GET /api/user/me` - Get current user info
- `POST /api/users` - Create user (requires `admin` or `user` role)
- `GET /admin/**` - Admin endpoints (requires `admin` role)

## Configuration Files

### `application.properties`
```properties
# Server runs on port 8080
server.port=8080

# Keycloak runs on port 8081
keycloak.server-url=http://localhost:8081
keycloak.realm=demo
keycloak.client-id=spring-admin
keycloak.client-secret=***

# JWT validation
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8081/realms/demo
```

## Dependencies

- **Spring Boot 3.5.7** - Web framework
- **Spring Security** - Security & authentication
- **OAuth2 Resource Server** - JWT validation
- **Keycloak Admin Client 25.0.6** - Keycloak integration
- **Lombok** - Code generation
- **SpringDoc OpenAPI** - API documentation

## Build & Run

```bash
# Build
./gradlew clean build

# Run
./gradlew bootRun
```

App runs on: **http://localhost:8080**
Keycloak runs on: **http://localhost:8081**

