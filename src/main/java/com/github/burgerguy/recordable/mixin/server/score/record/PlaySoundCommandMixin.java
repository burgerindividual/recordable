package com.github.burgerguy.recordable.mixin.server.score.record;

import com.github.burgerguy.recordable.server.score.ServerScoreRegistriesContainer;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.PlaySoundCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(PlaySoundCommand.class)
public class PlaySoundCommandMixin {
    @Inject(method = "playSound", at = @At("TAIL"))
    private static void captureSound(CommandSourceStack source, Collection<ServerPlayer> targets, ResourceLocation sound, SoundSource category, Vec3 pos, float volume, float pitch, float minVolume, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        ((ServerScoreRegistriesContainer) source.getLevel()).getScoreRecorderRegistry().captureSound(
                Registry.SOUND_EVENT.get(sound),
                pos.x,
                pos.y,
                pos.z,
                volume,
                pitch
        );
    }
}
