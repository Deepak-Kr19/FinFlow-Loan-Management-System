# FinFlow Backend — Complete Project Structure Explained

> **Why we created each service, each package, and each file — explained for viva/interview.**

---

## 🗂 High-Level Overview

```
FinFlow-Microservices/
├── eureka-server/          ← Service Registry (who is running where?)
├── api-gateway/            ← Single Entry Point (security + routing)
├── auth-service/           ← User Management (signup, login, JWT)
├── application-service/    ← Loan Applications (create, submit, track)
├── document-service/       ← File Management (upload, verify, download)
├── admin-service/          ← Admin Panel (decisions, reports, user mgmt)
├── docker-compose.yml      ← Orchestrates everything in containers
└── finflow-ui/             ← Angular 21 Frontend
```

### Why 6 separate projects?

We followed the **Single Responsibility Principle** at the service level. Each service handles one business domain:

| Service | Business Domain | Why Separate? |
|---------|----------------|---------------|
| **eureka-server** | Service Discovery | Infrastructure concern — has no business logic, just tracks which service is running on which port |
| **api-gateway** | Routing + Security | Cross-cutting concern — validates JWT once, routes to all services. Keeps security logic out of business services |
| **auth-service** | User Identity | Owns user data, passwords, and JWT generation. Isolated because security is sensitive — changes here shouldn't risk breaking loan logic |
| **application-service** | Loan Lifecycle | Core business domain — creating, editing, submitting loans. Separate DB so loan data is independent |
| **document-service** | File Storage | Deals with file I/O (disk operations, multipart uploads) — different scaling needs than pure DB services. If file storage gets heavy, we scale only this service |
| **admin-service** | Admin Decisions | Admin workflow is a separate concern — it orchestrates across other services. Has its own decision/report data |

---

## 1️⃣ EUREKA SERVER — Service Registry

**Why we need it:** In microservices, services run on dynamic ports/hosts (especially in Docker/cloud). Eureka lets services find each other automatically instead of hardcoding URLs.

```
eureka-server/
├── pom.xml                          ← Spring Cloud Netflix Eureka Server dependency
└── src/main/
    ├── java/.../EurekaServerApplication.java   ← @EnableEurekaServer annotation
    └── resources/application.yml               ← Port 8761, self-registration disabled
```

| File | Why It Exists |
|------|--------------|
| `EurekaServerApplication.java` | The main class with `@EnableEurekaServer`. This single annotation converts a Spring Boot app into a service registry. All other services register here on startup. |
| `application.yml` | Sets port `8761`, disables self-registration (`register-with-eureka: false`) because Eureka shouldn't register itself as a client, and disables fetching registry (`fetch-registry: false`) since it IS the registry. |

**In simple words:** Eureka is like a **phone book**. Every service tells Eureka "I'm alive at this address." When the API Gateway needs to call auth-service, it asks Eureka "Where is auth-service?" instead of hardcoding `localhost:8081`.

---

## 2️⃣ API GATEWAY — Single Entry Point

**Why we need it:** Without a gateway, the frontend would need to know the port of every service. The gateway provides a single URL (`:8080`) and handles JWT security centrally.

```
api-gateway/
├── pom.xml
└── src/main/
    ├── java/.../
    │   ├── ApiGatewayApplication.java
    │   ├── filter/
    │   │   ├── AuthenticationFilter.java    ← Global JWT validation
    │   │   └── RouteValidator.java          ← Defines which routes are public
    │   └── util/
    │       └── JwtUtil.java                 ← JWT token parsing (same secret as auth-service)
    └── resources/
        ├── application.yml                  ← Route definitions + CORS
        └── logback-spring.xml               ← Structured logging config
```

### Package-by-Package Explanation:

| Package/File | Why It Exists |
|-------------|--------------|
| **`filter/AuthenticationFilter.java`** | This is the **heart of security**. It's a `GlobalFilter` that runs before every request (order = -1). It checks if the route needs auth → extracts JWT → validates signature → injects `X-User-Id` and `X-User-Role` headers into the request so downstream services know who's calling. |
| **`filter/RouteValidator.java`** | A simple list of public URLs (`/auth/login`, `/auth/signup`, `/swagger-ui`). The filter checks this list to decide "should I validate JWT for this request?" Login and signup must be public — users don't have a token yet! |
| **`util/JwtUtil.java`** | Parses and validates JWT tokens using the **same 256-bit secret key** as auth-service. If the keys don't match, every authenticated request fails with 401. Has two methods: `validateToken()` (throws on invalid) and `extractAllClaims()` (reads userId, role). |
| **`application.yml`** | Defines **4 routes**: `/auth/**` → auth-service, `/applications/**` → application-service, `/documents/**` → document-service, `/admin/**` → admin-service. Uses `lb://` prefix for Eureka-based load balancing. Also configures global CORS to allow the Angular frontend. |

