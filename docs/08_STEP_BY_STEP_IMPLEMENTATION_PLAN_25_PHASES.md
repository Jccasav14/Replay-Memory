# REPLAY: MASTER STEP-BY-STEP IMPLEMENTATION PLAN (25 PHASES)

This document is the **authoritative chronological engineering guide** to build the **REPLAY** platform from an empty repository to a validated production build.

---

## Phase Overview

```text
[Phases 01-04]: Base Infrastructure & Docker (MongoDB, Elasticsearch, Redis, Spring Boot Monolith)
[Phases 05-07]: Base Clients & Security (Expo Mobile, Vite React Web, Spring Security JWT)
[Phases 08-10]: Core Memory & Media (CRUD Memories, Local File Storage, Expo Camera/Picker)
[Phases 11-15]: Cognitive AI & Search (Gemini Vision/Text, Dense Embeddings, Elasticsearch Hybrid Search)
[Phases 16-17]: Advanced Modeling (Timeline Visualizer, Life Graph Relational Network)
[Phases 18-21]: Mobile Capabilities (Expo SQLite Offline Sync, Background Geofencing, Push Notifications)
[Phases 22-25]: Hardening & Release (Security Hardening, Test Suite, Dev Build, EAS Release APK/AAB)
```

---

### Phase 01: Repository Initialization and Docker Infrastructure
* **Objective**: Configure version control and local persistence containers (MongoDB, Elasticsearch, Redis) with healthchecks.
* **Technologies**: Git, Docker Engine, Docker Compose v2.
* **Files**: `infrastructure/docker/docker-compose.yml`, `.env.example`, `.gitignore`.
* **Commands**:
  ```bash
  docker compose -f infrastructure/docker/docker-compose.yml up -d
  docker compose -f infrastructure/docker/docker-compose.yml ps
  ```
* **Validation**:
  * `curl http://localhost:9200` returns Elasticsearch 8.x cluster info.
  * `mongosh --port 27017 -u admin -p replay_secure_password_2026` connects successfully.
  * `redis-cli -a replay_redis_password ping` returns `PONG`.
* **Acceptance Criteria**: All 3 containers running with `healthy` status and persistent volumes attached.

---

### Phase 02: Spring Boot Backend Initialization (Modular Monolith)
* **Objective**: Create Java project with Maven, Spring Data MongoDB, Spring Data Redis, Elasticsearch Java Client, and Actuator metrics.
* **Technologies**: Java 17/21, Spring Boot 3.2+, Maven.
* **Commands**:
  ```bash
  cd backend
  ./mvnw clean compile
  ./mvnw spring-boot:run
  ```
* **Validation**: `http://localhost:8080/actuator/health` returns status `UP` for mongo, redis, and elasticsearch.
* **Acceptance Criteria**: Backend boots with zero exceptions and verifies connectivity to all 3 engines.

---

### Phase 03: Security and Authentication Module (JWT)
* **Objective**: Implement user registration, login, refresh token rotation, and Redis revocation with Spring Security.
* **Technologies**: Spring Security, JJWT (io.jsonwebtoken 0.12+), BCrypt / Argon2.
* **Validation**: POST `/api/v1/auth/register` creates user; POST `/api/v1/auth/login` returns token pair; protected routes return 401 without token.
* **Acceptance Criteria**: JWT authentication flow secured with Redis blacklist revocation.

---

### Phase 04: Local File Storage and Media Module
* **Objective**: Implement file upload/download service with magic-byte MIME validation, SHA-256 checksums, and image thumbnails.
* **Technologies**: Java NIO, Thumbnailator, Apache Tika.
* **Storage Paths**: `storage/images/`, `storage/thumbnails/`, `storage/documents/`.
* **Validation**: Upload 2MB JPEG; verify file is stored with UUID filename, 200x200 thumbnail generated, and path traversal blocked.
* **Acceptance Criteria**: Secure file ingestion with boundary isolation.

---

### Phase 05: Mobile Client Initialization (React Native + Expo TS)
* **Objective**: Initialize cross-platform mobile app with TypeScript, tab navigation, and Expo SecureStore.
* **Technologies**: Expo SDK 51+, React Native 0.74+, React Navigation, TypeScript.
* **Commands**:
  ```bash
  cd mobile
  npx create-expo-app@latest ./ --template blank-typescript
  npx expo start
  ```
