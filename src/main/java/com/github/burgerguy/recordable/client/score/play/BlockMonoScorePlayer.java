package com.github.burgerguy.recordable.client.score.play;

import com.github.burgerguy.recordable.client.score.FutureScore;
import com.github.burgerguy.recordable.client.score.PartialSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class BlockMonoScorePlayer extends ScorePlayer {

    private final BlockPos blockPos;
    private final float attenuationDistance;

    public BlockMonoScorePlayer(FutureScore futureScore, short startTick, SoundManager soundManager, BlockPos blockPos, float attenuationDistance) {
        super(futureScore, startTick, soundManager);
        this.blockPos = blockPos;
        this.attenuationDistance = attenuationDistance;
    }

    @Override
    public SoundInstance createSoundInstance(PartialSoundInstance partialSoundInstance) {
        // actually figure out what sound we're going to play.
        // this can vary depending on randomness because it's not based on a seed.
        // this is fixed in the latest snapshots.
        ResourceLocation resourceLocation = partialSoundInstance.soundEvent().getLocation();
        WeighedSoundEvents weighedSoundEvents = this.soundManager.getSoundEvent(resourceLocation);
        if (weighedSoundEvents == null) return null;
        Sound sound = weighedSoundEvents.getSound();
        float initialVolume = partialSoundInstance.volume() * sound.getVolume();

        // why mc uses linear attenuation instead of exponential clamped is beyond me, but
        // I'm going to replicate it here for accuracy
        // This does the inverse of what BlockStereoScorePlayer does pretty much, where we calculate
        // the volume going into the player and rebroadcast it.
        float relX = partialSoundInstance.relX();
        float relY = partialSoundInstance.relY();
        float relZ = partialSoundInstance.relZ();
        float distance = (float) Math.sqrt(relX * relX + relY * relY + relZ * relZ);
        float maxDistance = Math.max(initialVolume, 1.0f) * sound.getAttenuationDistance();
        float attenuatedInputVolume = initialVolume * (1 - distance / maxDistance);

        if (attenuatedInputVolume <= 0.0f) return null; // cull sound if not audible

        float outputVolume = attenuatedInputVolume / sound.getVolume();

        // this is irritating and makes it a bit less accurate, but whatever
        // TODO: make a sound class that allows for float attenuation, then mixin to SoundEngine and check for the instance
        int compensatedAttenuation = Math.round(this.attenuationDistance / Math.max(outputVolume, 1.0f));

        Sound newSound =  new Sound(
                sound.getLocation().toString(),
                sound.getVolume(),
                sound.getPitch(),
                sound.getWeight(),
                sound.getType(),
                sound.shouldStream(),
                sound.shouldPreload(),
                compensatedAttenuation
        );

        return new ScoreSoundInstance(
                weighedSoundEvents,
                newSound,
                resourceLocation,
                outputVolume,
                partialSoundInstance.pitch(),
                false, // looping
                0, // delay
                SoundInstance.Attenuation.LINEAR,
                this.blockPos.getX() + .5,
                this.blockPos.getY() + .5,
                this.blockPos.getZ() + .5,
                false // relative
        );
    }
}
