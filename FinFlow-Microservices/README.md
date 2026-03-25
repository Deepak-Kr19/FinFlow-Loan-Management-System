# FinFlow - Loan Management System

A complete microservices-based backend system built with Java 17, Spring Boot, Spring Cloud Gateway, JWT Authentication, and MySQL.

## Architecture

1. **API Gateway (Port 8080)**: Routes traffic and acts as a central JWT validator.
2. **Auth Service (Port 8081)**: Manages user registration, login, and issues JWT tokens.
3. **Application Service (Port 8082)**: Manages loan applications.
4. **Document Service (Port 8083)**: Handles file uploads and statuses.
5. **Admin Service (Port 8084)**: Reviews reports and delegates decisions.

## Setup & Running with Docker

1. Ensure Docker and Docker Compose are installed.
2. The provided `Dockerfile`s use Maven Multi-stage builds, meaning you do not need Maven installed locally to run it. Just use Docker:
   ```bash
   docker-compose up --build
   ```
   This will download dependencies, compile the source code cleanly in isolated containers, and start a MySQL database along with the 5 Spring Boot microservices.

## How Services Communicate

- All external client calls go to `http://localhost:8080` (API Gateway).
- API Gateway acts as a reverse proxy, inspecting the header for `Bearer <token>` on all endpoints except `/auth/signup` and `/auth/login`.
- If valid, the Gateway parses the JWT and forwards the request alongside `X-User-Id` and `X-User-Role` headers.
- Services retrieve these custom headers using `@RequestHeader` for stateless, scalable access control.
- **Admin Service** uses `RestTemplate` to fetch aggregate metadata directly from downstream siblings inside the Docker network.

## Testing

- Run the JUnit + Mockito test suite at `auth-service/src/test/java/com/finflow/authservice/service/AuthServiceTest.java`.
- You can run `mvn test` in the `auth-service` directory locally.

## Sample API Requests

### 1. Signup
`POST http://localhost:8080/auth/signup`
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "ROLE_APPLICANT"
}
```

### 2. Login
`POST http://localhost:8080/auth/login`
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```
*(Copy the JSON Web Token from the response)*

### 3. Create Application
`POST http://localhost:8080/applications`
**Headers:** `Authorization: Bearer <your_token>`
```json
{
  "personalDetails": "{\"age\": 30, \"address\": \"123 Main St\"}",
  "employmentDetails": "{\"company\": \"Tech Corp\", \"salary\": 80000}",
  "loanDetails": "{\"amount\": 50000, \"tenure\": 5}"
}
```
