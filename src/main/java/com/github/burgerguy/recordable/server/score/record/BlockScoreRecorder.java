package com.github.burgerguy.recordable.server.score.record;

import com.github.burgerguy.recordable.server.database.ScoreDatabase;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.core.BlockPos;

public class BlockScoreRecorder extends ScoreRecorder {
    private final BlockPos blockPos;
    private final AtomicBoolean recording;

    public BlockScoreRecorder(BlockPos blockPos, ScoreDatabase database, OnStopCallback onStopCallback) {
        super(database, onStopCallback);
        this.blockPos = blockPos;
        this.recording = new AtomicBoolean(false);
    }

    @Override
    public double getXPos() {
        return blockPos.getX() + .5;
    }

    @Override
    public double getYPos() {
        return blockPos.getY() + .5;
    }

    @Override
    public double getZPos() {
        return blockPos.getZ() + .5;
    }

    @Override
    public boolean isInRange(double x, double y, double z, float volume) {
        double radius = volume > 1.0F ? 16.0D * volume : 16.0;
        double relX = x - getXPos();
        double relY = y - getYPos();
        double relZ = z - getZPos();
        return relX * relX + relY * relY + relZ * relZ < radius * radius;
    }

    @Override
    public boolean isRecording() {
        return recording.getAcquire();
    }

    @Override
    protected void setRecording(boolean recording) {
        this.recording.setRelease(recording);
    }
}
