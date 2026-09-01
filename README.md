# Product API

A RESTful Product Management API built for the **Zest India Java Backend Developer assignment**.

The service provides versioned Product CRUD endpoints, Product–Item relationships, JWT authentication, refresh-token rotation, role-based authorization, validation, pagination, structured error handling, Flyway database migrations, Swagger/OpenAPI documentation, automated tests, and Docker Compose support.

## Tech stack

- Java 17
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA / Hibernate
- Spring Security
- JJWT 0.12.7
- PostgreSQL 16
- Flyway
- Jakarta Validation
- Springdoc OpenAPI / Swagger UI
- JUnit 5, Mockito, MockMvc, and H2
- Maven
- Docker and Docker Compose

## Architecture

```text
HTTP client
    ↓
Controllers
    ↓
Services
    ↓
Repositories
    ↓
PostgreSQL
```

Request and response DTOs are kept separate from JPA entities. Spring Security runs before the controllers and authenticates Bearer tokens through a stateless JWT filter.

## Features

### Product management

- Create products
- Retrieve paginated products
- Retrieve a product by ID
- Update products
- Delete products
- Retrieve items belonging to a product
- Record the authenticated username in audit fields

### Authentication and authorization

- User registration and login
- BCrypt password hashing
- Short-lived JWT access tokens
- Hashed refresh-token storage
- Refresh-token rotation
- Logout through refresh-token revocation
- Stateless Spring Security configuration
- `ROLE_USER` and `ROLE_ADMIN` authorization

Flyway initializes these roles:

```text
ROLE_USER
ROLE_ADMIN
```

New registrations receive only `ROLE_USER`.

| Operation | `ROLE_USER` | `ROLE_ADMIN` |
|---|---:|---:|
| Read products and items | Yes | Yes |
| Create a product | No | Yes |
| Update a product | No | Yes |
| Delete a product | No | Yes |

## API endpoints

Default base URL:

```text
http://localhost:8080
```

### Authentication

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Public | Register a user and issue tokens |
| `POST` | `/api/v1/auth/login` | Public | Authenticate and issue tokens |
| `POST` | `/api/v1/auth/refresh` | Public | Rotate a refresh token |
| `POST` | `/api/v1/auth/logout` | Public | Revoke a refresh token |

### Products

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/v1/products` | USER or ADMIN | Get paginated products |
| `GET` | `/api/v1/products/{productId}` | USER or ADMIN | Get a product by ID |
| `GET` | `/api/v1/products/{productId}/items` | USER or ADMIN | Get items for a product |
| `POST` | `/api/v1/products` | ADMIN | Create a product |
| `PUT` | `/api/v1/products/{productId}` | ADMIN | Update a product |
| `DELETE` | `/api/v1/products/{productId}` | ADMIN | Delete a product |

## Quick start with Docker

### Prerequisites

- Docker Desktop or Docker Engine with Compose

Java, Maven, and a locally installed PostgreSQL server are not required when using Docker Compose.

### 1. Optional environment customization

The committed Compose configuration contains development defaults, so a fresh clone can start without creating `.env`.

To customize the defaults, copy `.env.example` to `.env`. The example file contains:

```env
POSTGRES_DB=productdb
POSTGRES_USER=product_user
POSTGRES_PASSWORD=db12345678
POSTGRES_PORT=5432
API_PORT=8080
JWT_SECRET=VGhpc0lzQVN0cm9uZ0Rldk9ubHlTZWNyZXRLZXlGb3JKV1RUMjAyNg==
```

These are public development values intended only for local execution. Override them for any deployed environment. A production `JWT_SECRET` must be independently generated, Base64 encoded, and decode to at least 32 bytes. Never commit real production credentials or secrets.

The `api` service in `compose.yml` must forward the secret:

```yaml
environment:
  JWT_SECRET: ${JWT_SECRET:-VGhpc0lzQVN0cm9uZ0Rldk9ubHlTZWNyZXRLZXlGb3JKV1RUMjAyNg==}
