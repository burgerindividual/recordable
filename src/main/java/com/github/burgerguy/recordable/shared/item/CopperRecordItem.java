package com.github.burgerguy.recordable.shared.item;

import com.github.burgerguy.recordable.server.score.broadcast.ScoreBroadcaster;
import com.github.burgerguy.recordable.server.score.broadcast.ScoreBroadcasterContainer;
import com.github.burgerguy.recordable.server.score.record.BlockScoreRecorder;
import com.github.burgerguy.recordable.shared.Recordable;
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
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
        BlockState blockState = level.getBlockState(blockPos);
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
        } else if (blockEntity instanceof ScoreBroadcasterContainer scoreBroadcasterContainer && blockState.is(Blocks.JUKEBOX) && !blockState.getValue(JukeboxBlock.HAS_RECORD)) {
            ItemStack itemStack = context.getItemInHand();

            if (itemStack.getOrCreateTag().contains("ScoreID", Tag.TAG_LONG)) {
                if (!level.isClientSide) {
                     ScoreBroadcaster scoreBroadcaster = scoreBroadcasterContainer.getScoreBroadcaster();
                    if (scoreBroadcaster == null) return InteractionResult.FAIL;
                    ((JukeboxBlock)Blocks.JUKEBOX).setRecord(level, blockPos, blockState, itemStack);
                    // calling play will forcibly unpause the broadcaster. do we want this?
                    // should the play logic be moved to setRecord?
                    //noinspection ConstantConditions
                    scoreBroadcaster.play(itemStack.getTag().getLong("ScoreID"));
                    itemStack.shrink(1);
                    Player player = context.getPlayer();
                    if (player != null) {
                        player.awardStat(Stats.PLAY_RECORD);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
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
