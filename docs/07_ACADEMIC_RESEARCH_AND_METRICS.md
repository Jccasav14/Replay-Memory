# REPLAY: ACADEMIC RESEARCH AND SCIENTIFIC EVALUATION FRAMEWORK

## 1. Problem Statement
In the contemporary era of hyper-digitization, individuals generate and accumulate thousands of personal digital fragments each month (cloud photos, ephemeral notes, passive location logs, tickets, PDF documents). However, this data suffers from **cognitive fragmentation and digital scattering**: memories remain trapped in isolated silos and disorganized file folders.

When an individual attempts to retrieve autobiographical information (e.g. *"When was the last time I visited my grandfather with my brother?"* or *"Which document contained the measurements of the apartment I viewed last year?"*), traditional exact-match keyword search engines fail due to lack of relational context and absence of a spatio-temporal Life Graph.

---

## 2. Project Objectives

### 2.1. General Objective
To design, implement, and quantitatively evaluate a distributed software architecture and personal digital memory platform named **REPLAY**, integrating multimodal artificial intelligence, dense vector embeddings, and an offline-first **Life Graph** for biographical memory organization and semantic natural language retrieval.

### 2.2. Specific Objectives
1. Develop a modular Spring Boot monolith orchestrating MongoDB document persistence, Redis distributed caching, and Elasticsearch hybrid indexing.
2. Implement an automated multimodal cognitive pipeline utilizing Google Gemini API and `text-embedding-004` (768-dim dense representations).
3. Build a cross-platform React Native / Expo application with offline-first capabilities using Expo SQLite and resilient bidirectional synchronization.
4. Construct an interactive React + Vite web portal for chronological Timeline and topological Life Graph exploration.
5. Quantitatively evaluate retrieval precision (MRR, Recall@K), AI fidelity (Groundedness), and mobile power efficiency.

---

## 3. Evaluation Metrics

### 3.1. Information Retrieval Metrics
1. **Precision@K and Recall@K**:
   $$\text{Precision}@K = \frac{|\text{Relevant Documents Retrieved in Top } K|}{K}$$
   $$\text{Recall}@K = \frac{|\text{Relevant Documents Retrieved in Top } K|}{|\text{Total Relevant Documents in Corpus}|}$$

2. **Mean Reciprocal Rank (MRR)**:
   $$\text{MRR} = \frac{1}{|Q|} \sum_{i=1}^{|Q|} \frac{1}{\text{rank}_i}$$

---

### 3.2. AI Fidelity and Groundedness
$$\text{Groundedness Score} = \frac{\text{Verifiable Assertions in Context}}{\text{Total Assertions in Generated Answer}}$$
* **Target**: $\text{Groundedness} \ge 0.95$ (zero tolerance for autobiographical hallucinations).

---

### 3.3. System and Mobile Operational Targets
* Hybrid Search Latency: $P_{95} \le 350\text{ ms}$ (local execution).
* Asynchronous AI Inference: $P_{95} \le 4.5\text{ s}$ per image.
* Mobile Battery Impact: $\le 1.5\%$ per 24 hours of geofence monitoring.
* Offline Availability: 100% of local Timeline browsing and creation operations functional without internet connection.
