package com.github.burgerguy.recordable.server.database;

/**
 * Holder interface to store record database on the server instance
 */
public interface ScoreDatabaseContainer {
    ScoreDatabase getScoreDatabase();
    TickVolumeCache getTickVolumeCache();
    void setScoreDatabase(ScoreDatabase database);
}
