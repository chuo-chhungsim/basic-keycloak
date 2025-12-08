# Fix "Account is not fully set up" Error 🔧

## Quick Fix Steps

### Step 1: Open Keycloak Admin Console
1. Go to: **http://localhost:8081**
2. Login with admin credentials

### Step 2: Find Your User
1. Select realm: **`demo`**
2. Click **Users** in left menu
3. Find user: **`testuser`** (or search)

### Step 3: Set Password
1. Click on the user → Go to **Credentials** tab
2. Click **Set password** button
3. Enter:
   - **Password:** `123` (or your password)
   - **Password confirmation:** `123`
   - **Temporary:** Turn **OFF** ✅ (important!)
4. Click **Set password** button
5. Confirm in dialog

### Step 4: Enable User & Verify Email
1. Go to **Details** tab
2. Check these settings:
   - ✅ **Enabled:** ON
   - ✅ **Email verified:** ON (if email exists)
3. Click **Save**

### Step 5: Test Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123"}'
```

---

## Visual Guide

### In Keycloak Admin Console:

```
Users → testuser → Credentials Tab

┌─────────────────────────────────────┐
│ Set Password                        │
│ ─────────────────────────────────── │
│ Password: 123                        │
│ Password Confirmation: 123           │
│ Temporary: OFF ✅                    │
│ ─────────────────────────────────── │
│ [Set password]                      │
└─────────────────────────────────────┘

Details Tab:

┌─────────────────────────────────────┐
│ Username: testuser                  │
│ Email: testuser@example.com        │
│ Enabled: ON ✅                       │
│ Email verified: ON ✅               │
│ ─────────────────────────────────── │
│ [Save]                              │
└─────────────────────────────────────┘
```

---

## Common Causes

### ❌ Password Not Set
- **Fix:** Set password in Credentials tab

### ❌ Password is Temporary
- **Fix:** Turn "Temporary" OFF when setting password

### ❌ User is Disabled
- **Fix:** Enable user in Details tab

### ❌ Email Not Verified
- **Fix:** Check "Email verified" in Details tab

---

## Alternative: Create User via API

If you have admin access, create a properly configured user:

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '{
    "username": "testuser",
    "email": "testuser@example.com",
    "firstName": "Test",
    "lastName": "User",
    "password": "123",
    "enabled": true
  }'
```

This automatically:
- ✅ Creates user
- ✅ Sets password (non-temporary)
- ✅ Enables user
- ✅ Verifies email

---

## Verify User is Set Up Correctly

Check these in Keycloak:

- ✅ **Username** is set
- ✅ **Password** is set (Credentials tab)
- ✅ **Password is NOT temporary**
- ✅ **User is Enabled** (Details tab)
- ✅ **Email verified** (Details tab, if email exists)

---

## Test Directly with Keycloak

Test if user can login directly:

```bash
curl -X POST http://localhost:8081/realms/demo/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=spring-app" \
  -d "username=testuser" \
  -d "password=123"
```

**Expected:** Should return access_token, refresh_token, etc.

**If error:** User is not properly set up - follow steps above.

---

## Summary

**To fix "Account is not fully set up":**

1. ✅ Keycloak Admin Console → Users → Your user
2. ✅ **Credentials** tab → Set password (Temporary: OFF)
3. ✅ **Details** tab → Enable user, verify email
4. ✅ Save
5. ✅ Test login

Done! 🎉

