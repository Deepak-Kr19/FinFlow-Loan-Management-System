# 🌐 FinFlow - API Gateway (Microservice)

## 📌 Overview

The **API Gateway** is the central entry point for all client requests in the **FinFlow Loan Management System**.
It routes requests to appropriate microservices and enforces **centralized JWT-based authentication**.

This service is built using **Spring Cloud Gateway (WebFlux)** and acts as a secure and scalable routing layer in a microservices architecture.

---

## 🚀 Features

* Centralized API routing
* JWT Authentication & Validation
* Stateless Security (No Sessions)
* Request filtering using GlobalFilter
* Microservices communication entry point
* Scalable architecture

---

## 🏗️ Tech Stack

| Technology                 | Version  |
| -------------------------- | -------- |
| Java                       | 17       |
| Spring Boot                | 3.2.5    |
| Spring Cloud Gateway       | 2023.0.x |
| Spring Security (Reactive) | 6.x      |
| JWT (jjwt)                 | 0.11.5   |
| Maven                      | 3.9+     |

---

## 📁 Project Structure

```id="gw-structure"
api-gateway/
│
├── src/main/java/com/finflow/gateway/
│
│   ├── filter/
│   │   ├── JwtFilter.java
│   │   └── JwtUtil.java
│
│   ├── config/
│   │   └── SecurityConfig.java
│
│   └── ApiGatewayApplication.java
│
├── src/main/resources/
│   └── application.yml
│
├── Dockerfile
└── pom.xml
```

---

## 🔄 Architecture Flow

```id="gw-flow"
Client Request
     ↓
API Gateway
     ↓
JWT Filter (Validate Token)
     ↓
Route to Microservice
     ↓
Response back to Client
```

---

## 🔐 JWT Authentication Flow

```id="jwt-flow"
1. User logs in via Auth Service
2. Auth Service returns JWT token
3. Client sends token in header
4. Gateway validates token
5. If valid → request forwarded
6. If invalid → 401 Unauthorized
```

---

## ⚙️ Configuration

### 🔹 application.yml

Defines routing rules:

```yaml id="routes-config"
server:
  port: 8080

spring:
  cloud:
    gateway:
      routes:

        - id: auth-service
          uri: http://localhost:8081
          predicates:
            - Path=/gateway/auth/**

        - id: application-service
          uri: http://localhost:8082
          predicates:
            - Path=/gateway/applications/**

        - id: document-service
          uri: http://localhost:8083
          predicates:
            - Path=/gateway/documents/**

        - id: admin-service
          uri: http://localhost:8084
          predicates:
            - Path=/gateway/admin/**
```

---

## 🔐 Security Implementation

* JWT validation using GlobalFilter
* Reactive Security using WebFlux
* No session-based authentication
* Centralized access control at gateway

---

## ▶️ How to Run

### 1️⃣ Build Project

```bash id="build-cmd"
mvn clean install
```

---

### 2️⃣ Run Application

```bash id="run-cmd"
mvn spring-boot:run
```

---

### 3️⃣ Access Gateway

```id="gateway-url"
http://localhost:8080
```

---

## 📡 API Usage

### 🔹 Login (via Gateway)

```http id="login-api"
POST /gateway/auth/login
```

---

### 🔹 Signup

```http id="signup-api"
POST /gateway/auth/signup
```

---

### 🔹 Protected API

```http id="protected-api"
GET /gateway/auth/test
```

---

## 🔑 Request Header

```plaintext id="auth-header"
Authorization: Bearer <JWT_TOKEN>
```

---

## 🧪 Testing (Postman)

1. Call login API → get JWT
2. Add token in header
3. Call protected API

---

## 🐳 Docker Support

### Build Image

```bash id="docker-build"
docker build -t api-gateway .
```

### Run Container

```bash id="docker-run"
docker run -p 8080:8080 api-gateway
```

---

## ⚠️ Common Issues

### ❌ 401 Unauthorized

✔ Missing or invalid JWT token

---

### ❌ Gateway not routing

✔ Check service ports (8081, 8082, etc.)

---

### ❌ Security error (HttpSecurity)

✔ Use WebFlux Security (`ServerHttpSecurity`)

---

## 🎯 Future Enhancements

* Role-based authorization (RBAC)
* Rate limiting
* Circuit breaker (Resilience4j)
* Logging & monitoring
* Integration with Eureka / Kubernetes

---

## 👨‍💻 Author

Deepak Kumar
B.Tech CSE | Full Stack Developer

---

## ⭐ Contribution

Feel free to fork and enhance the project.

---
