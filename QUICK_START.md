# Quick Start Guide 🚀

## 1. Start Keycloak

```bash
docker run --name keycloak -p 8080:8080 \
  -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:26.1.0 start-dev
```

## 2. Keycloak Setup (One-Time)

1. **Login:** http://localhost:8080 (admin/admin)
2. **Create Realm:** `demo`
3. **Create Admin Client:**
   - Client ID: `spring-admin`
   - Client authentication: ON
   - Service accounts: ON
   - Copy client secret
4. **Create Login Client:**
   - Client ID: `spring-app` (or reuse `spring-admin`)
   - Direct access grants: ON
   - (Optional: Client authentication OFF for public client)
5. **Create Roles:** `admin`, `user`
6. **Create Test User:**
   - Username: `testuser`
   - Password: `password123`
   - Assign role: `user`

## 3. Configure App

Edit `application.properties`:
```properties
keycloak.server-url=http://localhost:8080
keycloak.realm=demo
keycloak.client-id=spring-admin  # or spring-app for login
keycloak.client-secret=YOUR_SECRET_HERE
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/demo
server.port=8081
```

## 4. Run App

```bash
./gradlew bootRun
```

## 5. Test Flow

### Login (Get JWT):
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

### Use JWT Token:
```bash
# Get user info
curl http://localhost:8081/api/user/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"

# Access protected endpoint
curl http://localhost:8081/api/users \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## 📖 Full Documentation

See `KEYCLOAK_JWT_WALKTHROUGH.md` for detailed explanation!
