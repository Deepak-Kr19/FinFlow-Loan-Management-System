# 🔐 Auth Service

> Handles user registration, authentication, and JWT token generation for the FinFlow Loan Management System.

![Port](https://img.shields.io/badge/Port-8081-blue?style=flat-square)
![Spring Security](https://img.shields.io/badge/Spring%20Security-Enabled-green?style=flat-square)
![JWT](https://img.shields.io/badge/Auth-JWT%20(HS256)-orange?style=flat-square)

---

## 📋 Overview

The Auth Service is the **identity provider** for the entire FinFlow ecosystem. It manages user accounts and issues JWT tokens that are validated by the API Gateway for all subsequent requests.

### Key Responsibilities
- User registration with BCrypt password hashing
- User login with JWT token generation
- Admin endpoints for user management (consumed by Admin Service)
- Token contains: `userId`, `role`, `email` as claims

---

## 🏗 Architecture

```
Client ──▶ API Gateway ──▶ Auth Service ──▶ MySQL (finflow_auth)
                │
                ▼
         JWT Token returned
         (used for all subsequent requests)
```

---

## 📡 API Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|:---:|
| `POST` | `/auth/signup` | Register a new user | ❌ |
| `POST` | `/auth/login` | Login and receive JWT token | ❌ |
| `GET` | `/auth/admin/users` | List all registered users | ❌* |
| `PUT` | `/auth/admin/users/{id}` | Update user details | ❌* |

> *These endpoints are called internally by the Admin Service via REST, not directly by users.

### Request/Response Examples

#### Register
```json
// POST /auth/signup
{
  "name": "Deepak Kumar",
  "email": "deepak@capg.com",
  "password": "Deepak@123",
  "role": "ROLE_APPLICANT"
}
// Response: "User registered successfully"
```

#### Login
```json
// POST /auth/login
{
  "email": "deepak@capg.com",
  "password": "Deepak@123"
}
// Response:
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "role": "ROLE_APPLICANT"
}
```

---

## 🗃 Database

**Database:** `finflow_auth` (auto-created)

### Users Table
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | Primary Key, Auto Increment |
| `name` | VARCHAR(255) | |
| `email` | VARCHAR(255) | Unique |
| `password` | VARCHAR(255) | BCrypt hashed |
| `role` | VARCHAR(255) | ROLE_APPLICANT / ROLE_ADMIN |

---

## 🔒 Security

- **Password Hashing:** BCrypt (automatic salting)
- **JWT Algorithm:** HMAC-SHA256
- **Token Expiry:** 24 hours
- **CSRF:** Disabled (stateless JWT-based auth)
- **CORS:** Enabled for all origins

### JWT Token Claims
```json
{
  "sub": "deepak@capg.com",
  "userId": 1,
  "role": "ROLE_APPLICANT",
  "iat": 1711670400,
  "exp": 1711756800
}
```

---

## 📂 Project Structure

```
auth-service/
├── src/main/java/com/capg/authservice/
│   ├── config/
│   │   └── SecurityConfig.java          # Spring Security + CORS + BCrypt
│   ├── controller/
│   │   └── AuthController.java          # REST endpoints
│   ├── dto/
│   │   ├── AuthRequest.java             # Login request DTO
│   │   ├── AuthResponse.java            # Login response (token + role)
│   │   └── RegisterRequest.java         # Registration request DTO
│   ├── entity/
│   │   └── User.java                    # JPA entity
│   ├── exception/
│   │   └── GlobalExceptionHandler.java  # Centralized error handling
│   ├── repository/
│   │   └── UserRepository.java          # JPA repository
│   ├── service/
│   │   └── AuthService.java             # Business logic
│   └── util/
│       └── JwtUtil.java                 # JWT generation & validation
├── src/main/resources/
│   ├── application.yml                  # Configuration
│   └── logback-spring.xml               # Logging configuration
├── src/test/java/com/capg/authservice/
│   ├── controller/
│   │   └── AuthControllerTest.java      # Controller unit tests (4 tests)
│   └── service/
│       └── AuthServiceTest.java         # Service unit tests (8 tests)
└── pom.xml
```

---

## 🧪 Tests

```bash
mvn test -f pom.xml
```

| Test Class | Tests | Type |
|-----------|-------|------|
| `AuthServiceTest` | 8 | Unit (Mockito) |
| `AuthControllerTest` | 4 | Controller (MockMvc) |

---

## ⚙️ Configuration

```yaml
server:
  port: 8081

spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/finflow_auth?createDatabaseIfNotExist=true}
  jpa:
    hibernate:
      ddl-auto: update

management:
  tracing:
    sampling:
      probability: 1.0   # 100% traces sent to Zipkin
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:mysql://localhost:3306/finflow_auth` | MySQL connection URL |
| `DB_USER` | `root` | Database username |
| `DB_PASSWORD` | `password` | Database password |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | Eureka server URL |
| `ZIPKIN_URL` | `http://localhost:9411/api/v2/spans` | Zipkin collector URL |
