# REPLAY: PERSONAL MEMORY ENGINE

> **REPLAY es una plataforma de memoria digital personal que permite a una persona capturar, organizar, relacionar, buscar y reconstruir acontecimientos de su vida mediante fotografías, documentos, ubicaciones, notas, personas, objetos y otros eventos. La aplicación utiliza inteligencia artificial para convertir información dispersa en recuerdos estructurados y permitir búsquedas semánticas sobre la vida del usuario.**

---

## Diferenciador Clave: "Personal Memory Engine" y "Life Graph"
A diferencia de simples galerías de fotos o aplicaciones de notas tradicionales, **REPLAY** actúa como un motor de memoria cognitiva que estructura la biografía del usuario en un grafo multidimensional (**Life Graph**) y ofrece un pipeline híbrido de búsqueda RAG (Retrieval-Augmented Generation) basado en evidencia histórica.

---

## Stack Tecnologico

| Capa | Tecnologias Seleccionadas |
| :--- | :--- |
| **Mobile** | React Native, Expo (TypeScript), Expo SQLite, Camera, Location, TaskManager, SecureStore |
| **Web** | React, Vite, TypeScript, Tailwind CSS / Vanilla CSS |
| **Backend** | Java 17/21, Spring Boot 3.x, Spring Security, JWT, Maven |
| **Persistencia Principal** | MongoDB 7.x (Fuente de Verdad de Recuerdos y Life Graph) |
| **Busqueda y Vectores** | Elasticsearch 8.x (Full-Text BM25 + KNN Dense Vector Search 768d) |
| **Cache y Asincronismo** | Redis 7.x (Cache, Rate Limiting, Colas de Procesamiento, Distributed Locks) |
| **Inteligencia Artificial** | Google Gemini API (Vision 1.5 Flash, Text 1.5 Flash, Embeddings-004) |
| **Infraestructura Local** | Docker y Docker Compose |

---

## Estructura del Repositorio

```text
replay/
│
├── mobile/                  # Aplicacion movil (React Native + Expo TS)
│   ├── src/
│   ├── app.json
│   └── package.json
│
├── web/                     # Aplicacion web (React + Vite TS)
│   ├── src/
│   ├── vite.config.ts
│   └── package.json
│
├── backend/                 # Monolito Modular (Spring Boot + Maven)
│   ├── src/
│   └── pom.xml
│
├── storage/                 # Almacenamiento local de archivos binarios
│   ├── images/
│   ├── thumbnails/
│   ├── documents/
│   └── videos/
│
├── infrastructure/          # Infraestructura local y contenedores
│   └── docker/
│       └── docker-compose.yml
│
├── docs/                    # Documentacion tecnica y academica completa
│   ├── 01_ARQUITECTURA_GENERAL_Y_DIAGRAMAS.md
│   ├── 02_MODELOS_DATOS_NOSQL_ES_REDIS_SQLITE.md
│   ├── 03_API_REST_ESPECIFICACION_ENDPOINTS.md
│   ├── 04_IA_GEMINI_EMBEDDINGS_BUSQUEDA_HIBRIDA.md
│   ├── 05_OFFLINE_FIRST_SYNC_BACKGROUND_MOBILE.md
│   ├── 06_SEGURIDAD_PRIVACY_BY_DESIGN_AUDITORIA.md
│   ├── 07_DOCUMENTACION_ACADEMICA_Y_METRICAS.md
│   └── 08_PLAN_DE_IMPLEMENTACION_PASO_A_PASO_25_FASES.md
│
├── .env.example             # Plantilla de variables de entorno
└── README.md                # Este archivo
```

---

## Inicio Rapido en Entorno Local

### 1. Iniciar Persistencia y Motores de Busqueda (Docker)
```bash
# Copiar variables de entorno
cp .env.example .env

# Levantar MongoDB, Elasticsearch y Redis
docker compose -f infrastructure/docker/docker-compose.yml up -d

# Verificar estado de los contenedores
docker compose -f infrastructure/docker/docker-compose.yml ps
```

### 2. Iniciar Backend (Spring Boot)
```bash
cd backend
./mvnw spring-boot:run
# Health check disponible en: http://localhost:8080/actuator/health
```

### 3. Iniciar Aplicacion Web (React + Vite)
```bash
cd web
npm install
npm run dev
# Acceso en: http://localhost:5173
```

### 4. Iniciar Aplicacion Movil (Expo)
```bash
cd mobile
npm install
npx expo start
```

---

## Mapa de Documentacion
Consulte los documentos en la carpeta `docs/` para especificaciones detalladas:
* [01. Arquitectura General y Diagramas Mermaid](docs/01_ARQUITECTURA_GENERAL_Y_DIAGRAMAS.md)
* [02. Modelos de Datos NoSQL, Elasticsearch, Redis y SQLite](docs/02_MODELOS_DATOS_NOSQL_ES_REDIS_SQLITE.md)
* [03. Especificacion Completa de la API REST](docs/03_API_REST_ESPECIFICACION_ENDPOINTS.md)
* [04. Integracion de Gemini, Embeddings y Busqueda Hibrida](docs/04_IA_GEMINI_EMBEDDINGS_BUSQUEDA_HIBRIDA.md)
* [05. Offline-First, Sincronizacion y Limites de Background Mobile](docs/05_OFFLINE_FIRST_SYNC_BACKGROUND_MOBILE.md)
* [06. Seguridad, Privacy by Design y Hardening](docs/06_SEGURIDAD_PRIVACY_BY_DESIGN_AUDITORIA.md)
* [07. Marco Academico, Estado del Arte y Metricas de Evaluacion](docs/07_DOCUMENTACION_ACADEMICA_Y_METRICAS.md)
* [08. Plan Maestro de Implementacion Paso a Paso (25 Fases)](docs/08_PLAN_DE_IMPLEMENTACION_PASO_A_PASO_25_FASES.md)
