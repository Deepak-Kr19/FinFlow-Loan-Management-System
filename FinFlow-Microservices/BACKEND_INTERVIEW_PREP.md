# FinFlow Backend — 50 Interview Questions & Answers

---

## 🏗 Architecture & Microservices (Q1–Q10)

### Q1. What is the architecture of your project?
**A:** FinFlow uses a **microservices architecture** with 4 business services (auth, application, document, admin), an **API Gateway** for routing, **Eureka Server** for service discovery, **RabbitMQ** for async messaging, **MySQL** as the database, and **Zipkin** for distributed tracing. The Angular frontend communicates only with the API Gateway on port 8080, which routes requests to downstream services.

### Q2. Why did you choose microservices over monolithic?
**A:** Each service has a distinct bounded context — authentication, loan applications, document management, and admin decisions. Microservices allow **independent deployment**, **technology flexibility**, **separate databases** (Database-per-Service pattern), and **fault isolation** — if the document service goes down, users can still log in and view applications.

### Q3. List all your microservices and their ports.
**A:**
| Service | Port | Database |
|---------|------|----------|
| Eureka Server | 8761 | — |
| API Gateway | 8080 | — |
| Auth Service | 8081 | finflow_auth |
| Application Service | 8082 | finflow_app |
| Document Service | 8083 | finflow_doc |
| Admin Service | 8084 | finflow_admin |

### Q4. What is the Database-per-Service pattern and why did you use it?
**A:** Each microservice owns its own database (finflow_auth, finflow_app, finflow_doc, finflow_admin). This ensures **loose coupling** — services don't share tables. If the auth DB schema changes, it doesn't break the document service. The tradeoff is that cross-service queries require REST calls or events.

### Q5. How do your services communicate with each other?
**A:** Two patterns:
1. **Synchronous (REST)** — Admin Service calls Application Service (`GET /applications/admin/all`) and Auth Service (`GET /auth/admin/users`) using `RestTemplate`.
2. **Asynchronous (RabbitMQ)** — Application Service publishes `APPLICATION_SUBMITTED` events; Admin Service publishes `DECISION_MADE` events; Document Service publishes `DOCUMENT_UPLOADED` events.

### Q6. What is the role of the API Gateway?
**A:** The API Gateway (`api-gateway`, port 8080) is the **single entry point** for all client requests. It performs: (1) **JWT validation** via `AuthenticationFilter`, (2) **routing** to downstream services via path predicates (`/auth/**` → auth-service), (3) injecting **X-User-Id** and **X-User-Role** headers so downstream services know who the caller is, and (4) **CORS** handling.

### Q7. What is Eureka and why is it needed?
**A:** Eureka is a **service discovery server**. Each microservice registers itself with Eureka on startup. The API Gateway uses `lb://auth-service` URIs which resolve to actual host:port via Eureka. This enables **dynamic scaling** — if we add a second instance of auth-service, the gateway load-balances automatically without config changes.

### Q8. What happens if Eureka goes down?
**A:** Services cache the registry locally. Existing routing continues to work for a while. However, new service instances won't be discovered. In production, you'd run **multiple Eureka instances** in a peer-aware cluster for high availability.

### Q9. What is Zipkin and how did you configure it?
**A:** Zipkin is a **distributed tracing** system. Each service sends trace spans to Zipkin (`http://zipkin:9411/api/v2/spans`). We set `management.tracing.sampling.probability=1.0` (100% sampling) in all services. This lets us trace a single request across gateway → auth → application → RabbitMQ → admin, identifying latency bottlenecks.

### Q10. Explain the `docker-compose.yml` startup order.
**A:** Uses `depends_on` with conditions:
1. **mysql-db** starts first with a `healthcheck` (mysqladmin ping)
2. **rabbitmq** starts with healthcheck (rabbitmqctl status)
3. **eureka-server** and **zipkin** start independently
4. **auth, application, document, admin** services wait for MySQL (healthy) + Eureka (started) + RabbitMQ (healthy)
5. **api-gateway** starts last — waits for all 4 business services

---

## 🔐 Authentication & Security (Q11–Q20)

### Q11. How does JWT authentication work in your project?
**A:** (1) User calls `POST /auth/login` with email/password. (2) Auth Service validates credentials, generates a JWT containing `userId`, `role`, and `email` as claims with 24h expiry. (3) Client stores the token and sends it as `Authorization: Bearer <token>` on every request. (4) API Gateway's `AuthenticationFilter` validates the JWT signature and extracts claims.

