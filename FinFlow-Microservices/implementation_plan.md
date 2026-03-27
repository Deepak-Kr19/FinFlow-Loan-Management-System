# RabbitMQ Integration — FinFlow Microservices

## Goal
Add asynchronous event-driven messaging via **RabbitMQ** to decouple inter-service communication. Instead of direct REST calls for notifications, services publish events that other services consume.

## Event Flows

```
┌──────────────┐     APPLICATION_SUBMITTED      ┌──────────────┐
│  Application │ ──────────────────────────────▶ │    Admin      │
│   Service    │                                 │   Service     │
│   (8082)     │ ◀────────────────────────────── │   (8084)      │
└──────────────┘     DECISION_MADE               └──────────────┘
       ▲
       │  DOCUMENT_UPLOADED
       │
┌──────────────┐
│  Document    │
│   Service    │
│   (8083)     │
└──────────────┘
```

| Event | Producer | Consumer | When |
|-------|----------|----------|------|
| `APPLICATION_SUBMITTED` | application-service | admin-service | Loan application is submitted |
| `DECISION_MADE` | admin-service | application-service | Admin approves/rejects |
| `DOCUMENT_UPLOADED` | document-service | application-service | Document is uploaded for an application |

## Proposed Changes

### Infrastructure

#### [MODIFY] [docker-compose.yml](file:///c:/Capgemini-Training/All-Capg-Workspaces/Capg-Sprint1-WorkSpace/FinFlow-Microservices/docker-compose.yml)
- Add `rabbitmq:3-management` container (ports 5672 + 15672 for management UI)
- Add `RABBITMQ_HOST` env var to application-service, document-service, admin-service

---

### Application Service (Producer + Consumer)

#### [MODIFY] [pom.xml](file:///c:/Capgemini-Training/All-Capg-Workspaces/Capg-Sprint1-WorkSpace/FinFlow-Microservices/application-service/pom.xml)
- Add `spring-boot-starter-amqp`

#### [NEW] ApplicationEvent.java
- DTO: `applicationId`, `userId`, `status`, `eventType`

#### [NEW] RabbitMQConfig.java
- Declare exchange, queues, bindings

#### [NEW] ApplicationEventProducer.java
- Publishes `APPLICATION_SUBMITTED` when application is submitted

#### [NEW] ApplicationEventConsumer.java
- Listens for `DECISION_MADE` → updates application status (Approved/Rejected)
- Listens for `DOCUMENT_UPLOADED` → logs document upload notification

#### [MODIFY] [LoanApplicationService.java](file:///c:/Capgemini-Training/All-Capg-Workspaces/Capg-Sprint1-WorkSpace/FinFlow-Microservices/application-service/src/main/java/com/capg/applicationservice/service/LoanApplicationService.java)
- Inject producer, publish event in `submitApplication()`

#### [MODIFY] [application.yml](file:///c:/Capgemini-Training/All-Capg-Workspaces/Capg-Sprint1-WorkSpace/FinFlow-Microservices/application-service/src/main/resources/application.yml)
- Add `spring.rabbitmq.host/port/username/password`

---

### Admin Service (Producer + Consumer)

#### [MODIFY] [pom.xml](file:///c:/Capgemini-Training/All-Capg-Workspaces/Capg-Sprint1-WorkSpace/FinFlow-Microservices/admin-service/pom.xml)
- Add `spring-boot-starter-amqp`

#### [NEW] DecisionEvent.java
- DTO: `applicationId`, `decision`, `remarks`

#### [NEW] RabbitMQConfig.java

#### [NEW] DecisionEventProducer.java
- Publishes `DECISION_MADE` when admin approves/rejects

#### [NEW] ApplicationEventConsumer.java
- Listens for `APPLICATION_SUBMITTED` → logs new submission notification

#### [MODIFY] [AdminService.java](file:///c:/Capgemini-Training/All-Capg-Workspaces/Capg-Sprint1-WorkSpace/FinFlow-Microservices/admin-service/src/main/java/com/capg/adminservice/service/AdminService.java)
- Inject producer, publish event in `makeDecision()`

#### [MODIFY] [application.yml](file:///c:/Capgemini-Training/All-Capg-Workspaces/Capg-Sprint1-WorkSpace/FinFlow-Microservices/admin-service/src/main/resources/application.yml)
- Add RabbitMQ connection properties

---

### Document Service (Producer)

#### [MODIFY] [pom.xml](file:///c:/Capgemini-Training/All-Capg-Workspaces/Capg-Sprint1-WorkSpace/FinFlow-Microservices/document-service/pom.xml)
- Add `spring-boot-starter-amqp`

#### [NEW] DocumentEvent.java
- DTO: `documentId`, `applicationId`, `type`, `status`

#### [NEW] RabbitMQConfig.java

#### [NEW] DocumentEventProducer.java
- Publishes `DOCUMENT_UPLOADED` when a document is uploaded

#### [MODIFY] [DocumentService.java](file:///c:/Capgemini-Training/All-Capg-Workspaces/Capg-Sprint1-WorkSpace/FinFlow-Microservices/document-service/src/main/java/com/capg/documentservice/service/DocumentService.java)
- Inject producer, publish event in `uploadDocument()`

#### [MODIFY] [application.yml](file:///c:/Capgemini-Training/All-Capg-Workspaces/Capg-Sprint1-WorkSpace/FinFlow-Microservices/document-service/src/main/resources/application.yml)
- Add RabbitMQ connection properties

## RabbitMQ Topology

| Exchange | Type | Queues | Routing Key |
|----------|------|--------|-------------|
| `finflow.exchange` | Topic | `queue.application.submitted` | `application.submitted` |
| | | `queue.decision.made` | `decision.made` |
| | | `queue.document.uploaded` | `document.uploaded` |

## Verification Plan

1. `docker-compose up --build -d` — verify RabbitMQ starts with management UI at `http://localhost:15672` (guest/guest)
2. Submit a loan application → check RabbitMQ management UI for message in `queue.application.submitted`
3. Make an admin decision → check application status updated via `DECISION_MADE` event
4. Upload a document → verify `DOCUMENT_UPLOADED` event published
5. Check Docker logs for consumer log messages
