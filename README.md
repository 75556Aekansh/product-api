# Product API

A RESTful Product Management API built as part of the **Java Backend Developer Hiring Assignment for Zest India IT Pvt Ltd**.

The application provides Product CRUD operations, Product-Item relationships, JWT authentication, refresh-token rotation, role-based authorization, request validation, pagination, standardized API error responses, Flyway database migrations, Swagger/OpenAPI documentation, automated tests, and Docker Compose support.

## Tech Stack

- Java 17
- Spring Boot 4.1.1
- Spring Web
- Spring Data JPA / Hibernate
- Spring Security
- JWT using JJWT 0.12.7
- PostgreSQL 16
- Flyway
- Jakarta Validation
- Springdoc OpenAPI / Swagger UI
- JUnit 5
- Mockito
- Spring Boot Test
- H2 (test database)
- Maven
- Docker
- Docker Compose

## Architecture

The application follows a layered architecture:

```text
Client
  |
  v
Controllers
  |
  v
Services
  |
  v
Repositories
  |
  v
PostgreSQL
```

### Main packages

```text
com.zestindia.assignment.productapi
├── config
│   ├── OpenApiConfig
│   └── SecurityConfig
├── controller
│   ├── AuthController
│   └── ProductController
├── dto
│   ├── request
│   └── response
├── entity
│   ├── AppUser
│   ├── Item
│   ├── Product
│   ├── RefreshToken
│   └── Role
├── exception
│   ├── ApiErrorResponse
│   ├── FieldValidationError
│   ├── GlobalExceptionHandler
│   └── ProductNotFoundException
├── repository
├── security
│   ├── CustomUserDetailsService
│   ├── JwtAuthenticationFilter
│   └── JwtService
└── service
    ├── AuthService
    ├── ItemService
    ├── ProductService
    └── RefreshTokenService
```

## Features

### Product Management

- Create products
- Retrieve paginated products
- Retrieve a product by ID
- Update a product
- Delete a product
- Retrieve items associated with a product

### Authentication & Security

- User registration
- User login
- JWT access tokens
- Refresh tokens
- Refresh-token rotation
- Logout / refresh-token revocation
- BCrypt password hashing
- Role-based authorization
- Stateless Spring Security configuration

Two roles are initialized by Flyway:

- `ROLE_USER`
- `ROLE_ADMIN`

Authorization rules:

| Operation | Required Role |
|---|---|
| Read products / items | USER or ADMIN |
| Create product | ADMIN |
| Update product | ADMIN |
| Delete product | ADMIN |

Authentication endpoints are publicly accessible.

## API Endpoints

Base URL:

```text
http://localhost:8080
```

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/auth/register` | Register a new user |
| POST | `/api/v1/auth/login` | Authenticate a user |
| POST | `/api/v1/auth/refresh` | Rotate refresh token and issue new tokens |
| POST | `/api/v1/auth/logout` | Revoke a refresh token |

### Products

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/products` | Get paginated products |
| GET | `/api/v1/products/{productId}` | Get product by ID |
| POST | `/api/v1/products` | Create a product |
| PUT | `/api/v1/products/{productId}` | Update a product |
| DELETE | `/api/v1/products/{productId}` | Delete a product |
| GET | `/api/v1/products/{productId}/items` | Get items for a product |

## Authentication Flow

### 1. Register

```http
POST /api/v1/auth/register
Content-Type: application/json
```

Request:

```json
{
  "username": "admin",
  "email": "admin@example.com",
  "password": "password123"
}
```

A successful registration returns an access token and refresh token.

### 2. Login

```http
POST /api/v1/auth/login
Content-Type: application/json
```

Request:

```json
{
  "username": "admin",
  "password": "password123"
}
```

Response:

```json
{
  "accessToken": "<JWT_ACCESS_TOKEN>",
  "refreshToken": "<REFRESH_TOKEN>",
  "tokenType": "Bearer"
}
```

### 3. Use the access token

Protected endpoints use:

```http
Authorization: Bearer <JWT_ACCESS_TOKEN>
```

