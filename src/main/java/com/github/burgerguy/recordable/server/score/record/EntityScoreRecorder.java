package com.github.burgerguy.recordable.server.score.record;

import com.github.burgerguy.recordable.server.database.RecordDatabase;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class EntityScoreRecorder extends ScoreRecorder {
    private final Entity entity;
    private boolean recording;

    public EntityScoreRecorder(Entity entity, RecordDatabase database, OnStopCallback onStopCallback) {
        super(database, onStopCallback);
        this.entity = entity;
    }

    @Override
    public double getXPos() {
        return entity.getX();
    }

    @Override
    public double getYPos() {
        return entity.getY();
    }

    @Override
    public double getZPos() {
        return entity.getZ();
    }

    @Override
    public boolean isInRange(double x, double y, double z, ResourceKey<Level> dimension, float volume) {
        if (dimension != entity.getLevel().dimension()) return false;

        double radius = volume > 1.0F ? 16.0D * volume : 16.0;
        double relX = x - getXPos();
        double relY = y - getYPos();
        double relZ = z - getZPos();
        return relX * relX + relY * relY + relZ * relZ < radius * radius;
    }

    @Override
    public boolean isRecording() {
        return recording;
    }

    @Override
    protected void setRecording(boolean recording) {
        this.recording = recording;
    }
}
