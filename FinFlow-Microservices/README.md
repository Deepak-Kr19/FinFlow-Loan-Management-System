# 🏦 FinFlow — Loan Management System

> A production-ready **Microservices Architecture** built with **Java 17**, **Spring Boot 3.4**, **Spring Cloud Gateway**, **Netflix Eureka**, **JWT Authentication**, **MySQL**, and **Docker**.

---

## 📐 Architecture Overview

```
                          ┌─────────────────────────┐
                          │     Eureka Server        │
                          │       (Port 8761)        │
                          └────────────┬────────────┘
                                       │ Service Discovery
                                       │
┌──────────┐              ┌────────────▼────────────┐
│  Client   │──── REST ──▶│      API Gateway         │
│ (Postman) │              │       (Port 8080)        │
└──────────┘              │  JWT Filter + Routing    │
                          └────┬───┬───┬───┬────────┘
                               │   │   │   │
              ┌────────────────┘   │   │   └────────────────┐
              ▼                    ▼   ▼                    ▼
     ┌────────────┐      ┌──────────┐ ┌──────────┐  ┌──────────┐
     │Auth Service│      │App Svc   │ │Doc Svc   │  │Admin Svc │
     │ (8081)     │      │ (8082)   │ │ (8083)   │  │ (8084)   │
     └──────┬─────┘      └────┬─────┘ └────┬─────┘  └────┬─────┘
            │                 │             │              │
            └────────────┬────┴─────────────┴──────────────┘
                         ▼
                ┌────────────────┐
                │   MySQL 8.0    │
                │  (Port 3306)   │
                │  4 Databases   │
                └────────────────┘
```

---

## 🧩 Microservices Breakdown

| Service | Port | Description |
|---------|------|-------------|
| **Eureka Server** | `8761` | Service Discovery & Registration Dashboard |
| **API Gateway** | `8080` | Central entry point — JWT validation, routing, header injection |
| **Auth Service** | `8081` | User registration, login, JWT token generation |
| **Application Service** | `8082` | Loan application CRUD, submission workflow |
| **Document Service** | `8083` | File upload, document verification |
| **Admin Service** | `8084` | Admin dashboard — view all applications, approve/reject, manage users |

---

## 🛠️ Tech Stack

| Technology | Purpose |
|------------|---------|
| Java 17 | Core Language |
| Spring Boot 3.4.1 | Framework |
| Spring Cloud Gateway | API Gateway (Reactive/Netty) |
| Netflix Eureka | Service Discovery |
| Spring Security + JWT | Authentication & Authorization |
| Spring Data JPA + Hibernate | ORM & Database Access |
| MySQL 8.0 | Relational Database |
| Docker & Docker Compose | Containerization & Orchestration |
| Maven | Build & Dependency Management |

---

## 🚀 Getting Started

### Prerequisites
- **Docker Desktop** installed and running
- **Postman** (for API testing)

### Run the Project

```bash
# Clone the repository
git clone <repository-url>
cd FinFlow-Microservices

# Start all services (builds + runs)
docker-compose up --build -d

# Check all containers are running
docker-compose ps
```

> ⏳ **First build takes ~5 minutes** (Maven downloads dependencies inside Docker).
> MySQL initializes first via healthcheck, then all services start.

### Verify Services

| Check | URL |
|-------|-----|
| Eureka Dashboard | http://localhost:8761 |
| API Gateway | http://localhost:8080 |

All 5 services should appear as **UP** in the Eureka Dashboard.

### Stop Everything
```bash
docker-compose down
```

---

## 🔐 Authentication Flow

```
1. POST /auth/signup     → Register a new user
2. POST /auth/login      → Get JWT token
3. Use token as:         Authorization: Bearer <token>
4. Gateway validates JWT → Injects X-User-Id & X-User-Role headers
5. Downstream services   → Read headers for access control
```

**Open Endpoints** (no token required): `/auth/signup`, `/auth/login`
**Protected Endpoints** (token required): Everything else

---

## 📬 API Reference

### Auth Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/auth/signup` | Register a new user |
| `POST` | `/auth/login` | Login and receive JWT token |

### Application Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/applications` | Create a new loan application |
| `GET` | `/applications/my` | Get all my applications |
| `PUT` | `/applications/{id}` | Update a draft application |
| `POST` | `/applications/{id}/submit` | Submit application for review |
| `GET` | `/applications/{id}/status` | Check application status |

### Document Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/documents/upload` | Upload a document (multipart) |
| `PUT` | `/documents/{id}/verify` | Verify/reject a document |

