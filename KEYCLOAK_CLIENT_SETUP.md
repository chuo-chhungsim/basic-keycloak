# Keycloak Client Setup Guide 🔐

## Problem: "Client not allowed for direct access grants"

This error means your Keycloak client doesn't have **Direct Access Grants** enabled, which is required for the password grant flow (username/password login).

## Solution: Enable Direct Access Grants

### Option 1: Enable Direct Access Grants on Existing Client (Quick Fix)

1. **Open Keycloak Admin Console**
   - Go to: http://localhost:8081
   - Login with admin credentials

2. **Navigate to Your Realm**
   - Select realm: `demo` (or your realm name)

3. **Go to Clients**
   - Click **Clients** in the left menu
   - Find and click on your client: `spring-admin`

4. **Enable Direct Access Grants**
   - Scroll down to **Settings** section
   - Find **Direct access grants** toggle
   - **Turn it ON** ✅
   - Click **Save**

5. **Test Login Again**
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"testuser","password":"123"}'
   ```

---

### Option 2: Create Separate Client for Login (Recommended)

If you want to keep your admin client (`spring-admin`) for service accounts only, create a separate **public client** for user login:

#### Step 1: Create New Client

1. **Clients** → **Create client**
2. **Settings:**
   - **Client ID:** `spring-app` (or any name)
   - **Client authentication:** **OFF** (Public client)
   - Click **Next**

3. **Capability config:**
   - **Direct access grants:** **ON** ✅
   - **Standard flow:** **ON** (optional, for browser-based login)
   - Click **Next**

4. **Login settings:**
   - **Valid redirect URIs:** `http://localhost:8080/*`
   - Click **Save**

#### Step 2: Update Application Properties

Edit `application.properties`:

```properties
# For login endpoint - use public client
keycloak.client-id=spring-app
keycloak.client-secret=  # Leave empty for public client

# For admin operations - keep using spring-admin
# (This is handled separately in KeycloakAdminConfig)
```

#### Step 3: Test Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123"}'
```

---

## Current Configuration

Based on your `application.properties`:

```properties
keycloak.server-url=http://localhost:8081
keycloak.realm=demo
keycloak.client-id=spring-admin
keycloak.client-secret=pCZb8T7SAhBvD4mDsAZAxZ5gy8wDJ8gB
```

**Your client `spring-admin` needs:**
- ✅ **Client authentication:** ON (Confidential)
- ✅ **Service accounts:** ON (for admin operations)
- ✅ **Direct access grants:** ON (for user login) ← **This is missing!**

---

## Quick Fix Steps (Visual Guide)

### In Keycloak Admin Console:

```
1. Login → http://localhost:8081
2. Select Realm: demo
3. Clients → spring-admin
4. Settings Tab:
   ┌─────────────────────────────────────┐
   │ Client ID: spring-admin              │
   │ Client authentication: ON             │
   │ Direct access grants: OFF ← Turn ON! │
   │ Service accounts: ON                  │
   └─────────────────────────────────────┘
5. Click Save
```

---

## Verify Client Configuration

After enabling Direct Access Grants, verify:

1. **Client Settings:**
   - Direct access grants: ✅ Enabled
   - Client authentication: ✅ Enabled (if confidential)
   - Service accounts: ✅ Enabled (for admin operations)

2. **Test Token Endpoint Directly:**
   ```bash
   curl -X POST http://localhost:8081/realms/demo/protocol/openid-connect/token \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "grant_type=password" \
     -d "client_id=spring-admin" \
     -d "client_secret=pCZb8T7SAhBvD4mDsAZAxZ5gy8wDJ8gB" \
     -d "username=testuser" \
     -d "password=123"
   ```

   **Expected:** Should return access_token, refresh_token, etc.

---

## Troubleshooting

### Still Getting Error?

1. **Check Client ID:**
   - Verify `keycloak.client-id` matches the client ID in Keycloak
   - Case-sensitive!

2. **Check Client Secret:**
   - For confidential clients, secret is required
   - Copy from **Credentials** tab in Keycloak

3. **Check Realm:**
   - Verify `keycloak.realm` matches your realm name
   - Default: `demo`

4. **Check User Exists:**
   - User must exist in the realm
   - User must have password set
   - User must be enabled

5. **Check Keycloak Logs:**
   ```bash
   docker logs keycloak
   ```

---

## Summary

**To fix "Client not allowed for direct access grants":**

1. ✅ Go to Keycloak Admin Console
2. ✅ Select your realm (`demo`)
3. ✅ Go to Clients → `spring-admin`
4. ✅ Enable **Direct access grants**
5. ✅ Click **Save**
6. ✅ Test login again

That's it! 🎉

