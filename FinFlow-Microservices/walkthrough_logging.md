# Logging in FinFlow — Complete Walkthrough

## What Is Logging & Why Do We Need It?

Logging = your application writing a **diary of everything that happens at runtime**.

Without logging, when something breaks in production, you're blind. With logging, you can trace exactly what happened, when, and in which service.

---

## Technology Stack

| Component | Role |
|-----------|------|
| **SLF4J** | Logging API (interface) — what you use in Java code |
| **Logback** | Logging implementation (engine) — processes and outputs logs |
| **logback-spring.xml** | XML config file — controls format, destinations, rotation |
| **Zipkin traceId/spanId** | Injected into every log line for cross-service correlation |

```
Your Java Code → SLF4J API → Logback Engine → Console + File
                                    ↑
                          logback-spring.xml (config)
```

---

## Part 1: The Java Code — `SLF4J` Usage

### How to add logging to any class:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthService {
    // Step 1: Create a logger (one per class)
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public String register(RegisterRequest request) {
        // Step 2: Use log methods at appropriate levels
        log.info("Registering new user with email: {}", request.getEmail());     // INFO
        log.warn("Registration failed — email exists: {}", request.getEmail());  // WARN
        log.error("Unhandled exception: {}", ex.getMessage(), ex);               // ERROR
        log.debug("Query returned {} rows", count);                              // DEBUG
    }
}
```

> **Key rule:** Use `{}` placeholders instead of string concatenation — Logback only creates the string if the level is active, saving performance.

### Files Where Logging Was Added

#### API Gateway (1 file)

| File | What's Logged |
|------|--------------|
| [AuthenticationFilter.java](file:///c:/Capgemini-Training/All-Capg-Workspaces/Capg-Sprint1-WorkSpace/FinFlow-Microservices/api-gateway/src/main/java/com/capg/apigateway/filter/AuthenticationFilter.java) | Every incoming request, JWT validation result, open route bypass |

```
INFO  Incoming request: POST /applications/1/submit
INFO  JWT validated — userId: 10, role: ROLE_APPLICANT, routing to: /applications/1/submit
WARN  Missing Authorization header for secured route: GET /admin/reports
ERROR JWT validation failed for GET /admin/reports: Token expired
DEBUG Open route — skipping auth: POST /auth/login
```

---

#### Auth Service (3 files)

| File | What's Logged |
|------|--------------|
| [AuthController.java](file:///c:/Capgemini-Training/All-Capg-Workspaces/Capg-Sprint1-WorkSpace/FinFlow-Microservices/auth-service/src/main/java/com/capg/authservice/controller/AuthController.java) | Endpoint hit with key params |
| [AuthService.java](file:///c:/Capgemini-Training/All-Capg-Workspaces/Capg-Sprint1-WorkSpace/FinFlow-Microservices/auth-service/src/main/java/com/capg/authservice/service/AuthService.java) | Register, login, update — success and failures |
| [GlobalExceptionHandler.java](file:///c:/Capgemini-Training/All-Capg-Workspaces/Capg-Sprint1-WorkSpace/FinFlow-Microservices/auth-service/src/main/java/com/capg/authservice/exception/GlobalExceptionHandler.java) | Validation errors (WARN), unhandled exceptions (ERROR + stack trace) |

```
INFO  POST /auth/signup — email: deepak@capg.com
INFO  Registering new user with email: deepak@capg.com
INFO  User registered successfully: deepak@capg.com (role: ROLE_APPLICANT)
WARN  Login failed — invalid password for: deepak@capg.com
ERROR Unhandled exception: Database connection refused [full stack trace]
```

---

#### Application Service (4 files)

| File | What's Logged |
|------|--------------|
| [LoanApplicationController.java](file:///c:/Capgemini-Training/All-Capg-Workspaces/Capg-Sprint1-WorkSpace/FinFlow-Microservices/application-service/src/main/java/com/capg/applicationservice/controller/LoanApplicationController.java) | Endpoint hit with userId |
| [LoanApplicationService.java](file:///c:/Capgemini-Training/All-Capg-Workspaces/Capg-Sprint1-WorkSpace/FinFlow-Microservices/application-service/src/main/java/com/capg/applicationservice/service/LoanApplicationService.java) | Create, update, submit, status — with RabbitMQ events |
| [ApplicationEventProducer.java](file:///c:/Capgemini-Training/All-Capg-Workspaces/Capg-Sprint1-WorkSpace/FinFlow-Microservices/application-service/src/main/java/com/capg/applicationservice/event/ApplicationEventProducer.java) | RabbitMQ event publishing |
| [ApplicationEventConsumer.java](file:///c:/Capgemini-Training/All-Capg-Workspaces/Capg-Sprint1-WorkSpace/FinFlow-Microservices/application-service/src/main/java/com/capg/applicationservice/event/ApplicationEventConsumer.java) | RabbitMQ event consumption |

```
INFO  POST /applications/1/submit — userId: 10
INFO  Submitting application id=1 by userId=10
INFO  Application submitted: id=1, status=Submitted
INFO  📤 Publishing APPLICATION_SUBMITTED event: {appId=1, userId=10}
INFO  Published APPLICATION_SUBMITTED event for applicationId=1
WARN  Unauthorized update attempt: userId=99 tried to update application owned by userId=10
```

---

#### Document Service (3 files)

| File | What's Logged |
|------|--------------|
| [DocumentController.java](file:///c:/Capgemini-Training/All-Capg-Workspaces/Capg-Sprint1-WorkSpace/FinFlow-Microservices/document-service/src/main/java/com/capg/documentservice/controller/DocumentController.java) | Upload and verify requests |
| [DocumentService.java](file:///c:/Capgemini-Training/All-Capg-Workspaces/Capg-Sprint1-WorkSpace/FinFlow-Microservices/document-service/src/main/java/com/capg/documentservice/service/DocumentService.java) | File details, save path, status transitions |
| [DocumentEventProducer.java](file:///c:/Capgemini-Training/All-Capg-Workspaces/Capg-Sprint1-WorkSpace/FinFlow-Microservices/document-service/src/main/java/com/capg/documentservice/event/DocumentEventProducer.java) | RabbitMQ event |

```
INFO  POST /documents/upload — applicationId: 100, type: ID_PROOF, file: passport.pdf
INFO  Uploading document: applicationId=100, type=ID_PROOF, fileName=passport.pdf, size=245KB
DEBUG Created upload directory: uploads/
DEBUG File saved to: uploads/a3b2c1d4_passport.pdf
INFO  Document saved: id=1, applicationId=100, type=ID_PROOF, status=PENDING
INFO  Document verified: id=1, status changed from PENDING to VERIFIED
ERROR Document upload failed: File size exceeds limit [stack trace]
```

---

#### Admin Service (4 files)

| File | What's Logged |
|------|--------------|
| [AdminController.java](file:///c:/Capgemini-Training/All-Capg-Workspaces/Capg-Sprint1-WorkSpace/FinFlow-Microservices/admin-service/src/main/java/com/capg/adminservice/controller/AdminController.java) | All admin endpoint hits |
| [AdminService.java](file:///c:/Capgemini-Training/All-Capg-Workspaces/Capg-Sprint1-WorkSpace/FinFlow-Microservices/admin-service/src/main/java/com/capg/adminservice/service/AdminService.java) | Decisions, cross-service REST calls (success/failure) |
| [DecisionEventProducer.java](file:///c:/Capgemini-Training/All-Capg-Workspaces/Capg-Sprint1-WorkSpace/FinFlow-Microservices/admin-service/src/main/java/com/capg/adminservice/event/DecisionEventProducer.java) | RabbitMQ event |
| [ApplicationEventConsumer.java](file:///c:/Capgemini-Training/All-Capg-Workspaces/Capg-Sprint1-WorkSpace/FinFlow-Microservices/admin-service/src/main/java/com/capg/adminservice/event/ApplicationEventConsumer.java) | RabbitMQ consumption |

```
INFO  POST /admin/applications/100/decision — decision: APPROVED, remarks: All verified
INFO  Making decision on applicationId=100: decision=APPROVED
INFO  Decision saved: id=1, applicationId=100, decision=APPROVED
INFO  📤 Publishing DECISION_MADE event: {appId=100, decision=APPROVED}
INFO  Fetching all users from auth-service: http://auth-service:8081
ERROR Failed to fetch users from auth-service: Connection refused
```

---

## Part 2: The XML Config — `logback-spring.xml`

Each service has a [logback-spring.xml](file:///c:/Capgemini-Training/All-Capg-Workspaces/Capg-Sprint1-WorkSpace/FinFlow-Microservices/auth-service/src/main/resources/logback-spring.xml) in `src/main/resources/`.

### Full Breakdown

```xml
<!-- 1. VARIABLES -->
<property name="APP_NAME" value="auth-service"/>
<property name="LOG_PATH" value="logs"/>
<property name="LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} [${APP_NAME}] [%thread] [%X{traceId:-},%X{spanId:-}] %-5level %logger{36} - %msg%n"/>
```

| Token | Output | Purpose |
|-------|--------|---------|
| `%d{yyyy-MM-dd HH:mm:ss.SSS}` | `2026-03-28 16:52:00.123` | Timestamp |
| `[${APP_NAME}]` | `[auth-service]` | Which service |
| `[%thread]` | `[http-nio-8081-exec-1]` | Which thread |
| `[%X{traceId:-},%X{spanId:-}]` | `[abc123,001]` | Zipkin trace/span IDs |
| `%-5level` | `INFO ` | Log level (padded to 5 chars) |
| `%logger{36}` | `c.c.a.service.AuthService` | Class name (truncated) |
| `%msg%n` | `Login successful...` | Your message + newline |

```xml
<!-- 2. CONSOLE APPENDER — prints to terminal/Docker -->
<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder><pattern>${LOG_PATTERN}</pattern></encoder>
</appender>
```

```xml
<!-- 3. FILE APPENDER — writes to log files with rotation -->
<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/auth-service.log</file>                    <!-- Current log file -->
    <rollingPolicy class="...SizeAndTimeBasedRollingPolicy">
        <fileNamePattern>logs/auth-service-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
        <maxFileSize>10MB</maxFileSize>     <!-- Rotate when file > 10MB -->
        <maxHistory>30</maxHistory>         <!-- Keep 30 days of logs -->
        <totalSizeCap>500MB</totalSizeCap>  <!-- Max 500MB total disk -->
    </rollingPolicy>
