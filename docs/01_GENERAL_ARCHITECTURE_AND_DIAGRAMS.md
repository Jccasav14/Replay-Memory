# REPLAY: GENERAL ARCHITECTURE AND SYSTEM DIAGRAMS

## 1. System Vision and Core Concept
**REPLAY** is not a simple file storage bucket or photo gallery with automatic labels. It is an **Offline-First Personal Memory Engine** engineered as an individual cognitive system that structures biographical data through a spatio-temporal graph called the **Life Graph**.

---

## 2. Architectural Diagrams (Mermaid)

### Diagram 1: High-Level System Architecture
```mermaid
graph TD
    subgraph Clients ["Client Layer"]
        Mobile["Mobile App (React Native + Expo TS)<br/>SQLite + SecureStore + TaskManager"]
        Web["Web App (React + Vite TS)<br/>State Management + Graph Visualizer"]
    end

    subgraph Gateway ["Local Entrypoint / API Layer"]
        SpringBoot["Modular Monolith Backend<br/>(Spring Boot 3.x + Java 17/21 + Spring Security)"]
    end

    subgraph Storage ["Binary File Storage"]
        LocalStorage["Local File Storage<br/>(storage/images, videos, docs)"]
        FutureCloud["[Abstraction] S3 / Cloud Storage"]
    end

    subgraph DataPlane ["Persistence and Cache Layer"]
        MongoDB[("MongoDB 7.x<br/>Source of Truth")]
        Redis[("Redis 7.x<br/>Cache + Rate Limit + Queues")]
        ES[("Elasticsearch 8.x<br/>Full-Text + KNN Vector Search")]
    end

    subgraph AICloud ["External Cognitive Engine"]
        GeminiAPI["Google Gemini API<br/>Vision 1.5 Flash + text-embedding-004"]
    end

    Mobile <-->|REST API + JWT / HTTPS| SpringBoot
    Web <-->|REST API + JWT / HTTPS| SpringBoot
    SpringBoot -->|Metadata & Entities| MongoDB
    SpringBoot -->|Cache, Locking & Jobs| Redis
    SpringBoot -->|Sync & Hybrid Queries| ES
    SpringBoot -->|I/O File Streams| LocalStorage
    SpringBoot <-->|Embeddings & Secure Inference| GeminiAPI
```

---

### Diagram 2: Backend Modular Monolith Architecture
```mermaid
graph LR
    subgraph API_Layer ["Controllers / REST Endpoints"]
        AuthController["Auth & Security"]
        MemoryController["Memories & Timeline"]
        SearchController["Hybrid Search"]
        GraphController["Life Graph & Entities"]
        SyncController["Offline Synchronization"]
    end

    subgraph Core_Modules ["Modular Monolith Business Layer"]
        AuthMod["auth & users"]
        MemMod["memories & events"]
        MediaMod["media & storage"]
        AIMod["ai & embeddings"]
        SearchMod["search (ES)"]
        SyncMod["synchronization"]
        GraphMod["people, places, objects"]
    end

    subgraph Data_Access ["Repositories & Clients"]
        MongoRepo["Spring Data MongoDB"]
        ESClient["Elasticsearch Java Client"]
        RedisTemplate["Spring Data Redis"]
        GeminiClient["Gemini REST Client"]
    end

    API_Layer --> Core_Modules
    Core_Modules --> Data_Access
```

---

