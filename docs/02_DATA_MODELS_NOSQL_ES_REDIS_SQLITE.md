# REPLAY: DATA MODELS AND PERSISTENCE STRATEGIES

## 1. Technical Justification of MongoDB as Primary Store
Human memories cannot be constrained by rigid, homogeneous tabular schemas. A memory entry may be a simple textual thought, a photograph tagged with five individuals and twenty computer vision labels, a PDF document with extracted OCR text, a geospatial location event, or an evolving polymorphic combination of these elements.

MongoDB provides:
1. **Polymorphic Document Modeling**: Storing heterogeneous memory types (`PHOTO`, `NOTE`, `DOCUMENT`, `LOCATION_EVENT`, etc.) within a unified collection.
2. **Embedded vs Referenced Strategies**: Embedding immutable or tightly coupled metadata (AI vision tags, EXIF, coordinate snapshots) while referencing independently managed entities (`Person`, `Location`, `Object`).
3. **High-Throughput Temporal and Geospatial Queries**: Compound indices on `{ userId: 1, occurredAt: -1 }` and `2dsphere` geospatial indices.

---

## 2. Design Strategy: Embedded Documents vs Logical References
* **Embedded Documents**:
  * AI analysis results (`aiAnalysis` inside `Memory`).
  * Media item metadata (`media` array inside `Memory`).
  * Point coordinates and address snapshots.
* **Referenced Entities (ObjectIds)**:
  * `Person`: People have identity and metrics across multiple memories.
  * `Location`: Frequently visited places (Home, Work, Campus) maintain aggregate statistics.
  * `Object`: Valuables and possessions possess an independent timeline.
  * `User`: Multi-tenant data partition.

---

## 3. MongoDB Schemas (BSON / JSON Schema)

### 3.1. Collection: `users`
```json
{
  "_id": { "$type": "objectId" },
  "email": { "$type": "string" },
  "passwordHash": { "$type": "string" },
  "fullName": { "$type": "string" },
  "role": { "$type": "string", "enum": ["ROLE_USER", "ROLE_ADMIN"] },
  "status": { "$type": "string", "enum": ["ACTIVE", "UNVERIFIED", "LOCKED"] },
  "settings": {
    "privacyLevel": { "$type": "string", "default": "STANDARD" },
    "allowBackgroundSync": { "$type": "bool", "default": true },
    "allowAiProcessing": { "$type": "bool", "default": true }
  },
  "createdAt": { "$type": "date" },
  "updatedAt": { "$type": "date" }
}
```
**Indices:**
* `db.users.createIndex({ "email": 1 }, { unique: true })`

---

### 3.2. Collection: `memories` (Core Collection)
```json
{
  "_id": { "$type": "objectId" },
  "userId": { "$type": "objectId" },
  "type": { "$type": "string", "enum": ["PHOTO", "VIDEO", "NOTE", "DOCUMENT", "LOCATION_EVENT", "MANUAL_EVENT", "SYSTEM_EVENT", "IMPORTED_EVENT"] },
  "title": { "$type": "string" },
  "description": { "$type": "string" },
  "occurredAt": { "$type": "date" },
  "timezone": { "$type": "string" },
  
  "location": {
    "locationId": { "$type": "objectId" },
    "name": { "$type": "string" },
    "geoPoint": {
      "type": { "$type": "string", "enum": ["Point"] },
      "coordinates": [{ "$type": "double" }, { "$type": "double" }]
    },
    "address": { "$type": "string" }
  },

  "media": [
    {
      "mediaId": { "$type": "objectId" },
      "fileType": { "$type": "string", "enum": ["IMAGE", "VIDEO", "AUDIO", "DOCUMENT"] },
      "storagePath": { "$type": "string" },
      "mimeType": { "$type": "string" },
      "fileSizeBytes": { "$type": "long" },
      "checksumSha256": { "$type": "string" },
      "exifData": {
        "cameraModel": { "$type": "string" },
        "iso": { "$type": "int" },
        "lens": { "$type": "string" }
      }
    }
  ],

  "peopleIds": [{ "$type": "objectId" }],
  "objectIds": [{ "$type": "objectId" }],
  "tags": [{ "$type": "string" }],

  "aiAnalysis": {
    "summary": { "$type": "string" },
    "detailedDescription": { "$type": "string" },
    "extractedText": { "$type": "string" },
    "detectedEmotions": [{ "$type": "string" }],
    "detectedCategories": [{ "$type": "string" }],
    "detectedObjects": [{ "$type": "string" }],
    "modelUsed": { "$type": "string" },
    "processedAt": { "$type": "date" }
  },

  "embedding": [{ "$type": "double" }],

  "processingStatus": { "$type": "string", "enum": ["PENDING_STORAGE", "PENDING_AI", "PROCESSED", "FAILED"] },
  "syncVersion": { "$type": "long", "default": 1 },
  "isDeleted": { "$type": "bool", "default": false },
  "createdAt": { "$type": "date" },
  "updatedAt": { "$type": "date" }
}
```
**Indices:**
* `db.memories.createIndex({ "userId": 1, "occurredAt": -1 })`
* `db.memories.createIndex({ "userId": 1, "isDeleted": 1, "processingStatus": 1 })`
* `db.memories.createIndex({ "location.geoPoint": "2dsphere" })`
* `db.memories.createIndex({ "userId": 1, "peopleIds": 1 })`
* `db.memories.createIndex({ "userId": 1, "objectIds": 1 })`
* `db.memories.createIndex({ "userId": 1, "tags": 1 })`

