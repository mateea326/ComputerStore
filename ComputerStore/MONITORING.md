# 🖥️ ComputerStore — Monitoring & API Gateway Guide

## Arhitectura completă

```
Browser / Client
       │
       ▼
┌─────────────────┐
│   API Gateway   │  :8080  (Spring Cloud Gateway)
│  Rate Limiting  │  Routes, Filters, CORS
│  Logging Filter │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
Store Svc  User Svc
  :8081      :8082
    │         │
    └────┬────┘
         │
    ┌────┴──────────────┐
    │  Config Server    │  :8888
    │  Discovery Server │  :8761 (Eureka)
    └───────────────────┘

Monitoring Stack (Docker):
    Prometheus :9090  ← scrape /actuator/prometheus
    Grafana    :3000  ← dashboards
    Zipkin     :9411  ← distributed tracing
```

---

## 🚀 Pornire completă

### 1. Pornire Monitoring Stack (Docker)
```bash
cd ComputerStore/
docker compose -f docker-compose-monitoring.yml up -d
```

### 2. Pornire microservicii (ordine corectă)
```bash
# Terminal 1 — Config Server
cd config-server && ../mvnw.cmd spring-boot:run

# Terminal 2 — Discovery Server (Eureka)
cd discovery-server && ../mvnw.cmd spring-boot:run

# Terminal 3 — API Gateway
cd api-gateway && ../mvnw.cmd spring-boot:run

# Terminal 4 — Store Service
cd store-service && ../mvnw.cmd spring-boot:run

# Terminal 5 — User Service
cd user-service && ../mvnw.cmd spring-boot:run
```

---

## 🔗 Endpoint-uri utile

| Endpoint | Descriere |
|---|---|
| `http://localhost:8080/api/store/...` | Store Service via Gateway |
| `http://localhost:8080/api/user/...` | User Service via Gateway |
| `http://localhost:8080/actuator/health` | Gateway health |
| `http://localhost:8081/actuator/health` | Store health (direct) |
| `http://localhost:8081/actuator/prometheus` | Store metrics raw |
| `http://localhost:8082/actuator/prometheus` | User metrics raw |
| `http://localhost:8761` | Eureka Dashboard |
| `http://localhost:9090` | Prometheus UI |
| `http://localhost:3000` | **Grafana Dashboard** (admin/admin123) |
| `http://localhost:9411` | Zipkin Tracing |

---

## 📊 Grafana Dashboard

Dashboard-ul **ComputerStore — Microservices Dashboard** se încarcă automat și conține:

1. **Health & Status** — UP/DOWN per serviciu
2. **CPU Usage** — per serviciu în timp real
3. **JVM Memory** — Heap + Non-Heap (MB)
4. **JVM Threads** — Live + Daemon threads
5. **HTTP Request Rate** — req/s per serviciu
6. **Latency Percentiles** — p50, p95, p99 (ms)
7. **HTTP Errors** — 4xx + 5xx per serviciu
8. **API Gateway Latency** — latency per downstream service
9. **Circuit Breaker Status** — CLOSED/OPEN/HALF_OPEN
10. **Business Metrics** — Comenzi, Produse active, Login-uri

---

## 🛡️ API Gateway — Features

### Rate Limiting
- **Store Service**: 20 req/s cu burst de 40
- **User Service**: 10 req/s cu burst de 20
- Răspuns la depășire: **HTTP 429 Too Many Requests**

### Request Filtering (adăugate la fiecare request)
- `X-Request-Id: <UUID>` — ID unic per request
- `X-Gateway-Source: ComputerStore-Gateway`
- `X-Request-Timestamp: <timestamp>`

### Response Filtering (adăugate la fiecare response)
- `X-Response-Time: <ms>` — durata în millisecunde
- `X-Processed-By: ComputerStore-API-Gateway`
- `X-Request-Id: <UUID>` — propagat în response

### Routes
| Path | Serviciu | Port |
|---|---|---|
| `/api/store/**` | store-service | 8081 |
| `/api/user/**` | user-service | 8082 |
| `/store/actuator/**` | store actuator | 8081 |
| `/user/actuator/**` | user actuator | 8082 |

---

## 🔍 Distributed Tracing (Zipkin — BONUS)

Fiecare request primit de API Gateway primește un `traceId` propagat automat la toate serviciile downstream.

- **Trace URL**: `http://localhost:9411`
- **Sampling**: 100% (toate requesturile — doar pentru demo)
- **În producție**: setați `management.tracing.sampling.probability=0.1` (10%)

---

## 🏥 Health Checks

### Store Service `/actuator/health`
```json
{
  "status": "UP",
  "components": {
    "storeDatabase": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "productsCount": 42
      }
    },
    "diskSpace": { "status": "UP" },
    "circuitBreakers": { "status": "UP" }
  }
}
```

### User Service `/actuator/health`
```json
{
  "status": "UP",
  "components": {
    "userDatabase": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "usersCount": 15
      }
    },
    "diskSpace": { "status": "UP" }
  }
}
```
