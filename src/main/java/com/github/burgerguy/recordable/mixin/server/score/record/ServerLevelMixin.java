package com.github.burgerguy.recordable.mixin.server.score.record;

import com.github.burgerguy.recordable.server.score.record.RecorderRegistry;
import com.github.burgerguy.recordable.server.score.record.RecorderRegistryContainer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements RecorderRegistryContainer {

    private final RecorderRegistry recorderRegistry = new RecorderRegistry();

    @Override
    public RecorderRegistry getRecorderRegistry() {
        return recorderRegistry;
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
}