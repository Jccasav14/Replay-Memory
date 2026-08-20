# REPLAY: SECURITY, PRIVACY BY DESIGN AND AUDIT CONTROLS

## 1. Privacy by Design Principles
Because **REPLAY** manages intimate autobiographical memories (loved ones, personal notes, private photographs, visited locations), privacy and data sovereignty are fundamental architectural foundations.

### 1.1. Data Minimization
1. **Gemini Ingestion Boundary**: Only the specific image or text required for semantic extraction is transmitted to Gemini API. **No user credentials, real names, emails, or authentication metadata are ever sent**.
2. **Local Isolation**: Raw files and sensitive JWT tokens are stored in hardware-isolated **Expo Secure Store** on mobile devices.

### 1.2. Right to Be Forgotten and Account Purge
* **Memory Deletion**:
  1. Soft-delete flag set in MongoDB (`isDeleted = true`).
  2. Immediate physical document deletion from Elasticsearch indices.
  3. Life Graph relationship disassociation.
  4. Physical binary deletion from storage (`storage/`).
* **Complete Account Hard-Delete (Cascade Purge)**:
  * Atomic transaction deleting user records in MongoDB (`users`, `memories`, `people`, `locations`, `objects`), purging Elasticsearch indices by `userId`, invalidating Redis sessions, and deleting the physical directory `storage/users/{userId}`.

---

## 2. Authentication and Password Security
* **JWT Implementation**: HMAC-SHA256 with 256-bit cryptographic keys (`JWT_SECRET`). 24-hour Access Tokens and 7-day Refresh Tokens with Redis-backed immediate blacklisting.
* **Password Hashing**: **Argon2id** (or **BCrypt** with cost factor $\ge 12$).

---

## 3. File Upload Hardening
1. **Magic Bytes Validation**: Verifies binary headers (e.g. `FF D8 FF` for JPEG, `89 50 4E 47` for PNG, `%PDF` for PDF) regardless of client extension or Content-Type header.
2. **Path Traversal Prevention**: Renames all incoming files to random UUIDs (`uuid.jpg`).
3. **Storage Boundary Isolation**: Storage directories remain outside the server's executable web root.
