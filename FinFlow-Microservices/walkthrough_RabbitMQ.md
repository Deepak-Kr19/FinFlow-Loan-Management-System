## 🐇 RabbitMQ Architecture in FinFlow

### Core Concepts

| Concept | What It Is | In Your Project |
|---------|-----------|-----------------|
| **Exchange** | A router that receives messages and decides which queue to send them to | `finflow.exchange` (Topic type) |
| **Queue** | A buffer that stores messages until a consumer picks them up | 3 queues (see below) |
| **Routing Key** | A label attached to each message — the exchange uses it to route to the right queue | `application.submitted`, `decision.made`, `document.uploaded` |
| **Producer** | Service that **sends** a message | The service where the action happens |
| **Consumer** | Service that **receives** and processes the message | The service that needs to react |

### Topology

```
                         finflow.exchange (Topic)
                                │
                ┌───────────────┼───────────────┐
                │               │               │
          routing key:    routing key:     routing key:
      application.submitted  decision.made  document.uploaded
                │               │               │
                ▼               ▼               ▼
  ┌─────────────────┐ ┌──────────────┐ ┌──────────────────┐
  │ queue.application│ │queue.decision│ │queue.document   │
  │ .submitted       │ │.made         │ │.uploaded        │
  └────────┬────────┘ └──────┬───────┘ └────────┬─────────┘
           │                 │                   │
           ▼                 ▼                   ▼
     admin-service     application-service  application-service
     (Consumer)        (Consumer)           (Consumer)
```

---

## 3 Event Flows — Step by Step

### Flow 1: `APPLICATION_SUBMITTED`

```
User clicks "Submit" in Postman
         │
         ▼
POST /applications/1/submit  (via API Gateway)
         │
         ▼
┌─ application-service ─────────────────────────────────┐
│  LoanApplicationService.submitApplication()           │
│    1. Sets status = "Submitted"                       │
│    2. Saves to MySQL                                  │
│    3. Creates ApplicationEvent                        │
│    4. eventProducer.publishApplicationSubmitted()  ───┼──► RabbitMQ
└───────────────────────────────────────────────────────┘
                                                             │
         finflow.exchange routes via "application.submitted" │
                                                             ▼
┌─ admin-service ────────────────────────────────────────┐
│  ApplicationEventConsumer.handleApplicationSubmitted() │
│    📥 Logs: "New loan application submitted"           │
│    (Admin gets notified of new submission)             │
└────────────────────────────────────────────────────────┘
```

### Flow 2: `DECISION_MADE` ⭐ (Most Important)

```
Admin approves/rejects in Postman
         │
         ▼
POST /admin/applications/1/decision?decision=APPROVED  (via API Gateway)
         │
         ▼
┌─ admin-service ───────────────────────────────────────┐
│  AdminService.makeDecision()                          │
│    1. Creates Decision entity (APPROVED)              │
│    2. Saves to MySQL (decisions table)                │
│    3. Creates DecisionEvent                           │
│    4. eventProducer.publishDecisionMade()  ───────────┼──► RabbitMQ
└───────────────────────────────────────────────────────┘
                                                            │
           finflow.exchange routes via "decision.made"      │
                                                            ▼
┌─ application-service ──────────────────────────────────┐
│  ApplicationEventConsumer.handleDecisionMade()         │
│    📥 Receives: {applicationId: 1, decision: APPROVED} │
│    1. Finds LoanApplication by ID                      │
│    2. Sets status = "APPROVED"                         │
│    3. Saves to MySQL                                   │
│    ✅ Application status auto-updated!                │
└────────────────────────────────────────────────────────┘
```

**This is the magic** — the admin-service doesn't need to call application-service via REST to update the status. It just publishes an event, and application-service updates itself!

### Flow 3: `DOCUMENT_UPLOADED`

```
User uploads a document in Postman
         │
         ▼
POST /documents/upload  (via API Gateway)
         │
         ▼
┌─ document-service ────────────────────────────────────┐
│  DocumentService.uploadDocument()                     │
│    1. Saves file to disk                              │
│    2. Creates Document entity (status: PENDING)       │
│    3. Saves to MySQL                                  │
│    4. eventProducer.publishDocumentUploaded()  ───────┼──► RabbitMQ
└───────────────────────────────────────────────────────┘
                                                            │
          finflow.exchange routes via "document.uploaded"   │
                                                            ▼
┌─ application-service ──────────────────────────────────┐
│  ApplicationEventConsumer.handleDocumentUploaded()     │
│    📥 Logs: "Document uploaded for application 1"      │
│    (Application service is notified)                   │
└────────────────────────────────────────────────────────┘
```

---

## Why RabbitMQ Instead of REST?

| Without RabbitMQ | With RabbitMQ |
|------------------|---------------|
| Admin-service must call `PUT /applications/1/status` via REST | Admin-service just publishes an event |
| If application-service is **down**, the update **fails** | Message stays in the queue until application-service comes **back up** |
| Services are **tightly coupled** | Services are **completely decoupled** |
| Synchronous — admin waits for response | Asynchronous — admin gets instant response |

## How to Verify It's Working

1. **RabbitMQ Dashboard:** `http://localhost:15672` (guest/guest) — you can see queues, message counts, and consumer connections
2. **Docker logs:** `docker-compose logs -f application-service` — watch for 📤📥 emojis in real-time
3. **The ultimate test:** Approve an application, then check its status — it should change from "Submitted" to "APPROVED" automatically!



