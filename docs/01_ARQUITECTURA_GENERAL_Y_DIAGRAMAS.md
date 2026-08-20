# REPLAY: ARQUITECTURA GENERAL Y DIAGRAMAS DEL SISTEMA

## 1. Visión y Concepto Central
**REPLAY** no es un almacén de archivos convencional ni una galería de fotos con tags automáticos. Es un **Motor de Memoria Personal (Personal Memory Engine)** concebido como un sistema cognitivo digital offline-first que estructura la vida de un individuo a través de un grafo espacio-temporal denominado **Life Graph**.

---

## 2. Diagramas de Arquitectura (Mermaid)

### Diagrama 1: Arquitectura General del Sistema
```mermaid
graph TD
    subgraph Clients ["Capa de Clientes"]
        Mobile["App Móvil (React Native + Expo TS)<br/>SQLite + SecureStore + TaskManager"]
        Web["App Web (React + Vite TS)<br/>State Management + Web Visualizer"]
    end

    subgraph Gateway ["Punto de Entrada Local / API Layer"]
        SpringBoot["Backend Monolito Modular<br/>(Spring Boot 3.x + Java 17/21 + Spring Security)"]
    end

    subgraph Storage ["Almacenamiento de Archivos"]
        LocalStorage["Local File Storage<br/>(storage/images, videos, docs)"]
        FutureCloud["[Abstracción] S3 / Cloud Storage"]
    end

    subgraph DataPlane ["Capa de Persistencia y Caché"]
        MongoDB[("MongoDB 7.x<br/>Fuente de Verdad")]
        Redis[("Redis 7.x<br/>Caché + Rate Limit + Colas")]
        ES[("Elasticsearch 8.x<br/>Full-Text + KNN Vector Search")]
    end

    subgraph AICloud ["Inteligencia Artificial Externa"]
        GeminiAPI["Google Gemini API<br/>Vision + Text-1.5-Flash + Embeddings-004"]
    end

    Mobile <-->|REST API + JWT / HTTPS| SpringBoot
    Web <-->|REST API + JWT / HTTPS| SpringBoot
    SpringBoot -->|Metadatos y Entidades| MongoDB
    SpringBoot -->|Caché & Locking & Jobs| Redis
    SpringBoot -->|Sync & Consultas Híbridas| ES
    SpringBoot -->|I/O Files| LocalStorage
    SpringBoot <-->|Embeddings & Inferencia Segura| GeminiAPI
```

---

### Diagrama 2: Arquitectura Modular del Backend (Spring Boot)
```mermaid
graph LR
    subgraph API_Layer ["Controllers / REST Endpoints"]
        AuthController["Auth & Security"]
        MemoryController["Memories & Timeline"]
        SearchController["Hybrid Search"]
        GraphController["Life Graph & Entities"]
        SyncController["Offline Synchronization"]
    end

    subgraph Core_Modules ["Módulos de Negocio Monolito"]
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

### Diagrama 3: Flujo de Creación y Procesamiento Asíncrono de un Recuerdo
```mermaid
sequenceDiagram
    autonumber
    actor Usuario
    participant Client as Cliente (Mobile/Web)
    participant Backend as Spring Boot API
    participant Mongo as MongoDB
    participant Redis as Redis Queue
    participant Worker as Async Processing Worker
    participant Gemini as Gemini API
    participant ES as Elasticsearch

    Usuario->>Client: Capturar/Subir Foto + Nota
    Client->>Backend: POST /api/memories (Multipart/FormData)
    Backend->>Backend: Guardar binario en Storage Local
    Backend->>Mongo: Crear Memory (status: PENDING_AI)
    Backend->>Redis: Encolar Job (memoryId, action: PROCESS_AI)
    Backend-->>Client: 202 Accepted (MemoryDTO status: PENDING_AI)
    
    Note over Worker,Redis: Procesamiento Asíncrono en Background
    Worker->>Redis: Dequeue Job
    Worker->>Gemini: Analizar Imagen + Extraer Contexto, Objetos, Personas
    Gemini-->>Worker: JSON {descripcion, tags, entities, category}
    Worker->>Gemini: Generar Vector Embedding (text-embedding-004)
    Gemini-->>Worker: Vector (768 dimensiones)
    Worker->>Mongo: Actualizar Memory (status: PROCESSED, aiAnalysis, entities)
    Worker->>ES: Indexar Documento en 'replay_memories' (Text + Dense_Vector)
    Worker->>Redis: Publicar evento / Invalidar Caché