**In simple words:** The gateway is like a **security guard + receptionist**. It checks your ID badge (JWT), then directs you to the right department (service).

---

## 3️⃣ AUTH SERVICE — User Identity & JWT

**Why we need it:** Every system needs authentication. We isolated it because user data (emails, passwords) is sensitive and should be managed independently from business logic.

```
auth-service/
├── pom.xml
└── src/
    ├── main/java/.../
    │   ├── AuthServiceApplication.java        ← Spring Boot entry point
    │   ├── config/
    │   │   ├── SecurityConfig.java            ← Spring Security: CSRF, CORS, BCrypt
    │   │   └── OpenApiConfig.java             ← Swagger API documentation
    │   ├── controller/
    │   │   └── AuthController.java            ← REST endpoints (5 endpoints)
    │   ├── dto/
    │   │   ├── AuthRequest.java               ← Login request body
    │   │   ├── AuthResponse.java              ← Login response (token + user info)
    │   │   └── RegisterRequest.java           ← Signup request body with validation
    │   ├── entity/
    │   │   └── User.java                      ← JPA entity → "users" table
    │   ├── exception/
    │   │   └── GlobalExceptionHandler.java    ← Centralized error responses
    │   ├── repository/
    │   │   └── UserRepository.java            ← JPA repo with findByEmail()
    │   ├── service/
    │   │   └── AuthService.java               ← Business logic (register, login, etc.)
    │   └── util/
    │       └── JwtUtil.java                   ← JWT generation + validation
    └── main/resources/
        ├── application.yml                    ← Port 8081, DB: finflow_auth
        └── logback-spring.xml                 ← Logging config
    └── test/java/.../
        ├── controller/AuthControllerTest.java ← MockMvc tests for HTTP layer
        └── service/AuthServiceTest.java       ← Mockito tests for business logic
```

### Why Each Package Exists:

| Package | Purpose | Why Separate? |
|---------|---------|--------------|
| **`config/`** | Framework configuration | Keeps Spring Security, CORS, Swagger setup in one place. Not business logic — it's infrastructure glue. |
| **`controller/`** | REST API endpoints | The **entry point** for HTTP requests. Only handles request/response mapping — delegates all logic to service layer. Separation ensures we can test HTTP and business logic independently. |
| **`dto/`** | Data Transfer Objects | **Why not use the Entity directly?** Because (1) entities expose internal fields like password, (2) request/response shapes differ from DB schema, (3) validation annotations belong on DTOs, not entities. |
| **`entity/`** | JPA entities (DB tables) | Maps to the `users` table. Has `@JsonIgnore` on password so it's never sent in API responses. |
| **`exception/`** | Error handling | `@ControllerAdvice` catches all exceptions and returns clean JSON error responses instead of ugly stack traces. |
| **`repository/`** | Database access | `UserRepository extends JpaRepository` — Spring Data auto-generates all CRUD methods. We only added `findByEmail()` which Spring generates from the method name. |
| **`service/`** | Business logic | The **brain** of the service. Handles password hashing, duplicate email checks, JWT generation, user updates. Controller never touches the DB directly — it always goes through service. |
| **`util/`** | JWT utilities | Token generation with claims (userId, role, email) and 24h expiry. Shared logic used by the service layer. |

### File-by-File Deep Dive:

| File | What It Does | Key Details |
|------|-------------|-------------|
| `AuthController.java` | 5 endpoints: `POST /signup`, `POST /login`, `GET /admin/users`, `PUT /admin/users/{id}`, `GET /profile` | Uses `@Valid` on signup/login for input validation. Profile uses `X-User-Id` header from gateway. |
| `AuthService.java` | `register()` — checks duplicate email, hashes password, saves user. `login()` — validates credentials, generates JWT. `getProfile()` — returns user by ID. | Never stores plain-text passwords. Uses `passwordEncoder.matches()` for comparison. |
| `RegisterRequest.java` | Login DTO with `@NotBlank` and `@Email` validation | Default role is `ROLE_APPLICANT` so regular users don't need to specify role. |
| `AuthResponse.java` | Returns `{token, role, userId, name, email}` | Frontend needs userId/name/email for display; role for routing decisions. |
| `User.java` | JPA entity with `@JsonIgnore` on password | `@Column(unique=true)` on email prevents duplicate accounts at DB level. |
| `JwtUtil.java` | Generates JWT with `Jwts.builder()`, 24h expiry, HMAC-SHA256 | Claims contain `userId` and `role` — gateway reads these to create X-User-Id/X-User-Role headers. |
| `SecurityConfig.java` | Disables CSRF (we use JWT, not cookies), configures CORS, provides `BCryptPasswordEncoder` bean | All `/auth/**` endpoints are public since they handle login/signup. |