</appender>
```

```xml
<!-- 4. LOGGER LEVELS — control verbosity -->
<logger name="com.capg.authservice" level="DEBUG"/>  <!-- Your code: show everything -->
<logger name="org.springframework" level="WARN"/>     <!-- Spring: only warnings+ -->
<logger name="org.hibernate" level="WARN"/>           <!-- Hibernate: only warnings+ -->
<logger name="org.hibernate.SQL" level="DEBUG"/>      <!-- SQL queries: show them -->
<root level="INFO"/>                                  <!-- Everything else: INFO+ -->
```

### Log File Structure on Disk

```
auth-service/
└── logs/
    ├── auth-service.log                    ← Current log (active)
    ├── auth-service-2026-03-27.0.log       ← Yesterday's log
    ├── auth-service-2026-03-26.0.log       ← Day before
    └── auth-service-2026-03-26.1.log       ← Second file from that day (hit 10MB)
```

---

## Part 3: How Logging + Zipkin Work Together

### A single request creates this log trail across services:

```
[api-gateway]          [abc123,001] INFO  Incoming request: POST /applications/1/submit
[api-gateway]          [abc123,001] INFO  JWT validated — userId: 10, role: ROLE_APPLICANT
[application-service]  [abc123,002] INFO  POST /applications/1/submit — userId: 10
[application-service]  [abc123,002] INFO  Application submitted: id=1, status=Submitted
[application-service]  [abc123,002] INFO  📤 Publishing APPLICATION_SUBMITTED event
[admin-service]        [abc123,003] INFO  📥 Received APPLICATION_SUBMITTED event
```

**All lines share `abc123`** — grep across all services:

```bash
# In Docker:
docker-compose logs | grep "abc123"

