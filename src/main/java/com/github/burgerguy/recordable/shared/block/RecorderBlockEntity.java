package com.github.burgerguy.recordable.shared.block;

import com.github.burgerguy.recordable.shared.Recordable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RecorderBlockEntity extends BlockEntity {

    public RecorderBlockEntity(BlockPos pos, BlockState state) {
        super(Recordable.RECORDER_BLOCK_ENTITY, pos, state);
    }
}
