# 📄 Document Service

> Handles document upload, storage, and verification for loan applications in the FinFlow system.

![Port](https://img.shields.io/badge/Port-8083-blue?style=flat-square)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Producer-orange?style=flat-square)
![Upload](https://img.shields.io/badge/Max%20File%20Size-10MB-green?style=flat-square)

---

## 📋 Overview

The Document Service manages the document lifecycle for loan applications. Applicants upload supporting documents (ID proof, salary slips, etc.) which are stored on the filesystem. Admins can then verify or reject these documents.

### Key Responsibilities
- File upload to local filesystem with UUID-based naming
- Document metadata storage in MySQL
- Document verification (VERIFIED / REJECTED) by admin
- Publishing DOCUMENT_UPLOADED events to RabbitMQ

---

## 🔄 Document Lifecycle

```
  ┌──────────┐     admin verifies     ┌───────────┐
  │ PENDING  │ ──────────────────────▶│ VERIFIED  │
  └──────────┘                        └───────────┘
      ▲                                     or
      │ upload                        ┌───────────┐
      │                               │ REJECTED  │
                                      └───────────┘
```

---

## 📡 API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|:----:|
| `POST` | `/documents/upload` | Upload a document file | ✅ |
| `PUT` | `/documents/{id}/verify` | Verify or reject a document | ✅ |

### Request Examples

#### Upload Document
```bash
# Multipart form-data request
curl -X POST http://localhost:8083/documents/upload \
  -F "applicationId=1" \
  -F "type=ID_PROOF" \
  -F "file=@/path/to/passport.pdf"
```

#### Verify Document
```bash
curl -X PUT "http://localhost:8083/documents/1/verify?status=VERIFIED"
```

---

## 📨 RabbitMQ Events

### Produces
| Event | Routing Key | When |
|-------|-------------|------|
| `DOCUMENT_UPLOADED` | `document.uploaded` | After a document is uploaded and saved |

**Event Payload:**
```json
{
  "documentId": 1,
  "applicationId": 100,
  "type": "ID_PROOF",
  "status": "PENDING"
}
```

---

## 🗃 Database

**Database:** `finflow_doc` (auto-created)

### Documents Table
| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | Primary Key, Auto Increment |
| `application_id` | BIGINT | Associated loan application |
| `type` | VARCHAR | ID_PROOF, SALARY_SLIP, ADDRESS_PROOF |
| `file_path` | VARCHAR | Local filesystem path |
| `status` | VARCHAR | PENDING / VERIFIED / REJECTED |

---

## 📁 File Storage

Files are stored locally in the `uploads/` directory with UUID prefix to prevent naming conflicts:

```
uploads/
├── a3b2c1d4-5e6f-7890-abcd-ef1234567890_passport.pdf
├── b4c3d2e1-6f7a-8901-bcde-f12345678901_salary_slip.pdf
└── c5d4e3f2-7a8b-9012-cdef-012345678912_address_proof.jpg
```

**Max file size:** 10MB (configurable in `application.yml`)

---

## 📂 Project Structure

```
document-service/
├── src/main/java/com/capg/documentservice/
│   ├── config/
│   │   └── RabbitMQConfig.java           # Exchange, queues, bindings
│   ├── controller/
│   │   └── DocumentController.java       # REST endpoints
│   ├── entity/
│   │   └── Document.java                 # JPA entity
│   ├── event/
│   │   ├── DocumentEvent.java            # Event DTO
│   │   └── DocumentEventProducer.java    # Publishes to RabbitMQ
│   ├── exception/
│   │   └── GlobalExceptionHandler.java   # Error handling
│   ├── repository/
│   │   └── DocumentRepository.java       # JPA repository
│   └── service/
│       └── DocumentService.java          # Business logic + file I/O
├── src/main/resources/
│   ├── application.yml                   # Configuration
│   └── logback-spring.xml                # Logging configuration
├── src/test/java/com/capg/documentservice/
│   ├── controller/
│   │   └── DocumentControllerTest.java   # 2 tests
│   └── service/
│       └── DocumentServiceTest.java      # 4 tests
└── pom.xml
```

---

## 🧪 Tests

```bash
mvn test -f pom.xml
```

| Test Class | Tests | Type |
|-----------|-------|------|
| `DocumentServiceTest` | 4 | Unit (Mockito) |
| `DocumentControllerTest` | 2 | Controller (MockMvc) |

---

## ⚙️ Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:mysql://localhost:3306/finflow_doc` | MySQL connection URL |
| `DB_USER` | `root` | Database username |
| `DB_PASSWORD` | `password` | Database password |
| `RABBITMQ_HOST` | `localhost` | RabbitMQ server host |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | Eureka server URL |
| `ZIPKIN_URL` | `http://localhost:9411/api/v2/spans` | Zipkin collector URL |