# In log files:
grep "abc123" logs/*.log
```

---

## Part 4: Log Level Strategy

| Level | When Used | Example |
|-------|----------|---------|
| **`DEBUG`** | Verbose details, only for dev | `"Found 5 applications for userId: 10"` |
| **`INFO`** | Normal operations | `"User registered successfully"` |
| **`WARN`** | Expected failures | `"Login failed — invalid password"` |
| **`ERROR`** | Unexpected failures + stack trace | `"Database connection refused"` |

### Where Each Level Is Used

| Location | INFO | WARN | ERROR |
|----------|------|------|-------|
| **Controllers** | Endpoint hit | — | — |
| **Services** | Success operations | Auth failures, not-found | — |
| **Exception Handlers** | — | Validation errors | Unhandled exceptions |
| **Gateway Filter** | Request routing, JWT success | Missing auth header | JWT validation failure |
| **RabbitMQ** | Event publish/consume | — | Event processing failure |

---

## Part 5: Debugging Scenarios

### "Why did login fail?"
```bash
grep "Login" logs/auth-service.log
```
```
INFO  Login attempt for email: deepak@capg.com
WARN  Login failed — invalid password for: deepak@capg.com
```

### "Which service is slow?"
Look at timestamps + Zipkin traceId:
```
16:52:00.100 [api-gateway]          [abc123,001] Incoming request
16:52:00.150 [application-service]  [abc123,002] Submitting application
16:52:04.900 [application-service]  [abc123,002] Application submitted  ← 4.75s gap = slow DB
```

### "Why can't admin see applications?"
```bash
grep "application-service" logs/admin-service.log
```
```
INFO  Fetching all applications from application-service: http://application-service:8082
ERROR Failed to fetch applications from application-service: Connection refused
```
→ Application service is down!
