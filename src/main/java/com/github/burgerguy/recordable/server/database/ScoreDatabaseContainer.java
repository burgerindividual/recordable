package com.github.burgerguy.recordable.server.database;

/**
 * Holder interface to store record database in ServerWorld
 */
public interface ScoreDatabaseContainer {
    ScoreDatabase getScoreDatabase();
    TickVolumeCache getTickVolumeCache();
    void setScoreDatabase(ScoreDatabase database);
}
