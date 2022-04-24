package com.github.burgerguy.recordable.shared.util;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MenuUtil {

    // only called on server
    public static void updateBlockEntity(BlockEntity blockEntity) {
        blockEntity.setChanged();
        BlockState blockState = blockEntity.getBlockState();
        blockEntity.getLevel().sendBlockUpdated(blockEntity.getBlockPos(), blockState, blockState, 2); // or the flag with 1 to cause a block update
    }

}
