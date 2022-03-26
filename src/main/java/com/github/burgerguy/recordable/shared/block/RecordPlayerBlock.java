package com.github.burgerguy.recordable.shared.block;

import com.github.burgerguy.recordable.server.score.broadcast.BlockScoreBroadcaster;
import com.github.burgerguy.recordable.shared.Recordable;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class RecordPlayerBlock extends BaseEntityBlock {
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(Recordable.MOD_ID, "record_player");
    public static final Block INSTANCE = new RecordPlayerBlock(FabricBlockSettings.of(Material.METAL).strength(4.0f));

    public RecordPlayerBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RecordPlayerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, RecordPlayerBlockEntity.INSTANCE, RecordPlayerBlockEntity::tick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        RecordPlayerBlockEntity recordPlayerBlockEntity = (RecordPlayerBlockEntity) level.getBlockEntity(pos);
        if (recordPlayerBlockEntity == null) throw new IllegalStateException("Record player block does not have accompanying block entity at " + pos);

        if (!level.isClientSide) {
            BlockScoreBroadcaster scoreBroadcaster = recordPlayerBlockEntity.getScoreBroadcaster();
            if (scoreBroadcaster == null) return InteractionResult.FAIL;
            if (scoreBroadcaster.isPlaying()) {
                scoreBroadcaster.stop();
                recordPlayerBlockEntity.dropAndRemoveRecord();
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        return recordPlayerBlockEntity.hasRecord() ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

}
