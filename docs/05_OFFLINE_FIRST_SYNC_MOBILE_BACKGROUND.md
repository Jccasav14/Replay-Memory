# REPLAY: OFFLINE-FIRST ARCHITECTURE, SYNCHRONIZATION AND MOBILE BACKGROUND PROCESSING

## 1. Offline-First Philosophy with Expo SQLite
In **REPLAY**, offline state is a first-class citizen rather than an edge case.

### 1.1. Local Lifecycle
1. **Local Instant Persistence**: Snapping a photo or taking a note offline assigns a local UUID (`id: "loc-xxxx"`), saves the binary file locally in `FileSystem.documentDirectory`, and inserts a record into `local_memories` with `sync_status = 'PENDING_INSERT'`.
2. **Sync Queue Ingestion**: Records the operation (`INSERT`, timestamp, serialized payload) into `sync_queue`.
3. **Optimistic UI**: Displays the memory instantly on the mobile Timeline with a subtle pending sync badge.

---

## 2. Conflict Resolution: Last-Write-Wins (LWW) with Versioning
* Each entity maintains a monotonic `syncVersion` integer and UTC timestamp `updatedAt`.
* When synchronizing changes via `/api/v1/sync/batch`:
  * If fields are non-overlapping: automated three-way merge.
  * If field collisions occur: the most recent timestamp wins (`Last-Write-Wins`), and a trace is recorded in the audit log.

---

## 3. Realities and Mobile Background Execution Limits

> **ENGINEERING REALITY**:
> Neither Android nor iOS allows an application to run arbitrary, continuous JavaScript code 24/7 in the background. Mobile operating systems enforce aggressive power management constraints.

### 3.1. iOS Limitations
* **Background Tasks**: iOS allocates 15–30 seconds maximum upon background transition to complete pending tasks.
* **Background App Refresh**: The OS heuristically decides when to wake the app based on battery and historical usage patterns.
* **Significant Location Change Service**: Wakes the app only when switching cellular towers or major Wi-Fi access points (~500 meters).

### 3.2. Android Limitations
* **Doze Mode**: Restricts network access and suspends deferred sync jobs when the device is stationary and screen off.
* **Foreground Services**: Mandatory for long-running processes, requiring a persistent user notification.

### 3.3. REPLAY Background Design (Expo TaskManager & Geofencing)
REPLAY registers **discrete significant location events (Geofencing)** rather than continuous GPS polling:
1. Registers geofences around critical Life Graph locations (Home, Campus, Office).
2. Uses `Location.startGeofencingAsync(TASK_GEOFENCE, regions)`.
3. The OS awakens the app briefly (< 5 seconds) to log a `LOCATION_EVENT` in local SQLite.
4. Heavy sync occurs upon foreground transition or while connected to Wi-Fi and power.

---

## 4. Mobile Evolution Path

```text
[1. Expo Go]
      │
      ▼
[2. Expo Development Build] -> Custom native modules (SQLite, Background Location, TaskManager).
      │
      ▼
[3. EAS Build]              -> Signed standalone release binaries (APK/AAB for Android, IPA for iOS).
      │
      ▼
[4. Production Stores]       -> App Store and Google Play distribution.
```
