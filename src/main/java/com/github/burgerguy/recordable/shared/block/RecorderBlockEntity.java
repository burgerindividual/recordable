package com.github.burgerguy.recordable.shared.block;

import com.github.burgerguy.recordable.server.score.record.BlockScoreRecorder;
import com.github.burgerguy.recordable.shared.Recordable;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RecorderBlockEntity extends BlockEntity {
    public static final BlockEntityType<RecorderBlockEntity> INSTANCE = FabricBlockEntityTypeBuilder.create(RecorderBlockEntity::new, RecorderBlock.INSTANCE).build(null);
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(Recordable.MOD_ID, "recorder");

    private final BlockScoreRecorder scoreRecorder;

    public RecorderBlockEntity(BlockPos pos, BlockState state) {
        super(INSTANCE, pos, state);
        this.scoreRecorder = new BlockScoreRecorder(getBlockPos(),)
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, RecorderBlockEntity blockEntity) {

    }
}