# FinFlow — Step-by-Step Testing Guide

---

## Step 1: Build & Start Everything with Docker

Open a terminal in the project root folder (`FinFlow-Microservices`) and run:

```bash
docker-compose down          # Stop any old containers
docker-compose up --build -d # Build fresh images and start
```

Wait **~60 seconds** for all containers to fully boot up. You can monitor with:
```bash
docker ps
```
You should see **7 containers** all showing `Up`:
| Container | Port |
|---|---|
| mysql-db | 3306 |
| eureka-server | 8761 |
| api-gateway | 8080 |
| auth-service | (internal) |
| application-service | (internal) |
| document-service | (internal) |
| admin-service | (internal) |

---

## Step 2: Verify Eureka Dashboard

Open your browser and go to: **http://localhost:8761**

You should see all 5 services registered:
- `API-GATEWAY`
- `AUTH-SERVICE`
- `APPLICATION-SERVICE`
- `DOCUMENT-SERVICE`
- `ADMIN-SERVICE`

> [!IMPORTANT]
> If any service is missing, check its Docker logs: `docker logs <container-name>`

---

## Step 3: Open Postman & Test APIs

All requests go through the **API Gateway** at `http://localhost:8080`.

You can use the existing `FinFlow_Postman_Collection.json` file, but here are the exact requests to test manually:

---

### 3.1 — Signup (Register a new user)

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `http://localhost:8080/auth/signup` |
| **Headers** | `Content-Type: application/json` |

**Body (raw JSON):**
```json
{
    "name": "Deepak Kumar",
    "email": "deepak@capg.com",
    "password": "Deepak@123",
    "role": "ROLE_APPLICANT"
}
```

**Expected Response:** `200 OK` — `"User registered successfully"`

---

### 3.2 — Login (Get JWT Token)

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `http://localhost:8080/auth/login` |
| **Headers** | `Content-Type: application/json` |

**Body (raw JSON):**
```json
{
    "email": "deepak@capg.com",
    "password": "Deepak@123"
}
```

**Expected Response:** `200 OK`
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

> [!TIP]
> **Copy the `token` value!** You will use it as a `Bearer Token` in all subsequent requests.

---

### 3.3 — Create a Loan Application

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `http://localhost:8080/applications` |

**Headers:**
| Key | Value |
|---|---|
| `Content-Type` | `application/json` |
| `Authorization` | `Bearer <paste_your_token_here>` |

**Body (raw JSON):**
```json
{
    "personalDetails": "Deepak Kumar, Age 28, Mumbai",
    "employmentDetails": "Software Engineer at Capgemini, 5 years experience",
    "loanDetails": "Home Loan, Rs. 50,00,000, 20 years tenure"
}
```

**Expected Response:** `200 OK` — Returns the created `LoanApplication` object with an `id`.

---

### 3.4 — Get My Applications

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `http://localhost:8080/applications/my` |
| `Authorization` | `Bearer <your_token>` |

**Expected Response:** `200 OK` — Array of your loan applications.

---

### 3.5 — Submit Application

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `http://localhost:8080/applications/1/submit` |
| `Authorization` | `Bearer <your_token>` |

**Expected Response:** `200 OK` — `"Application submitted successfully"`

---

### 3.6 — Upload a Document

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `http://localhost:8080/documents/upload` |
| `Authorization` | `Bearer <your_token>` |

**Body type:** `form-data` (NOT raw JSON)

| Key | Type | Value |
|---|---|---|
| `applicationId` | Text | `1` |
| `type` | Text | `AADHAAR` |
| `file` | File | (select any PDF/image file) |

**Expected Response:** `200 OK` — Returns the saved `Document` object.

---

### 3.7 — Admin: Register & Login as Admin

Repeat **Step 3.1** with:
```json
{
    "name": "Admin User",
    "email": "admin@capg.com",
    "password": "Admin@123",
    "role": "ROLE_ADMIN"
}
```
Then **login** with `admin@capg.com` / `Admin@123` and copy the new admin token.

---

### 3.8 — Admin: View All Applications

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `http://localhost:8080/admin/applications` |
| `Authorization` | `Bearer <admin_token>` |

---

### 3.9 — Admin: Make Decision on Application

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `http://localhost:8080/admin/applications/1/decision?decision=APPROVED&remarks=All+documents+verified` |
| `Authorization` | `Bearer <admin_token>` |

---

## Troubleshooting

| Problem | Solution |
|---|---|
| `Connection refused` on 8080 | Wait 60s, check `docker ps` for api-gateway status |
| `401 Unauthorized` | Token expired — login again to get a fresh token |
| `503 Service Unavailable` | Service not yet registered in Eureka — wait and retry |
| Missing services in Eureka | Run `docker logs <service-name>` to check for errors |
