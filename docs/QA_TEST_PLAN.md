# REPLAY: QA Verification & Test Execution Plan

## 1. Overview
This document specifies the Quality Assurance (QA) test plan, execution matrices, and validation scenarios for the **REPLAY Personal Memory Engine** platform, encompassing backend services, web portal, mobile client, and offline synchronization mechanisms.

---

## 2. Test Execution Matrix

| Test Suite ID | Component | Type | Description | Priority |
|---|---|---|---|---|
| **TS-AUTH-01** | Backend & Clients | Integration | User registration, password hashing (BCrypt), JWT generation & validation | P0 |
| **TS-AUTH-02** | Backend & Clients | Security | Token expiry, unauthorized access rejection (RFC 7807 problem details) | P0 |
| **TS-MEM-01** | Backend / Web / Mobile | Functional | Memory ingestion with multipart image upload, note text, and timestamp | P0 |
| **TS-MEM-02** | Backend / Mobile | Functional | Soft deletion and file cleanup verification | P1 |
| **TS-AI-01** | AI Service | Integration | Gemini Vision image analysis extraction, categories, and emotion tagging | P1 |
| **TS-AI-02** | AI Service | Resilience | Fallback to mock embedding generation when API keys are absent or rate-limited | P1 |
| **TS-SYNC-01** | Mobile / Backend | Functional | Offline SQLite write queue persistence and sync batch resolution | P0 |
| **TS-GRAPH-01** | Backend / Web | Functional | Life Graph node/edge creation for people, locations, and objects | P1 |
| **TS-SEARCH-01**| Backend / Web / Mobile | Functional | Natural language query search over personal timeline events | P1 |

---

## 3. Automated Smoke Test Instructions

### Running Backend Smoke Tests (PowerShell)
```powershell
./infrastructure/scripts/smoke-test-qa.ps1 -BaseUrl "http://localhost:8080"
```

### Running Backend Smoke Tests (Bash)
```bash
chmod +x ./infrastructure/scripts/smoke-test-qa.sh
./infrastructure/scripts/smoke-test-qa.sh http://localhost:8080
```

---

## 4. Offline Sync & Edge Case Validation Steps

1. **Simulate Offline Mode on Mobile:**
   - Disconnect network / activate airplane mode in simulator or device.
   - Capture a photo note with title and description.
   - Verify entry appears in local SQLite list with `is_synced = 0`.
2. **Re-establish Connectivity:**
   - Reconnect Wi-Fi/cellular network.
   - Trigger sync service or pull-to-refresh on `HomeScreen`.
   - Verify backend receives batch and marks local items as `is_synced = 1`.
