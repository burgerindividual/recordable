package com.github.burgerguy.recordable.shared.block;

import com.github.burgerguy.recordable.server.score.record.BlockScoreRecorder;
import com.github.burgerguy.recordable.shared.Recordable;
import java.util.Random;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
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
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class RecorderBlock extends BaseEntityBlock {
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(Recordable.MOD_ID, "recorder");
    public static final Block INSTANCE = new RecorderBlock(FabricBlockSettings.of(Material.METAL).strength(4.0f));

    public RecorderBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RecorderBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof RecorderBlockEntity recorderBlockEntity) {
            if (!level.isClientSide) {
                BlockScoreRecorder scoreRecorder = recorderBlockEntity.getScoreRecorder();
                if (scoreRecorder == null) return InteractionResult.FAIL;
                if (scoreRecorder.isRecording()) {
                    scoreRecorder.stop();
                    recorderBlockEntity.setRecordItem(null);
                    return InteractionResult.SUCCESS;
                }
            }

            boolean hadRecord = recorderBlockEntity.hasRecord();
            if (hadRecord && !level.isClientSide) {
                recorderBlockEntity.dropRecord();
            }

            recorderBlockEntity.setRecordItem(null);
            return hadRecord ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else {
            return InteractionResult.FAIL;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (level.getBlockEntity(pos) instanceof RecorderBlockEntity recorderBlockEntity) {
            if (!level.isClientSide && recorderBlockEntity.hasRecord()) {
                recorderBlockEntity.dropRecord();
            }
            recorderBlockEntity.setRecordItem(null);
        }
    }
}