---

## 4️⃣ APPLICATION SERVICE — Loan Lifecycle

**Why we need it:** This is the **core business domain** — managing the loan application from creation to final decision. Separated from auth because loan data and user data have different access patterns and scaling needs.

```
application-service/
├── pom.xml
└── src/main/java/.../
    ├── ApplicationServiceApplication.java
    ├── config/
    │   ├── CorsConfig.java               ← CORS for direct access (dev)
    │   ├── OpenApiConfig.java            ← Swagger docs
    │   └── RabbitMQConfig.java           ← Exchange, queues, bindings
    ├── controller/
    │   └── LoanApplicationController.java ← 6 REST endpoints
    ├── dto/
    │   ├── ApplicationRequest.java        ← Create/update request body
    │   └── ApplicationResponse.java       ← (Optional response shaping)
    ├── entity/
    │   └── LoanApplication.java           ← JPA entity → "loan_applications" table
    ├── event/
    │   ├── ApplicationEvent.java          ← Event payload POJO
    │   ├── ApplicationEventProducer.java  ← Publishes to RabbitMQ
    │   └── ApplicationEventConsumer.java  ← Listens for DECISION_MADE events
    ├── exception/
    │   └── GlobalExceptionHandler.java
    ├── repository/
    │   └── LoanApplicationRepository.java ← JPA repo with findByUserId()
    └── service/
        └── LoanApplicationService.java    ← Full lifecycle business logic
```

### Why `event/` Package Exists:

This is where **asynchronous communication** happens. We created it because:

1. **`ApplicationEventProducer.java`** — When a user submits a loan, we publish an `APPLICATION_SUBMITTED` event to RabbitMQ. The admin-service picks this up. **Why not REST call?** Because (a) the application service shouldn't wait for admin to process, (b) if admin-service is down, the message is queued and delivered later.

2. **`ApplicationEventConsumer.java`** — Listens for `DECISION_MADE` events from admin-service. When admin approves/rejects, this consumer updates the application status. **Why not let admin call our REST API?** Because event-driven is more resilient — the services are decoupled.

3. **`ApplicationEvent.java`** — A POJO containing `{applicationId, userId, status, eventType}`. This is the message format sent through RabbitMQ.

### Why `RabbitMQConfig.java` Exists:

Defines the messaging topology:
- **1 Topic Exchange** (`finflow.exchange`) — acts as the message router
- **3 Queues** — `queue.application.submitted`, `queue.decision.made`, `queue.document.uploaded`
- **3 Bindings** — connects queues to the exchange via routing keys
- **`Jackson2JsonMessageConverter`** — messages are serialized as JSON, not Java serialization

**Why not define this in just one service?** Each service that produces/consumes messages needs the queue/exchange definitions so RabbitMQ creates them on startup, regardless of which service starts first.

### Key Design Decisions:

| Decision | Why |
|----------|-----|
| `personalDetails` stored as JSON TEXT | Flexibility — add fields without DB migrations |
| Ownership check on every mutation | Security — User A can't edit User B's application |
| Status as String, not Enum | Simpler — no migration needed when adding new statuses |
| `findByUserId()` in repository | Spring Data generates the SQL from the method name |

---

## 5️⃣ DOCUMENT SERVICE — File Management

**Why we need it:** Document handling involves **file I/O** (disk reads/writes, multipart uploads) which is fundamentally different from database CRUD. If document uploads spike, we scale only this service without affecting loan processing.

