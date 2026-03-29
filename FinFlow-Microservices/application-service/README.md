# 📝 Application Service

> Manages the full lifecycle of loan applications — from creation to submission and status tracking.

![Port](https://img.shields.io/badge/Port-8082-blue?style=flat-square)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Producer%20%26%20Consumer-orange?style=flat-square)
![Status](https://img.shields.io/badge/Lifecycle-Draft→Submitted→Approved/Rejected-green?style=flat-square)

---

## 📋 Overview

The Application Service is the **core business service** of FinFlow. It allows applicants to create, update, and submit loan applications. On submission, it publishes an event to RabbitMQ and listens for decision events from the Admin Service.

### Key Responsibilities
- CRUD operations for loan applications
- Application submission with RabbitMQ event publishing
- Consuming DECISION_MADE events to update application status
- Consuming DOCUMENT_UPLOADED events for tracking
- Ownership validation (users can only access their own applications)

---

## 🔄 Application Lifecycle

```
  ┌────────┐     submit      ┌───────────┐     admin decision     ┌───────────┐
  │ Draft  │ ───────────────▶│ Submitted │ ──────────────────────▶│ APPROVED  │
  └────────┘                 └───────────┘                        │    or     │
      ▲                                                           │ REJECTED  │
      │ create/update                                             └───────────┘
```

---

## 📡 API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|:----:|
| `POST` | `/applications` | Create new application (Draft) | ✅ |
| `GET` | `/applications/my` | List user's applications | ✅ |
| `PUT` | `/applications/{id}` | Update draft application | ✅ |
| `POST` | `/applications/{id}/submit` | Submit for admin review | ✅ |
| `GET` | `/applications/{id}/status` | Check current status | ✅ |
| `GET` | `/applications/admin/all` | List all applications | Internal |

> **Note:** `X-User-Id` header is automatically injected by the API Gateway from the JWT token.

### Request/Response Examples

#### Create Application
```json
// POST /applications
// Header: Authorization: Bearer <jwt-token>
{
  "personalDetails": "Name: Deepak Kumar, DOB: 1995-05-15, Address: Bangalore",
  "employmentDetails": "Company: Capgemini, Salary: 120000, Designation: Associate",
  "loanDetails": "Amount: 500000, Tenure: 36 months, Purpose: Home Renovation"
}
// Response: LoanApplication object with id and status="Draft"
```

#### Submit Application
```json
// POST /applications/1/submit
// Header: Authorization: Bearer <jwt-token>
// Response: "Application submitted successfully"
// Side effect: APPLICATION_SUBMITTED event published to RabbitMQ
```

---

## 📨 RabbitMQ Events

### Produces
| Event | Routing Key | When |
|-------|-------------|------|
| `APPLICATION_SUBMITTED` | `application.submitted` | User submits an application |

### Consumes
| Event | Queue | Action |
|-------|-------|--------|
| `DECISION_MADE` | `queue.decision.made` | Updates application status to APPROVED/REJECTED |
| `DOCUMENT_UPLOADED` | `queue.document.uploaded` | Logs document upload for the application |

---

## 🗃 Database

**Database:** `finflow_app` (auto-created)

### Loan Applications Table
| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | Primary Key, Auto Increment |
| `user_id` | BIGINT | Owner (FK to auth-service users) |
| `personal_details` | TEXT | JSON — personal information |
| `employment_details` | TEXT | JSON — employment information |
| `loan_details` | TEXT | JSON — loan requirements |
| `status` | VARCHAR | Draft / Submitted / APPROVED / REJECTED |

---

## 📂 Project Structure

```
application-service/
├── src/main/java/com/capg/applicationservice/
│   ├── config/
│   │   └── RabbitMQConfig.java              # Exchange, queues, bindings
│   ├── controller/
│   │   └── LoanApplicationController.java   # REST endpoints
│   ├── dto/
│   │   └── ApplicationRequest.java          # Request DTO
│   ├── entity/
│   │   └── LoanApplication.java             # JPA entity
│   ├── event/
│   │   ├── ApplicationEvent.java            # Event DTO
│   │   ├── ApplicationEventProducer.java    # Publishes to RabbitMQ
│   │   └── ApplicationEventConsumer.java    # Listens from RabbitMQ
│   ├── exception/
│   │   └── GlobalExceptionHandler.java      # Error handling
│   ├── repository/
│   │   └── LoanApplicationRepository.java   # JPA repository
│   └── service/
│       └── LoanApplicationService.java      # Business logic
├── src/main/resources/
│   ├── application.yml                      # Configuration
│   └── logback-spring.xml                   # Logging configuration
├── src/test/java/com/capg/applicationservice/
│   ├── controller/
│   │   └── LoanApplicationControllerTest.java  # 5 tests
│   └── service/
│       └── LoanApplicationServiceTest.java     # 8 tests
└── pom.xml
```

---

## 🧪 Tests

```bash
mvn test -f pom.xml
```

| Test Class | Tests | Type |
|-----------|-------|------|
| `LoanApplicationServiceTest` | 8 | Unit (Mockito) |
| `LoanApplicationControllerTest` | 5 | Controller (MockMvc) |

---

## ⚙️ Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:mysql://localhost:3306/finflow_app` | MySQL connection URL |
| `DB_USER` | `root` | Database username |
| `DB_PASSWORD` | `password` | Database password |
| `RABBITMQ_HOST` | `localhost` | RabbitMQ server host |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | Eureka server URL |
| `ZIPKIN_URL` | `http://localhost:9411/api/v2/spans` | Zipkin collector URL |
