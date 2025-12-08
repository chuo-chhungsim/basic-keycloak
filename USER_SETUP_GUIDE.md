# Keycloak User Setup Guide 👤

## Problem: "Account is not fully set up"

This error means the user exists in Keycloak but:
- ❌ Doesn't have a password set
- ❌ User is disabled
- ❌ Email verification required
- ❌ Account setup incomplete

## Solution: Set Up User Properly

### Option 1: Set Up User via Keycloak Admin Console (Manual)

#### Step 1: Find the User

1. **Open Keycloak Admin Console**
   - Go to: http://localhost:8081
   - Login with admin credentials

2. **Navigate to Users**
   - Select realm: `demo`
   - Click **Users** in the left menu
   - Find your user: `testuser` (or search)

#### Step 2: Set Password

1. **Click on the user** → Go to **Credentials** tab
2. **Set Password:**
   - Enter password: `123` (or your desired password)
   - **Temporary:** Turn **OFF** (so user doesn't need to change it)
   - Click **Set Password**
   - Confirm in the dialog

#### Step 3: Enable User (if disabled)

1. Go to **Details** tab
2. Check **Email verified** (if email is set)
3. Check **Enabled** toggle → Should be **ON** ✅
4. Click **Save**

#### Step 4: Test Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123"}'
```

---

### Option 2: Create User via API (Recommended)

Use the existing `/api/users` endpoint to create a properly configured user:

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "testuser@example.com",
    "firstName": "Test",
    "lastName": "User",
    "password": "123",
    "enabled": true
  }'
```

**Note:** This endpoint requires authentication (admin or user role).

---

### Option 3: Fix Existing User via Keycloak Admin Console

If user already exists but password is missing:

1. **Users** → Click on `testuser`
2. **Credentials** tab
3. **Set Password:**
   ```
   Password: 123
   Password Confirmation: 123
   Temporary: OFF
   ```
4. Click **Set Password**
5. **Details** tab:
   - **Enabled:** ON ✅
   - **Email verified:** ON ✅ (if email exists)
6. Click **Save**

---

## Quick Checklist

When creating/setting up a user, ensure:

- ✅ **Username** is set
- ✅ **Password** is set (via Credentials tab)
- ✅ **Password is NOT temporary** (unless you want user to change it)
- ✅ **User is Enabled** (Details tab)
- ✅ **Email verified** (if email is provided)
- ✅ **User has roles assigned** (Role mapping tab)

---

## Verify User Setup

### Check User Status

In Keycloak Admin Console:

```
Users → testuser → Details Tab

┌─────────────────────────────────────┐
│ Username: testuser                  │
│ Email: testuser@example.com         │
│ Enabled: ON ✅                      │
│ Email verified: ON ✅               │
└─────────────────────────────────────┘

Credentials Tab:
┌─────────────────────────────────────┐
│ Password: [Set] ✅                 │
│ Temporary: OFF ✅                  │
└─────────────────────────────────────┘
```

### Test User Login Directly

Test with Keycloak token endpoint:

```bash
curl -X POST http://localhost:8081/realms/demo/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=spring-app" \
  -d "username=testuser" \
  -d "password=123"
```

**Expected Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIg...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCIg...",
  "token_type": "Bearer",
  "expires_in": 300,
  "refresh_expires_in": 1800
}
```

---

## Common Issues

### Issue 1: "User not found"

**Solution:**
- User doesn't exist in the realm
- Create user first (via Admin Console or API)

### Issue 2: "Invalid credentials"

**Solution:**
- Password is incorrect
- Password is temporary and needs to be changed
- Reset password in Credentials tab

### Issue 3: "User is disabled"

**Solution:**
- Go to User → Details tab
- Enable the user (toggle ON)
- Click Save

### Issue 4: "Email not verified"

**Solution:**
- Go to User → Details tab
- Check "Email verified" checkbox
- Click Save

---

## Step-by-Step: Complete User Setup

### 1. Create User (if doesn't exist)

**Via Admin Console:**
- Users → Create new user
- Username: `testuser`
- Email: `testuser@example.com`
- First name: `Test`
- Last name: `User`
- Click **Create**

### 2. Set Password

- Go to **Credentials** tab
- Click **Set password**
- Password: `123`
- Password confirmation: `123`
- **Temporary:** OFF
- Click **Set password**

### 3. Enable User

- Go to **Details** tab
- **Enabled:** ON ✅
- **Email verified:** ON ✅
- Click **Save**

### 4. Assign Roles (Optional)

- Go to **Role mapping** tab
- Assign realm roles: `user` or `admin`
- Click **Assign**

### 5. Test Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123"}'
```

---

## Using the Create User API

If you have admin access, use the API to create users:

```bash
# First, get admin token (if needed)
# Then create user:
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '{
    "username": "newuser",
    "email": "newuser@example.com",
    "firstName": "New",
    "lastName": "User",
    "password": "password123",
    "enabled": true
  }'
```

The API automatically:
- ✅ Creates the user
- ✅ Sets the password
- ✅ Enables the user

---

## Summary

**To fix "Account is not fully set up":**

1. ✅ Go to Keycloak Admin Console
2. ✅ Users → Find your user
3. ✅ **Credentials** tab → Set password (Temporary: OFF)
4. ✅ **Details** tab → Enable user, verify email
5. ✅ Save changes
6. ✅ Test login

That's it! 🎉

