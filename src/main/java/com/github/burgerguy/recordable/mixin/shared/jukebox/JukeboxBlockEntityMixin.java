package com.github.burgerguy.recordable.mixin.shared.jukebox;

import com.github.burgerguy.recordable.server.score.ServerScoreRegistriesContainer;
import com.github.burgerguy.recordable.server.score.broadcast.BlockScoreBroadcaster;
import com.github.burgerguy.recordable.server.score.broadcast.ScoreBroadcasterContainer;
import com.github.burgerguy.recordable.shared.score.PlayerConstants;
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
            this.scoreBroadcaster = new BlockScoreBroadcaster(PlayerConstants.DISTANCE_FACTOR * PlayerConstants.VOLUME,
                                                              this.getBlockPos());
            ((ServerScoreRegistriesContainer) serverLevel).getScoreBroadcasterRegistry().add(this.scoreBroadcaster);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (this.level == null) throw new IllegalStateException("Removed record player block entity with no level");
        if (!this.level.isClientSide) {
            if (this.scoreBroadcaster.isBroadcasting()) this.scoreBroadcaster.stop();
            ServerLevel serverLevel = (ServerLevel) this.level;
            ((ServerScoreRegistriesContainer) serverLevel).getScoreBroadcasterRegistry().remove(this.scoreBroadcaster);
            this.scoreBroadcaster = null;
        }
    }

    @Nullable
    @Unique
    @Override
    public BlockScoreBroadcaster getScoreBroadcaster() {
        return this.scoreBroadcaster;
    }
}
