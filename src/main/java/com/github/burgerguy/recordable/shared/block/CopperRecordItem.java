package com.github.burgerguy.recordable.shared.block;

import com.github.burgerguy.recordable.server.score.broadcast.BlockScoreBroadcaster;
import com.github.burgerguy.recordable.server.score.record.BlockScoreRecorder;
import com.github.burgerguy.recordable.shared.Recordable;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CopperRecordItem extends Item {
    public static final Item INSTANCE = new CopperRecordItem(new FabricItemSettings().group(CreativeModeTab.TAB_MISC));
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(Recordable.MOD_ID, "copper_record");

    public CopperRecordItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos();

        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof RecorderBlockEntity recorderBlockEntity && !recorderBlockEntity.hasRecord()) {
            ItemStack itemStack = context.getItemInHand();

            if (itemStack.is(CopperRecordItem.INSTANCE) && !itemStack.getOrCreateTag().contains("scoreId", Tag.TAG_LONG)) {
                recorderBlockEntity.setRecordItem(itemStack.split(1));
                if (!level.isClientSide) {
                    BlockScoreRecorder scoreRecorder = recorderBlockEntity.getScoreRecorder();
                    if (scoreRecorder == null) return InteractionResult.FAIL;
                    if (!scoreRecorder.isRecording()) {
                        scoreRecorder.start();
                        return InteractionResult.SUCCESS;
                    } else {
                        return InteractionResult.PASS;
                    }
                } else {
                    return InteractionResult.CONSUME;
                }
            }
        } else if (blockEntity instanceof RecordPlayerBlockEntity recordPlayerBlockEntity && !recordPlayerBlockEntity.hasRecord()) {
            ItemStack itemStack = context.getItemInHand();

            if (itemStack.is(CopperRecordItem.INSTANCE) && itemStack.getOrCreateTag().contains("scoreId", Tag.TAG_LONG)) {
                recordPlayerBlockEntity.setRecordItem(itemStack.split(1));
                if (!level.isClientSide) {
                    BlockScoreBroadcaster scoreBroadcaster = recordPlayerBlockEntity.getScoreBroadcaster();
                    if (scoreBroadcaster == null) return InteractionResult.FAIL;
                    if (!scoreBroadcaster.isPlaying()) {
                        // can't be null because we checked in this branch
                        //noinspection ConstantConditions
                        scoreBroadcaster.play(itemStack.getTag().getLong("scoreId"));
                        return InteractionResult.SUCCESS;
                    } else {
                        return InteractionResult.PASS;
                    }
                } else {
                    return InteractionResult.CONSUME;
                }
            }
        }

        return InteractionResult.PASS;
    }
}
