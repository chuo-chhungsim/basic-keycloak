# Troubleshooting Guide 🔧

## ✅ Fixed Issues

### 1. **Dependency Resolution Error** ✅ FIXED
**Problem:** `Could not find org.keycloak:keycloak-admin-client:26.1.3`

**Solution:** Updated to version `25.0.6` which is available in Maven Central
- Removed `keycloak-core` dependency (not needed)
- Removed JBoss repository (Maven Central has everything)
- Updated springdoc version to `2.8.5`

**Status:** ✅ Build now succeeds!

---

## 🚨 Common Runtime Issues

### Issue 1: App Won't Start - Keycloak Connection Error

**Symptoms:**
- Application fails to start
- Error about connecting to Keycloak
- OAuth2 Resource Server can't fetch JWKS

**Cause:** 
Spring Security tries to validate JWT configuration at startup by connecting to Keycloak's issuer URI.

**Solution:**
1. **Start Keycloak first:**
   ```bash
   docker run --name keycloak -p 8080:8080 \
     -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin \
     quay.io/keycloak/keycloak:26.1.0 start-dev
   ```

2. **Wait for Keycloak to be ready** (takes ~30 seconds)
   ```bash
   curl http://localhost:8080/realms/demo
   ```

3. **Then start the Spring Boot app:**
   ```bash
   ./gradlew bootRun
   ```

---

### Issue 2: "Unable to find matching target resource method"

**Symptoms:**
- App seems to start but endpoints return this error
- Requests to `/api/auth/login` fail

**Possible Causes:**
1. **Keycloak not running** - OAuth2 Resource Server can't initialize
2. **Wrong issuer URI** - Check `application.properties`
3. **Port conflict** - Another app using port 8081

**Solution:**
1. Verify Keycloak is running:
   ```bash
   curl http://localhost:8080/realms/demo/.well-known/openid-configuration
   ```

2. Check `application.properties`:
   ```properties
   spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/demo
   ```

3. Check if port 8081 is available:
   ```bash
   lsof -i :8081
   ```

---

### Issue 3: Login Fails - "Invalid login request"

**Symptoms:**
- POST to `/api/auth/login` returns error
- "Invalid username or password" or "Invalid login request"

**Causes:**
1. **Client doesn't have "Direct access grants" enabled**
2. **Wrong client ID/secret**
3. **User doesn't exist in Keycloak**
4. **Password grant not enabled for client**

**Solution:**
1. In Keycloak Admin Console → Your realm → Clients → Your client
2. **Settings** tab:
   - Enable "Direct access grants" (for password grant)
   - Verify Client ID matches `application.properties`
3. **Credentials** tab:
   - Copy Client Secret (if confidential client)
4. Create test user:
   - Users → Create user
   - Set username/password
   - Assign roles

---

### Issue 4: JWT Validation Fails - 401 Unauthorized

**Symptoms:**
- Login works, but protected endpoints return 401
- "Invalid token" errors

**Causes:**
1. **Token expired**
2. **Wrong issuer URI**
3. **Token format incorrect** (missing "Bearer " prefix)

**Solution:**
1. Check token expiration:
   ```bash
   # Decode JWT at jwt.io to see 'exp' claim
   ```

2. Verify issuer URI matches Keycloak realm:
   ```properties
   spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/demo
   ```

3. Use correct header format:
   ```bash
   curl -H "Authorization: Bearer YOUR_TOKEN_HERE" http://localhost:8081/api/user/me
   ```

---

### Issue 5: Access Denied - 403 Forbidden

**Symptoms:**
- Request authenticated but returns 403
- "Access Denied" error

**Causes:**
1. **User doesn't have required role**
2. **Role mapping incorrect**
3. **SecurityConfig rules too restrictive**

**Solution:**
1. Check user's roles in Keycloak:
   - Users → Your user → Role mapping
   - Verify roles are assigned

2. Check SecurityConfig authorization rules:
   ```java
   .requestMatchers("/admin/**").hasRole("admin")
   .requestMatchers("/api/users/**").hasAnyRole("admin", "user")
   ```

3. Verify role extraction in JWT:
   ```bash
   # Decode JWT and check 'realm_access.roles' array
   ```

---

## 🔍 Debugging Steps

### Step 1: Verify Build
```bash
./gradlew clean build
```
Should complete successfully ✅

### Step 2: Check Keycloak is Running
```bash
curl http://localhost:8080/realms/demo/.well-known/openid-configuration
```
Should return JSON with issuer, jwks_uri, etc.

### Step 3: Test Login Endpoint
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```
Should return JWT tokens.

### Step 4: Test Protected Endpoint
```bash
# First get token from login
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}' | jq -r '.accessToken')

# Then use token
curl http://localhost:8081/api/user/me \
  -H "Authorization: Bearer $TOKEN"
```

---

## 📋 Pre-Flight Checklist

Before running the app, ensure:

- [ ] Keycloak is running on port 8080
- [ ] Realm `demo` exists in Keycloak
- [ ] Client configured with correct settings
- [ ] Client secret copied to `application.properties`
- [ ] Test user created with password
- [ ] User has roles assigned
- [ ] `application.properties` has correct values:
  - [ ] `keycloak.server-url`
  - [ ] `keycloak.realm`
  - [ ] `keycloak.client-id`
  - [ ] `keycloak.client-secret`
  - [ ] `spring.security.oauth2.resourceserver.jwt.issuer-uri`

---

## 🆘 Still Having Issues?

1. **Check application logs:**
   ```bash
   ./gradlew bootRun 2>&1 | tee app.log
   ```

2. **Verify Keycloak logs:**
   ```bash
   docker logs keycloak
   ```

3. **Test Keycloak directly:**
   ```bash
   # Get token directly from Keycloak
   curl -X POST http://localhost:8080/realms/demo/protocol/openid-connect/token \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "client_id=spring-app" \
     -d "username=testuser" \
     -d "password=password123" \
     -d "grant_type=password"
   ```

4. **Check Spring Security debug logs:**
   Add to `application.properties`:
   ```properties
   logging.level.org.springframework.security=DEBUG
   ```

---

## ✅ Success Indicators

Your setup is working when:
- ✅ Build completes without errors
- ✅ App starts on port 8081
- ✅ Login endpoint returns JWT tokens
- ✅ Protected endpoints accept JWT tokens
- ✅ Role-based access control works

Good luck! 🚀