### 4. Refresh the access token

```http
POST /api/v1/auth/refresh
Content-Type: application/json
```

Request:

```json
{
  "refreshToken": "<REFRESH_TOKEN>"
}
```

The existing refresh token is revoked and a new access-token / refresh-token pair is issued.

### 5. Logout

```http
POST /api/v1/auth/logout
Content-Type: application/json
```

Request:

```json
{
  "refreshToken": "<REFRESH_TOKEN>"
}
```

The refresh token is revoked.

## Product API Examples

### Create Product

```http
POST /api/v1/products
Authorization: Bearer <ADMIN_ACCESS_TOKEN>
Content-Type: application/json
```

Request:

```json
{
  "productName": "Wireless Keyboard"
}
```

A successful creation returns `201 Created` and includes a `Location` header for the created product.

### Get Products

```http
GET /api/v1/products?page=0&size=20
Authorization: Bearer <ACCESS_TOKEN>
```

Pagination defaults:

- `page = 0`
- `size = 20`
- Maximum page size = 100

Example response structure:

```json
{
  "content": [
    {
      "id": 1,
      "productName": "Wireless Keyboard",
      "createdBy": "system",
      "createdOn": "2026-08-31T10:00:00Z",
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

### Get Product

```http
GET /api/v1/products/1
Authorization: Bearer <ACCESS_TOKEN>
```

### Update Product

```http
PUT /api/v1/products/1
Authorization: Bearer <ADMIN_ACCESS_TOKEN>
Content-Type: application/json
```

Request:

```json
{
  "productName": "Mechanical Keyboard"
}
```

### Delete Product

```http
DELETE /api/v1/products/1
Authorization: Bearer <ADMIN_ACCESS_TOKEN>
```

Returns:

```text
204 No Content
```

### Get Product Items

```http
GET /api/v1/products/1/items
Authorization: Bearer <ACCESS_TOKEN>
```

## Validation

Product names are validated using Jakarta Validation.

Rules include:

- Product name must not be blank.
- Product name must not exceed 255 characters.
- Username must contain 3–100 characters.
- Email must be a valid email address.
- Password must contain 8–100 characters.
- Required authentication fields must not be blank.

Invalid requests return a standardized JSON error response.

## Error Handling

Errors are handled centrally using `GlobalExceptionHandler`.

The API provides structured information including:

```json
{
  "timestamp": "2026-08-31T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Request validation failed",
  "path": "/api/v1/products",
  "fieldErrors": [
    {
      "field": "productName",
      "message": "productName is required"
    }
  ]
}
```

The application handles cases such as:

- Product not found
- Validation failures
- Invalid request parameters
- Invalid JSON request bodies
- Database integrity conflicts

## Database Design

The database is managed using Flyway migrations.

### Product

```text
product
--------------------------------
id              BIGINT PK
product_name    VARCHAR(255)
created_by      VARCHAR(100)
created_on      TIMESTAMP WITH TIME ZONE
modified_by     VARCHAR(100)
modified_on     TIMESTAMP WITH TIME ZONE
```

### Item

```text
item
--------------------------------
id              BIGINT PK
product_id      BIGINT FK
quantity        INTEGER
```

### Authentication Tables

```text
app_user
--------------------------------
id
username
email
password_hash
enabled
created_on
modified_on
```

```text
app_role
--------------------------------
id
name
```

```text
user_role
--------------------------------
user_id
role_id
```

```text
refresh_token
--------------------------------
id
token_hash
user_id
expires_at
revoked
created_on
revoked_on
```

### Relationships

```text
Product 1 ─────────── * Item

AppUser * ─────────── * Role

