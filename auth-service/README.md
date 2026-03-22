# 🔐 FinFlow - Auth Service (Microservice)

## 📌 Overview

The **Auth Service** is a core microservice in the **FinFlow Loan Management System**.
It handles user authentication, authorization, and secure access using **JWT (JSON Web Tokens)**.

This service is built using **Spring Boot**, **Spring Security**, and **JPA/Hibernate**, following a clean microservices architecture.

---

## 🚀 Features

* User Registration (Signup)
* User Login with JWT Token
* Password Encryption using BCrypt
* Stateless Authentication
* JWT-based Security
* Protected APIs
* MySQL Database Integration

---

## 🏗️ Tech Stack

| Technology      | Version |
| --------------- | ------- |
| Java            | 17      |
| Spring Boot     | 3.2.5   |
| Spring Security | 6.x     |
| JPA / Hibernate | 6.x     |
| MySQL           | 8       |
| JWT             | 0.11.5  |
| Maven           | 3.9+    |

---

## 📁 Project Structure

```
auth-service/
│
├── controller/
│   └── AuthController.java
│
├── service/
│   ├── AuthService.java
│   └── CustomUserDetailsService.java
│
├── repository/
│   └── UserRepository.java
│
├── entity/
│   └── User.java
│
├── dto/
│   ├── LoginRequest.java
│   ├── SignupRequest.java
│   └── AuthResponse.java
│
├── security/
│   ├── JwtUtil.java
│   ├── JwtFilter.java
│   └── SecurityConfig.java
│
└── AuthServiceApplication.java
```

---

## 🔐 Authentication Flow

```
User Login → Validate Credentials → Generate JWT → Return Token
       ↓
Client sends JWT in Header
       ↓
JwtFilter validates token
       ↓
Access granted / denied
```

---

## ⚙️ Setup & Installation

### 1️⃣ Clone Repository

```bash
git clone https://github.com/your-username/auth-service.git
cd auth-service
```

---

### 2️⃣ Configure Database

Create MySQL database:

```sql
CREATE DATABASE finflow;
```

Update `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/finflow
spring.datasource.username=root
spring.datasource.password=root
```

---

### 3️⃣ Build Project

```bash
mvn clean install
```

---

### 4️⃣ Run Application

```bash
mvn spring-boot:run
```

App runs on:

```
http://localhost:8081
```

---

## 📡 API Endpoints

### 🔹 Signup

```http
POST /auth/signup
```

Request:

```json
{
  "name": "Deepak",
  "email": "deepak@test.com",
  "password": "123456"
}
```

---

### 🔹 Login

```http
POST /auth/login
```

Response:

```json
{
  "token": "JWT_TOKEN"
}
```

---

### 🔹 Protected API

```http
GET /auth/test
```

Header:

```
Authorization: Bearer <JWT_TOKEN>
```

---

## 🔑 Security Implementation

* JWT-based authentication
* Password hashing using BCrypt
* Spring Security filter chain
* Stateless session management

---

## 🧪 Testing

Use **Postman** to test APIs:

1. Signup user
2. Login to get JWT
3. Access protected API with token

---

## 🐳 Docker Support

### Build Image

```bash
docker build -t auth-service .
```

### Run Container

```bash
docker run -p 8081:8081 auth-service
```

---

## ⚠️ Common Issues

### ❌ Unknown database 'finflow'

✔ Create database manually

### ❌ 401 Unauthorized

✔ Check JWT token in header

---

## 🎯 Future Enhancements

* Role-based authorization (ADMIN / USER)
* Refresh tokens
* OAuth2 integration
* API Gateway integration

---

## 👨‍💻 Author

Deepak Kumar
B.Tech CSE | Full Stack Developer

---

## ⭐ Contribution

Feel free to fork and contribute to improve this service.

---
