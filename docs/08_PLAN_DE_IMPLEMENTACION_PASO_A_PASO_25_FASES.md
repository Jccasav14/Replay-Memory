# REPLAY: PLAN DE IMPLEMENTACIÓN MAESTRO PASO A PASO (25 FASES)

Este documento es la **guía de ingeniería exhaustiva y cronológica** para construir la plataforma **REPLAY** desde un repositorio en blanco hasta una versión de producción validada.

---

## Estructura Integral de Fases

```text
[Fase 01-04]: Infraestructura Base & Docker (MongoDB, Elasticsearch, Redis, Spring Boot Monolith)
[Fase 05-07]: Clientes Base & Autenticación (Expo Mobile, Vite React Web, Spring Security JWT)
[Fase 08-10]: Núcleo de Recuerdos & Multimedia (CRUD Memories, Local File Storage, Expo Camera/Picker)
[Fase 11-15]: Motor Cognitivo de IA & Búsqueda (Gemini Vision/Text, Dense Embeddings, Elasticsearch Hybrid Search)
[Fase 16-17]: Modelado Avanzado (Timeline Visualizer, Life Graph Relational Network)
[Fase 18-21]: Capacidades Móviles Críticas (Expo SQLite Offline Sync, Background Geofencing, Push Notifications)
[Fase 22-25]: Aseguramiento & Producción (Security Hardening, Test Suite, Dev Build, EAS Release APK/AAB)
```

---

### Fase 01: Inicialización del Repositorio e Infraestructura Docker
* **Objetivo**: Configurar el control de versiones y los contenedores de persistencia local (MongoDB, Elasticsearch, Redis) con healthchecks.
* **Tecnologías**: Git, Docker Engine, Docker Compose v2.
* **Carpetas y Archivos**:
  * `infrastructure/docker/docker-compose.yml`
  * `.env.example`, `.env`, `.gitignore`
* **Comandos**:
  ```bash
  docker compose -f infrastructure/docker/docker-compose.yml up -d
  docker compose -f infrastructure/docker/docker-compose.yml ps
  ```
* **Pruebas de Validación**:
  * `curl http://localhost:9200` $\rightarrow$ Retorna cluster name de Elasticsearch 8.x.
  * `mongosh --port 27017 -u admin -p replay_secure_password_2026` $\rightarrow$ Conexión exitosa.
  * `redis-cli -a replay_redis_password ping` $\rightarrow$ Retorna `PONG`.
* **Criterio de Aceptación**: Los 3 servicios en estado `healthy` y con volúmenes persistentes montados.

---

### Fase 02: Inicialización del Backend Spring Boot (Monolito Modular)
* **Objetivo**: Crear el proyecto base en Java con Maven y configurar las conexiones de datos y métricas Actuator.
* **Tecnologías**: Java 17/21, Spring Boot 3.2+, Maven, Spring Data MongoDB, Spring Data Redis, Elasticsearch Java Client.
* **Estructura de Carpetas**:
  ```text
  backend/
  ├── pom.xml
  └── src/main/java/com/replay/
      ├── ReplayApplication.java
      ├── config/ (MongoConfig, RedisConfig, ElasticConfig, StorageConfig)
      └── common/ (exception, response, util)
  ```
* **Comandos**:
  ```bash
  cd backend
  ./mvnw clean compile
  ./mvnw spring-boot:run
  ```
* **Pruebas de Validación**:
  * Acceso a `http://localhost:8080/actuator/health` retorna `{"status":"UP","components":{"mongo":{"status":"UP"},"redis":{"status":"UP"},"elasticsearch":{"status":"UP"}}}`.
* **Criterio de Aceptación**: El backend arranca sin excepciones y valida la conectividad con los 3 motores.

---

### Fase 03: Módulo de Seguridad y Autenticación JWT
* **Objetivo**: Implementar registro, login, refresh token y revocación en Redis con Spring Security.
* **Tecnologías**: Spring Security, JJWT (io.jsonwebtoken 0.12+), BCryptPasswordEncoder / Argon2.
* **Archivos Clave**:
  * `backend/src/main/java/com/replay/auth/` (`AuthController`, `AuthService`, `JwtTokenProvider`, `JwtAuthFilter`, `SecurityConfig`).
* **Pruebas**:
  * POST `/api/v1/auth/register` $\rightarrow$ Crea usuario en MongoDB con contraseña hasheada.
  * POST `/api/v1/auth/login` $\rightarrow$ Genera par `accessToken` y `refreshToken`.
* **Criterio de Aceptación**: Endpoints protegidos devuelven 401 si no hay token o 403 si el token expiró/revocado.

---

