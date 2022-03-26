package com.github.burgerguy.recordable.shared.block;

import com.github.burgerguy.recordable.server.database.ScoreDatabaseContainer;
import com.github.burgerguy.recordable.server.score.record.BlockScoreRecorder;
import com.github.burgerguy.recordable.server.score.record.ScoreRecorderRegistryContainer;
import com.github.burgerguy.recordable.shared.Recordable;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RecorderBlockEntity extends BlockEntity {
    public static final BlockEntityType<RecorderBlockEntity> INSTANCE = FabricBlockEntityTypeBuilder.create(RecorderBlockEntity::new, RecorderBlock.INSTANCE).build(null);
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(Recordable.MOD_ID, "recorder");

    private BlockScoreRecorder scoreRecorder;
    private ItemStack recordItem; // the record has to be blank

    public RecorderBlockEntity(BlockPos pos, BlockState state) {
        super(INSTANCE, pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains("record")) {
            recordItem = ItemStack.of(tag.getCompound("record"));
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        // Save the current value of the number to the tag
        if (hasRecord()) {
            tag.put("record", recordItem.save(new CompoundTag()));
        }

        super.saveAdditional(tag);
    }

//    @Nullable
//    @Override
//    public Packet<ClientGamePacketListener> getUpdatePacket() {
//        return ClientboundBlockEntityDataPacket.create(this);
//    }
//
//    @Override
//    public CompoundTag getUpdateTag() {
//        return saveWithoutMetadata();
//    }

    public boolean hasRecord() {
        return recordItem != null;
    }

    public void setRecordItem(ItemStack recordItem) {
        this.recordItem = recordItem;
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            scoreRecorder = new BlockScoreRecorder(getBlockPos(), ((ScoreDatabaseContainer) serverLevel.getServer()).getScoreDatabase(), (r, id) -> {
                recordItem.addTagElement("scoreId", LongTag.valueOf(id));
                dropRecord();
            });
            ((ScoreRecorderRegistryContainer) serverLevel).getScoreRecorderRegistry().addRecorder(scoreRecorder);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level == null) throw new IllegalStateException("Removed recorder block entity with no level");
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            ((ScoreRecorderRegistryContainer) serverLevel).getScoreRecorderRegistry().removeRecorder(scoreRecorder);
            scoreRecorder.close();
            scoreRecorder = null;
        }
    }

    public void dropRecord() {
        if (level == null) throw new IllegalStateException("Tried to drop record from recorder block entity with no level");
        if (!level.isClientSide) {
            float multiplier = 0.7F;
            double randOffX = (double) (level.random.nextFloat() * multiplier) + 0.15;
            double randOffY = (double) (level.random.nextFloat() * multiplier) + 0.66;
            double randOffZ = (double) (level.random.nextFloat() * multiplier) + 0.15;
            BlockPos pos = getBlockPos();

            ItemEntity itemEntity = new ItemEntity(level, pos.getX() + randOffX, pos.getY() + randOffY, pos.getZ() + randOffZ, recordItem);
            itemEntity.setDefaultPickUpDelay();

            level.addFreshEntity(itemEntity);
        }
    }

    @Nullable
    public BlockScoreRecorder getScoreRecorder() {
        return scoreRecorder;
    }
}