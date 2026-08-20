# REPLAY: ESPECIFICACIÓN DE LA API RESTful

Todas las rutas están prefijadas por `/api/v1`. Salvo en endpoints de autenticación pública, todas las peticiones requieren el encabezado:
`Authorization: Bearer <JWT_ACCESS_TOKEN>`

---

## 1. Módulo: Autenticación y Dispositivos (`/api/v1/auth`)

### 1.1. Registro de Usuario
* **POST** `/auth/register`
* **Request**:
```json
{
  "email": "jean.valjean@example.com",
  "password": "PasswordSegura#2026",
  "fullName": "Jean Valjean"
}
```
* **Response (201 Created)**:
```json
{
  "userId": "66c4a1e9b2123a4567890123",
  "email": "jean.valjean@example.com",
  "fullName": "Jean Valjean",
  "message": "Usuario registrado exitosamente."
}
```

### 1.2. Inicio de Sesión
* **POST** `/auth/login`
* **Request**:
```json
{
  "email": "jean.valjean@example.com",
  "password": "PasswordSegura#2026",
  "deviceId": "dev-ios-uuid-8831",
  "deviceName": "iPhone 15 Pro",
  "platform": "IOS"
}
```
* **Response (200 OK)**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsIn...",
  "refreshToken": "d8a7c6b5-4321-ef09-...",
  "tokenType": "Bearer",
  "expiresInMs": 86400000,
  "user": {
    "id": "66c4a1e9b2123a4567890123",
    "email": "jean.valjean@example.com",
    "fullName": "Jean Valjean"
  }
}
```

### 1.3. Refresco de Token
* **POST** `/auth/refresh`
* **Request**:
```json
{
  "refreshToken": "d8a7c6b5-4321-ef09-...",
  "deviceId": "dev-ios-uuid-8831"
}
```
* **Response (200 OK)**: Retorna nuevo par `accessToken` y `refreshToken`.

### 1.4. Cierre de Sesión / Revocación
* **POST** `/auth/logout` (Invalida token en Redis blacklist).

---

## 2. Módulo: Recuerdos (`/api/v1/memories`)

### 2.1. Crear Recuerdo (Multipart)
* **POST** `/memories` (Content-Type: `multipart/form-data`)
* **FormData Params**:
  * `data`: JSON String con metadatos (`type`, `title`, `description`, `occurredAt`, `latitude`, `longitude`, `locationName`, `peopleIds`, `objectIds`, `tags`).
  * `files`: Archivos binarios adjuntos (imágenes, audios, documentos).
* **Response (202 Accepted)**:
```json
{
  "id": "66c4b220b2123a4567890456",
  "type": "PHOTO",
  "title": "Almuerzo de trabajo con Carlos",
  "processingStatus": "PENDING_AI",
  "occurredAt": "2026-08-20T14:30:00Z",
  "mediaCount": 1,
  "createdAt": "2026-08-20T17:22:00Z"
}
```

### 2.2. Listar Recuerdos Paginados y Filtrados
* **GET** `/memories?page=0&size=20&type=PHOTO&startDate=2026-01-01&endDate=2026-12-31&personId=...`
* **Response (200 OK)**:
```json
{
  "content": [
    {
      "id": "66c4b220b2123a4567890456",
      "type": "PHOTO",
      "title": "Almuerzo de trabajo con Carlos",
      "description": "Reunión técnica sobre arquitectura",
      "occurredAt": "2026-08-20T14:30:00Z",
      "media": [
        {
          "mediaId": "66c4b220b2123a4567890789",
          "url": "/api/v1/media/66c4b220b2123a4567890789/preview",
          "mimeType": "image/jpeg"
        }
      ],
      "tags": ["reunión", "universidad", "arquitectura"],
      "aiSummary": "Discusión técnica en la cafetería del campus.",
      "processingStatus": "PROCESSED"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### 2.3. Obtener Recuerdo por ID
* **GET** `/memories/{id}`
* **Response (200 OK)**: Detalle completo incluyendo Life Graph links y análisis extendido de IA.

### 2.4. Actualizar Recuerdo
* **PUT** `/memories/{id}`
* **Request**: JSON con campos editables.
* **Response (200 OK)**.

### 2.5. Eliminar Recuerdo (Soft Delete)
* **DELETE** `/memories/{id}`
* **Response (204 No Content)**: Marca `isDeleted: true`, elimina de Elasticsearch y encola limpieza de storage.

---

## 3. Módulo: Timeline (`/api/v1/timeline`)

### 3.1. Obtener Vista Agrupada de la Línea Temporal
* **GET** `/timeline?granularity=MONTH&year=2026&month=8`
* **Response (200 OK)**:
```json
{
  "year": 2026,
  "month": 8,
  "totalMemories": 42,
  "days": [
    {
      "date": "2026-08-20",
      "memoryCount": 3,
      "memories": [
        {
          "id": "66c4b220b2123a4567890456",
          "time": "14:30",
          "type": "PHOTO",
          "title": "Almuerzo de trabajo con Carlos",
          "locationName": "Campus Universitario"
        }
      ]
    }
  ]
}
```

---

## 4. Módulo: Búsqueda Inteligente (`/api/v1/search`)

### 4.1. Búsqueda Textual y Filtros
* **GET** `/search?q=tesis+universidad&person=Carlos&startDate=2026-01-01`
* **Response (200 OK)**: Resultados rankeados por BM25 con fragmentos resaltados (highlights).

### 4.2. Búsqueda Semántica con IA y RAG
* **POST** `/search/semantic`
* **Request**:
```json
{
  "query": "¿Cuándo fue la última vez que trabajé en la universidad con Carlos?",
  "topK": 5,
  "generateAnswer": true
}
```
* **Response (200 OK)**:
```json
{
  "answer": "La última vez registrada que trabajaste con Carlos en la universidad fue el 20 de agosto de 2026, durante un almuerzo de trabajo donde discutieron la arquitectura de la tesis.",
  "matchedMemories": [
    {
      "id": "66c4b220b2123a4567890456",
      "title": "Almuerzo de trabajo con Carlos",
      "occurredAt": "2026-08-20T14:30:00Z",
      "similarityScore": 0.894,
      "location": "Campus Universitario"
    }
  ],
  "extractedEntities": {
    "people": ["Carlos"],
    "locations": ["Universidad"],
    "intent": "TEMPORAL_QUERY"
  }
}
```

---

## 5. Módulo: Life Graph (`/api/v1/graph`)

### 5.1. Obtener Grafo de Conexiones
* **GET** `/graph/nodes?centerEntity=USER&depth=2`
* **Response (200 OK)**: Nodos (`nodes`: People, Locations, Objects, Memories) y Aristas (`edges`: INVOLVES, LOCATED_AT, OWNS).

---

## 6. Módulo: Sincronización Offline (`/api/v1/sync`)

### 6.1. Procesamiento de Lote Offline
* **POST** `/sync/batch`
* **Request**:
```json
{
  "deviceId": "dev-ios-uuid-8831",
  "lastSyncTimestamp": "2026-08-20T10:00:00Z",
  "operations": [
    {
      "localId": "loc-uuid-101",
      "operation": "INSERT",
      "entityType": "MEMORY",
      "occurredAt": "2026-08-20T11:15:00Z",
      "data": {
        "title": "Nota rápida sin red",
        "description": "Comprar batería de repuesto",
        "type": "NOTE"
      }
    }
  ]
}
```
* **Response (200 OK)**:
```json
{
  "status": "SUCCESS",
  "processedAt": "2026-08-20T17:25:00Z",
  "mappings": [
    { "localId": "loc-uuid-101", "remoteId": "66c4b999b2123a4567890999", "status": "SYNCED" }
  ],
  "serverChanges": []
}
```

---

## 7. Manejo Centralizado de Errores (RFC 7807 Problem Details)
Todas las respuestas de error devuelven:
```json
{
  "type": "https://replay.app/errors/resource-not-found",
  "title": "Recurso no encontrado",
  "status": 404,
  "detail": "El recuerdo con ID 66c4b220b2123a4567890456 no existe o no le pertenece al usuario.",
  "instance": "/api/v1/memories/66c4b220b2123a4567890456",
  "timestamp": "2026-08-20T17:30:00Z",
  "correlationId": "req-98fa-4b12-98cd"
}
```