### Fase 04: Almacenamiento de Archivos Local y Media Module
* **Objetivo**: Crear servicio de subida y lectura de archivos con validación MIME, cálculo de SHA-256 y thumbnails.
* **Tecnologías**: Java NIO, Thumbnailator, Apache Tika.
* **Estructura**:
  * `storage/images/`, `storage/thumbnails/`, `storage/documents/`
  * `backend/src/main/java/com/replay/media/` (`StorageService`, `LocalStorageServiceImpl`, `MediaController`).
* **Pruebas**: Subir una imagen JPEG de 2MB; verificar guardado con nombre UUID, generación de thumbnail de 200x200 y respuesta con URL local.
* **Criterio de Aceptación**: Prevención de path traversal y validación exitosa de magic bytes.

---

### Fase 05: Inicialización de la Aplicación Móvil (React Native + Expo)
* **Objetivo**: Inicializar el cliente móvil con TypeScript, navegación por tabs y Expo SecureStore.
* **Tecnologías**: Expo SDK 51+, React Native 0.74+, React Navigation, TypeScript.
* **Estructura**:
  ```text
  mobile/
  ├── app.json
  ├── tsconfig.json
  └── src/
      ├── navigation/ (RootNavigator, TabNavigator, AuthNavigator)
      ├── screens/ (LoginScreen, HomeScreen, TimelineScreen, CreateMemoryScreen)
      ├── services/ (apiClient.ts, authStorage.ts)
      └── hooks/ (useAuth.ts)
  ```
* **Comandos**:
  ```bash
  cd mobile
  npx create-expo-app@latest ./ --template blank-typescript
  npx expo start
  ```
* **Pruebas**: Iniciar sesión desde el emulador Android / simulador iOS; guardar JWT en `SecureStore` y navegar a la pantalla Home.
* **Criterio de Aceptación**: Flujo de autenticación móvil persistente entre reinicios de app.

---

### Fase 06: Inicialización de la Aplicación Web (React + Vite)
* **Objetivo**: Crear el portal web administrativo con diseño responsivo, Tailwind CSS / Vanilla CSS y Axios interceptors.
* **Tecnologías**: React 18+, Vite, TypeScript, Lucide Icons, React Router DOM 6+.
* **Estructura**:
  ```text
  web/
  ├── vite.config.ts
  └── src/
      ├── components/ (Navbar, Sidebar, MemoryCard, TimelineGrid)
      ├── pages/ (DashboardPage, TimelinePage, LifeGraphPage, SearchPage)
      ├── services/ (api.ts, authService.ts, memoryService.ts)
      └── context/ (AuthContext.tsx)
  ```
* **Comandos**:
  ```bash
  cd web
  npm create vite@latest ./ -- --template react-ts
  npm install
  npm run dev
  ```
* **Pruebas**: Login desde navegador web en `http://localhost:5173`; acceso protegido al Dashboard.
* **Criterio de Aceptación**: Interceptores de Axios adjuntan automáticamente el token Bearer y manejan el refresco ante error 401.

---

### Fase 07: Modelo y CRUD de Recuerdos (Memories Module)
* **Objetivo**: Implementar en backend y clientes la creación, consulta paginada, detalle, edición y soft-delete de recuerdos.
* **Colecciones**: `memories` en MongoDB.
* **Pruebas**:
  * Petición POST `/api/v1/memories` con imagen y notas.
  * Petición GET `/api/v1/memories?page=0&size=10` con orden descendente por fecha.
* **Criterio de Aceptación**: Los recuerdos se persisten con estado inicial `PENDING_AI`.

---

### Fase 08: Integración de Cámara y Galería Móvil (Expo Camera & Picker)
* **Objetivo**: Permitir al usuario capturar fotografías en vivo o seleccionarlas de su carrete con metadatos EXIF.
* **Tecnologías**: `expo-camera`, `expo-image-picker`, `expo-media-library`.
* **Pruebas**: Capturar foto con la cámara del dispositivo; previsualizarla en `CreateMemoryScreen` y enviarla mediante FormData al backend.
* **Criterio de Aceptación**: Flujo completo de captura $\rightarrow$ subida $\rightarrow$ confirmación visual en pantalla.

---

### Fase 09: Cola Asíncrona de Procesamiento de Trabajos (Redis Worker)
* **Objetivo**: Desacoplar la recepción de recuerdos del procesamiento pesado de IA.
* **Tecnologías**: Redis Streams / Redis Queues, Spring `@Async` / TaskExecutor con ThreadPool dedicado.
* **Componentes**: `MemoryProcessingProducer`, `MemoryProcessingConsumer`, `AiJobPayload`.
* **Pruebas**: Subir 10 fotos simultáneas; verificar respuesta HTTP 202 inmediata y procesamiento ordenado en los logs del worker.
* **Criterio de Aceptación**: Ninguna llamada HTTP se bloquea por más de 500ms al subir un recuerdo.