* **Validation**: Log in from emulator; store JWT in SecureStore; persist session across restarts.
* **Acceptance Criteria**: Mobile authentication flow fully functional and persistent.

---

### Phase 06: Web Portal Initialization (React + Vite TS)
* **Objective**: Create web portal with responsive design, Axios interceptors, and protected dashboard routing.
* **Technologies**: React 18+, Vite, TypeScript, Axios, React Router DOM 6+.
* **Commands**:
  ```bash
  cd web
  npm create vite@latest ./ -- --template react-ts
  npm install
  npm run dev
  ```
* **Validation**: Access `http://localhost:5173`; login and view protected Dashboard.
* **Acceptance Criteria**: Interceptors attach Bearer tokens automatically and handle 401 refresh seamlessly.

---

### Phase 07: Memories Model and CRUD Module
* **Objective**: Implement memory creation, paginated listing, detail view, editing, and soft deletion.
* **Endpoints**: POST, GET, PUT, DELETE under `/api/v1/memories`.
* **Validation**: POST `/api/v1/memories` creates document with status `PENDING_AI`.
* **Acceptance Criteria**: Memory lifecycle persisted in MongoDB.

---

### Phase 08: Mobile Camera and Media Picker Integration
* **Objective**: Enable capturing live photos or selecting gallery items with EXIF metadata parsing.
* **Technologies**: `expo-camera`, `expo-image-picker`, `expo-media-library`.
* **Validation**: Capture image; preview in `CreateMemoryScreen`; upload via FormData to backend.
* **Acceptance Criteria**: End-to-end capture to server upload confirmation.

---

### Phase 09: Asynchronous Processing Queue (Redis Worker)
* **Objective**: Decouple memory ingestion from heavy AI inference tasks.
* **Technologies**: Redis Queues, Spring `@Async` ThreadPoolTaskExecutor.
* **Validation**: Upload 10 images concurrently; verify instant 202 Accepted and FIFO processing in worker logs.
* **Acceptance Criteria**: HTTP requests never block on AI processing.

---

### Phase 10: Google Gemini API Integration (Vision & Text)
* **Objective**: Connect backend worker to Gemini 1.5 Flash for multimodal scene analysis and JSON extraction.
* **Technologies**: Google GenAI Java SDK / Spring RestClient, Gemini 1.5 Flash.
* **Validation**: Analyze uploaded photo; verify JSON extraction of summary, objects, people descriptions, and context category.
* **Acceptance Criteria**: Schema-enforced JSON saved to `aiAnalysis` field in MongoDB.

---

### Phase 11: Dense Vector Embeddings Generation (text-embedding-004)
* **Objective**: Generate 768-dimensional dense vectors from canonical memory text.
* **Technologies**: Gemini Embeddings API (`text-embedding-004`).
* **Validation**: Verify output vector has length 768 and Euclidean norm $\approx 1.0$.
* **Acceptance Criteria**: Embeddings stored in MongoDB and ready for Elasticsearch indexing.

---

### Phase 12: Elasticsearch Indexing and Full-Text Search
* **Objective**: Create `replay_memories` index with text analyzers and synchronize processed memories.
* **Technologies**: Elasticsearch 8.x Java Client.
* **Validation**: Fuzzy search query (`"almueso"` matches `"Almuerzo"`).
* **Acceptance Criteria**: Automated indexing on worker completion and removal on soft-delete.

---

### Phase 13: Vector Search (KNN Cosine Similarity)
* **Objective**: Implement dense vector similarity search in Elasticsearch.
* **Validation**: Search *"relaxing moment by the ocean"* and retrieve beach photo without matching words.
* **Acceptance Criteria**: Similarity score $> 0.75$ for semantically related queries.

---

### Phase 14: Hybrid Search Engine and Grounded RAG
* **Objective**: Combine BM25 + KNN with Reciprocal Rank Fusion and synthesize factual answers with Gemini.
* **Endpoint**: POST `/api/v1/search/semantic`.
* **Validation**: Query *"When did I buy my laptop?"* returns exact date and citations from memories.
* **Acceptance Criteria**: Generated answers contain only information present in retrieved memory context.

---