### Admin Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/admin/applications` | View all submitted applications |
| `POST` | `/admin/applications/{id}/decision` | Approve/reject an application |
| `GET` | `/admin/reports` | View system reports |
| `GET` | `/admin/users` | View all registered users |
| `PUT` | `/admin/users/{id}` | Update user details |

---

## 🧪 Testing with Postman

1. Import `FinFlow_Postman_Collection.json` into Postman
2. Create a Postman **Environment** and add a variable `jwt_token` (leave value empty)
3. Run requests in order — the Login request auto-saves the token!

### Sample Flow

**Step 1 — Signup:**
```json
POST http://localhost:8080/auth/signup
{
    "name": "Deepak Kumar",
    "email": "deepak@capg.com",
    "password": "Deepak@123",
    "role": "ROLE_APPLICANT"
}
```

**Step 2 — Login:**
```json
POST http://localhost:8080/auth/login
{
    "email": "deepak@capg.com",
    "password": "Deepak@123"
}
// Response: { "token": "eyJhbGciOi..." }
```

**Step 3 — Create Loan Application:**
```json
POST http://localhost:8080/applications
Authorization: Bearer <token>
{
    "personalDetails": "Deepak Kumar, Age 28, Mumbai, Maharashtra",
    "employmentDetails": "Software Engineer at Capgemini, 5 years, Rs.12 LPA",
    "loanDetails": "Home Loan, Rs. 50,00,000, 20 years tenure, 8.5% interest"
}
```

---

## 📁 Project Structure

```
FinFlow-Microservices/
├── api-gateway/                # Spring Cloud Gateway + JWT Filter
│   └── src/main/java/com/capg/apigateway/
│       ├── filter/             # AuthenticationFilter, RouteValidator
│       └── util/               # JwtUtil
├── auth-service/               # User Auth + JWT Generation
│   └── src/main/java/com/capg/authservice/
│       ├── controller/         # AuthController
│       ├── dto/                # AuthRequest, AuthResponse, RegisterRequest
│       ├── entity/             # User
│       ├── repository/         # UserRepository
│       ├── service/            # AuthService
│       ├── config/             # SecurityConfig
│       └── util/               # JwtUtil
├── application-service/        # Loan Application Lifecycle
│   └── src/main/java/com/capg/applicationservice/
│       ├── controller/         # LoanApplicationController
│       ├── dto/                # ApplicationRequest
│       ├── entity/             # LoanApplication
│       ├── repository/         # LoanApplicationRepository
│       └── service/            # LoanApplicationService
├── document-service/           # Document Upload & Verification
│   └── src/main/java/com/capg/documentservice/
│       ├── controller/         # DocumentController
│       ├── entity/             # Document
│       ├── repository/         # DocumentRepository
│       └── service/            # DocumentService
├── admin-service/              # Admin Operations & Decisions
│   └── src/main/java/com/capg/adminservice/
│       ├── controller/         # AdminController
│       ├── config/             # AppConfig (@LoadBalanced RestTemplate)
│       ├── entity/             # Decision, Report
│       ├── repository/         # DecisionRepository, ReportRepository
│       └── service/            # AdminService
├── eureka-server/              # Netflix Eureka Service Registry
├── docker-compose.yml          # Full stack orchestration
└── FinFlow_Postman_Collection.json
```

---

## 🗃️ Database Schema

| Database | Service | Tables |
|----------|---------|--------|
| `finflow_auth` | Auth Service | `users` |
| `finflow_app` | Application Service | `loan_applications` |
| `finflow_doc` | Document Service | `documents` |
| `finflow_admin` | Admin Service | `decisions`, `reports` |

All databases are auto-created via `createDatabaseIfNotExist=true`. Tables are auto-generated via Hibernate `ddl-auto: update`.

---

## 🐳 Docker Services

```bash
docker-compose ps    # View running containers
docker-compose logs  # View all logs
docker-compose logs auth-service  # View specific service logs
docker-compose down  # Stop everything
docker-compose up -d # Start without rebuild
```

---

## 🤝 Design Patterns & Best Practices

- **Clean Architecture**: Controller → Service → Repository layering
- **DTO Pattern**: Request/Response DTOs for all APIs
- **Global Exception Handling**: `@RestControllerAdvice` in every service
- **Stateless Authentication**: JWT tokens — no server-side sessions
- **Service Discovery**: Netflix Eureka for dynamic routing
- **Client-Side Load Balancing**: `@LoadBalanced` RestTemplate
- **Multi-Stage Docker Builds**: Optimized image sizes using Alpine JRE
- **Health Checks**: MySQL readiness probes prevent race conditions

---

## 👨‍💻 Author

**Deepak Kumar** — Capgemini Sprint 1 Project
