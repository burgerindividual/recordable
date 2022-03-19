package com.github.burgerguy.recordable.server.score.record;

import com.github.burgerguy.recordable.server.database.RecordDatabase;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class BlockEntityScoreRecorder extends ScoreRecorder {

    public BlockEntityScoreRecorder(RecordDatabase database, OnStopCallback onStopCallback) {
        super(database, onStopCallback);
    }

    @Override
    public double getXPos() {
        return 0;
    }

    @Override
    public double getYPos() {
        return 0;
    }

    @Override
    public double getZPos() {
        return 0;
    }

    @Override
    public boolean isInRange(double x, double y, double z, ResourceKey<Level> dimension, float volume) {
        return false;
    }

    @Override
    public boolean isRecording() {
        return false;
    }

    @Override
    protected void setRecording(boolean recording) {

    }
}
