package com.github.burgerguy.recordable.shared.block;

import com.github.burgerguy.recordable.server.database.ScoreDatabaseContainer;
import com.github.burgerguy.recordable.server.score.record.BlockScoreRecorder;
import com.github.burgerguy.recordable.server.score.record.RecorderRegistryContainer;
import com.github.burgerguy.recordable.shared.Recordable;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.core.jmx.Server;

public class RecorderBlockEntity extends BlockEntity {
    public static final BlockEntityType<RecorderBlockEntity> INSTANCE = FabricBlockEntityTypeBuilder.create(RecorderBlockEntity::new, RecorderBlock.INSTANCE).build(null);
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(Recordable.MOD_ID, "recorder");

    private BlockScoreRecorder scoreRecorder;

    public RecorderBlockEntity(BlockPos pos, BlockState state) {
        super(INSTANCE, pos, state);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            scoreRecorder = new BlockScoreRecorder(getBlockPos(), serverLevel, ((ScoreDatabaseContainer) serverLevel.getServer()).getScoreDatabase(), (r, id) -> {
                Recordable.LOGGER.info(Long.toString(id));
            });
            ((RecorderRegistryContainer) serverLevel).getRecorderRegistry().addRecorder(scoreRecorder);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level == null || !level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            ((RecorderRegistryContainer) serverLevel).getRecorderRegistry().removeRecorder(scoreRecorder);
            scoreRecorder.close();
            scoreRecorder = null;
        }
    }
}