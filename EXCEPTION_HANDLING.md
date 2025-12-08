# Exception Handling with Problem Details (RFC 7807) 📋

This project uses **RFC 7807 Problem Details** for standardized error responses.

## 🎯 Features

- ✅ **Problem Details (RFC 7807)** - Standardized error format
- ✅ **Global Exception Handler** - Centralized error handling
- ✅ **Custom Exceptions** - Domain-specific exceptions
- ✅ **Validation Errors** - Detailed field-level validation messages
- ✅ **Keycloak Integration** - Proper error extraction from Keycloak responses

## 📁 Exception Structure

```
exception/
├── AuthenticationException.java    # Authentication failures (401)
├── KeycloakException.java          # Keycloak-related errors
└── GlobalExceptionHandler.java    # Global exception handler
```

## 🔧 Custom Exceptions

### AuthenticationException
**HTTP Status:** `401 Unauthorized`

Thrown when authentication fails (invalid credentials).

```java
throw new AuthenticationException("Authentication failed: Invalid username or password");
```

**Response:**
```json
{
  "type": "about:blank",
  "title": "Authentication Failed",
  "status": 401,
  "detail": "Authentication failed: Invalid username or password"
}
```

### KeycloakException
**HTTP Status:** Configurable (400, 409, 500, etc.)

Thrown when Keycloak operations fail.

```java
throw new KeycloakException("User already exists", HttpStatus.CONFLICT);
```

**Response:**
```json
{
  "type": "about:blank",
  "title": "Keycloak Error",
  "status": 409,
  "detail": "User already exists"
}
```

## 📝 Exception Handler

### GlobalExceptionHandler

Handles all exceptions and converts them to Problem Details format.

#### Handled Exceptions:

1. **AuthenticationException** → `401 Unauthorized`
2. **KeycloakException** → Configurable status code
3. **IllegalArgumentException** → `400 Bad Request`
4. **IllegalStateException** → `409 Conflict`
5. **ConstraintViolationException** → `400 Bad Request` (with field errors)
6. **MethodArgumentNotValidException** → `400 Bad Request` (with validation errors)
7. **Exception** → `500 Internal Server Error` (catch-all)

### Validation Error Response

When validation fails, the response includes field-level errors:

```json
{
  "type": "about:blank",
  "title": "Validation Error",
  "status": 400,
  "detail": "Validation failed",
  "errors": {
    "username": "Username or email is required",
    "password": "Password is required"
  }
}
```

## 🔍 Login Service Error Handling

The `LoginService` now:

1. **Uses direct HTTP calls** to Keycloak token endpoint (more reliable)
2. **Extracts error messages** from Keycloak responses
3. **Throws proper exceptions** with detailed error messages

### Error Extraction

When Keycloak returns an error, the service extracts:
- `error_description` (preferred)
- `error` (fallback)
- Default message if parsing fails

### Example Error Flow

```
User Request → LoginService → Keycloak
                              ↓ (400 Bad Request)
                              Extract error_description
                              ↓
                              Throw AuthenticationException
                              ↓
                              GlobalExceptionHandler
                              ↓
                              Problem Details Response (401)
```

## 🧪 Testing Error Responses

### Invalid Credentials

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"wrong","password":"wrong"}'
```

**Response:**
```json
{
  "type": "about:blank",
  "title": "Authentication Failed",
  "status": 401,
  "detail": "Authentication failed: Invalid user credentials"
}
```

### Validation Error

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":""}'
```

**Response:**
```json
{
  "type": "about:blank",
  "title": "Validation Error",
  "status": 400,
  "detail": "Validation failed",
  "errors": {
    "username": "Username or email is required",
    "password": "Password is required"
  }
}
```

### User Already Exists

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"existing","email":"existing@example.com","password":"pass123"}'
```

**Response:**
```json
{
  "type": "about:blank",
  "title": "Keycloak Error",
  "status": 409,
  "detail": "User already exists"
}
```

## 🔐 Keycloak Client Configuration

**Important:** For login to work, your Keycloak client must have:

1. **Direct Access Grants Enabled** (for password grant)
2. **Valid Client ID** (can be public or confidential)
3. **Client Secret** (if confidential client)

### Public Client (Recommended for Login)

- Client authentication: **OFF**
- Direct access grants: **ON**
- Standard flow: **ON** (optional)

### Confidential Client

- Client authentication: **ON**
- Direct access grants: **ON**
- Client secret: **Required**

## 📊 Error Response Format

All errors follow RFC 7807 Problem Details:

```json
{
  "type": "about:blank",           // Error type URI
  "title": "Error Title",          // Short error title
  "status": 400,                   // HTTP status code
  "detail": "Detailed error message", // Human-readable details
  "errors": {                      // Optional: Field-level errors
    "field": "error message"
  }
}
```

## 🎨 Benefits

1. **Standardized** - RFC 7807 compliant
2. **Consistent** - Same format across all errors
3. **Detailed** - Field-level validation errors
4. **Informative** - Clear error messages
5. **Traceable** - Logged for debugging

## 📚 References

- [RFC 7807 - Problem Details for HTTP APIs](https://tools.ietf.org/html/rfc7807)
- [Spring Boot Error Handling](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.spring-application.error-handling)

