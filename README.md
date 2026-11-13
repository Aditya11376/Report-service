# COSMOS Clinical Report Service 📄🧠 (Microservice)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)
[![Render](https://img.shields.io/badge/Deployment-Render-46e3b7.svg)](https://render.com)
[![Status](https://img.shields.io/badge/Port-8082-purple.svg)]()

A high-performance, stateless microservice engineered for **real-time clinical medical report synthesis and A4 PDF rendering** within the **COSMOS Autonomous Acute Stroke Screening Platform**.

Operating on **Port 8082**, this service ingests patient demographics, quantitative electrophysiological biomarkers (BSI, ADR), and cerebral biopotential traces (Lead F3 Left vs Lead F4 Right) to generate publication-grade, 1-page A4 diagnostic reports formatted with clinical typography, pink millimeter calibration grid traces, and cryptographic verification stamps.

---

## 🌟 Key Architectural Features

- **Stateless & High-Throughput:** Operates with **zero database coupling** and zero disk staging. All PDF rendering occurs in transient memory buffers (`ByteArrayOutputStream`) and streams directly to HTTP clients.
- **Authentic Millimeter EEG Grid:** Powered by Java AWT `Graphics2D` rendering an authentic pink millimeter grid (minor 6px / major 30px intervals) with condition-aware lead visualization (Lead F3 slow delta elevation vs Lead F4 10 Hz alpha baseline).
- **Dual-Layer Microservice Security:**
  - **Perimeter Defense:** Protected by constant-time cryptographic equality (`MessageDigest.isEqual`) on the `X-Internal-Secret` header to ensure only authorized API Gateways/BFFs can trigger generation.
  - **Zero-Trust Identity & RBAC:** Enforces `ROLE_DOCTOR` authorization extracted from incoming `Authorization: Bearer <JWT>` claims without shared database state.
- **Enterprise Error Handling:** Centralized `@RestControllerAdvice` (`ReportExceptionHandler`) mapping domain exceptions to standard RFC-compliant `ErrorResponseDto` bodies.

---

## 🏗️ System Architecture Flow

```
[ Frontend Doctor Dashboard ]
        │
        │ 1. POST /api/reports/generate { patientId, sessionId }
        ▼
[ API Gateway / Backend Orchestrator (Port 8080) ]
        │
        ├── 2. Resolves Patient & Telemetry Waveforms (Port 8085)
        ├── 3. Attaches X-Internal-Secret & Doctor JWT
        ▼
[ Report Service (Port 8082 - Stateless Engine) ]
        │
        ├── 4. Timing-safe Secret Validation (X-Internal-Secret)
        ├── 5. Doctor Identity & Role Check (ROLE_DOCTOR)
        ├── 6. In-Memory OpenPDF & Graphics2D Waveform Rendering
        ▼
[ Streamed Binary Response: Content-Disposition: attachment ]
        │
        ▼
[ Instant Browser Download: COSMOS_Report_<ID>.pdf ]
```

---

## 🔒 Security & Safe Configuration

All sensitive parameters are resolved through environment variables. **Never commit real credentials or production keys to version control.**

| Environment Variable | Description | Default / Dummy Sample | Required in Production |
| :--- | :--- | :--- | :--- |
| `PORT` | HTTP Server port | `8082` | No (Cloud auto-sets `$PORT`) |
| `APP_INTERNAL_SECRET` | 256-bit secret for internal gateway communication | `your-256-bit-internal-secret-here` | **Yes** |

---

## 📡 API Reference

### 1. Health Check
Checks service uptime and readiness.

```http
GET /api/health HTTP/1.1
Host: localhost:8082
```

#### Response (`200 OK`):
```json
{
  "status": "UP",
  "service": "cosmos-report-service",
  "port": 8082,
  "branding": "COSMOS Neurocare Clinical System"
}
```

---

### 2. Generate Clinical Medical Report (PDF)
Generates and streams an authoritative single-page clinical A4 medical report.

```http
POST /api/reports/generate HTTP/1.1
Host: localhost:8082
Content-Type: application/json
X-Internal-Secret: your-256-bit-internal-secret-here
Authorization: Bearer <DOCTOR_JWT_TOKEN>
```

#### Sample Request Body (Temporary / Dummy Data):
```json
{
  "reportId": "CR11202613001",
  "patient": {
    "patientName": "Demo Patient",
    "patientId": "PT-DEMO-2026-001",
    "age": 58,
    "gender": "Male"
  },
  "doctor": {
    "doctorName": "Dr. Demo Neurologist, MD",
    "licenseNumber": "MED-REG-DEMO-12345",
    "designation": "Consultant Neurologist",
    "department": "Department of Neuro-Critical Care",
    "signatureId": "DSIG-DEMO-98124"
  },
  "biomarkers": {
    "bsi": 0.384,
    "adr": 0.420,
    "alphaPower": 4.2,
    "deltaPower": 46.8,
    "f4Power": 28.5,
    "confidencePercentage": 96.4,
    "diagnosticStatus": "ACUTE ISCHEMIC STROKE (HIGH RISK)"
  },
  "f3Wave": [ 4.2, 5.1, 8.4, 12.0, 16.5, 22.1, 14.2, 6.0, -3.2, -8.1 ],
  "f4Wave": [ 2.1, 3.4, 4.0, 3.2, 2.0, 1.1, -1.2, -2.5, -1.0, 1.2 ],
  "clinicalFindings": [
    "Marked hemispheric power asymmetry detected between F3 (Left) and F4 (Right) leads.",
    "Focal delta band slowing coupled with profound alpha rhythm attenuation over left frontopolar territory.",
    "Immediate non-contrast CT angiography and acute neurovascular evaluation recommended."
  ]
}
```

#### Response:
- **Status:** `200 OK`
- **Headers:**
  - `Content-Type: application/pdf`
  - `Content-Disposition: attachment; filename="COSMOS_Report_CR11202613001.pdf"`
- **Body:** Binary raw PDF byte stream.

#### Error Responses:
- **`401 Unauthorized`:** Missing or invalid `X-Internal-Secret`.
- **`403 Forbidden`:** Authenticated user lacks `ROLE_DOCTOR`.
- **`400 Bad Request`:** Missing or malformed payload attributes.

---

## 🛠️ Local Development & Build

### Prerequisites
- Java 21 (JDK 21 LTS)
- Maven 3.9+ (or included `mvnw`)

### 1. Run Tests & Compile
```bash
# Using Maven Wrapper
./mvnw clean test
```

### 2. Package Executable JAR
```bash
./mvnw clean package -DskipTests
```

### 3. Run Locally
```bash
java -jar target/report-service-1.0.0.jar
```

---

## 🐳 Docker Deployment

The service includes an optimized multi-stage `Dockerfile` with headless Java AWT support and system font libraries (`fontconfig`, `libfreetype6`, `fonts-dejavu-core`).

### Build Docker Image:
```bash
docker build -t cosmos-report-service:1.0.0 .
```

### Run Container:
```bash
docker run -d \
  --name cosmos-report \
  -p 8082:8082 \
  -e PORT=8082 \
  -e APP_INTERNAL_SECRET=your-256-bit-internal-secret-here \
  cosmos-report-service:1.0.0
```

---

## ⚖️ License & Disclaimer

© 2026 GUPTA SOFTWORKS ORGANISATION. All rights reserved.