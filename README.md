# REPLAY: PERSONAL MEMORY ENGINE

> **REPLAY is a personal digital memory platform that allows an individual to capture, organize, relate, search, and reconstruct life events using photographs, documents, locations, notes, people, objects, and other milestones. The system leverages artificial intelligence to transform fragmented inputs into structured biographical memories and enables natural language semantic search across the user's personal timeline.**

---

## Core Differentiator: Personal Memory Engine and Life Graph
Unlike conventional cloud photo galleries or basic note-taking applications, **REPLAY** functions as a cognitive personal memory engine that structures an individual's life into a multidimensional topological network (**Life Graph**). It provides an evidence-based hybrid Retrieval-Augmented Generation (Grounded RAG) pipeline without hallucinations.

---

## Technology Stack

| Layer | Selected Technologies |
| :--- | :--- |
| **Mobile** | React Native, Expo (TypeScript), Expo SQLite, Camera, Location, TaskManager, SecureStore |
| **Web** | React, Vite, TypeScript, Vanilla CSS / Tailwind CSS |
| **Backend** | Java 17/21, Spring Boot 3.x, Spring Web, Spring Security, JWT, Maven |
| **Primary Persistence** | MongoDB 7.x (Source of Truth for Memories and Life Graph) |
| **Search and Vectors** | Elasticsearch 8.x (BM25 Full-Text + 768-dim Dense Vector KNN Search) |
| **Cache and Async Jobs** | Redis 7.x (Cache, Rate Limiting, Async Job Queues, Distributed Locks) |
| **Artificial Intelligence** | Google Gemini API (Vision 1.5 Flash, Text 1.5 Flash, text-embedding-004) |
| **Local Infrastructure** | Docker and Docker Compose |

---

## Repository Structure

```text
replay/
│
├── mobile/                  # Mobile Application (React Native + Expo TS)
│   ├── src/
│   ├── app.json
│   └── package.json
│
├── web/                     # Web Portal (React + Vite TS)
│   ├── src/
│   ├── vite.config.ts
│   └── package.json
│
├── backend/                 # Modular Monolith (Spring Boot + Maven)
│   ├── src/
│   └── pom.xml
│
├── storage/                 # Local filesystem storage abstraction
│   ├── images/
│   ├── thumbnails/
│   ├── documents/
│   └── videos/
│
├── infrastructure/          # Containerization & local deployment
│   └── docker/
│       └── docker-compose.yml
│
├── docs/                    # Complete technical and academic specifications
│   ├── 01_GENERAL_ARCHITECTURE_AND_DIAGRAMS.md
│   ├── 02_DATA_MODELS_NOSQL_ES_REDIS_SQLITE.md
│   ├── 03_REST_API_SPECIFICATION.md
│   ├── 04_AI_GEMINI_EMBEDDINGS_HYBRID_SEARCH.md
│   ├── 05_OFFLINE_FIRST_SYNC_MOBILE_BACKGROUND.md
│   ├── 06_SECURITY_PRIVACY_BY_DESIGN_AUDIT.md
│   ├── 07_ACADEMIC_RESEARCH_AND_METRICS.md
│   └── 08_STEP_BY_STEP_IMPLEMENTATION_PLAN_25_PHASES.md
│
├── .env.example             # Environment variable template
└── README.md                # Project entry point
```

---

## Quickstart Guide for Local Environment

### 1. Launch Persistence and Search Engines (Docker)
```bash
# Copy environment template
cp .env.example .env

# Start MongoDB, Elasticsearch, and Redis
docker compose -f infrastructure/docker/docker-compose.yml up -d

# Verify container health status
docker compose -f infrastructure/docker/docker-compose.yml ps
```

### 2. Launch Backend (Spring Boot)
```bash
cd backend
./mvnw spring-boot:run
# Health endpoint available at: http://localhost:8080/actuator/health
```

### 3. Launch Web Application (React + Vite)
```bash
cd web
npm install
npm run dev
# Web application accessible at: http://localhost:5173
```

### 4. Launch Mobile Application (Expo)
```bash
cd mobile
npm install
npx expo start
```

---

## Documentation Map
Refer to the `docs/` directory for exhaustive technical specifications:
* [01. General Architecture and Mermaid Diagrams](docs/01_GENERAL_ARCHITECTURE_AND_DIAGRAMS.md)
* [02. Data Models: MongoDB, Elasticsearch, Redis and SQLite](docs/02_DATA_MODELS_NOSQL_ES_REDIS_SQLITE.md)
* [03. REST API Specification and Contracts](docs/03_REST_API_SPECIFICATION.md)
* [04. AI Architecture, Gemini API and Hybrid Search](docs/04_AI_GEMINI_EMBEDDINGS_HYBRID_SEARCH.md)
* [05. Offline-First Sync Engine and Mobile Background Processing](docs/05_OFFLINE_FIRST_SYNC_MOBILE_BACKGROUND.md)
* [06. Security, Privacy by Design and Hardening](docs/06_SECURITY_PRIVACY_BY_DESIGN_AUDIT.md)
* [07. Academic Research, State of the Art and Metrics](docs/07_ACADEMIC_RESEARCH_AND_METRICS.md)
* [08. Master Step-by-Step Implementation Plan (25 Phases)](docs/08_STEP_BY_STEP_IMPLEMENTATION_PLAN_25_PHASES.md)
