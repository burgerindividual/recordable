package com.github.burgerguy.recordable.mixin.server.score.database;

import com.github.burgerguy.recordable.server.database.RecordDatabase;
import com.github.burgerguy.recordable.server.database.RecordDatabaseContainer;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftServer.class)
public class MinecraftServerExtended implements RecordDatabaseContainer {
    private RecordDatabase recordDatabase;

    @Override
    public RecordDatabase getRecordDatabase() {
        return recordDatabase;
    }

    @Override
    public void setRecordDatabase(RecordDatabase database) {
        this.recordDatabase = database;
    }
}
