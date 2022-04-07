package com.github.burgerguy.recordable.mixin.server.score.record;

import com.github.burgerguy.recordable.server.score.broadcast.ScoreBroadcasterRegistry;
import com.github.burgerguy.recordable.server.score.record.ScoreRecorderRegistry;
import com.github.burgerguy.recordable.server.score.ServerScoreRegistriesContainer;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level implements ServerScoreRegistriesContainer {

    private final ScoreRecorderRegistry recorderRegistry = new ScoreRecorderRegistry();
    private final ScoreBroadcasterRegistry broadcasterRegistry = new ScoreBroadcasterRegistry();

    protected ServerLevelMixin(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean bl, boolean bl2, long l) {
        super(writableLevelData, resourceKey, holder, supplier, bl, bl2, l);
    }

    @Override
    public ScoreRecorderRegistry getScoreRecorderRegistry() {
        return recorderRegistry;
    }

    @Override
    public ScoreBroadcasterRegistry getScoreBroadcasterRegistry() {
        return broadcasterRegistry;
    }

    @Inject(method = "playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V", at = @At("TAIL"))
    private void captureSound(Player player, double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch, CallbackInfo ci) {
        // we probably don't need to deal with dimensions, because dimensions are stored in their own ServerLevel
        recorderRegistry.captureSound(
                sound,
                x,
                y,
                z,
                volume,
                pitch
        );
    }

    @Inject(method = "playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V", at = @At("TAIL"))
    private void captureSound(Player player, Entity entity, SoundEvent sound, SoundSource category, float volume, float pitch, CallbackInfo ci) {
        recorderRegistry.captureSound(
                sound,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                volume,
                pitch
        );
    }

    @Override
    public void playLocalSound(double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch, boolean distanceDelay) {
        super.playLocalSound(x, y, z, sound, category, volume, pitch, distanceDelay);
        recorderRegistry.captureSound(
                sound,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                volume,
                pitch
        );
    }
}