---

### Fase 10: Integración con Google Gemini API (Vision & Text)
* **Objetivo**: Conectar el worker de backend con Gemini 1.5 Flash para análisis multimodal y extracción de JSON estructurado.
* **Tecnologías**: Google GenAI Java SDK / Spring RestClient, Gemini 1.5 Flash.
* **Configuración**: `GEMINI_API_KEY` inyectada en `@ConfigurationProperties`.
* **Pruebas**: Enviar fotografía de un viaje a la playa; verificar que el JSON retorne resumen, objetos (`"sombrilla"`, `"mar"`) y categoría `"VIAJE"`.
* **Criterio de Aceptación**: Validación estricta del JSON retornado y persistencia en el campo `aiAnalysis` de MongoDB.

---

### Fase 11: Generación de Embeddings Vectoriales (text-embedding-004)
* **Objetivo**: Construir la cadena contextual de cada recuerdo y generar su vector de 768 dimensiones.
* **Tecnologías**: Gemini Embeddings API (`text-embedding-004`), Java DTOs.
* **Pruebas**: Validar que el vector generado tenga exactamente 768 números flotantes y una norma Euclidiana aproximada a 1.0.
* **Criterio de Aceptación**: Persistencia del vector en MongoDB y preparación para indexación en Elasticsearch.

---

### Fase 12: Indexación y Búsqueda Full-Text en Elasticsearch
* **Objetivo**: Crear el índice `replay_memories` con analizador en español y sincronizar los recuerdos procesados.
* **Tecnologías**: Elasticsearch 8.x Java Client, Mappings con `search_as_you_type` y `dense_vector`.
* **Pruebas**: Ejecutar búsqueda con comodines y fuzzy (`"almueso"` $\rightarrow$ encuentra `"Almuerzo"`).
* **Criterio de Aceptación**: Indexación automática tras el procesamiento del worker y borrado al eliminar el recuerdo.

---

### Fase 13: Búsqueda Semántica Vectorial (KNN Cosine Similarity)
* **Objetivo**: Implementar búsqueda vectorial en Elasticsearch comparando el vector de consulta con los recuerdos almacenados.
* **Pruebas**: Consultar *"momento relajante junto al agua"* y recuperar la foto de la playa sin que coincidan palabras exactas.
* **Criterio de Aceptación**: Retorno de recuerdos con similarity score mayor a 0.75.

---

### Fase 14: Motor de Búsqueda Híbrida y RAG Grounded
* **Objetivo**: Integrar BM25 + KNN en una sola consulta con Reciprocal Rank Fusion y generar respuestas redactadas con Gemini.
* **Endpoint**: POST `/api/v1/search/semantic`.
* **Pruebas**: Consultar *"¿Cuándo compré mi laptop?"* $\rightarrow$ Gemini responde con fecha exacta citando el recuerdo correspondiente.
* **Criterio de Aceptación**: Las respuestas generadas solo contienen información verificable en los recuerdos adjuntos en el prompt.

---

### Fase 15: Sistema de Línea Temporal (Timeline Visualizer)
* **Objetivo**: Construir la vista cronológica agrupada por año, mes y día en web y mobile con filtros multidimensionales.
* **Componentes**: `TimelineView.tsx`, `TimelineFilterBar.tsx`, scroll infinito virtualizado.
* **Pruebas**: Navegar por la línea temporal y filtrar por persona `"Carlos"` o categoría `"TRABAJO"`.
* **Criterio de Aceptación**: Renderizado fluido a 60fps con cientos de recuerdos cargados.

---

### Fase 16: Life Graph: Grafo Relacional de Vida
* **Objetivo**: Gestionar entidades de personas, lugares y objetos y calcular sus relaciones estadísticas y topológicas.
* **Colecciones**: `people`, `locations`, `objects`.
* **Visualización**: Componente interactivo en Web con visualizador de grafos (Canvas 2D / SVG interactivo).
* **Pruebas**: Hacer clic en un nodo de persona (Carlos) y desplegar todos los recuerdos, lugares y objetos en los que ha coincidido con el usuario.
* **Criterio de Aceptación**: Navegación bidireccional entre recuerdos y nodos del Life Graph.

---