```
document-service/
├── pom.xml
└── src/main/java/.../
    ├── DocumentServiceApplication.java
    ├── config/
    │   ├── CorsConfig.java
    │   ├── OpenApiConfig.java
    │   └── RabbitMQConfig.java           ← Queue definitions for document events
    ├── controller/
    │   └── DocumentController.java        ← 4 endpoints: upload, verify, list, download
    ├── entity/
    │   └── Document.java                  ← JPA entity → "documents" table
    ├── event/
    │   ├── DocumentEvent.java             ← Event payload
    │   └── DocumentEventProducer.java     ← Publishes DOCUMENT_UPLOADED events
    ├── exception/
    │   └── GlobalExceptionHandler.java
    ├── repository/
    │   └── DocumentRepository.java        ← findByApplicationId()
    └── service/
        └── DocumentService.java           ← File I/O + DB + event publishing
```

### Why Each File Exists:

| File | Purpose |
|------|---------|
| `DocumentController.java` | **4 endpoints**: `POST /upload` (multipart), `PUT /{id}/verify`, `GET /application/{appId}`, `GET /{id}/download`. Upload accepts `MultipartFile`. Download returns a `Resource` with content-disposition header. |
| `DocumentService.java` | **Upload**: generates unique filename → writes to `uploads/` dir → saves entity with `PENDING` status → publishes event. **Verify**: finds doc → updates status to `VERIFIED`/`REJECTED`. **Download**: finds doc → loads file as `UrlResource`. |
| `Document.java` | Entity with `{id, applicationId, type, filePath, status}`. `applicationId` links to a loan application in the other service's DB (loose reference, not a foreign key — because separate databases). |
| `DocumentRepository.java` | `findByApplicationId(Long)` — returns all documents for a specific loan application. Spring Data generates this from method name. |
| `DocumentEventProducer.java` | Publishes `DOCUMENT_UPLOADED` event to RabbitMQ so application-service knows a document was uploaded. |

### Why `document.upload-dir` in YAML:

```yaml
document:
  upload-dir: uploads/
```
The upload directory is configured externally so it can be different in dev (`./uploads/`) vs Docker (`/app/uploads/`) vs production (S3 bucket path). The service reads it via `@Value("${document.upload-dir}")`.

---

## 6️⃣ ADMIN SERVICE — Admin Panel & Orchestrator

**Why we need it:** Admin operations **span multiple services** — viewing applications (from application-service), managing users (from auth-service), and making decisions (its own data). It acts as an **orchestrator** that coordinates across services.

```
admin-service/
├── pom.xml
└── src/main/java/.../
    ├── AdminServiceApplication.java
    ├── config/
    │   ├── AppConfig.java                ← RestTemplate bean definition
    │   ├── CorsConfig.java
    │   ├── OpenApiConfig.java
    │   └── RabbitMQConfig.java
    ├── controller/
    │   └── AdminController.java           ← 5 REST endpoints
    ├── entity/
    │   ├── Decision.java                  ← Stores approval/rejection records
    │   └── Report.java                    ← Stores system reports
    ├── event/
    │   ├── ApplicationEventConsumer.java   ← Listens for APPLICATION_SUBMITTED
    │   ├── DecisionEvent.java             ← Decision event payload
    │   └── DecisionEventProducer.java     ← Publishes DECISION_MADE events
    ├── exception/
    │   └── GlobalExceptionHandler.java
    ├── repository/
    │   ├── DecisionRepository.java
    │   └── ReportRepository.java
    └── service/
        └── AdminService.java              ← Orchestration + decision logic
```

### Why `AppConfig.java` (RestTemplate) Exists:

```java
@Bean
public RestTemplate restTemplate() {
    return new RestTemplate();
}
```
Admin-service calls other services via REST. Spring doesn't auto-create `RestTemplate` — you must declare it as a bean. Without this, the application fails at startup with `NoSuchBeanDefinitionException`.

### Why Two Entities (Decision + Report):

| Entity | Why It Exists |
|--------|--------------|
| `Decision` | Records every admin decision: `{applicationId, decision, remarks}`. This creates an **audit trail** — we can always check who approved/rejected and why. |
| `Report` | Stores generated reports: `{type, data}`. Type identifies the report kind (e.g., "MONTHLY_SUMMARY"), data stores the report content as JSON TEXT. |

### Why Admin Service Does REST Calls:

**Database-per-Service pattern** means admin-service **cannot** query `finflow_app` or `finflow_auth` databases directly. So:
- To show all applications → `RestTemplate.getForEntity(applicationServiceUrl + "/applications/admin/all")`
- To show all users → `RestTemplate.getForEntity(authServiceUrl + "/auth/admin/users")`
- Service URLs come from `application.yml` with Docker-overridable env vars

---

## 7️⃣ SHARED PATTERNS — Why Every Service Has These

