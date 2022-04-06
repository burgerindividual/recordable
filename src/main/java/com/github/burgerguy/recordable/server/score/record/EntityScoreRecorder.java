package com.github.burgerguy.recordable.server.score.record;

import com.github.burgerguy.recordable.server.database.ScoreDatabase;
import com.mojang.math.Quaternion;
import net.minecraft.world.entity.Entity;

public class EntityScoreRecorder extends ScoreRecorder {
    private final Entity entity;

    public EntityScoreRecorder(Entity entity, ScoreDatabase database, OnStopCallback onStopCallback) {
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
    public Quaternion createRotation() {
        return new Quaternion(entity.getXRot(), entity.getYRot(), 0.0f, false);
    }

    @Override
    public boolean isInRange(double x, double y, double z, float volume) {
        double radius = volume > 1.0F ? 16.0D * volume : 16.0;
        double relX = x - getXPos();
        double relY = y - getYPos();
        double relZ = z - getZPos();
        return relX * relX + relY * relY + relZ * relZ < radius * radius;
    }

}
