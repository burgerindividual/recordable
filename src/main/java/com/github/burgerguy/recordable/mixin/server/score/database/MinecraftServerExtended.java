package com.github.burgerguy.recordable.mixin.server.score.database;

import com.github.burgerguy.recordable.server.database.ScoreDatabase;
import com.github.burgerguy.recordable.server.database.ScoreDatabaseContainer;
import com.github.burgerguy.recordable.server.database.TickVolumeCache;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftServer.class)
public class MinecraftServerExtended implements ScoreDatabaseContainer {
    private ScoreDatabase scoreDatabase;
    private TickVolumeCache tickVolumeCache;

    @Override
    public ScoreDatabase getScoreDatabase() {
        return scoreDatabase;
    }

    @Override
    public TickVolumeCache getTickVolumeCache() {
        return tickVolumeCache;
    }

    @Override
    public void setScoreDatabase(ScoreDatabase database) {
        this.scoreDatabase = database;
        this.tickVolumeCache = new TickVolumeCache(database);
    }
}