### Q12. Where is the JWT validated — in each service or at the gateway?
**A:** **Only at the API Gateway**. The `AuthenticationFilter` validates the token once and injects `X-User-Id` and `X-User-Role` headers. Downstream services trust these headers and never parse the JWT themselves. This is the **gateway-level auth** pattern.

### Q13. What signing algorithm do you use for JWT?
**A:** **HMAC-SHA256** (`HS256`). The secret key is a 256-bit hex string: `3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b`. The same key exists in both `auth-service/JwtUtil` (for signing) and `api-gateway/JwtUtil` (for verification).

### Q14. What would happen if the JWT secret keys don't match between auth-service and api-gateway?
**A:** The gateway would throw a `SignatureException` when validating the token. Every authenticated request would fail with **401 Unauthorized**, even if the user has a valid token from auth-service. This was a deliberate design decision — both must share the same secret.

### Q15. How do you hash passwords?
**A:** Using **BCrypt** via Spring Security's `BCryptPasswordEncoder`. BCrypt automatically generates a random salt for each password, making it resistant to rainbow table attacks. The hashed password is stored in the `users` table. During login, we use `passwordEncoder.matches(rawPassword, hashedPassword)`.

### Q16. Why did you add `@JsonIgnore` on the User entity's password field?
**A:** Without it, endpoints like `GET /auth/admin/users` and `GET /auth/profile` would return the BCrypt-hashed password in the JSON response. Even though it's hashed, exposing it is a **security risk** (attackers could attempt offline brute-force). `@JsonIgnore` ensures the password is never serialized to JSON.

### Q17. How does the `AuthenticationFilter` work step by step?
**A:** (1) Intercepts every request as a `GlobalFilter` with order -1. (2) Checks if the route is "open" using `RouteValidator` (login, signup, swagger are open). (3) For secured routes, extracts the `Authorization` header, strips "Bearer ", calls `jwtUtil.validateToken()`. (4) On success, extracts `userId` and `role` from claims and adds them as `X-User-Id` and `X-User-Role` headers. (5) On failure, returns **401** and terminates the chain.

### Q18. What routes are public (don't require JWT)?
**A:** Defined in `RouteValidator.openApiEndpoints`:
- `/auth/signup`, `/auth/login` — registration/login
- `/swagger-ui`, `/v3/api-docs`, `/swagger-resources`, `/webjars` — API documentation

### Q19. How does CSRF protection work in your project?
**A:** CSRF is **disabled** (`csrf.disable()`) in `SecurityConfig`. This is correct because we use **stateless JWT tokens**, not session cookies. CSRF attacks exploit cookie-based authentication; since we use `Authorization` headers, CSRF protection is unnecessary and would actually break API calls.

### Q20. How does CORS work across your services?
**A:** CORS is configured at **two levels**: (1) API Gateway's `application.yml` has `globalcors` allowing all origins, methods, and headers. (2) Auth Service's `SecurityConfig` has a `CorsConfigurationSource` bean with `allowedOriginPatterns: *`. This dual config ensures CORS works whether the frontend hits the gateway or directly hits auth-service during development.

---

## 📝 Application Service (Q21–Q28)

### Q21. What is the loan application lifecycle?
**A:** `Draft` → `Submitted` → `APPROVED` / `REJECTED`. A user creates an application (Draft), can edit it, then submits it. Once submitted, it's locked. An admin reviews and approves or rejects it. The status update from admin happens via RabbitMQ events.

### Q22. Why do you store personalDetails, employmentDetails, and loanDetails as JSON strings?
**A:** Using `@Column(columnDefinition = "TEXT")` with JSON strings provides **schema flexibility** — we can add new fields (e.g., "co-applicant") without database migrations. The frontend serializes form data with `JSON.stringify()` and the backend stores it as-is. This is the **schemaless fields** pattern within a relational database.

### Q23. How does the ownership check work?
**A:** Every mutation (update, submit, view) compares `app.getUserId()` with the `X-User-Id` from the request header: `if (!app.getUserId().equals(userId)) throw new RuntimeException("Unauthorized")`. This prevents User A from modifying User B's application.

### Q24. What happens when an application is submitted?
**A:** In `submitApplication()`: (1) Ownership check. (2) Status changed to "Submitted" and saved. (3) An `ApplicationEvent` is created with `{applicationId, userId, status, eventType}`. (4) Published to RabbitMQ exchange `finflow.exchange` with routing key `application.submitted`. (5) Admin Service's consumer picks it up.

### Q25. How does the Application Service know when an admin makes a decision?
**A:** It has a `DecisionEventConsumer` listening on `queue.decision.made`. When admin-service publishes a `DECISION_MADE` event, the consumer updates the application's status to `APPROVED` or `REJECTED` in the database. This is **event-driven architecture** — the services are decoupled.

