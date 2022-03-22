package com.github.burgerguy.recordable.shared.block;

import com.github.burgerguy.recordable.shared.Recordable;
import java.util.Random;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class RecorderBlock extends BaseEntityBlock {
    public static final BooleanProperty RECORDING = BooleanProperty.create("recording");
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(Recordable.MOD_ID, "recorder");
    public static final Block INSTANCE = new RecorderBlock(FabricBlockSettings.of(Material.METAL).strength(4.0f));

    public RecorderBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(RECORDING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RECORDING);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            state = state.cycle(RECORDING);
            level.setBlock(pos, state, 2); // TODO: probably don't need this until rendering stuff? maybe?
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        state.setValue(RECORDING, false);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RecorderBlockEntity(pos, state);
    }
}
