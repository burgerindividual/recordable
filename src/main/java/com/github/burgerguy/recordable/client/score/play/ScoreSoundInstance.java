package com.github.burgerguy.recordable.client.score.play;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;

public class ScoreSoundInstance extends AbstractSoundInstance {
    private final WeighedSoundEvents weighedSoundEvents;

    protected ScoreSoundInstance(
            WeighedSoundEvents weighedSoundEvents,
            Sound sound,
            ResourceLocation resourceLocation,
            float volume,
            float pitch,
            boolean looping,
            int delay,
            SoundInstance.Attenuation attenuation,
            double x,
            double y,
            double z,
            boolean relative
    ) {
        super(resourceLocation, SoundSource.RECORDS);
        this.weighedSoundEvents = weighedSoundEvents;
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.x = x;
        this.y = y;
        this.z = z;
        this.looping = looping;
        this.delay = delay;
        this.attenuation = attenuation;
        this.relative = relative;
    }

    @Override
    public WeighedSoundEvents resolve(@NotNull SoundManager manager) {
        return this.weighedSoundEvents;
    }
}
