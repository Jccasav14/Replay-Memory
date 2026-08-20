# REPLAY: RESTFUL API SPECIFICATION

All endpoints are prefixed by `/api/v1`. Protected routes require the standard header:
`Authorization: Bearer <JWT_ACCESS_TOKEN>`

---

## 1. Authentication and Device Management (`/api/v1/auth`)

### 1.1. User Registration
* **POST** `/auth/register`
* **Request Body**:
```json
{
  "email": "user@example.com",
  "password": "SecurePassword#2026",
  "fullName": "Jean Valjean"
}
```
* **Response (201 Created)**:
```json
{
  "userId": "66c4a1e9b2123a4567890123",
  "email": "user@example.com",
  "fullName": "Jean Valjean",
  "message": "User registered successfully."
}
```

### 1.2. User Login
* **POST** `/auth/login`
* **Request Body**:
```json
{
  "email": "user@example.com",
  "password": "SecurePassword#2026",
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
    "email": "user@example.com",
    "fullName": "Jean Valjean"
  }
}
```

### 1.3. Token Refresh
* **POST** `/auth/refresh`
* **Request Body**: `{ "refreshToken": "...", "deviceId": "..." }`
* **Response (200 OK)**: Returns new `accessToken` and `refreshToken` pair.

### 1.4. Logout
* **POST** `/auth/logout` (Blacklists token in Redis).

---

## 2. Memories Module (`/api/v1/memories`)

### 2.1. Create Memory (Multipart)
* **POST** `/memories` (`multipart/form-data`)
* **FormData Parameters**:
  * `data`: JSON string with payload (`type`, `title`, `description`, `occurredAt`, `latitude`, `longitude`, `locationName`, `peopleIds`, `objectIds`, `tags`).
  * `files`: Binary attachments (images, audios, PDFs).
* **Response (202 Accepted)**:
```json
{
  "id": "66c4b220b2123a4567890456",
  "type": "PHOTO",
  "title": "Working lunch with Carlos",
  "processingStatus": "PENDING_AI",
  "occurredAt": "2026-08-20T14:30:00Z",
  "mediaCount": 1,
  "createdAt": "2026-08-20T17:22:00Z"
}
```

### 2.2. List Paginated Memories
* **GET** `/memories?page=0&size=20&type=PHOTO&startDate=2026-01-01&endDate=2026-12-31`
* **Response (200 OK)**: Paginated response with total counts and items.

### 2.3. Get Memory Details
* **GET** `/memories/{id}`
* **Response (200 OK)**: Complete entity details including AI analysis and Life Graph associations.

### 2.4. Delete Memory
* **DELETE** `/memories/{id}`
* **Response (204 No Content)**: Soft-deletes document, removes index from Elasticsearch, enqueues storage cleanup.

---

## 3. Timeline Module (`/api/v1/timeline`)

### 3.1. Get Aggregated Timeline
* **GET** `/timeline?granularity=MONTH&year=2026&month=8`
* **Response (200 OK)**: Monthly grouping of days and daily memory items.

---

## 4. Intelligent Search Module (`/api/v1/search`)

### 4.1. Textual Search
* **GET** `/search?q=university+thesis&person=Carlos`
* **Response (200 OK)**: BM25 ranked memories with highlighted text snippets.

### 4.2. Semantic and Natural Language Query (Grounded RAG)
* **POST** `/search/semantic`
* **Request Body**:
```json
{
  "query": "When was the last time I worked at the university with Carlos?",
  "topK": 5,
  "generateAnswer": true
}
```
* **Response (200 OK)**:
```json
{
  "answer": "The last recorded time you worked with Carlos at the university was on August 20, 2026, during a working lunch where you discussed the project architecture.",
  "matchedMemories": [
    {
      "id": "66c4b220b2123a4567890456",
      "title": "Working lunch with Carlos",
      "occurredAt": "2026-08-20T14:30:00Z",
      "similarityScore": 0.894,
      "location": "University Campus"
    }
  ],
  "extractedEntities": {
    "people": ["Carlos"],
    "locations": ["University"],
    "intent": "TEMPORAL_QUERY"
  }
}
```

---

## 5. Offline Synchronization Module (`/api/v1/sync`)

### 5.1. Batch Synchronization
* **POST** `/sync/batch`
* **Request Body**:
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
        "title": "Quick note created offline",
        "description": "Buy backup battery",
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

## 6. Error Handling Format (RFC 7807 Problem Details)
All error responses adhere to standard format:
```json
{
  "type": "https://replay.app/errors/resource-not-found",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "Memory with ID 66c4b220b2123a4567890456 does not exist or does not belong to the authenticated user.",
  "instance": "/api/v1/memories/66c4b220b2123a4567890456",
  "timestamp": "2026-08-20T17:30:00Z",
  "correlationId": "req-98fa-4b12-98cd"
}
```