### Standard Package Structure (repeated in each service):

```
com.capg.{servicename}/
├── config/          ← Framework configuration
├── controller/      ← REST API layer
├── dto/             ← Request/Response objects
├── entity/          ← JPA entities (DB tables)
├── event/           ← RabbitMQ producers/consumers
├── exception/       ← Error handling
├── repository/      ← Database access
└── service/         ← Business logic
```

**Why this layered structure?**

```
HTTP Request → Controller → Service → Repository → Database
                  ↑              ↑
                 DTO           Entity
```

| Layer | Rule | Reason |
|-------|------|--------|
| **Controller** | No business logic | Only maps HTTP requests to service calls. Makes API testing easy (MockMvc). |
| **Service** | No HTTP concerns | Pure business logic. Can be called from controllers, event consumers, or scheduled tasks. |
| **Repository** | No logic at all | Just a JPA interface. Spring generates the SQL implementation. |
| **DTO** | Never exposed to DB | Prevents leaking internal fields (like password). Allows request/response to differ from DB schema. |
| **Entity** | Never returned directly in responses (ideally) | DB structure shouldn't dictate API shape. We used entities directly in some responses for simplicity, but added `@JsonIgnore` where needed. |

### Why Every Service Has `application.yml`:

Each service needs its own config for:
- **Port** — unique per service (8081, 8082, 8083, 8084)
- **Database URL** — separate database per service
- **RabbitMQ connection** — same RabbitMQ but different queues
- **Eureka URL** — to register with service discovery
- **Zipkin URL** — to send tracing data
- All values use `${ENV_VAR:default}` syntax so Docker can override them

### Why Every Service Has `logback-spring.xml`:

Structured logging with **correlation IDs** (traceId, spanId from Zipkin). When a request flows through gateway → auth → application, all log entries share the same traceId, making debugging across services possible.

---

## 8️⃣ DOCKER-COMPOSE — Why Each Container Exists

```yaml
services:
  mysql-db:          ← Single MySQL instance (4 databases inside it)
  rabbitmq:          ← Message broker for async events
  zipkin:            ← Distributed tracing UI
  eureka-server:     ← Service registry
  api-gateway:       ← Entry point (depends on all services)
  auth-service:      ← User management
  application-service: ← Loan lifecycle
  document-service:  ← File management
  admin-service:     ← Admin operations
```

**Startup Order Logic:**
1. MySQL and RabbitMQ start first (with health checks — services wait until they're ready)
2. Eureka and Zipkin start independently (no health deps)
3. Business services start after MySQL is healthy + Eureka is started
4. Gateway starts **last** — waits for all 4 business services to be available

---

## 📊 Communication Map

```
┌─────────────────────────────────────────────────────────────┐
│                        FRONTEND (Angular)                    │
│                    http://localhost:4200                      │
└───────────────────────────┬─────────────────────────────────┘
                            │ All requests via proxy
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    API GATEWAY (:8080)                        │
│           JWT Validation + Route to Service                  │
└──┬──────────┬──────────┬──────────┬─────────────────────────┘
   │          │          │          │
   ▼          ▼          ▼          ▼
┌──────┐ ┌────────┐ ┌────────┐ ┌───────┐
│ AUTH │ │  APP   │ │  DOC   │ │ ADMIN │
│:8081 │ │ :8082  │ │ :8083  │ │ :8084 │
└──┬───┘ └──┬──┬──┘ └──┬────┘ └─┬──┬──┘
   │        │  │        │        │  │
   │        │  │ RabbitMQ│        │  │
   │        │  └◄───────┘        │  │ REST calls
   │        └◄───────────────────┘  │ (RestTemplate)
   └◄───────────────────────────────┘
```

**Data Flow Examples:**
- **Login**: Frontend → Gateway → Auth Service → MySQL → JWT back
- **Submit App**: Frontend → Gateway → App Service → MySQL + RabbitMQ → Admin consumes event
- **Approve Loan**: Frontend → Gateway → Admin Service → Decision saved + RabbitMQ → App Service consumes → status updated
- **Upload Doc**: Frontend → Gateway → Doc Service → File saved + MySQL + RabbitMQ → App Service notified

---

> **Key Takeaway for Viva:** We didn't create 6 projects randomly. Each service exists because it owns a distinct **business domain** with its own **data**, **lifecycle**, and **scaling needs**. The infrastructure services (Eureka, Gateway) exist to solve **cross-cutting concerns** (discovery, security) that shouldn't be duplicated in every business service.
