package com.github.burgerguy.recordable.shared.block;

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

        if (level.getBlockEntity(blockPos) instanceof RecorderBlockEntity recorderBlockEntity && !recorderBlockEntity.hasRecord()) {
            ItemStack itemStack = context.getItemInHand();

            if (!itemStack.getOrCreateTag().contains("scoreId", Tag.TAG_LONG)) {
                recorderBlockEntity.setRecordItem(itemStack.split(1));
                if (!level.isClientSide) {
                    BlockScoreRecorder scoreRecorder = recorderBlockEntity.getScoreRecorder();
                    if (scoreRecorder == null) return InteractionResult.FAIL;
                    if (!scoreRecorder.isRecording()) {
                        scoreRecorder.start();
                        return InteractionResult.SUCCESS;
                    }
                    return InteractionResult.PASS;
                }
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.FAIL;
    }
}
