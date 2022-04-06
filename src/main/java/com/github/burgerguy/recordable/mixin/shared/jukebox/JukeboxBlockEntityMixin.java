package com.github.burgerguy.recordable.mixin.shared.jukebox;

import com.github.burgerguy.recordable.server.database.ScoreDatabaseContainer;
import com.github.burgerguy.recordable.server.score.ServerScoreRegistriesContainer;
import com.github.burgerguy.recordable.server.score.broadcast.BlockScoreBroadcaster;
import com.github.burgerguy.recordable.server.score.broadcast.ScoreBroadcasterContainer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxBlockEntityMixin extends BlockEntity implements ScoreBroadcasterContainer {

    @Unique
    private BlockScoreBroadcaster scoreBroadcaster;

    private JukeboxBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            scoreBroadcaster = new BlockScoreBroadcaster(((ScoreDatabaseContainer) serverLevel.getServer()).getTickVolumeCache(), getBlockPos());
            ((ServerScoreRegistriesContainer) serverLevel).getScoreBroadcasterRegistry().add(scoreBroadcaster);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level == null) throw new IllegalStateException("Removed record player block entity with no level");
        if (!level.isClientSide) {
            if (scoreBroadcaster.isBroadcasting()) scoreBroadcaster.stop();
            ServerLevel serverLevel = (ServerLevel) level;
            ((ServerScoreRegistriesContainer) serverLevel).getScoreBroadcasterRegistry().remove(scoreBroadcaster);
            scoreBroadcaster = null;
        }
    }

    @Nullable
    @Unique
    @Override
    public BlockScoreBroadcaster getScoreBroadcaster() {
        return scoreBroadcaster;
    }
}