```

### 2. Build and start

A new contributor can clone the repository and immediately run:

```bash
docker compose up --build
```

Run in the background:

```bash
docker compose up -d --build
```

The API waits for PostgreSQL's health check before starting.

### 3. Stop

```bash
docker compose down
```

To also delete the local database volume:

```bash
docker compose down -v
```

> `docker compose down -v` permanently removes the local PostgreSQL data stored in the Compose volume.

## Authentication workflow

### Register

```http
POST /api/v1/auth/register
Content-Type: application/json
```

```json
{
  "username": "aekansh",
  "email": "aekansh@example.com",
  "password": "StrongPass@123"
}
```

Successful registration returns `201 Created`:

```json
{
  "accessToken": "<JWT_ACCESS_TOKEN>",
  "refreshToken": "<REFRESH_TOKEN>",
  "tokenType": "Bearer"
}
```

### Login

```http
POST /api/v1/auth/login
Content-Type: application/json
```

```json
{
  "username": "aekansh",
  "password": "StrongPass@123"
}
```

### Call a protected endpoint

```http
GET /api/v1/products
Authorization: Bearer <JWT_ACCESS_TOKEN>
```

Access tokens expire after 15 minutes by default.

### Refresh

```http
POST /api/v1/auth/refresh
Content-Type: application/json
```

```json
{
  "refreshToken": "<REFRESH_TOKEN>"
}
```

A successful refresh revokes the submitted refresh token and returns a new access-token/refresh-token pair. Reusing the old token returns `401 Unauthorized`.

### Logout

```http
POST /api/v1/auth/logout
Content-Type: application/json
```

```json
{
  "refreshToken": "<REFRESH_TOKEN>"
}
```

Successful logout returns `204 No Content`. The revoked refresh token cannot be used again. An already issued access token remains valid until it expires.

## Grant a local test user admin access

Registration must not allow clients to assign themselves `ROLE_ADMIN`. For local assignment testing, promote a registered account directly through PostgreSQL:

```bash
docker compose exec database psql -U product_user -d productdb -c "INSERT INTO user_role (user_id, role_id) SELECT u.id, r.id FROM app_user u CROSS JOIN app_role r WHERE u.username = 'aekansh' AND r.name = 'ROLE_ADMIN' ON CONFLICT DO NOTHING;"
```

Verify the assigned roles:

```bash
docker compose exec database psql -U product_user -d productdb -c "SELECT u.username, r.name FROM app_user u JOIN user_role ur ON ur.user_id = u.id JOIN app_role r ON r.id = ur.role_id WHERE u.username = 'aekansh';"
```

Expected result:

```text
aekansh | ROLE_USER
aekansh | ROLE_ADMIN
```

Log in again and use the returned access token for Product write operations.

## Product examples

### Create a product

```http
POST /api/v1/products
Authorization: Bearer <ADMIN_ACCESS_TOKEN>
Content-Type: application/json
```

```json
{
  "productName": "Mouse"
}
```

A successful request returns `201 Created` and a `Location` header.

### List products

```http
GET /api/v1/products?page=0&size=20
Authorization: Bearer <ACCESS_TOKEN>
```

Pagination defaults:

- `page`: `0`
- `size`: `20`
- Maximum page size: `100`

Example response:

```json
{
  "content": [
    {
      "id": 1,
      "productName": "Keyboard",
      "createdBy": "aekansh",
      "createdOn": "2026-09-01T10:00:00Z",
      "modifiedBy": null,
      "modifiedOn": null
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

`modifiedBy` and `modifiedOn` are `null` until the product is updated.

### Update a product

```http
PUT /api/v1/products/1
Authorization: Bearer <ADMIN_ACCESS_TOKEN>
Content-Type: application/json
```

```json
{
  "productName": "Mechanical Keyboard"
}
```

The update response contains the authenticated username in `modifiedBy` and an updated `modifiedOn` timestamp.

### Delete a product

```http
DELETE /api/v1/products/1
Authorization: Bearer <ADMIN_ACCESS_TOKEN>
```

Successful deletion returns `204 No Content`.

## Swagger and OpenAPI

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

To authorize in Swagger:

1. Call the login or registration endpoint.
2. Copy the `accessToken` value.
3. Select **Authorize**.
4. Paste only the raw token beginning with `eyJ...`.

Swagger adds the `Bearer` prefix automatically.

## Validation and errors

Validation includes:

- Product name must not be blank and must not exceed 255 characters.
- Username must contain 3–100 characters.
- Email must be valid and no longer than 255 characters.
- Password must contain 8–100 characters.
- Required authentication fields must not be blank.

Common statuses:

| Status | Meaning |
|---:|---|
| `400` | Validation failure or malformed request |
| `401` | Missing, invalid, expired, or revoked credentials |
| `403` | Authenticated user lacks the required role |
| `404` | Product does not exist |
| `409` | Username or email already exists, or another integrity conflict occurred |

## Database and migrations

Flyway owns production schema creation and Hibernate uses `ddl-auto: validate`.

```text
src/main/resources/db/migration
├── V1__create_product_and_item_tables.sql
└── V2__create_auth_tables.sql
```

Main relationships:

```text
Product 1 ─── * Item
AppUser * ─── * Role
AppUser 1 ─── * RefreshToken
```

Indexes cover product names, creation timestamps, Product–Item lookups, refresh-token users, and refresh-token expiry.

## Testing

Run all tests:

```bash
mvn clean test
```

Run the full Maven verification lifecycle:

```bash
mvn clean verify
```

The current 12-test suite covers:

- Spring application context startup
- Product-name trimming and authenticated audit assignment
- Product-not-found behavior
- Registration and default role assignment
- Duplicate username rejection
- Incorrect-password rejection
- Refresh-token rotation
- Logout revocation
- Anonymous request rejection
- `ROLE_USER` read access
- `ROLE_USER` write denial
- `ROLE_ADMIN` product creation

Tests use an H2 in-memory database in PostgreSQL compatibility mode. The test profile uses Hibernate `create-drop`; production continues to use PostgreSQL and Flyway.

## Running without Docker

Requirements:

- Java 17+
- Maven 3.9+
- PostgreSQL 16-compatible server

Set these application environment variables:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD
SERVER_PORT
JWT_SECRET
```

Then run:

```bash
mvn clean verify
mvn spring-boot:run
```

## Docker image design

The Dockerfile uses a multi-stage build:

1. Maven with Eclipse Temurin 17 compiles the application.
2. Eclipse Temurin 17 JRE Alpine runs the packaged JAR.

The runtime container executes as the non-root `spring` user.

## Project structure

```text
product-api/
├── .dockerignore
├── .env.example
├── .gitignore
├── Dockerfile
├── compose.yml
├── pom.xml
├── README.md
└── src
    ├── main
    │   ├── java/com/zestindia/assignment/productapi
    │   │   ├── config
    │   │   ├── controller
    │   │   ├── dto
    │   │   ├── entity
    │   │   ├── exception
    │   │   ├── repository
    │   │   ├── security
    │   │   └── service
    │   └── resources
    │       ├── application.yml
    │       └── db/migration
    └── test
        ├── java
        └── resources/application-test.yml
```

## Design decisions

- **DTO boundaries:** JPA entities are not exposed directly through controllers.
- **Stateless authentication:** JWT access tokens replace server-side HTTP sessions.
- **Password protection:** BCrypt hashes are stored instead of plaintext passwords.
- **Refresh-token protection:** Only SHA-256 hashes of refresh tokens are stored.
- **Rotation:** Every successful refresh revokes the submitted refresh token.
- **Auditing:** Product create/update operations record the authenticated username.
- **Schema ownership:** Flyway manages production schema evolution; Hibernate validates it.
- **Container security:** The runtime image uses a non-root user.

## Author

**Aekansh Singh**  
`akaekansh26@gmail.com`
