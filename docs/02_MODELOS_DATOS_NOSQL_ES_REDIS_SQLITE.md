# REPLAY: MODELOS DE DATOS Y ESTRATEGIAS DE PERSISTENCIA

## 1. Justificación de MongoDB como Fuente Principal de Datos
Los recuerdos humanos no se adaptan a un esquema tabular rígido con columnas fijas. Un recuerdo puede ser una simple nota de texto, una fotografía con 5 personas etiquetadas y 20 tags de visión computacional, un documento PDF con texto extraído, un evento geográfico con coordenadas y polígonos, o una combinación polimórfica de todos los anteriores.

MongoDB permite:
1. **Modelado Documental Polimórfico**: Alojar tipos heterogéneos de `Memory` (`PHOTO`, `NOTE`, `DOCUMENT`, `LOCATION_EVENT`, etc.) en una sola colección optimizada.
2. **Estructuras Embebidas vs Referenciadas**: Embeber metadatos inmutables o fuertemente acoplados (análisis de IA, coordenadas, metadatos EXIF) y referenciar entidades reutilizables con ciclo de vida independiente (`Person`, `Location`, `Object`).
3. **Alto Rendimiento en Consultas Temporales y Geoespaciales**: Índices compuestos `{ userId: 1, occurredAt: -1 }` y geoespaciales `2dsphere`.

---

## 2. Estrategia de Diseño: Embebido vs Referenciado
* **Documentos Embebidos (Embedded Documents)**:
  * Metadatos de análisis de IA (`ai_analysis` dentro de `Memory`).
  * Metadatos de archivos multimedia (`media_items` dentro de `Memory`).
  * Datos geoespaciales puntuales (`location_point` con coordenadas exactas del recuerdo).
  * Snapshots históricos (nombre de la persona en el momento en que se tomó la foto).
* **Referencias por ID (`@DBRef` o `ObjectId`)**:
  * `Person`: Una persona existe independientemente de si aparece en 1 o 100 recuerdos.
  * `Location` (Lugares conocidos): "Casa", "Universidad", "Oficina" tienen estadísticas agregadas.
  * `Object`: "Mi Laptop", "Mi Auto", "Pasaporte" tienen su propia línea de tiempo histórica.
  * `User`: Propietario de la información (Tenant isolation).

---

## 3. Esquemas de MongoDB (BSON / JSON Schema)

### 3.1. Colección `users`
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
**Índices:**
* `db.users.createIndex({ "email": 1 }, { unique: true })`

---

### 3.2. Colección `memories` (Colección Central)
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
**Índices:**
* `db.memories.createIndex({ "userId": 1, "occurredAt": -1 })`
* `db.memories.createIndex({ "userId": 1, "isDeleted": 1, "processingStatus": 1 })`
* `db.memories.createIndex({ "location.geoPoint": "2dsphere" })`
* `db.memories.createIndex({ "userId": 1, "peopleIds": 1 })`
* `db.memories.createIndex({ "userId": 1, "objectIds": 1 })`
* `db.memories.createIndex({ "userId": 1, "tags": 1 })`

---

### 3.3. Colecciones de Entidades del Life Graph (`people`, `locations`, `objects`)
* **`people`**:
  * Campos: `_id`, `userId`, `name`, `relationship` (amigo, familia, colega), `avatarStoragePath`, `notes`, `firstMetDate`, `interactionCount`, `createdAt`.
  * Índice: `{ userId: 1, name: 1 }`.
* **`locations`**:
  * Campos: `_id`, `userId`, `name`, `category` (HOGAR, TRABAJO, ESTUDIO, OCIO), `geoPoint`, `radiusMeters`, `visitCount`, `createdAt`.
  * Índice: `{ "geoPoint": "2dsphere" }`, `{ userId: 1, name: 1 }`.
* **`objects`**:
  * Campos: `_id`, `userId`, `name`, `category` (DISPOSITIVO, VEHICULO, HERRAMIENTA, JOYERIA), `serialNumber`, `acquisitionDate`, `photoStoragePath`, `createdAt`.
  * Índice: `{ userId: 1, name: 1 }`.

---

## 4. Mapeo de Elasticsearch (`replay_memories`)
Elasticsearch actúa como **índice de búsqueda derivado** y soporte para **KNN Vector Search**.

```json
{
  "mappings": {
    "properties": {
      "id": { "type": "keyword" },
      "userId": { "type": "keyword" },
      "type": { "type": "keyword" },
      "title": { 
        "type": "text",
        "analyzer": "spanish",
        "fields": {
          "suggest": { "type": "search_as_you_type" }
        }
      },
      "description": { 
        "type": "text", 
        "analyzer": "spanish" 
      },
      "aiSummary": { 
        "type": "text", 
        "analyzer": "spanish" 
      },
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

## 5. Taxonomía de Claves en Redis
Redis almacena exclusivamente datos efímeros, caché volátil, contadores y tokens:

| Patrón de Clave | Tipo Redis | TTL | Propósito |
| :--- | :--- | :--- | :--- |
| `auth:token:blacklist:{jti}` | String | Expiración del JWT | Revocación inmediata de tokens |
| `auth:refresh:{userId}:{deviceId}` | String | 7 días | Validación de Refresh Token seguro |
| `ratelimit:{ip}:{endpoint}` | String (INCR) | 1 minuto | Protección contra abuso / DoS |
| `cache:timeline:{userId}:{year}:{month}` | String (JSON) | 1 hora | Caché de Timeline mensual |
| `cache:lifegraph:stats:{userId}` | String (JSON) | 30 minutos | Conteo de nodos y aristas del Life Graph |
| `job:queue:ai_processing` | List / Stream | Persistente hasta ACK | Cola de procesamiento asíncrono |
| `lock:sync:{userId}` | String (SET NX) | 30 segundos | Distributed lock para evitar carreras de sync |

---

## 6. Esquema Local en Mobile (Expo SQLite)
Esquema DDL ejecutado en el dispositivo móvil:

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
    tags TEXT, -- JSON Array string
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
