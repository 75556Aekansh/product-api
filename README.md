# Product API

Java backend assignment implementation using Spring Boot, PostgreSQL, Flyway,
and Docker Compose.

## Current milestone

- Product CRUD REST API
- PostgreSQL database container
- Product and Item JPA entities
- Versioned Flyway database migration
- Database indexes and foreign-key constraint
- Request validation and standardized JSON error responses
- Pagination and safe product sorting
- H2 test configuration
- Multi-stage application Docker image

Security, JWT authentication, refresh tokens, OpenAPI documentation, and
additional test coverage will be added in later milestones.

## Requirements

- Docker Desktop (includes Docker Compose)

Java and Maven are only needed when running outside Docker.

## Run with Docker

```bash
docker compose up --build
```

The application will be available at `http://localhost:8080`. A successful
startup confirms that PostgreSQL, Flyway, and JPA schema validation are working
together.

Stop the containers:

```bash
docker compose down
```

Stop the containers and remove the local database volume:

```bash
docker compose down -v
```

## Configuration

Copy `.env.example` to `.env` before changing the default development
credentials:

```bash
cp .env.example .env
```

Do not commit `.env`; it is intentionally ignored by Git.

## Database design

`product` has a one-to-many relationship with `item`. Deleting a product also
deletes its items through the database foreign key. The schema is created by
Flyway from `src/main/resources/db/migration` rather than by Hibernate.

Indexes are included for product-name lookups, product creation-date sorting,
and item lookup by product ID.

## Product API

| Method | Endpoint | Result |
| --- | --- | --- |
| `GET` | `/api/v1/products?page=0&size=20&sort=createdOn,desc` | Paginated products |
| `GET` | `/api/v1/products/{id}` | One product |
| `POST` | `/api/v1/products` | Creates a product |
| `PUT` | `/api/v1/products/{id}` | Updates a product |
| `DELETE` | `/api/v1/products/{id}` | Deletes a product and its items |

Create or update request body:

```json
{
  "productName": "Wireless Keyboard"
}
```

Example create request:

```bash
curl -i -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{"productName":"Wireless Keyboard"}'
```

Until JWT authentication is added, the API uses `system` as the audit user.

## Run tests locally

With Java 17 and Maven installed:

```bash
mvn test
```

Tests use an H2 in-memory database in PostgreSQL compatibility mode.
