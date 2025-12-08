# Keycloak + Spring Boot (Gradle) Demo – Create User

This demo shows how to create users in Keycloak using the Admin REST API via the Java Admin Client from a Spring Boot app.

## Prerequisites
- Java 21
- Gradle (wrapper included)
- Keycloak 24+ (example uses 26)
- Optional: Docker to run Keycloak locally

## Run Keycloak locally (Docker)
```bash
docker run --name keycloak -p 8080:8080 \
  -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:26.1.0 start-dev
```

Then create a realm for the demo, e.g. `demo`.

## Create a confidential client with service account
1. In Keycloak Admin Console → your realm (`demo`).
2. Clients → Create:
   - Client ID: `spring-admin`
   - Client type: OpenID Connect
   - Access type: Confidential (or toggle "Client authentication" on in new UI)
   - Standard Flow: Off (not needed)
   - Service accounts: On
3. Save, then go to Credentials and copy the Client Secret.
4. Go to Service Account Roles and assign realm role(s):
   - From client `realm-management`: add `manage-users` (and optionally `view-users`)

If you authenticate against `master` realm with a client registered in `master`, keep `keycloak.admin-realm=master`. If your client is in the same realm as where users are created, set `keycloak.admin-realm` to that realm.

## Configure the app
Edit `src/main/resources/application.properties`:
```
keycloak.server-url=http://localhost:8080
keycloak.admin-realm=master
keycloak.realm=demo
keycloak.client-id=spring-admin
keycloak.client-secret=YOUR_SECRET
```

- `keycloak.admin-realm`: realm used to authenticate the service account (often `master`)
- `keycloak.realm`: realm where the new users will be created

## Run the app
```bash
./gradlew bootRun
```

Swagger UI: http://localhost:8081/swagger-ui.html (if you change server port accordingly). By default Spring Boot uses 8080; if it conflicts with Keycloak, run the app on another port:
```
server.port=8081
```

## Create a user (HTTP)
POST `http://localhost:8081/api/users`
```json
{
  "username": "jdoe",
  "email": "jdoe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "password": "StrongPassw0rd!",
  "enabled": true
}
```
Response `201 Created`:
```json
{ "id": "<keycloak-user-id>" }
```

## Notes
- The service first creates the user and then sets the password via `resetPassword`.
- On 409, you get a conflict when the username or email already exists.
- Ensure your service account has `realm-management` → `manage-users` in the target realm.