---

### 3.3. Life Graph Collections (`people`, `locations`, `objects`)
* **`people`**: Fields `_id`, `userId`, `name`, `relationship`, `avatarStoragePath`, `notes`, `firstMetDate`, `interactionCount`, `createdAt`.
* **`locations`**: Fields `_id`, `userId`, `name`, `category`, `geoPoint`, `radiusMeters`, `visitCount`, `createdAt`.
* **`objects`**: Fields `_id`, `userId`, `name`, `category`, `serialNumber`, `acquisitionDate`, `photoStoragePath`, `createdAt`.

---

## 4. Elasticsearch Mapping (`replay_memories`)
Elasticsearch serves as a **derived search index** providing **BM25 full-text** and **KNN Dense Vector Search**.

```json
{
  "mappings": {
    "properties": {
      "id": { "type": "keyword" },
      "userId": { "type": "keyword" },
      "type": { "type": "keyword" },
      "title": { 
        "type": "text",
        "analyzer": "standard",
        "fields": {
          "suggest": { "type": "search_as_you_type" }
        }
      },
      "description": { "type": "text", "analyzer": "standard" },
      "aiSummary": { "type": "text", "analyzer": "standard" },
      "tags": { "type": "keyword" },
      "peopleNames": { 
        "type": "text",
        "fields": { "raw": { "type": "keyword" } }
      },
      "locationName": { 
        "type": "text",
        "fields": { "raw": { "type": "keyword" } }
      },
      "objectNames": { 
        "type": "text",
        "fields": { "raw": { "type": "keyword" } }
      },
      "occurredAt": { "type": "date" },
      "geoPoint": { "type": "geo_point" },
      "embeddingVector": {
        "type": "dense_vector",
        "dims": 768,
        "index": true,
        "similarity": "cosine"
      }
    }
  }
}
```

---

## 5. Redis Key Taxonomy and TTL Policy

| Key Pattern | Data Structure | TTL | Responsibility |
| :--- | :--- | :--- | :--- |
| `auth:token:blacklist:{jti}` | String | JWT Expiry | Immediate revocation of invalidated tokens |
| `auth:refresh:{userId}:{deviceId}` | String | 7 days | Refresh token validation |
| `ratelimit:{ip}:{endpoint}` | String (INCR) | 1 minute | API rate limiting protection |
| `cache:timeline:{userId}:{year}:{month}` | String (JSON) | 1 hour | Cached monthly timeline views |
| `cache:lifegraph:stats:{userId}` | String (JSON) | 30 minutes | Life Graph node/edge count aggregations |
| `job:queue:ai_processing` | List / Stream | Persistent until ACK | Asynchronous background processing queue |
| `lock:sync:{userId}` | String (SET NX) | 30 seconds | Distributed synchronization mutex |

---

## 6. Mobile Local Schema (Expo SQLite DDL)

```sql
CREATE TABLE IF NOT EXISTS local_memories (
    id TEXT PRIMARY KEY,
    remote_id TEXT NULL,
    type TEXT NOT NULL,
    title TEXT,
    description TEXT,
    occurred_at TEXT NOT NULL,
    latitude REAL,
    longitude REAL,
    location_name TEXT,
    tags TEXT,
    media_local_uri TEXT,
    sync_status TEXT CHECK(sync_status IN ('SYNCED', 'PENDING_INSERT', 'PENDING_UPDATE', 'PENDING_DELETE')) NOT NULL DEFAULT 'PENDING_INSERT',
    sync_version INTEGER DEFAULT 1,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS sync_queue (
    queue_id INTEGER PRIMARY KEY AUTOINCREMENT,
    entity_type TEXT NOT NULL,
    local_id TEXT NOT NULL,
    operation TEXT CHECK(operation IN ('INSERT', 'UPDATE', 'DELETE')) NOT NULL,
    payload TEXT NOT NULL,
    retry_count INTEGER DEFAULT 0,
    created_at TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_memories_sync ON local_memories(sync_status);
CREATE INDEX IF NOT EXISTS idx_memories_occurred ON local_memories(occurred_at DESC);
```