### Fase 17: Almacenamiento Local Offline en Mobile (Expo SQLite)
* **Objetivo**: Implementar la capa de base de datos SQLite en el dispositivo para operaciones sin conexión.
* **Tecnologías**: `expo-sqlite`, SQLite DDL migrations.
* **Pruebas**: Poner el dispositivo en Modo Avión, crear un nuevo recuerdo; verificar que se inserte en `local_memories` con estado `PENDING_INSERT`.
* **Criterio de Aceptación**: La app es 100% interactiva en lectura y escritura local sin red.

---

### Fase 18: Motor de Sincronización Bidireccional Mobile $\leftrightarrow$ Backend
* **Objetivo**: Implementar el protocolo de sincronización por lotes (`/api/v1/sync/batch`) con resolución de conflictos LWW.
* **Tecnologías**: `@react-native-community/netinfo`, `SyncManager.ts`.
* **Pruebas**: Desactivar Modo Avión; el `SyncManager` detecta conexión, envía la cola pendiente y actualiza los IDs locales con los IDs definitivos de MongoDB.
* **Criterio de Aceptación**: Cero duplicados y sincronización limpia garantizada.

---

### Fase 19: Geocercas y Tareas en Segundo Plano (Expo TaskManager & Location)
* **Objetivo**: Registrar eventos de entrada y salida a lugares habituales (Life Graph Locations) de forma eficiente.
* **Tecnologías**: `expo-location`, `expo-task-manager`.
* **Pruebas**: Simular cambio de ubicación en el emulador entrando a la geocerca de la "Universidad"; verificar la inserción en segundo plano de un `LOCATION_EVENT`.
* **Criterio de Aceptación**: Consumo despreciable de batería sin polling GPS continuo.

---

### Fase 20: Sistema de Notificaciones Inteligentes (Expo Notifications)
* **Objetivo**: Enviar notificaciones locales y recordatorios discretos de sincronización o recapitulaciones semanales.
* **Tecnologías**: `expo-notifications`.
* **Pruebas**: Disparar notificación local cuando existan más de 5 recuerdos pendientes de sincronizar en Wi-Fi.
* **Criterio de Aceptación**: Notificaciones respetuosas sin spam.

---

### Fase 21: Blindaje de Seguridad, Privacidad y Hardening
* **Objetivo**: Implementar rate limiting en Redis, validación de Magic Bytes, headers de seguridad OWASP y borrado seguro de cuentas.
* **Pruebas**: Intentar subir un script PHP disfrazado de `.jpg` $\rightarrow$ Backend rechaza con 400 Bad Request; solicitar eliminación total de cuenta y verificar purga en MongoDB, ES, Redis y disco.
* **Criterio de Aceptación**: Cumplimiento de Privacy by Design.

---

### Fase 22: Suite de Pruebas Automatizadas (Unit, Integration & E2E)
* **Objetivo**: Garantizar la robustez del sistema mediante tests unitarios y de integración con Testcontainers.
* **Tecnologías**: JUnit 5, Mockito, Testcontainers (Mongo/ES/Redis), Vitest, React Native Testing Library.
* **Comandos**:
  ```bash
  cd backend && ./mvnw test
  cd web && npm test
  cd mobile && npm test
  ```
* **Criterio de Aceptación**: Cobertura de código $\ge 80\%$ en lógica de negocio crítica.

---

### Fase 23: Transición a Expo Development Build
* **Objetivo**: Salir de Expo Go para compilar el cliente con soporte nativo completo para background tasks y geofencing.
* **Comandos**:
  ```bash
  cd mobile
  npx expo install expo-dev-client
  npx expo prebuild
  ```
* **Criterio de Aceptación**: App corriendo en emulador como binario nativo de desarrollo (`com.replay.app`).

---

### Fase 24: Generación de Binarios de Producción con EAS Build
* **Objetivo**: Configurar `eas.json` y generar el archivo instalable APK / AAB para Android y `.ipa` para iOS.
* **Tecnologías**: EAS CLI (`eas-cli`), Expo Application Services.
* **Comandos**:
  ```bash
  npm install -g eas-cli
  eas login
  eas build:configure
  eas build --platform android --profile preview
  ```
* **Criterio de Aceptación**: Descarga e instalación exitosa del archivo `.apk` autónomo en un teléfono físico Android.

---

### Fase 25: Auditoría Final, Métricas y Despliegue Local Consolidado
* **Objetivo**: Validar el funcionamiento integral del ecosistema REPLAY (Docker + Backend + Web + Mobile APK).
* **Pruebas**: Ejecución de las 10 consultas complejas de prueba; cálculo de MRR, Precision@K y Groundedness Score.
* **Criterio de Aceptación**: Todos los flujos operativos al 100%, documentación técnica completa y sistema listo para demostración o migración a producción.
