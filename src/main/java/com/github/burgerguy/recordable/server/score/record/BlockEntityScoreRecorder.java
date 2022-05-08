package com.github.burgerguy.recordable.server.score.record;

import com.github.burgerguy.recordable.server.database.ScoreDatabase;
import com.mojang.math.Quaternion;
import java.util.function.Supplier;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BlockEntityScoreRecorder extends ScoreRecorder {
    private final BlockEntity blockEntity;
    private final Supplier<Quaternion> rotationSupplier;

    public BlockEntityScoreRecorder(BlockEntity blockEntity, Supplier<Quaternion> rotationSupplier, ScoreDatabase database, OnStopCallback onStopCallback) {
        super(database, onStopCallback);
        this.blockEntity = blockEntity;
        this.rotationSupplier = rotationSupplier;
    }

    @Override
    public double getXPos() {
        return this.blockEntity.getBlockPos().getX() + .5;
    }

    @Override
    public double getYPos() {
        return this.blockEntity.getBlockPos().getY() + .5;
    }

    @Override
    public double getZPos() {
        return this.blockEntity.getBlockPos().getZ() + .5;
    }

    @Override
    public Quaternion createRotation() {
        return this.rotationSupplier.get();
    }

    @Override
    public boolean isInRange(double x, double y, double z, float volume) {
        double radius = volume > 1.0F ? 16.0D * volume : 16.0;
        double relX = x - this.getXPos();
        double relY = y - this.getYPos();
        double relZ = z - this.getZPos();
        return relX * relX + relY * relY + relZ * relZ < radius * radius;
    }

}
