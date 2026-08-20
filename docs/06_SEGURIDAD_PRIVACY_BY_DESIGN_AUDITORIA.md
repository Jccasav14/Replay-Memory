# REPLAY: SEGURIDAD, PRIVACY BY DESIGN Y AUDITORÍA

## 1. Filosofía Privacy by Design
Dado que **REPLAY** procesa la memoria íntima de una persona (ubicaciones, personas queridas, documentos personales, recuerdos fotográficos), la privacidad y la soberanía del dato no son agregados posteriores, sino los cimientos arquitectónicos del sistema.

### 1.1. Principio de Minimización de Datos
1. **Qué se envía a Gemini API**: Únicamente el contenido multimedia o texto estrictamente necesario para el análisis semántico y generación de embeddings. **Nunca se envían credenciales, identificadores reales de usuario, correos electrónicos ni metadatos de autenticación**.
2. **Qué permanece estrictamente local en el dispositivo**:
   * Claves de cifrado locales.
   * Tokens JWT protegidos en **Expo Secure Store** (KeyStore en Android / Keychain en iOS).
   * Archivos sin procesar antes de la sincronización explícita.

### 1.2. Derecho al Olvido y Eliminación Total de Cuenta (GDPR / ISO 27701)
* **Eliminación de un Recuerdo**:
  1. Soft delete en MongoDB (`isDeleted = true`).
  2. Eliminación física inmediata del índice de Elasticsearch para prevenir apariciones en búsquedas semánticas o textuales.
  3. Desvinculación de aristas en el Life Graph.
  4. Borrado físico del archivo binario en el sistema de almacenamiento (`storage/`).
* **Eliminación Definitiva de Cuenta (Hard Delete Cascada)**:
  * Ejecución de transacción atómica que elimina al usuario en MongoDB, borra todas sus colecciones (`memories`, `people`, `locations`, `objects`, `sync_operations`), purga todos los documentos de Elasticsearch con `userId == targetUserId`, revoca sesiones en Redis y elimina el directorio físico del usuario en `storage/users/{userId}`.

---

## 2. Estrategia de Autenticación y Autorización

### 2.1. Tokens JWT de Alta Seguridad
* **Algoritmo**: HMAC-SHA256 con clave secreta criptográfica de al menos 256 bits (`JWT_SECRET`).
* **Vigencia**:
  * **Access Token**: 24 horas (corta duración).
  * **Refresh Token**: 7 días (rotación obligatoria con revocación de tokens previos).
* **Almacenamiento Seguro**:
  * En Web: `HttpOnly`, `SameSite=Strict`, `Secure` cookies o memoria segura.
  * En Mobile: **Expo Secure Store** (aislamiento por hardware).

### 2.2. Hashing de Contraseñas
Se utiliza **Argon2id** (o **BCrypt** con factor de coste $\ge 12$) para garantizar protección extrema contra ataques de diccionario y fuerza bruta por GPU.

---

## 3. Seguridad en la Carga de Archivos (File Upload Security)
Para prevenir ataques de ejecución remota de código (RCE) o Server-Side Request Forgery (SSRF):
1. **Validación de Magic Bytes**: No confiar en la extensión `.jpg` o en la cabecera `Content-Type`. Se inspeccionan los primeros bytes binarios del archivo (p. ej., `FF D8 FF` para JPEG, `89 50 4E 47` para PNG, `%PDF` para PDF).
2. **Prevención de Path Traversal**: Todos los archivos se renombran a UUIDs v4 aleatorios (`66c4a1e9-b212-4a45-8901-23456789abcd.jpg`). Se rechaza cualquier nombre de archivo original que contenga `../` o caracteres no alfanuméricos.
3. **Límites de Tamaño**:
   * Imágenes: Máximo 15 MB.
   * Documentos: Máximo 25 MB.
   * Videos cortos: Máximo 100 MB.
4. **Aislamiento de Storage**: La carpeta `storage/` se encuentra fuera de la raíz web ejecutable del servidor.

---

## 4. Manejo Seguro de Errores y Registro de Auditoría
1. **Sin Fugas de Stack Trace**: En producción y desarrollo, los errores retornan un payload uniforme `ProblemDetail` (RFC 7807) sin exponer trazas de clases Java ni versiones de base de datos.
2. **Correlation ID**: Cada petición HTTP entrante recibe un identificador único `X-Correlation-Id` registrado en los logs estructurados (Logback / JSON) para trazabilidad forense.
