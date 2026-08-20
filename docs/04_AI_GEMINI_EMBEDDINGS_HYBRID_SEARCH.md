# REPLAY: AI ARCHITECTURE, GEMINI API, EMBEDDINGS AND HYBRID SEARCH

## 1. Vector Embeddings and Dense Representations
A **vector embedding** is a dense continuous numerical representation ($\mathbf{v} \in \mathbb{R}^d$) mapping textual and visual semantic meaning into a geometric vector space where Euclidean distance or Cosine Similarity reflects conceptual closeness.

$$\text{Cosine Similarity}(\mathbf{u}, \mathbf{v}) = \frac{\mathbf{u} \cdot \mathbf{v}}{\|\mathbf{u}\|_2 \|\mathbf{v}\|_2}$$

* **Selected Model**: `text-embedding-004` (Google AI).
* **Dimensionality**: $d = 768$.
* **Memory Canonical Context Construction**:
  ```text
  [TYPE: PHOTO] [DATE: 2026-08-20] [LOCATION: Campus Cafeteria] [PEOPLE: Carlos Mendoza]
  [OBJECTS: ThinkPad Laptop, Coffee] [TITLE: Working lunch]
  [DESCRIPTION: Architectural review for software engineering thesis project.]
  [AI ANALYSIS: Two individuals discussing software diagrams in front of a laptop computer.]
  ```

---

## 2. Secure Gemini API Integration Principles

> **CRITICAL SECURITY RULES**:
> 1. The `GEMINI_API_KEY` resides **exclusively within the Spring Boot backend** as an environment variable.
> 2. Gemini has **NO direct access to MongoDB or Elasticsearch**.
> 3. AI outputs are structured strictly as JSON and validated via Java DTOs before persistence.

```text
[Client: Mobile / Web]
        |
        v  (1. Upload media + metadata)
[Spring Boot Backend]
        |
        +---> Saves binary to local file storage (storage/images/uuid.jpg)
        |
        +---> (2. Invokes Gemini with Structured Schema)
        v
[Google Gemini 1.5 Flash API]
        |
        v  (3. Returns Strict JSON)
[Spring Boot: DTO Validator & Sanitizer]
        |
        +---> (4. Generates Vector Embedding with text-embedding-004)
        |
        +---> (5. Saves to MongoDB with status PROCESSED)
        |
        +---> (6. Indexes into Elasticsearch: BM25 text + Dense Vector)
```

---

## 3. Structured Prompts and Schema Enforcement

```json
{
  "system_instruction": "You are the cognitive extraction engine of REPLAY. Extract objective, respectful, structured metadata from the provided image. Respond ONLY in valid JSON conforming to the schema.",
  "generation_config": {
    "response_mime_type": "application/json",
    "temperature": 0.2
  },
  "prompt": "Analyze the attached image and return:\n{\n  \"summary\": \"Concise single sentence summary\",\n  \"detailedDescription\": \"Thorough description of scene, atmosphere, objects and clothing\",\n  \"detectedObjects\": [\"object1\", \"object2\"],\n  \"detectedPeopleDescription\": [\"young man with glasses\", \"woman in red coat\"],\n  \"detectedContextCategory\": \"WORK | FAMILY | TRAVEL | STUDY | CELEBRATION | DAILY\",\n  \"detectedEmotions\": [\"joy\", \"focus\"],\n  \"extractedTextOcr\": \"Any visible text\"\n}"
}
```

---

## 4. Grounded RAG Pipeline (Evidence-Based Generation)
When the user asks: *"When did I work at university with Carlos?"*
1. **Query Intent & Entity Extraction**: Backend detects entities (`person="Carlos"`, `location="University"`).
2. **Dense Vectorization**: Generates 768-dim vector of query string.
3. **Elasticsearch Hybrid Query**: Executes BM25 multi-match + KNN vector similarity search with Reciprocal Rank Fusion.
4. **Context Hydration & Grounding**: Fetches original documents from MongoDB and prompts Gemini with the exact retrieved records as context.
5. **Hallucination-Free Synthesis**: Gemini generates the factual natural language answer citing the retrieved memories.
