package com.github.burgerguy.recordable.server.score.record;

import com.github.burgerguy.recordable.server.database.ScoreDatabase;
import com.github.burgerguy.recordable.shared.block.RecorderBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class BlockScoreRecorder extends ScoreRecorder {
    private final BlockPos blockPos;
    private final ServerLevel level;

    public BlockScoreRecorder(BlockPos blockPos, ServerLevel level, ScoreDatabase database, OnStopCallback onStopCallback) {
        super(database, onStopCallback);
        this.blockPos = blockPos;
        this.level = level;
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
        return level.getBlockState(blockPos).getValue(RecorderBlock.RECORDING);
    }

    @Override
    protected void setRecording(boolean recording) {
        level.getBlockState(blockPos).setValue(RecorderBlock.RECORDING, recording);
    }
}