### Diagram 3: Memory Creation and Asynchronous Processing Lifecycle
```mermaid
sequenceDiagram
    autonumber
    actor User as End User
    participant Client as Client (Mobile/Web)
    participant Backend as Spring Boot API
    participant Mongo as MongoDB
    participant Redis as Redis Queue
    participant Worker as Async Processing Worker
    participant Gemini as Gemini API
    participant ES as Elasticsearch

    User->>Client: Capture/Upload Photo + Note
    Client->>Backend: POST /api/v1/memories (Multipart/FormData)
    Backend->>Backend: Store binary in local file storage
    Backend->>Mongo: Create Memory document (status: PENDING_AI)
    Backend->>Redis: Enqueue Job (memoryId, action: PROCESS_AI)
    Backend-->>Client: 202 Accepted (MemoryDTO status: PENDING_AI)
    
    Note over Worker,Redis: Background Async Processing
    Worker->>Redis: Dequeue Job
    Worker->>Gemini: Analyze Image + Extract Context, Objects, People
    Gemini-->>Worker: JSON {summary, detailedDescription, objects, people, category}
    Worker->>Gemini: Generate Dense Vector (text-embedding-004)
    Gemini-->>Worker: Dense Vector (768 dimensions)
    Worker->>Mongo: Update Memory (status: PROCESSED, aiAnalysis, entities)
    Worker->>ES: Index document into 'replay_memories' (Text + Dense_Vector)
    Worker->>Redis: Invalidate Cache / Publish Event
```

---

### Diagram 4: Hybrid Intelligent Search Pipeline (Full-Text + Semantic Vector)
```mermaid
flowchart TD
    A["Natural Language Query:<br/>'When did I work at university with Carlos?'"] --> B["Backend: SearchService"]
    B --> C["Gemini API: Intent & Entity Extractor + Embedding"]
    C -->|Extracted Filters| D["Filters: person='Carlos', place='University'"]
    C -->|768-dim Query Vector| E["Dense Vector"]
    
    B --> F["Elasticsearch Multi-Match BM25 (Full-Text)"]
    E --> G["Elasticsearch KNN Vector Search (Cosine Similarity)"]
    
    F --> H["Reciprocal Rank Fusion (RRF) / Hybrid Scoring"]
    G --> H
    D --> H
    
    H --> I["Top-K Candidate Memory IDs"]
    I --> J["Hydrate from MongoDB (Source of Truth)"]
    J --> K["Gemini Generator: Contextual Grounded Answer"]
    K --> L["Final Response: Natural Answer + Grounded Citations"]
```

---

### Diagram 5: Offline-First Synchronization Lifecycle (Expo SQLite)
```mermaid
sequenceDiagram
    autonumber
    actor User as User (No Connectivity)
    participant SQLite as Local SQLite (Mobile)
    participant SyncEngine as Sync Engine (Mobile)
    participant Backend as Spring Boot
    participant Mongo as MongoDB

    User->>SQLite: Create Memory / Snap Photo
    SQLite->>SQLite: Save into 'local_memories' (sync_status='PENDING_INSERT')
    SQLite->>SQLite: Register in 'sync_queue' (op='INSERT', timestamp)
    
    Note over SyncEngine: Network Reachability Detected (NetInfo)
    SyncEngine->>Backend: POST /api/v1/sync/batch (Batch Payload)
    Backend->>Backend: Execute transactions with conflict resolution (LWW)
    Backend->>Mongo: Persist or update definitive records
    Backend-->>SyncEngine: 200 OK (ID Mapping localId -> remoteId, resolved conflicts)
    SyncEngine->>SQLite: Update 'sync_status' to 'SYNCED', set remote_id
```

---

### Diagram 6: Logical Life Graph Model
```mermaid
graph TD
    User((User)) -->|Owns| M1["Memory: Final Thesis (2026-05)"]
    User -->|Knows| P1["Person: Carlos Mendoza"]
    User -->|Frequents| L1["Location: Central Library"]
    User -->|Possesses| O1["Object: ThinkPad Laptop"]
    
    M1 -->|Involves| P1
    M1 -->|Occurred At| L1
    M1 -->|Utilized| O1
    M1 -->|Contains| D1["Document: thesis_v1.pdf"]
    M1 -->|Contains| F1["Media: defense_photo.jpg"]

    M2["Memory: Celebration Coffee"] -->|Involves| P1
    M2 -->|Occurred At| L2["Location: El Prado Cafe"]
    M2 -->|Derived From| M1
```
