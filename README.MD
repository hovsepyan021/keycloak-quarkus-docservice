## 🧪 Keycloak Configuration

Realm: `test-realm`

**Clients:**
- `documentservice` (confidential, `client_secret` = `QvuDMibZZpkEa1hig0K9s3kHXQg9nhep`)

**Users & Roles:**

| Username             | Password   | Roles    | Tenant   |
|----------------------|------------|----------|----------|
| `admin-user`         | `admin123` | `admin`  | tenant-1 |
| `admin-other-tenant` | `admin123` | `admin`  | tenant-2 |
| `viewer-user`        | `viewer123`| `viewer` | tenant-1 |
| `viewer-other-tenant`| `viewer123`| `viewer` | tenant-2 |

JWT should include:
```json
{
  "preferred_username": "admin-user",
  "tenant_id": "tenant-1",
  "realm_access": {
    "roles": ["admin"]
  },
  "resource_access": {
    "documentservice": {
      "roles": ["admin"]
    }
  }
}
```

🧪 Configure the keyclock locally

You will find Keycloak jsons in the following paths:

1) keyclock/test-realm-export-from-docker-custom.json -> This is the JSON generated from the docker CLI.

Run this command to run the Keyclock

Option 1 (manually)
```angular2html
docker run -d --name keycloak-dev -p 8080:8080 \
-e KEYCLOAK_ADMIN=admin \
-e KEYCLOAK_ADMIN_PASSWORD=admin \
quay.io/keycloak/keycloak:24.0.3 \
start-dev
```

1) Open -> http://0.0.0.0:8080/
2) Login with `admin` and `admin`
3) Create realm
4) Import the file (test-realm-export-from-docker-custom.json)

Option 2 (automatically)

Go to keylock folder and execute:
```angular2html
docker run -d --name keycloak-dev -p 8080:8080 \
-e KEYCLOAK_ADMIN=admin \
-e KEYCLOAK_ADMIN_PASSWORD=admin \
-v "$(pwd)/test-realm-export-from-docker-custom.json:/opt/keycloak/data/import/realm.json" \
quay.io/keycloak/keycloak:24.0.3 \
start-dev --import-realm
```

🧪 Run All Tests

```angular2html
./mvnw verify
```

if you got this error: 
```
.mvn/wrapper/maven-wrapper.jar: No such file or directory
```

execute:
```angular2html
mvn -N io.takari:maven:wrapper
```

🧪 Run the project 
```angular2html
./mvnw quarkus:dev
```



Notes:
Make sure that your local Keycloak instance is running and accessible before starting the project.