### Q26. Why does `GET /applications/admin/all` not require `X-User-Id`?
**A:** It's an admin-only endpoint called by the Admin Service via REST (`RestTemplate`). Admin operations need to see all applications regardless of owner. The endpoint returns `repository.findAll()` without any user filtering.

### Q27. What is `LoanApplicationRepository` and what custom methods does it have?
**A:** It extends `JpaRepository<LoanApplication, Long>`. It has one custom query method: `findByUserId(Long userId)` which Spring Data JPA auto-implements based on the method name convention — it generates `SELECT * FROM loan_applications WHERE user_id = ?`.

### Q28. How would you handle concurrent updates to the same application?
**A:** Currently, there's no optimistic locking. In production, I'd add a `@Version` field to `LoanApplication`. JPA would then throw `OptimisticLockException` if two users try to update simultaneously. The second update would fail and retry.

---

## 📎 Document Service (Q29–Q35)

### Q29. How does file upload work?
**A:** The `POST /documents/upload` endpoint accepts `MultipartFile`. The service: (1) Generates a unique filename using `System.currentTimeMillis() + "_" + originalFilename`. (2) Writes the file to the `uploads/` directory using `Files.copy()`. (3) Creates a `Document` entity with `status=PENDING` and `filePath`. (4) Publishes a `DOCUMENT_UPLOADED` event to RabbitMQ.

### Q30. How does the download endpoint work?
**A:** `GET /documents/{id}/download`: (1) Finds the Document entity by ID. (2) Resolves the file path using `Paths.get(document.getFilePath())`. (3) Creates a `UrlResource` from the path. (4) Returns it with `Content-Disposition: attachment` header and auto-detected content type via `Files.probeContentType()`.

### Q31. What is the document verification workflow?
**A:** Admin calls `PUT /documents/{id}/verify?status=VERIFIED` (or `REJECTED`). The service finds the document, updates its status, and saves. The frontend shows ✓ (verify) and ✕ (reject) buttons only for `PENDING` documents.

### Q32. How did you configure the max upload size?
**A:** In `application.yml`:
```yaml
spring.servlet.multipart.max-file-size: 10MB
spring.servlet.multipart.max-request-size: 10MB
```

### Q33. What happens if the file doesn't exist on disk when downloading?
**A:** The `UrlResource` would be non-readable. The controller checks `resource.exists() && resource.isReadable()` — if false, it throws a `RuntimeException("File not found")` which results in a **500 Internal Server Error**. In production, I'd return a proper **404** using `@ExceptionHandler`.

### Q34. Why did you add `getDocumentById()` to the service?
**A:** The download endpoint needs to look up the document's `filePath` by ID. The existing `findByApplicationId()` returns a list — we needed a single-entity lookup. `getDocumentById()` uses `repository.findById(id).orElseThrow()`.

### Q35. How does the Document Service notify the Application Service about uploads?
**A:** Via RabbitMQ. `DocumentEventProducer` publishes a `DocumentEvent` with routing key `document.uploaded`. The Application Service has a consumer that listens on `queue.document.uploaded` and can update the application's document status accordingly.

---

## 🛡 Admin Service (Q36–Q42)

### Q36. Why does the Admin Service use `RestTemplate` instead of direct DB access?
**A:** Because of **Database-per-Service** pattern. The Admin Service has its own DB (`finflow_admin`) and cannot access `finflow_app` or `finflow_auth`. It uses `RestTemplate` to call Application Service's `/applications/admin/all` and Auth Service's `/auth/admin/users` endpoints. This maintains service boundaries.

### Q37. How does the decision-making flow work end-to-end?
**A:** (1) Admin calls `POST /admin/applications/{id}/decision?decision=APPROVED&remarks=...`. (2) `AdminService.makeDecision()` saves a `Decision` entity to `finflow_admin.decisions`. (3) Creates a `DecisionEvent` and publishes to RabbitMQ with routing key `decision.made`. (4) Application Service's `DecisionEventConsumer` picks it up and updates the application status to APPROVED/REJECTED.

### Q38. What entities does the Admin Service own?
**A:** Two entities: `Decision` (applicationId, decision, remarks) and `Report` (type, data). These are stored in `finflow_admin` database.

### Q39. How are the service URLs configured for RestTemplate calls?
**A:** Via `@Value` injection from `application.yml`:
```yaml
services:
  application-service: ${SERVICES_APPLICATION_SERVICE:http://localhost:8082}
  auth-service: ${SERVICES_AUTH_SERVICE:http://localhost:8081}
```
In Docker, environment variables override these with container hostnames (`http://application-service:8082`).

