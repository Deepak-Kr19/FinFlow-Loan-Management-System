# 🔍 Eureka Server

> Service discovery and registration server for all FinFlow microservices.

![Port](https://img.shields.io/badge/Port-8761-blue?style=flat-square)
![Netflix Eureka](https://img.shields.io/badge/Netflix-Eureka-red?style=flat-square)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2024.0.0-brightgreen?style=flat-square)

---

## 📋 Overview

The Eureka Server acts as the **service registry** for the FinFlow microservices ecosystem. All services register themselves with Eureka at startup, and the API Gateway uses Eureka to discover and route requests to available service instances.

### Key Responsibilities
- Maintain a registry of all running service instances
- Provide service discovery for the API Gateway's load-balanced routing
- Health monitoring of registered services
- Dashboard for visualizing registered services

---

## 🏗 How It Works

```
  ┌──────────────┐
  │ Eureka Server│ ◀── All services register on startup
  │   (:8761)    │
  └──────▲───────┘
         │
         │  Service discovery queries
         │
  ┌──────┴──────┐
  │ API Gateway │ ── "Where is auth-service?" ── Eureka responds with host:port
  └─────────────┘
```

### Registered Services

| Service Name | Default Instance |
|-------------|-----------------|
| `auth-service` | `auth-service:8081` |
| `application-service` | `application-service:8082` |
| `document-service` | `document-service:8083` |
| `admin-service` | `admin-service:8084` |
| `api-gateway` | `api-gateway:8080` |

---

## 🖥 Dashboard

Access the Eureka Dashboard to view all registered services:

**URL:** `http://localhost:8761`

The dashboard shows:
- Registered application instances
- Service status (UP/DOWN)
- Instance details (host, port)
- Lease renewal information

---

## 📂 Project Structure

```
eureka-server/
├── src/main/java/com/capg/eurekaserver/
│   └── EurekaServerApplication.java     # @EnableEurekaServer
├── src/main/resources/
│   └── application.yml                  # Server configuration
└── pom.xml
```

---

## ⚙️ Configuration

```yaml
server:
  port: 8761

eureka:
  client:
    register-with-eureka: false    # Don't register itself
    fetch-registry: false          # Don't fetch registry (it IS the registry)
```

---

## 🚀 Running

```bash
# Local
mvn spring-boot:run

# Docker (via docker-compose)
docker-compose up eureka-server
```

Eureka Server must be started **before** all other services, as they register with it on startup.
