package com.github.burgerguy.recordable.mixin.shared.jukebox;

import com.github.burgerguy.recordable.server.score.broadcast.ScoreBroadcaster;
import com.github.burgerguy.recordable.server.score.broadcast.ScoreBroadcasterContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(JukeboxBlock.class)
public class JukeboxBlockMixin {

    @Inject(method = "use",
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.BEFORE,
                    target = "Lnet/minecraft/world/level/block/JukeboxBlock;dropRecording(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"
            )
    )
    private void useExtended(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof ScoreBroadcasterContainer scoreBroadcasterContainer) {
            ScoreBroadcaster scoreBroadcaster = scoreBroadcasterContainer.getScoreBroadcaster();
            if (scoreBroadcaster == null) {
                throw new IllegalStateException("Jukebox block entity score broadcaster is still null during useExtended (should have been called on place)");
            } else if (scoreBroadcaster.isBroadcasting()) {
                scoreBroadcaster.stop();
            }
        }
    }
}
