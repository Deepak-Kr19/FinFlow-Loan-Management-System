# 🛡 Admin Service

> Administrative panel for loan application decisions, user management, and reporting.

![Port](https://img.shields.io/badge/Port-8084-blue?style=flat-square)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Producer%20%26%20Consumer-orange?style=flat-square)
![REST](https://img.shields.io/badge/Inter--Service-REST%20Calls-purple?style=flat-square)

---

## 📋 Overview

The Admin Service is the **administrative hub** of FinFlow. It enables admins to review loan applications, make approval/rejection decisions, manage users, and view reports. It communicates with other services via both REST calls and RabbitMQ events.

### Key Responsibilities
- Approve or reject loan applications (publishes DECISION_MADE event)
- Fetch applications from Application Service (REST)
- Fetch/update users from Auth Service (REST)
- Consume APPLICATION_SUBMITTED events from RabbitMQ
- Generate and display reports

---

## 🔗 Inter-Service Communication

```
┌──────────────┐  REST: GET /applications/admin/all  ┌────────────────────┐
│ Admin Service │ ──────────────────────────────────▶ │ Application Service│
│              │                                      └────────────────────┘
│              │  REST: GET /auth/admin/users          ┌────────────────────┐
│              │ ──────────────────────────────────▶   │ Auth Service       │
│              │                                      └────────────────────┘
│              │                                      
│              │  RabbitMQ: decision.made              ┌────────────────────┐
│              │ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ▶ │ Application Service│
│              │                                      └────────────────────┘
│              │  RabbitMQ: application.submitted      ┌────────────────────┐
│              │ ◀─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─  │ Application Service│
└──────────────┘                                      └────────────────────┘
```

---

## 📡 API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|:----:|
| `GET` | `/admin/applications` | List all loan applications | ✅ |
| `POST` | `/admin/applications/{id}/decision` | Approve or reject | ✅ |
| `GET` | `/admin/reports` | View system reports | ✅ |
| `GET` | `/admin/users` | List all registered users | ✅ |
| `PUT` | `/admin/users/{id}` | Update user details | ✅ |

### Request Examples

#### Make Decision
```bash
curl -X POST "http://localhost:8084/admin/applications/1/decision?decision=APPROVED&remarks=All%20documents%20verified"
```

**Response:**
```json
{
  "id": 1,
  "applicationId": 1,
  "decision": "APPROVED",
  "remarks": "All documents verified"
}
```

---

## 📨 RabbitMQ Events

### Produces
| Event | Routing Key | When |
|-------|-------------|------|
| `DECISION_MADE` | `decision.made` | Admin approves/rejects an application |

**Event Payload:**
```json
{
  "applicationId": 1,
  "decision": "APPROVED",
  "remarks": "All documents verified"
}
```

### Consumes
| Event | Queue | Action |
|-------|-------|--------|
| `APPLICATION_SUBMITTED` | `queue.application.submitted` | Logs notification of new application |

---

## 🗃 Database

**Database:** `finflow_admin` (auto-created)

### Decisions Table
| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | Primary Key, Auto Increment |
| `application_id` | BIGINT | Loan application reference |
| `decision` | VARCHAR | APPROVED / REJECTED |
| `remarks` | VARCHAR | Admin's explanation |

### Reports Table
| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | Primary Key, Auto Increment |
| `type` | VARCHAR | Report type identifier |
| `data` | TEXT | JSON report content |

---

## 📂 Project Structure

```
admin-service/
├── src/main/java/com/capg/adminservice/
│   ├── config/
│   │   ├── AppConfig.java               # RestTemplate bean
│   │   └── RabbitMQConfig.java           # Exchange, queues, bindings
│   ├── controller/
│   │   └── AdminController.java          # REST endpoints
│   ├── entity/
│   │   ├── Decision.java                 # Decision JPA entity
│   │   └── Report.java                   # Report JPA entity
│   ├── event/
│   │   ├── ApplicationEventConsumer.java # Listens for submitted apps
│   │   ├── DecisionEvent.java            # Decision event DTO
│   │   └── DecisionEventProducer.java    # Publishes decisions
│   ├── exception/
│   │   └── GlobalExceptionHandler.java   # Error handling
│   ├── repository/
│   │   ├── DecisionRepository.java       # Decision JPA repository
│   │   └── ReportRepository.java         # Report JPA repository
│   └── service/
│       └── AdminService.java             # Business logic + REST calls
├── src/main/resources/
│   ├── application.yml                   # Configuration
│   └── logback-spring.xml                # Logging configuration
├── src/test/java/com/capg/adminservice/
│   ├── controller/
│   │   └── AdminControllerTest.java      # 6 tests
│   └── service/
│       └── AdminServiceTest.java         # 5 tests
└── pom.xml
```

---

## 🧪 Tests

```bash
mvn test -f pom.xml
```
```
| Test Class 	| Tests | Type                |
|----------------------|-------|---------------------|
| `AdminServiceTest` 	| 5  	 | Unit (Mockito)       |
| `AdminControllerTest`| 6     | Controller (MockMvc) |
```
---

## ⚙️ Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:mysql://localhost:3306/finflow_admin` | MySQL connection URL |
| `DB_USER` | `root` | Database username |
| `DB_PASSWORD` | `password` | Database password |
| `RABBITMQ_HOST` | `localhost` | RabbitMQ server host |
| `SERVICES_APPLICATION_SERVICE` | `http://localhost:8082` | Application Service URL |
| `SERVICES_AUTH_SERVICE` | `http://localhost:8081` | Auth Service URL |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | Eureka server URL |
| `ZIPKIN_URL` | `http://localhost:9411/api/v2/spans` | Zipkin collector URL |
