package com.github.burgerguy.recordable.shared.block;

import com.github.burgerguy.recordable.server.database.ScoreDatabaseContainer;
import com.github.burgerguy.recordable.server.score.record.BlockEntityScoreRecorder;
import com.github.burgerguy.recordable.server.score.ServerScoreRegistriesContainer;
import com.github.burgerguy.recordable.shared.Recordable;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.awt.Color;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class RecorderBlockEntity extends BlockEntity {
    public static final BlockEntityType<RecorderBlockEntity> INSTANCE = FabricBlockEntityTypeBuilder.create(RecorderBlockEntity::new, RecorderBlock.INSTANCE).build(null);
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(Recordable.MOD_ID, "recorder");

    private BlockEntityScoreRecorder scoreRecorder;
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
            scoreRecorder = new BlockEntityScoreRecorder(
                    this,
                    () -> {
                        BlockState blockState = RecorderBlockEntity.this.getBlockState();
                        Direction blockFacing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
                        Direction micFacing = blockFacing.getOpposite();
                        return switch(micFacing) {
                            case NORTH -> Quaternion.ONE;
                            case EAST -> Vector3f.YP.rotationDegrees(90.0F);
                            case SOUTH -> Vector3f.YP.rotationDegrees(180.0F);
                            case WEST -> Vector3f.YP.rotationDegrees(-90.0F);
                            default -> throw new IllegalStateException("Unexpected rotation value: " + blockFacing);
                        };
                    },
                    ((ScoreDatabaseContainer) serverLevel.getServer()).getScoreDatabase(),
                    (r, id) -> {
                        recordItem.addTagElement("ScoreID", LongTag.valueOf(id));
                        recordItem.addTagElement("Color", IntTag.valueOf(Color.HSBtoRGB((float) Math.random(), 1.0f, 0.8f)));
                        dropRecord();
                    }
            );
            ((ServerScoreRegistriesContainer) serverLevel).getScoreRecorderRegistry().add(scoreRecorder);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level == null) throw new IllegalStateException("Removed recorder block entity with no level");
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            ((ServerScoreRegistriesContainer) serverLevel).getScoreRecorderRegistry().remove(scoreRecorder);
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
    public BlockEntityScoreRecorder getScoreRecorder() {
        return scoreRecorder;
    }
}