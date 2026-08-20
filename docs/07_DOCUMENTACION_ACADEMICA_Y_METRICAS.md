# REPLAY: DOCUMENTACIÓN ACADÉMICA Y MARCO DE EVALUACIÓN CIENTÍFICO

## 1. Planteamiento del Problema
En la era contemporánea de la hiperdigitalización, los seres humanos generan y almacenan miles de fragmentos de información personal al mes (fotografías en la nube, notas efímeras, geolocalizaciones pasivas, boletos electrónicos, documentos en PDF). Sin embargo, este volumen masivo sufre de **fragmentación cognitiva y dispersión digital**: los recuerdos quedan sepultados en carpetas no relacionadas o silos de aplicaciones incompatibles.

Cuando un individuo intenta responder preguntas sobre su propia vida (p. ej., *"¿Cuándo fue la última vez que visité a mi abuelo con mi hermano?"* o *"¿En qué documento anoté las medidas del departamento que vi el año pasado?"*), los motores de búsqueda tradicionales basados en coincidencia léxica exacta fracasan debido a la ausencia de contexto y la falta de un grafo relacional espacio-temporal.

---

## 2. Objetivos

### 2.1. Objetivo General
Diseñar, implementar y evaluar una arquitectura de software distribuida y un motor de memoria digital personal denominado **REPLAY**, que integre modelos de inteligencia artificial multimodal, representaciones vectoriales densas y un grafo relacional de vida (**Life Graph**) para la captura, organización semántica y recuperación contextual de acontecimientos biográficos con soporte offline-first.

### 2.2. Objetivos Específicos
1. Desarrollar un backend modular resiliente en Spring Boot que orqueste la persistencia documental polimórfica en MongoDB, el almacenamiento en caché distribuido en Redis y la indexación híbrida en Elasticsearch.
2. Implementar un pipeline de inferencia con Google Gemini API y `text-embedding-004` para la extracción automática de entidades, resumen contextual y generación de embeddings densos de 768 dimensiones.
3. Diseñar una aplicación móvil multiplataforma en React Native / Expo con capacidades offline-first basadas en Expo SQLite y sincronización bidireccional tolerante a fallos.
4. Construir una interfaz web interactiva en React + Vite para la visualización cronológica (Timeline) y navegación topológica del Life Graph.
5. Evaluar cuantitativamente el rendimiento del sistema mediante métricas de recuperación de información (MRR, Recall@K), fidelidad de la IA (Groundedness) y eficiencia operacional móvil.

---

## 3. Estado del Arte y Antecedentes
* **Apple Memories / Google Photos**: Centrados primordialmente en visión computacional sobre imágenes, carentes de integración holística con documentos, notas contextuales complejas o consultas analíticas en lenguaje natural profundo.
* **Sistemas RAG (Retrieval-Augmented Generation)**: Ampliamente utilizados en entornos corporativos sobre bases de conocimiento estáticas, pero escasamente adaptados a grafos de vida personales dinámicos con restricciones móviles offline-first.
* **Memex (Vannevar Bush, 1945)**: Concepto pionero de dispositivo electromecánico para almacenar libros, registros y comunicaciones, indexados por asociación asociativa; REPLAY representa la materialización moderna de este paradigma mediante LLMs y embeddings vectoriales.

---

## 4. Métricas de Evaluación del Sistema

### 4.1. Métricas de Recuperación de Información (Search & Retrieval)

#### 1. Precision@K y Recall@K
$$\text{Precision}@K = \frac{|\text{Documentos Relevantes Recuperados en los Top } K|}{K}$$

$$\text{Recall}@K = \frac{|\text{Documentos Relevantes Recuperados en los Top } K|}{|\text{Total de Documentos Relevantes Existentes}|}$$

#### 2. Mean Reciprocal Rank (MRR)
Mide la capacidad del motor de ubicar el primer recuerdo correcto en la posición más alta posible:
$$\text{MRR} = \frac{1}{|Q|} \sum_{i=1}^{|Q|} \frac{1}{\text{rank}_i}$$
Donde $\text{rank}_i$ es la posición del primer recuerdo relevante para la consulta $i$.

---

### 4.2. Métricas de Fidelidad de Inteligencia Artificial (Groundedness)

#### 1. Índice de Fundamentación (Groundedness Score)
Proporción de afirmaciones generadas en la respuesta de Gemini que pueden ser mapeadas directamente a hechos verificables contenidos en los recuerdos recuperados de MongoDB:
$$\text{Groundedness} = \frac{\text{Afirmaciones Verificables en Contexto}}{\text{Total de Afirmaciones en la Respuesta}}$$
* **Meta**: $\text{Groundedness} \ge 0.95$ (Cero tolerancia a alucinaciones sobre la vida del usuario).

---

### 4.3. Métricas de Rendimiento de Sistema y Mobile
1. **Latencia de Búsqueda Híbrida**: $P_{95} \le 350\text{ ms}$ (en entorno local).
2. **Tiempo de Inferencia Asíncrona de IA**: $P_{95} \le 4.5\text{ s}$ por imagen procesada.
3. **Consumo de Batería en Mobile**: $\le 1.5\%$ de impacto por cada 24 horas de monitoreo discreto por geocercas.
4. **Disponibilidad Offline**: 100% de operaciones de lectura del Timeline local y creación de recuerdos operativas sin conexión a Internet.
