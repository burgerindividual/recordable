package com.github.burgerguy.recordable.server.score.record;

import com.github.burgerguy.recordable.server.database.ScoreDatabase;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class BlockEntityScoreRecorder extends ScoreRecorder {
    private final BlockEntity blockEntity;

    public BlockEntityScoreRecorder(BlockEntity blockEntity, ScoreDatabase database, OnStopCallback onStopCallback) {
        super(database, onStopCallback);
        this.blockEntity = blockEntity;
    }

    @Override
    public double getXPos() {
        return blockEntity.getBlockPos().getX() + .5;
    }

    @Override
    public double getYPos() {
        return blockEntity.getBlockPos().getY() + .5;
    }

    @Override
    public double getZPos() {
        return blockEntity.getBlockPos().getZ() + .5;
    }

    @Override
    public Quaternion createRotation() {
        Direction direction;
        BlockState blockState = blockEntity.getBlockState();
        if (blockState.hasProperty(BlockStateProperties.FACING)) {
            direction = blockState.getValue(BlockStateProperties.FACING);
        } else if (blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            direction = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        } else {
            direction = Direction.NORTH;
        }

        Quaternion quaternion = Quaternion.fromXYZ(direction.getStepX(), direction.getStepY(), direction.getStepZ());
        quaternion.mul(Vector3f.YP.rotationDegrees(180.0F));
        return quaternion;

//        return switch(direction) {
//            case DOWN -> Vector3f.XP.rotationDegrees(-90.0F);
//            case UP -> Vector3f.XP.rotationDegrees(90.0F);
//            case NORTH -> Quaternion.ONE.copy();
//            case SOUTH -> Vector3f.ZP.rotationDegrees(180.0F);
//            case WEST -> Vector3f.ZP.rotationDegrees(-90.0F);
//            case EAST -> Vector3f.ZP.rotationDegrees(90.0F);
//        };
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