## 🐇 RabbitMQ Architecture in FinFlow

### Core Concepts

| Concept | What It Is | In Your Project |
|---------|-----------|-----------------|
| **Exchange** | A router that receives messages and decides which queue to send them to | `finflow.exchange` (Topic type) |
| **Queue** | A buffer that stores messages until a consumer picks them up | 3 queues (see below) |
| **Routing Key** | A label attached to each message — the exchange uses it to route to the right queue | `application.submitted`, `decision.made`, `document.uploaded` |
| **Producer** | Service that **sends** a message | The service where the action happens |
| **Consumer** | Service that **receives** and processes the message | The service that needs to react |

### Topology

```
                         finflow.exchange (Topic)
                                │
                ┌───────────────┼───────────────┐
                │               │               │
          routing key:    routing key:     routing key:
      application.submitted  decision.made  document.uploaded
                │               │               │
                ▼               ▼               ▼
  ┌─────────────────┐ ┌──────────────┐ ┌───────────────v──┐
  │ queue.application│ │queue.decision│ │queue.document   │
  │ .submitted       │ │.made         │ │.uploaded        │
  └────────┬────────┘ └──────┬───────┘ └────────┬─────────┘
           │                 │                   │
           ▼                 ▼                   ▼
     admin-service     application-service  application-service
     (Consumer)        (Consumer)           (Consumer)
```

---

## 3 Event Flows — Step by Step

### Flow 1: `APPLICATION_SUBMITTED`

```
User clicks "Submit" in Postman
         │
         ▼
POST /applications/1/submit  (via API Gateway)
         │
         ▼
┌─ application-service ─────────────────────────────────┐
│  LoanApplicationService.submitApplication()           │
│    1. Sets status = "Submitted"                       │
│    2. Saves to MySQL                                  │
│    3. Creates ApplicationEvent                        │
│    4. eventProducer.publishApplicationSubmitted()  ───┼──► RabbitMQ
└───────────────────────────────────────────────────────┘
                                                             │
         finflow.exchange routes via "application.submitted" │
                                                             ▼
┌─ admin-service ────────────────────────────────────────┐
│  ApplicationEventConsumer.handleApplicationSubmitted() │
│    📥 Logs: "New loan application submitted"           │
│    (Admin gets notified of new submission)             │
└────────────────────────────────────────────────────────┘
```

### Flow 2: `DECISION_MADE` ⭐ (Most Important)

```
Admin approves/rejects in Postman
         │
         ▼
POST /admin/applications/1/decision?decision=APPROVED  (via API Gateway)
         │
         ▼
┌─ admin-service ───────────────────────────────────────┐
│  AdminService.makeDecision()                          │
│    1. Creates Decision entity (APPROVED)              │
│    2. Saves to MySQL (decisions table)                │
│    3. Creates DecisionEvent                           │
│    4. eventProducer.publishDecisionMade()  ───────────┼──► RabbitMQ
└───────────────────────────────────────────────────────┘
                                                            │
           finflow.exchange routes via "decision.made"      │
                                                            ▼
┌─ application-service ──────────────────────────────────┐
│  ApplicationEventConsumer.handleDecisionMade()         │
│    📥 Receives: {applicationId: 1, decision: APPROVED} │
│    1. Finds LoanApplication by ID                      │
│    2. Sets status = "APPROVED"                         │
│    3. Saves to MySQL                                   │
│    ✅ Application status auto-updated!                │
└────────────────────────────────────────────────────────┘
```

**This is the magic** — the admin-service doesn't need to call application-service via REST to update the status. It just publishes an event, and application-service updates itself!

### Flow 3: `DOCUMENT_UPLOADED`

```
User uploads a document in Postman
         │
         ▼
POST /documents/upload  (via API Gateway)
         │
         ▼
┌─ document-service ────────────────────────────────────┐
│  DocumentService.uploadDocument()                     │
│    1. Saves file to disk                              │
│    2. Creates Document entity (status: PENDING)       │
│    3. Saves to MySQL                                  │
│    4. eventProducer.publishDocumentUploaded()  ───────┼──► RabbitMQ
└───────────────────────────────────────────────────────┘
                                                            │
          finflow.exchange routes via "document.uploaded"     │
                                                            ▼
┌─ application-service ─────────────────────────────────┐
│  ApplicationEventConsumer.handleDocumentUploaded()    │
│    📥 Logs: "Document uploaded for application 1"     │
│    (Application service is notified)                  │
└───────────────────────────────────────────────────────┘
```

---

## Why RabbitMQ Instead of REST?

| Without RabbitMQ | With RabbitMQ |
|------------------|---------------|
| Admin-service must call `PUT /applications/1/status` via REST | Admin-service just publishes an event |
| If application-service is **down**, the update **fails** | Message stays in the queue until application-service comes **back up** |
| Services are **tightly coupled** | Services are **completely decoupled** |
| Synchronous — admin waits for response | Asynchronous — admin gets instant response |

## How to Verify It's Working

1. **RabbitMQ Dashboard:** `http://localhost:15672` (guest/guest) — you can see queues, message counts, and consumer connections
2. **Docker logs:** `docker-compose logs -f application-service` — watch for 📤📥 emojis in real-time
3. **The ultimate test:** Approve an application, then check its status — it should change from "Submitted" to "APPROVED" automatically!