```

---

### Diagrama 4: Pipeline de Búsqueda Híbrida Inteligente (Full-Text + Semantic Vector)
```mermaid
flowchart TD
    A["Consulta en Lenguaje Natural:<br/>'¿Cuándo trabajé en la universidad con Carlos?'"] --> B["Backend: SearchService"]
    B --> C["Gemini API: Intent & Entity Extractor + Embedding"]
    C -->|Filtros detectados| D["Filtros: person='Carlos', place='Universidad'"]
    C -->|Vector Consulta 768d| E["Dense Vector"]
    
    B --> F["Elasticsearch Multi-Match BM25 (Full-Text)"]
    E --> G["Elasticsearch KNN Vector Search (Cosine Similarity)"]
    
    F --> H["Reciprocal Rank Fusion (RRF) / Hybrid Scoring"]
    G --> H
    D --> H
    
    H --> I["Top K Recuerdos Candidatos (IDs)"]
    I --> J["Hidratación desde MongoDB (Fuente de Verdad)"]
    J --> K["Gemini Generator: Contextual Grounded Answer"]
    K --> L["Respuesta Final: Respuesta redactada + Recuerdos citados"]
```

---

### Diagrama 5: Sincronización Offline-First con Expo SQLite
```mermaid
sequenceDiagram
    autonumber
    actor User as Usuario (Sin Internet)
    participant SQLite as SQLite Local (Mobile)
    participant SyncEngine as Sync Engine (Mobile)
    participant Backend as Spring Boot
    participant Mongo as MongoDB

    User->>SQLite: Crea Recuerdo / Toma Foto
    SQLite->>SQLite: Guardar en tabla 'local_memories' (sync_status='PENDING_INSERT')
    SQLite->>SQLite: Guardar en cola 'sync_queue' (op='INSERT', timestamp)
    
    Note over SyncEngine: Detección de Conexión de Red (NetInfo)
    SyncEngine->>Backend: POST /api/sync/batch (Payload con operaciones pendientes)
    Backend->>Backend: Procesar operaciones con resolución de conflictos (LWW / Versioning)
    Backend->>Mongo: Persistir o actualizar registros definitivos
    Backend-->>SyncEngine: 200 OK (Mapeo ID local -> ID remoto, conflictos resueltos)
    SyncEngine->>SQLite: Actualizar 'sync_status' a 'SYNCED', actualizar remote_id
```

---

### Diagrama 6: Modelo Relacional Lógico Life Graph
```mermaid
graph TD
    User((Usuario)) -->|Posee| M1["Memory: Tesis Final (2026-05)"]
    User -->|Conoce| P1["Person: Carlos Mendoza"]
    User -->|Frecuenta| L1["Location: Biblioteca Central"]
    User -->|Es dueño de| O1["Object: Laptop ThinkPad"]
    
    M1 -->|Involucra| P1
    M1 -->|Ocurrió en| L1
    M1 -->|Utilizó| O1
    M1 -->|Contiene| D1["Document: tesis_v1.pdf"]
    M1 -->|Contiene| F1["Media: foto_defensa.jpg"]

    M2["Memory: Café de celebración"] -->|Involucra| P1
    M2 -->|Ocurrió en| L2["Location: Cafetería El Prado"]
    M2 -->|Deriva de| M1
```
