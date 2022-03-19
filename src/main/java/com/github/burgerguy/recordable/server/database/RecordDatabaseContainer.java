package com.github.burgerguy.recordable.server.database;

/**
 * Holder interface to store record database in ServerWorld
 */
public interface RecordDatabaseContainer {
    RecordDatabase getRecordDatabase();
    void setRecordDatabase(RecordDatabase database);
}
