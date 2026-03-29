# 🌐 API Gateway

> Centralized entry point for all FinFlow microservices — handles routing, JWT authentication, and CORS.

![Port](https://img.shields.io/badge/Port-8080-blue?style=flat-square)
![Spring Cloud Gateway](https://img.shields.io/badge/Spring%20Cloud-Gateway-brightgreen?style=flat-square)
![JWT](https://img.shields.io/badge/Auth-JWT%20Validation-orange?style=flat-square)

---

## 📋 Overview

The API Gateway is the **single entry point** for all client requests in the FinFlow system. Built on Spring Cloud Gateway (reactive, non-blocking), it handles request routing to backend services, JWT token validation, and cross-origin resource sharing (CORS).

### Key Responsibilities
- Route requests to the correct microservice via Eureka service discovery
- Validate JWT tokens on secured routes
- Inject `X-User-Id` and `X-User-Role` headers for downstream services
- Aggregate Swagger UI documentation from all services
- Handle CORS for browser-based clients

---

## 🏗 Request Flow

```
Client Request
     │
     ▼
┌─────────────────────────────────────────┐
│              API Gateway (:8080)         │
│                                         │
│  1. RouteValidator: Is this a secured    │
│     route or an open route?              │
│                                         │
│  2. AuthenticationFilter: If secured,    │
│     validate JWT from Authorization      │
│     header and extract userId + role     │
│                                         │
│  3. Inject headers:                      │
│     X-User-Id: 10                        │
│     X-User-Role: ROLE_APPLICANT          │
│                                         │
│  4. Route to correct service via Eureka: │
│     /auth/**        → auth-service       │
│     /applications/**→ application-service│
│     /documents/**   → document-service   │
│     /admin/**       → admin-service      │
└──────────┬──────────────────────────────┘
           │
           ▼
    Backend Microservice
```

---

## 🛣 Routing Configuration

| Path Pattern | Target Service | Load Balanced |
|:-------------|:--------------|:---:|
| `/auth/**` | auth-service | ✅ |
| `/applications/**` | application-service | ✅ |
| `/documents/**` | document-service | ✅ |
| `/admin/**` | admin-service | ✅ |

All routes use `lb://` (load-balanced) URIs resolved via Eureka service discovery.

---

## 🔓 Open Routes (No Auth Required)

The following routes bypass JWT validation:

| Route | Reason |
|-------|--------|
| `/auth/signup` | User registration |
| `/auth/login` | User authentication |
| `/eureka/**` | Service discovery |
| `/swagger-ui/**` | API documentation |
| `/**/v3/api-docs/**` | OpenAPI specs |

---

## 📚 Aggregated Swagger UI

Access all service APIs from a single Swagger UI:

**URL:** `http://localhost:8080/swagger-ui.html`

Use the dropdown to switch between services:
- Auth Service
- Application Service
- Document Service
- Admin Service

---

## 📂 Project Structure

```
api-gateway/
├── src/main/java/com/capg/apigateway/
│   ├── filter/
│   │   ├── AuthenticationFilter.java    # Global JWT validation filter
│   │   └── RouteValidator.java          # Open/secured route definitions
│   └── util/
│       └── JwtUtil.java                 # JWT parsing & validation
├── src/main/resources/
│   ├── application.yml                  # Routes, CORS, Zipkin config
│   └── logback-spring.xml               # Logging configuration
└── pom.xml
```

---

## ⚙️ Configuration

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://auth-service           # Eureka-resolved
          predicates:
            - Path=/auth/**
        - id: application-service
          uri: lb://application-service
          predicates:
            - Path=/applications/**
        - id: document-service
          uri: lb://document-service
          predicates:
            - Path=/documents/**
        - id: admin-service
          uri: lb://admin-service
          predicates:
            - Path=/admin/**
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `EUREKA_URL` | `http://localhost:8761/eureka/` | Eureka server URL |
| `ZIPKIN_URL` | `http://localhost:9411/api/v2/spans` | Zipkin collector URL |

---

## 🔒 JWT Validation

The gateway uses the **same secret key** as the Auth Service to validate tokens:

1. Client sends: `Authorization: Bearer <token>`
2. Gateway strips "Bearer " prefix
3. Validates signature + expiration using HMAC-SHA256
4. Extracts `userId` and `role` from claims
5. Injects `X-User-Id` and `X-User-Role` headers
6. Forwards request to the target service

> ⚠️ The secret key in `JwtUtil.java` must be identical across auth-service and api-gateway.
