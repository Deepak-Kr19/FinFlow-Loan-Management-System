# 🏦 FinFlow — Loan Management System

> A production-ready microservices-based loan management system built with **Spring Boot 3.4**, **Spring Cloud 2024**, **RabbitMQ**, **Zipkin**, and **Docker**.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen?style=flat-square&logo=springboot)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2024.0.0-blue?style=flat-square)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3-orange?style=flat-square&logo=rabbitmq)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)

---

## 📋 Table of Contents

- [Architecture](#-architecture)
- [Services](#-services)
- [Tech Stack](#-tech-stack)
- [Quick Start](#-quick-start)
- [API Endpoints](#-api-endpoints)
- [Messaging (RabbitMQ)](#-messaging-rabbitmq)
- [Distributed Tracing (Zipkin)](#-distributed-tracing-zipkin)
- [Testing](#-testing)
- [Project Structure](#-project-structure)

---

## 🏗 Architecture

```
                              ┌──────────────────┐
                              │   Zipkin (:9411)  │
                              └──────▲───────────┘
                                     │ traces
┌─────────┐    ┌──────────────┐    ┌─┴──────────┐    ┌───────────┐
│  Client  │───▶│ API Gateway  │───▶│   Eureka   │    │  RabbitMQ │
│          │    │   (:8080)    │    │  (:8761)   │    │  (:5672)  │
└─────────┘    └──────┬───────┘    └────────────┘    └─────┬─────┘
                      │                                     │
         ┌────────────┼────────────────┐                    │
         │            │                │                    │
   ┌─────▼─────┐ ┌───▼──────┐  ┌─────▼─────┐  ┌──────────▼──┐
   │   Auth    │ │Application│  │ Document  │  │   Admin     │
   │  Service  │ │  Service  │  │  Service  │  │   Service   │
   │  (:8081)  │ │  (:8082)  │  │  (:8083)  │  │   (:8084)   │
   └─────┬─────┘ └────┬──────┘  └─────┬─────┘  └──────┬──────┘
         │            │                │               │
         └────────────┴────────────────┴───────────────┘
                              │
                      ┌───────▼───────┐
                      │   MySQL 8.0   │
                      │   (:3306)     │
                      └───────────────┘
```

---

## 🔧 Services

| Service | Port | Description |
|---------|------|-------------|
| **[Eureka Server](./eureka-server/)** | 8761 | Service discovery and registration |
| **[API Gateway](./api-gateway/)** | 8080 | Centralized routing, JWT validation, CORS |
| **[Auth Service](./auth-service/)** | 8081 | User registration, login, JWT token generation |
| **[Application Service](./application-service/)** | 8082 | Loan application CRUD and lifecycle management |
| **[Document Service](./document-service/)** | 8083 | Document upload, storage, and verification |
| **[Admin Service](./admin-service/)** | 8084 | Application decisions, user management, reports |

---

## 🛠 Tech Stack

| Category | Technology |
|----------|-----------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.4.1 |
| **Cloud** | Spring Cloud 2024.0.0 (Eureka, Gateway) |
| **Security** | Spring Security + JWT (HMAC-SHA256) |
| **Database** | MySQL 8.0 (JPA/Hibernate) |
| **Messaging** | RabbitMQ 3 (Topic Exchange) |
| **Tracing** | Zipkin + Micrometer Tracing (Brave) |
| **Logging** | SLF4J + Logback (rolling file appender) |
| **Docs** | SpringDoc OpenAPI 3 (Swagger UI) |
| **Testing** | JUnit 5 + Mockito |
| **Containerization** | Docker + Docker Compose |

---

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### Run with Docker (Recommended)

```bash
# Clone and start all services
git clone <repository-url>
cd FinFlow-Microservices
docker-compose up --build -d
```

### Access Points

| Resource | URL |
|----------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Eureka Dashboard | http://localhost:8761 |
| Zipkin UI | http://localhost:9411 |
| RabbitMQ Management | http://localhost:15672 (guest/guest) |

### Run Locally (Without Docker)

```bash
# Start each service (requires local MySQL and RabbitMQ)
cd auth-service && mvn spring-boot:run
cd application-service && mvn spring-boot:run
cd document-service && mvn spring-boot:run
cd admin-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
cd eureka-server && mvn spring-boot:run
```

---

## 📡 API Endpoints

### Auth Service (`/auth`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/signup` | Register a new user |
| POST | `/auth/login` | Login and receive JWT token |
| GET | `/auth/admin/users` | List all users (admin) |
| PUT | `/auth/admin/users/{id}` | Update user details (admin) |

### Application Service (`/applications`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/applications` | Create a loan application |
| GET | `/applications/my` | List user's applications |
| PUT | `/applications/{id}` | Update draft application |
| POST | `/applications/{id}/submit` | Submit for review |
| GET | `/applications/{id}/status` | Check application status |
| GET | `/applications/admin/all` | List all applications (admin) |

### Document Service (`/documents`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/documents/upload` | Upload a document |
| PUT | `/documents/{id}/verify` | Verify/reject document (admin) |

### Admin Service (`/admin`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/applications` | View all applications |
| POST | `/admin/applications/{id}/decision` | Approve/reject application |
| GET | `/admin/reports` | View reports |
| GET | `/admin/users` | View all users |
| PUT | `/admin/users/{id}` | Update user |

---

## 📨 Messaging (RabbitMQ)

Uses a **Topic Exchange** (`finflow.exchange`) with three event types:

```
┌─────────────────┐     application.submitted     ┌───────────────────────────┐
│ Application Svc │ ──────────────────────────────▶│ Admin Svc (Consumer)      │
└─────────────────┘                                └───────────────────────────┘

┌─────────────────┐     decision.made              ┌───────────────────────────┐
│ Admin Service   │ ──────────────────────────────▶│ Application Svc (Consumer)│
└─────────────────┘                                └───────────────────────────┘

┌─────────────────┐     document.uploaded           ┌───────────────────────────┐
│ Document Svc    │ ──────────────────────────────▶│ Application Svc (Consumer)│
└─────────────────┘                                └───────────────────────────┘
```

---

## 🔍 Distributed Tracing (Zipkin)

Every request is assigned a **Trace ID** that propagates across all services. View traces at `http://localhost:9411`.

**Log format with correlation:**
```
2026-03-29 23:00:00 [auth-service] [http-nio-8081-exec-1] [traceId,spanId] INFO AuthService - Login successful
```

---

## 🧪 Testing

```bash
# Run all tests for a service
mvn test -f auth-service/pom.xml
mvn test -f application-service/pom.xml
mvn test -f document-service/pom.xml
mvn test -f admin-service/pom.xml
```

| Service | Tests | Coverage |
|---------|-------|----------|
| Auth Service | 12 (8 service + 4 controller) | Service + Controller layers |
| Application Service | 13 (8 service + 5 controller) | Service + Controller layers |
| Document Service | 6 (4 service + 2 controller) | Service + Controller layers |
| Admin Service | 11 (5 service + 6 controller) | Service + Controller layers |

---

## 📂 Project Structure

```
FinFlow-Microservices/
├── eureka-server/          # Service Discovery
├── api-gateway/            # API Gateway + JWT Validation
├── auth-service/           # Authentication & User Management
├── application-service/    # Loan Application Management
├── document-service/       # Document Upload & Verification
├── admin-service/          # Admin Panel & Decisions
├── docker-compose.yml      # Orchestration for all services
└── README.md               # This file
```

---

## 👤 Author

**Deepak Kumar**

---

## 📜 License

This project is built for training purposes at **Capgemini**.
