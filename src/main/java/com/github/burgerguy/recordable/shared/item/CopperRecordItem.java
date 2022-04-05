package com.github.burgerguy.recordable.shared.item;

import com.github.burgerguy.recordable.server.score.broadcast.BlockScoreBroadcaster;
import com.github.burgerguy.recordable.server.score.record.BlockScoreRecorder;
import com.github.burgerguy.recordable.shared.Recordable;
import com.github.burgerguy.recordable.shared.block.RecordPlayerBlockEntity;
import com.github.burgerguy.recordable.shared.block.RecorderBlockEntity;
import java.util.List;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class CopperRecordItem extends Item {
    public static final Item INSTANCE = new CopperRecordItem(
            new FabricItemSettings()
                    .group(CreativeModeTab.TAB_MISC)
                    .maxCount(1)
    );
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

            if (!itemStack.getOrCreateTag().contains("ScoreID", Tag.TAG_LONG)) {
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

            if (itemStack.getOrCreateTag().contains("ScoreID", Tag.TAG_LONG)) {
                recordPlayerBlockEntity.setRecordItem(itemStack.split(1));
                if (!level.isClientSide) {
                    BlockScoreBroadcaster scoreBroadcaster = recordPlayerBlockEntity.getScoreBroadcaster();
                    if (scoreBroadcaster == null) return InteractionResult.FAIL;
                    if (!scoreBroadcaster.isPlaying()) {
                        // can't be null because we checked in this branch
                        //noinspection ConstantConditions
                        scoreBroadcaster.play(itemStack.getTag().getLong("ScoreID"));
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

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        boolean isWritten = stack.getOrCreateTag().contains("ScoreID", Tag.TAG_LONG);
        if (!isWritten) {
            tooltipComponents.add(new TranslatableComponent("item.recordable.copper_record.blank").withStyle(ChatFormatting.GRAY));
        }

        if (stack.getOrCreateTag().contains("SongInfo", Tag.TAG_COMPOUND)) {
            CompoundTag songInfo = stack.getOrCreateTag().getCompound("SongInfo");
            Component author = songInfo.contains("Author") ?
                    new TextComponent(songInfo.getString("Author")) :
                    new TranslatableComponent("item.recordable.copper_record.unknown_author");

            Component title = songInfo.contains("Title") ?
                    new TextComponent(songInfo.getString("Title")) :
                    new TranslatableComponent("item.recordable.copper_record.unknown_title");

            tooltipComponents.add(new TranslatableComponent("item.recordable.copper_record.song_info", author, title).withStyle(ChatFormatting.GRAY));
        } else if (isWritten) {
            tooltipComponents.add(
                    new TranslatableComponent("item.recordable.copper_record.song_info",
                            new TranslatableComponent("item.recordable.copper_record.unknown_author"),
                            new TranslatableComponent("item.recordable.copper_record.unknown_title")
                    ).withStyle(ChatFormatting.GRAY)
            );
        }
    }

    public static int getColor(ItemStack stack, int layerId) {
        if (layerId == 1) { // middle color layer
            if (stack.getOrCreateTag().contains("Color", Tag.TAG_INT)) {
                return stack.getOrCreateTag().getInt("Color");
            } else {
                return 0xbd5e3b; // normal copper color
            }
        } else if (layerId == 2) { // engravings layer if written to
            if (stack.getOrCreateTag().contains("ScoreID", Tag.TAG_LONG)) {
                return 0xd66d48; // lighter color for engravings
            } else {
                return 0xbd5e3b; // normal copper color
            }
        }
        return -1;
    }

}
