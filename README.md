# E-Commerce Backend API

A clean, layered Spring Boot backend for an e-commerce storefront — product catalog,
categories, JWT authentication, shopping cart, and order checkout — backed by an
embedded H2 database. Built as the backend counterpart to a React e-commerce frontend
that previously relied on public third-party APIs (dummyjson.com).

## Tech Stack

- **Java 17** + **Spring Boot 3.3**
- **Spring Data JPA** + **H2** (file-based, so data survives restarts)
- **Spring Security 6** with **JWT** (jjwt) authentication
- **springdoc-openapi** (Swagger UI) for interactive API docs
- **Lombok**, **Bean Validation**
- **Maven**

## Project Structure

```
src/main/java/com/ecommerce/backend/
├── config/        SecurityConfig, OpenApiConfig, DataSeeder
├── controller/     REST controllers (Auth, Category, Product, Cart, Order)
├── dto/            request/ and response/ payloads
├── entity/         JPA entities
├── exception/       custom exceptions + GlobalExceptionHandler
├── repository/      Spring Data JPA repositories
├── security/         JWT filter, JwtService, UserDetails
└── service/          business logic
```

## Running Locally

Requires JDK 17+ and Maven 3.9+.

```bash
mvn clean package
java -jar target/ecommerce-backend.jar
```

The app starts on **http://localhost:8080**.

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- H2 Console: http://localhost:8080/h2-console
  (JDBC URL `jdbc:h2:file:./data/ecommercedb`, user `sa`, empty password)

On first startup, the database is seeded automatically with 20 categories, ~4 products
each, and two demo accounts:

| Role  | Email             | Password  |
|-------|-------------------|-----------|
| Admin | admin@example.com | Admin@123 |
| User  | user@example.com  | User@123  |

## Running with Docker

```bash
docker build -t ecommerce-backend .
docker run -p 8080:8080 -v $(pwd)/data:/app/data ecommerce-backend
```

The volume mount keeps the H2 file database persistent across container restarts.

## Configuration

All configuration lives in `src/main/resources/application.yml` and can be overridden
via environment variables when deploying:

| Env Var                 | Purpose                                   | Default            |
|--------------------------|--------------------------------------------|---------------------|
| `JWT_SECRET`              | Base64 HMAC key used to sign JWTs           | dev key (change in prod) |
| `JWT_EXPIRATION_MS`       | Token lifetime in milliseconds              | 86400000 (24h)      |
| `CORS_ALLOWED_ORIGINS`    | Comma-separated list of allowed origins     | http://localhost:3000 |
| `PORT` / `server.port`   | HTTP port                                   | 8080                |

**Before hosting publicly, generate a fresh `JWT_SECRET`:**
```bash
openssl rand -base64 64
```

## API Overview

### Auth (`/api/auth`) — public
- `POST /register` — create an account, returns a JWT
- `POST /login` — authenticate, returns a JWT

### Categories (`/api/categories`)
- `GET /` — list all categories (public)
- `GET /{slug}` — get one category (public)
- `POST /`, `PUT /{id}`, `DELETE /{id}` — admin only

### Products (`/api/products`)
- `GET /?category=&search=&page=&size=` — paginated browse/search/filter (public)
- `GET /{id}` — product detail (public)
- `POST /`, `PUT /{id}`, `DELETE /{id}` — admin only

### Cart (`/api/cart`) — requires login
- `GET /` — view cart
- `POST /items` — add product `{ productId, quantity }`
- `PUT /items/{itemId}` — update quantity
- `DELETE /items/{itemId}` — remove item
- `DELETE /` — clear cart

### Orders (`/api/orders`) — requires login
- `POST /checkout` — `{ shippingAddress }`, converts cart to an order, decrements stock
- `GET /?page=&size=` — order history
- `GET /{id}` — order detail

## Authentication

1. `POST /api/auth/login` with email/password to get a `token`.
2. Send it on subsequent requests: `Authorization: Bearer <token>`.
3. In Swagger UI, click **Authorize** and paste the token.

## Deployment Notes

This is a standard Spring Boot fat-jar / Docker image, so it runs on any Java host or
container platform (Render, Railway, Fly.io, AWS Elastic Beanstalk, a VPS, etc.).

- The H2 database is file-based; mount a persistent volume at `/app/data` (Docker) or
  point `spring.datasource.url` elsewhere if you outgrow H2 — swapping to Postgres/MySQL
  only requires changing the datasource dependency/URL, since JPA handles the rest.
- Set `JWT_SECRET` and `CORS_ALLOWED_ORIGINS` as environment variables in your host's
  dashboard before going live.
- `spring.jpa.hibernate.ddl-auto` is set to `update` for convenience; for a production
  database you'd typically manage schema via a migration tool (Flyway/Liquibase) instead.
