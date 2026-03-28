# Zipkin Distributed Tracing — Complete Walkthrough

## What Problem Does Zipkin Solve?

In a monolith, a single request = one log file. Easy to debug.

In microservices, a single user action (e.g., "submit a loan application") touches **multiple services**:

```
User → API Gateway → Application Service → RabbitMQ → Admin Service
```

Without Zipkin, if something is slow or fails, you'd need to search logs across **5 different services** with no way to connect them. Zipkin solves this by assigning a **single Trace ID** to the entire request chain.

---

## Core Concepts

| Concept | Definition | Example |
|---------|-----------|---------|
| **Trace** | The entire journey of a request across all services | One full loan submission flow |
| **Span** | A single unit of work within a trace | "auth-service: validate JWT" |
| **Trace ID** | Unique ID shared across ALL spans in a trace | `6f3b8c2d4e5a1b0f` |
| **Span ID** | Unique ID for each individual span | `a9c3d2e1f0b5a4c7` |
| **Parent Span** | The span that triggered the current span | Gateway span is parent of auth-service span |

---

## Architecture in FinFlow

```
┌─────────────────────────────────────────────────────────┐
│                    Zipkin Server (:9411)                  │
│              Collects and visualizes traces               │
└──────────▲──────▲──────▲──────▲──────▲──────────────────┘
           │      │      │      │      │
     sends traces via HTTP POST /api/v2/spans
           │      │      │      │      │
      ┌────┴─┐ ┌──┴──┐ ┌─┴──┐ ┌┴───┐ ┌┴────┐
      │ Gate │ │Auth │ │App │ │Doc │ │Admin│
      │ way  │ │Svc  │ │Svc │ │Svc │ │Svc  │
      └──────┘ └─────┘ └────┘ └────┘ └─────┘
```

Each service independently sends its spans to Zipkin. Zipkin correlates them using the shared **Trace ID**.

---

## What We Added — File by File

### 1. Dependencies (`pom.xml` × 5 services)

```xml
<!-- Exposes /actuator endpoints (health, metrics) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Brave tracer — creates spans, propagates trace context -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>

<!-- Sends spans to Zipkin server over HTTP -->
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

**How it works:** Spring Boot auto-configures tracing when it detects these on the classpath — zero Java code needed!

### 2. Configuration (`application.yml` × 5 services)

```yaml
management:
  tracing:
    sampling:
      probability: 1.0    # ← Sample 100% of requests (use 0.1 for prod)
  zipkin:
    tracing:
      endpoint: ${ZIPKIN_URL:http://localhost:9411/api/v2/spans}

logging:
  pattern:
    correlation: '[${spring.application.name:},%X{traceId:-},%X{spanId:-}] '
```

| Property | What It Does |
|----------|-------------|
| `sampling.probability: 1.0` | Traces 100% of requests. In production, set to `0.1` (10%) to reduce overhead |
| `zipkin.tracing.endpoint` | URL where spans are sent via HTTP POST |
| `logging.pattern.correlation` | Adds `[service-name,traceId,spanId]` to every log line |

### 3. Docker Container (`docker-compose.yml`)

```yaml
zipkin:
  image: openzipkin/zipkin    # Official Zipkin Docker image
  container_name: zipkin
  ports:
    - "9411:9411"             # Zipkin UI + API
```

Plus `ZIPKIN_URL=http://zipkin:9411/api/v2/spans` in every service's environment.

---

## How a Trace Flows — Step by Step

### Example: User submits a loan application

```
Step 1: POST http://localhost:8080/applications/1/submit
        ↓
Step 2: API Gateway receives request
        → Creates ROOT SPAN (traceId=abc123, spanId=001)
        → Adds headers: X-B3-TraceId, X-B3-SpanId to downstream request
        ↓
Step 3: Application Service receives request
        → Reads trace headers from request
        → Creates CHILD SPAN (traceId=abc123, spanId=002, parentId=001)
        → Updates application status to "Submitted"
        → Publishes RabbitMQ event (APPLICATION_SUBMITTED)
        → Sends span to Zipkin
        ↓
Step 4: Admin Service consumes RabbitMQ message
        → Trace context is propagated through AMQP headers
        → Creates CHILD SPAN (traceId=abc123, spanId=003, parentId=002)
        → Logs "New application submitted"
        → Sends span to Zipkin
        ↓
Step 5: Zipkin Server
        → Receives spans 001, 002, 003
        → Links them via traceId=abc123
        → Builds visual timeline
```

### What you see in the logs:

```
[api-gateway,abc123,001]          Routing to application-service
[application-service,abc123,002]  Application 1 submitted
[application-service,abc123,002]  📤 Publishing APPLICATION_SUBMITTED event
[admin-service,abc123,003]        📥 Received APPLICATION_SUBMITTED event
```

All log lines share `abc123` — you can grep for it across all services!

---

## Reading the Zipkin UI

### Access: `http://localhost:9411`

### Main Page — Search Traces
1. Select **Service Name** dropdown → pick any service
2. Click **Run Query** → see all recent traces
3. Each row = one trace, showing:
   - Total duration
   - Number of spans
   - Services involved

### Trace Detail Page — Timeline View

When you click a trace, you see a **waterfall diagram**:

```
├── api-gateway              [──────────────── 150ms ────────────────]
│   └── application-service  [───── 80ms ─────]
│       └── MySQL query       [── 5ms ──]
│       └── RabbitMQ publish   [── 2ms ──]
│           └── admin-service  [──── 30ms ────] (async)
```

Each bar shows:
- **Service name** on the left
- **Duration** as the bar width
- **Start time** relative to the root span
- Click any span → see **tags** (HTTP method, URL, status code, etc.)

### Dependencies Page
Shows a directed graph of which services call which:

```
api-gateway → auth-service
api-gateway → application-service
api-gateway → admin-service
application-service → RabbitMQ → admin-service
```

---

## Real-World Debugging Scenarios

### Scenario 1: "The submit API is slow"
1. Open Zipkin → search for traces on `api-gateway`
2. Find a slow trace (e.g., 5000ms)
3. Click it → see which child span is the bottleneck
4. If `application-service` MySQL query = 4800ms → **database issue**

### Scenario 2: "Admin doesn't see new applications"
1. Search for traces on `application-service`
2. Find a submit trace → check if RabbitMQ publish span exists
3. If missing → event wasn't published (code bug)
4. If present → check admin-service consumer span
5. If admin-service span missing → consumer not running or queue misconfigured

### Scenario 3: "Random 500 errors"
1. Search Zipkin for traces with `error=true` tag
2. Click the failing span → see error message and stack trace
3. Trace ID in Zipkin → grep in Docker logs for full context

---

## Port Reference

| Service | Port | URL |
|---------|------|-----|
| **Zipkin UI** | 9411 | `http://localhost:9411` |
| RabbitMQ UI | 15672 | `http://localhost:15672` |
| Eureka Dashboard | 8761 | `http://localhost:8761` |
| API Gateway | 8080 | `http://localhost:8080` |
| Swagger UI | 8080 | `http://localhost:8080/swagger-ui.html` |