AppUser 1 ─────────── * RefreshToken
```

Deleting a Product cascades to its associated Items at the database level.

## Database Indexes

The migration includes indexes for:

- `product.product_name`
- `product.created_on`
- `item.product_id`
- `refresh_token.user_id`
- `refresh_token.expires_at`

## Flyway Migrations

Database schema creation is handled by Flyway rather than Hibernate.

```text
src/main/resources/db/migration
├── V1__create_product_and_item_tables.sql
└── V2__create_auth_tables.sql
```

Hibernate is configured with:

```text
ddl-auto: validate
```

This allows Hibernate to validate the database schema without creating or modifying it.

## Configuration

The application reads database and server settings from environment variables.

Example `.env` configuration:

```env
POSTGRES_DB=productdb
POSTGRES_USER=product_user
POSTGRES_PASSWORD=db12345678
POSTGRES_PORT=5432
API_PORT=8080
```

Database environment variables used by the application:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD
SERVER_PORT
JWT_SECRET
```

The repository contains `.env.example` as a template.

**Do not commit real passwords or production JWT secrets to GitHub.**

## Running with Docker Compose

### Prerequisites

- Docker Desktop

Java and Maven are not required when running the complete application through Docker.

### Configure environment

Copy the example environment file:

```bash
cp .env.example .env
```

Update the credentials if required.

### Start the application

```bash
docker compose up --build
```

This starts:

- PostgreSQL 16
- Product API

The API will be available at:

```text
http://localhost:8080
```

PostgreSQL uses a health check, and the API waits for the database service to become healthy before starting.

### Run in background

```bash
docker compose up -d --build
```

### Stop containers

```bash
docker compose down
```

### Stop containers and remove the database volume

```bash
docker compose down -v
```

## Docker Image

The application uses a multi-stage Docker build:

1. Maven + Java 17 image for compiling the application.
2. Lightweight Java 17 JRE Alpine image for running the application.

The final container runs as a non-root `spring` user.

## Swagger / OpenAPI

OpenAPI documentation is configured for the Product API.

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

The API documentation includes JWT Bearer authentication support.

## Testing

The project uses:

- JUnit 5
- Mockito
- Spring Boot Test
- H2 in-memory database

Run the tests with Maven:

```bash
mvn test
```

The test configuration uses H2 in PostgreSQL compatibility mode:

```text
jdbc:h2:mem:productdb;MODE=PostgreSQL
```

Current tests include:

- Spring application context loading
- Product creation behavior
- Product-name trimming
- Audit-user assignment during creation
- Product-not-found behavior

## Running Without Docker

Requirements:

- Java 17+
- Maven
- PostgreSQL

Configure the database connection using the environment variables described above, then run:

```bash
mvn clean install
```

Start the application:

```bash
mvn spring-boot:run
```

## Project Structure

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
    │   ├── java
    │   │   └── com/zestindia/assignment/productapi
    │   └── resources
    │       ├── application.yml
    │       └── db/migration
    └── test
        ├── java
        └── resources
```

## Design Decisions

### DTOs

Request and response records are separated from JPA entities to avoid exposing persistence entities directly through the API.

### Stateless Authentication

Spring Security is configured with `SessionCreationPolicy.STATELESS`. Authentication is performed using JWTs rather than server-side HTTP sessions.

### Password Security

Passwords are stored using BCrypt hashes rather than plaintext values.

### Refresh Token Security

Refresh tokens are stored as SHA-256 hashes in the database. The raw refresh token is returned to the client but is not stored directly.

### Refresh Token Rotation

When a refresh token is used successfully:

1. The existing token is revoked.
2. A new access token is generated.
3. A new refresh token is generated and stored.

This prevents a previously used refresh token from being reused.

### Schema Management

Flyway owns database schema creation and versioning, while Hibernate validates the resulting schema.

## Assignment Alignment

The implementation covers the core requirements from the Zest India assignment:

- RESTful Product CRUD APIs
- `/api/v1/` API versioning
- JSON request/response format
- Standardized error handling
- Pagination
- Java 17+
- Spring Boot
- Spring Data JPA / Hibernate
- PostgreSQL
- Spring Security with JWT
- Refresh tokens with rotation
- Role-based authorization
- Jakarta Validation
- Database indexing
- JUnit 5 and Mockito
- Spring Boot integration testing
- H2 test database
- Swagger/OpenAPI
- Docker and Docker Compose

## Author

**Name:** Aekansh Singh

**Email:** `akaekansh26@gmail.com`

