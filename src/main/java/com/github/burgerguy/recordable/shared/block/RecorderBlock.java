package com.github.burgerguy.recordable.shared.block;

import com.github.burgerguy.recordable.server.score.record.BlockScoreRecorder;
import com.github.burgerguy.recordable.shared.Recordable;
import java.util.Map;
import java.util.Random;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

public class RecorderBlock extends Block {
    public static final Block INSTANCE = new Block(FabricBlockSettings.of(Material.METAL).strength(4.0f));
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(Recordable.MOD_ID, "recorder");
    public static final BooleanProperty RECORDING = BooleanProperty.create("recording")

    private Map<BlockPos, BlockScoreRecorder>

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
        state = state.cycle(RECORDING);
        level.setBlock(pos, state, 2); // TODO: probably don't need this until rendering stuff? maybe?
        if (!level.isClientSide) {
            // FIXME: BAD. VERY BAD. maybe schedule tick for 0?
            tick(state, (ServerLevel) level, pos, null);
        }
        return InteractionResult.SUCCESS;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        super.tick(state, level, pos, random);
        if (level.getBlockState(pos).getValue(RECORDING)) {
            Recordable.LOGGER.info("Ayo! This is the recorder block on the " + (level.isClientSide ? "client" : "server") + " side!");
            level.scheduleTick(pos, this, 1); // 1 tick ahead of this
        }
    }
}
