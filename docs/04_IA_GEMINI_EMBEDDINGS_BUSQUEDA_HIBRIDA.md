# REPLAY: ARQUITECTURA DE IA, GEMINI API, EMBEDDINGS Y BÚSQUEDA HÍBRIDA

## 1. Fundamentos de Embeddings y Representación Vectorial
Un **embedding** es una representación numérica de alta dimensión (vector denso en $\mathbb{R}^d$) que proyecta el significado semántico de un texto o concepto en un espacio continuo donde la cercanía geométrica (medida mediante la similitud del coseno) equivale a la similitud conceptual.

$$\text{Similitud Coseno}(\mathbf{u}, \mathbf{v}) = \frac{\mathbf{u} \cdot \mathbf{v}}{\|\mathbf{u}\|_2 \|\mathbf{v}\|_2}$$

* **Modelo Seleccionado**: `text-embedding-004` (Google AI).
* **Dimensionalidad**: $d = 768$.
* **Vectorización de Recuerdos**:
  Para cada recuerdo, se construye una cadena consolidada de contexto:
  ```text
  [TIPO: PHOTO] [FECHA: 2026-08-20] [LUGAR: Cafetería Campus] [PERSONAS: Carlos Mendoza] 
  [OBJETOS: Laptop ThinkPad, Café] [TÍTULO: Almuerzo de trabajo] 
  [DESCRIPCIÓN: Revisión del diseño de arquitectura de software para el proyecto final.]
  [ANÁLISIS IA: Dos personas dialogando frente a una computadora portátil con diagramas técnicos.]
  ```
  Este texto consolidado es enviado a la API de embeddings para obtener el vector de 768 flotantes indexado en Elasticsearch (`embeddingVector`).

---

## 2. Flujo de Integración Segura con Gemini API

> **PRINCIPIO DE SEGURIDAD ABSOLUTO**:
> 1. La API Key de Gemini reside **exclusivamente en el backend (Spring Boot)** en variables de entorno seguras.
> 2. Gemini **NO tiene acceso directo a la base de datos** MongoDB ni Elasticsearch.
> 3. Las respuestas de Gemini son **sintetizadas como JSON estructurado y validadas mediante DTOs estrictos** en Java antes de cualquier persistencia.

```text
[Cliente Móvil / Web]
        |
        v  (1. Envío de imagen + notas)
[Spring Boot Backend]
        |
        +---> Almacena archivo localmente (storage/images/uuid.jpg)
        |
        +---> (2. Invoca Gemini con Prompt Estructurado)
        v
[Google Gemini 1.5 Flash API]
        |
        v  (3. Retorna JSON Schema estricto)
[Spring Boot: AiResponseValidator & Sanitize]
        |
        +---> (4. Genera Embedding con text-embedding-004)
        |
        +---> (5. Guarda en MongoDB con estado PROCESSED)
        |
        +---> (6. Indexa en Elasticsearch: Text + Vector)
```

---

## 3. Prompts Estructurados y Manejo de Respuestas JSON

### Prompt de Análisis Multimodal de Fotografías
```json
{
  "system_instruction": "Eres el motor de análisis cognitivo de REPLAY, una plataforma de memoria personal. Tu objetivo es describir de manera objetiva, respetuosa y detallada el contenido de la imagen provista para facilitar su recuperación semántica futura. Responde EXCLUSIVAMENTE en formato JSON con la siguiente estructura.",
  "generation_config": {
    "response_mime_type": "application/json",
    "temperature": 0.2
  },
  "prompt": "Analiza la imagen adjunta y extrae la información requerida. Estructura esperada:\n{\n  \"summary\": \"Resumen conciso en 1 frase\",\n  \"detailedDescription\": \"Descripción exhaustiva de la escena, vestimenta, ambiente, iluminación\",\n  \"detectedObjects\": [\"objeto1\", \"objeto2\"],\n  \"detectedPeopleDescription\": [\"hombre joven con gafas\", \"mujer con chaqueta roja\"],\n  \"detectedContextCategory\": \"TRABAJO | FAMILIA | VIAJE | ESTUDIO | CELEBRACION | COTIDIANO\",\n  \"detectedEmotions\": [\"alegría\", \"concentración\"],\n  \"extractedTextOcr\": \"Cualquier texto legible en carteles o pantallas\"\n}"
}
```

---

## 4. Pipeline de Búsqueda Híbrida Inteligente (Full-Text + KNN + RAG)

Cuando el usuario consulta: *"¿Cuándo trabajé en la universidad con Carlos?"*

### Paso 1: Extracción de Intención y Entidades (Query Understanding)
El backend procesa la consulta con Gemini / Regex para extraer:
* **Entidades Clave**: Persona = `"Carlos"`, Lugar = `"Universidad"`.
* **Intención**: Temporal / Histórica (`TEMPORAL_QUERY`).

### Paso 2: Generación del Vector de Consulta
Se envía la frase a `text-embedding-004` obteniendo el vector $\mathbf{q} \in \mathbb{R}^{768}$.

### Paso 3: Ejecución de Consulta Híbrida en Elasticsearch
```json
{
  "query": {
    "bool": {
      "should": [
        {
          "multi_match": {
            "query": "universidad Carlos trabajé",
            "fields": ["title^3", "description^2", "aiSummary^2", "peopleNames^4", "locationName^4"],
            "fuzziness": "AUTO",
            "boost": 0.4
          }
        }
      ]
    }
  },
  "knn": {
    "field": "embeddingVector",
    "query_vector": [0.012, -0.045, ...],
    "k": 10,
    "num_candidates": 50,
    "boost": 0.6
  }
}
```

### Paso 4: Reranking y Fusión de Resultados (Reciprocal Rank Fusion)
Se combinan las puntuaciones textuales y vectoriales para seleccionar los Top 5 recuerdos más relevantes.

### Paso 5: Respuesta Basada en Evidencia (Grounded RAG)
El backend recupera los 5 documentos originales completos de MongoDB y envía el contexto a Gemini:
```text
CONTEXTO DE RECUERDOS RECUPERADOS:
- Recuerdo 1: [Fecha: 2026-08-20, Lugar: Campus Universitario, Personas: Carlos Mendoza] "Almuerzo de trabajo discutiendo arquitectura de software."
- Recuerdo 2: [Fecha: 2026-05-12, Lugar: Biblioteca Central, Personas: Carlos Mendoza] "Estudio para el examen de sistemas distribuidos."

PREGUNTA DEL USUARIO: "¿Cuándo fue la última vez que trabajé en la universidad con Carlos?"

INSTRUCCIÓN: Responde basándote ÚNICAMENTE en la evidencia provista. Si no hay información suficiente, indícalo claramente.
```
**Respuesta Generada**:
> *"La última vez registrada que trabajaste con Carlos en la universidad fue el 20 de agosto de 2026, durante un almuerzo de trabajo donde revisaron la arquitectura del proyecto final."*

---

## 5. Resiliencia, Rate Limiting y Circuit Breakers
1. **Timeouts**: Timeout estricto de 8 segundos en llamadas a Gemini.
2. **Circuit Breaker (Resilience4j)**: Si la API de Gemini devuelve errores 429 (Rate Limit) o 5xx repetidamente, el circuito se abre y el sistema encola las tareas en Redis para reintento diferido (Exponential Backoff: 2s, 4s, 8s, 16s, máx 5 reintentos).
3. **Fallback**: Si la IA está temporalmente no disponible, el recuerdo se guarda con estado `PENDING_AI` y queda accesible de inmediato mediante búsqueda textual básica y visualización en el Timeline.