### Q40. What happens if the Application Service is down when Admin tries to fetch applications?
**A:** The `RestTemplate` call is wrapped in a try-catch. If it fails, the method returns `"Application Service unavailable or endpoint missing"` as a string instead of crashing. In production, I'd add **Circuit Breaker** (Resilience4j) to handle this more gracefully with fallback responses.

### Q41. Why did you use `Object` as the return type for `getAllApplications()` in AdminService?
**A:** Because the Admin Service doesn't have the `LoanApplication` entity class — that belongs to Application Service. Using `Object` lets us pass through the JSON response from RestTemplate without needing to duplicate the entity class. It's a pragmatic choice for inter-service communication.

### Q42. What is `RestTemplateConfig` and why is it needed?
**A:** It's a `@Configuration` class that creates a `RestTemplate` bean via `@Bean`. Spring doesn't auto-create RestTemplate — you must define it. Without this config, the AdminService constructor injection would fail with `NoSuchBeanDefinitionException`.

---

## 🐇 RabbitMQ & Events (Q43–Q47)

### Q43. Explain the RabbitMQ topology in your project.
**A:** One **Topic Exchange** (`finflow.exchange`) with three **durable queues**:
| Queue | Routing Key | Producer | Consumer |
|-------|------------|----------|----------|
| `queue.application.submitted` | `application.submitted` | Application Service | Admin Service |
| `queue.decision.made` | `decision.made` | Admin Service | Application Service |
| `queue.document.uploaded` | `document.uploaded` | Document Service | Application Service |

### Q44. Why did you choose a Topic Exchange over Direct or Fanout?
**A:** Topic exchange supports **pattern-based routing** (e.g., `application.*`). While we currently use exact keys, a topic exchange gives us flexibility to add patterns later (e.g., `application.#` to match all application events). Direct would also work for our current use case.

### Q45. Why are your queues durable?
**A:** `new Queue(name, true)` — the `true` means **durable**. If RabbitMQ restarts, durable queues survive. Non-durable queues would be deleted, and any unprocessed messages would be lost. For a financial application, message loss is unacceptable.

### Q46. What message format do you use?
**A:** **JSON** via `Jackson2JsonMessageConverter`. The `RabbitTemplate` is configured with this converter, so Java objects are automatically serialized to JSON when publishing and deserialized when consuming. For example, `ApplicationEvent` becomes `{"applicationId":1, "userId":5, "status":"Submitted", "eventType":"APPLICATION_SUBMITTED"}`.

### Q47. What happens if the consumer is down when a message is published?
**A:** The message stays in the **durable queue** until a consumer connects and acknowledges it. RabbitMQ provides **at-least-once delivery** — the message won't be lost. When the consumer comes back online, it processes all queued messages.

---

## 🧪 Testing & Best Practices (Q48–Q50)

### Q48. What testing strategy did you follow?
**A:** Two layers per service:
1. **Unit Tests** (`@ExtendWith(MockitoExtension.class)`) — test service logic with mocked dependencies. Example: `AuthServiceTest` mocks `UserRepository`, `PasswordEncoder`, `JwtUtil`.
2. **Controller Tests** (`@WebMvcTest`) — test HTTP layer with `MockMvc`. Example: `AuthControllerTest` sends actual HTTP requests to verify status codes, JSON responses, and validation.

Total: **46 tests across 4 services, 0 failures**.

### Q49. Why did you use `@MockitoBean` instead of `@MockBean`?
**A:** `@MockitoBean` is the **Spring Boot 3.4+** replacement for `@MockBean` (which is deprecated). It uses `@org.springframework.test.context.bean.override.mockito.MockitoBean` and does the same thing — creates a Mockito mock and registers it in the Spring context, replacing the real bean.

### Q50. How would you improve this project for production?
**A:** Key improvements:
1. **Circuit Breaker** (Resilience4j) on RestTemplate calls in AdminService
2. **API rate limiting** at the gateway level
3. **Centralized config** with Spring Cloud Config Server
4. **Optimistic locking** (`@Version`) on entities for concurrent updates
5. **Global exception handler** (`@ControllerAdvice`) returning proper error DTOs
6. **JWT refresh tokens** to avoid forcing re-login after 24h
7. **Docker health checks** on Java services (not just MySQL/RabbitMQ)
8. **API versioning** (e.g., `/api/v1/applications`)
9. **Password validation** (min length, special chars) in RegisterRequest
10. **Pagination** on list endpoints (`Pageable`) for large datasets

---

> 💡 **Tip**: For viva, focus on *why* you made each decision, not just *what* you built. Interviewers value architectural reasoning over code recitation.