### Phase 15: Timeline Visualization System
* **Objective**: Build chronological timeline view grouped by year, month, and day with multi-attribute filtering.
* **Components**: Virtualized scrolling list in Web and Mobile.
* **Validation**: Smooth scrolling at 60fps across hundreds of loaded memories.
* **Acceptance Criteria**: Interactive multidimensional filtering by date, tag, and entity.

---

### Phase 16: Life Graph: Relational Network Module
* **Objective**: Manage People, Locations, Objects and calculate topological co-occurrence graphs.
* **Collections**: `people`, `locations`, `objects`.
* **Validation**: Click a person node (Carlos) to view all associated memories, locations, and objects.
* **Acceptance Criteria**: Bidirectional navigation between memories and Life Graph nodes.

---

### Phase 17: Mobile Offline Storage (Expo SQLite)
* **Objective**: Implement local SQLite database for full offline CRUD operations.
* **Technologies**: `expo-sqlite`.
* **Validation**: Enable Airplane Mode; create memory; verify record in `local_memories` with `sync_status='PENDING_INSERT'`.
* **Acceptance Criteria**: 100% functional local read/write capabilities offline.

---

### Phase 18: Bidirectional Sync Engine (Mobile <-> Backend)
* **Objective**: Implement batch sync protocol (`/api/v1/sync/batch`) with Last-Write-Wins (LWW) conflict resolution.
* **Technologies**: `@react-native-community/netinfo`, `SyncManager.ts`.
* **Validation**: Disable Airplane Mode; verify queue dispatch and ID reconciliation with MongoDB.
* **Acceptance Criteria**: Zero duplicate records and clean sync status transition.

---

### Phase 19: Geofencing and Mobile Background Tasks
* **Objective**: Register discrete check-in events at Life Graph locations with minimal battery drain.
* **Technologies**: `expo-location`, `expo-task-manager`.
* **Validation**: Simulate location entering "Campus" geofence; verify background entry of `LOCATION_EVENT`.
* **Acceptance Criteria**: Low battery consumption without continuous GPS polling.

---

### Phase 20: Smart Notification System
* **Objective**: Schedule local notifications for pending offline sync reminders and weekly recap highlights.
* **Technologies**: `expo-notifications`.
* **Validation**: Trigger reminder when $>5$ memories await Wi-Fi sync.
* **Acceptance Criteria**: Non-intrusive notification dispatch.

---

### Phase 21: Security Hardening and Privacy Compliance
* **Objective**: Enforce Redis rate limiting, magic bytes inspection, OWASP headers, and account purge cascade.
* **Validation**: Upload disguised PHP script (rejected with 400); execute account deletion (purges MongoDB, ES, Redis, and disk).
* **Acceptance Criteria**: Strict adherence to Privacy by Design.

---

### Phase 22: Automated Testing Suite (Unit, Integration, E2E)
* **Objective**: Validate business logic and integration resilience.
* **Technologies**: JUnit 5, Mockito, Testcontainers, Vitest, React Native Testing Library.
* **Commands**:
  ```bash
  cd backend && ./mvnw test
  cd web && npm test
  cd mobile && npm test
  ```
* **Acceptance Criteria**: $\ge 80\%$ test coverage on critical domain modules.

---

### Phase 23: Transition to Expo Development Build
* **Objective**: Build custom native development client to support native background location and SQLite drivers.
* **Commands**:
  ```bash
  cd mobile
  npx expo install expo-dev-client
  npx expo prebuild
  ```
* **Acceptance Criteria**: App runs as custom native build (`com.replay.app`).

---

### Phase 24: Standalone Production Build via EAS
* **Objective**: Configure `eas.json` and generate signed standalone `.apk` / `.aab` for Android and `.ipa` for iOS.
* **Technologies**: EAS CLI (`eas-cli`).
* **Commands**:
  ```bash
  npm install -g eas-cli
  eas login
  eas build:configure
  eas build --platform android --profile preview
  ```
* **Acceptance Criteria**: Standalone APK installed and operational on physical test hardware.

---

### Phase 25: Final Audit, Metrics Verification and Local Milestone Signoff
* **Objective**: Execute end-to-end integration validation across all services (Docker + Backend + Web + Mobile APK).
* **Validation**: Execute 10 benchmark queries; compute MRR, Precision@K, and Groundedness Score.
* **Acceptance Criteria**: All 25 phases verified, full documentation aligned, and system ready for demo or production deployment.
