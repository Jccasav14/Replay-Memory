import * as SQLite from 'expo-sqlite';

let db: SQLite.SQLiteDatabase | null = null;

export const initSqliteDb = async (): Promise<SQLite.SQLiteDatabase> => {
  if (!db) {
    db = await SQLite.openDatabaseAsync('replay_local.db');

    await db.execAsync(`
      CREATE TABLE IF NOT EXISTS local_memories (
          id TEXT PRIMARY KEY,
          remote_id TEXT,
          type TEXT NOT NULL,
          title TEXT,
          description TEXT,
          occurred_at TEXT NOT NULL,
          latitude REAL,
          longitude REAL,
          location_name TEXT,
          tags TEXT,
          media_local_uri TEXT,
          sync_status TEXT CHECK(sync_status IN ('SYNCED', 'PENDING_INSERT', 'PENDING_UPDATE', 'PENDING_DELETE')) NOT NULL DEFAULT 'PENDING_INSERT',
          sync_version INTEGER DEFAULT 1,
          created_at TEXT NOT NULL,
          updated_at TEXT NOT NULL
      );

      CREATE TABLE IF NOT EXISTS sync_queue (
          queue_id INTEGER PRIMARY KEY AUTOINCREMENT,
          entity_type TEXT NOT NULL,
          local_id TEXT NOT NULL,
          operation TEXT CHECK(operation IN ('INSERT', 'UPDATE', 'DELETE')) NOT NULL,
          payload TEXT NOT NULL,
          retry_count INTEGER DEFAULT 0,
          created_at TEXT NOT NULL
      );
    `);
  }
  return db;
};

export const sqliteStorage = {
  async insertMemory(memory: {
    id: string;
    type: string;
    title?: string;
    description?: string;
    occurredAt: string;
    latitude?: number;
    longitude?: number;
    locationName?: string;
    tags?: string[];
    mediaLocalUri?: string;
  }) {
    const database = await initSqliteDb();
    const now = new Date().toISOString();

    await database.runAsync(
      `INSERT INTO local_memories (id, type, title, description, occurred_at, latitude, longitude, location_name, tags, media_local_uri, sync_status, created_at, updated_at)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING_INSERT', ?, ?)`,
      [
        memory.id,
        memory.type,
        memory.title || null,
        memory.description || null,
        memory.occurredAt,
        memory.latitude || null,
        memory.longitude || null,
        memory.locationName || null,
        JSON.stringify(memory.tags || []),
        memory.mediaLocalUri || null,
        now,
        now,
      ]
    );

    // Enqueue for background sync
    await database.runAsync(
      `INSERT INTO sync_queue (entity_type, local_id, operation, payload, created_at)
       VALUES ('MEMORY', ?, 'INSERT', ?, ?)`,
      [memory.id, JSON.stringify(memory), now]
    );
  },

  async getLocalMemories() {
    const database = await initSqliteDb();
    return await database.getAllAsync(`SELECT * FROM local_memories ORDER BY occurred_at DESC`);
  },

  async getPendingSyncItems() {
    const database = await initSqliteDb();
    return await database.getAllAsync(`SELECT * FROM sync_queue ORDER BY queue_id ASC`);
  },

  async markAsSynced(localId: string, remoteId: string) {
    const database = await initSqliteDb();
    await database.runAsync(
      `UPDATE local_memories SET remote_id = ?, sync_status = 'SYNCED' WHERE id = ?`,
      [remoteId, localId]
    );
    await database.runAsync(`DELETE FROM sync_queue WHERE local_id = ?`, [localId]);
  },
};
