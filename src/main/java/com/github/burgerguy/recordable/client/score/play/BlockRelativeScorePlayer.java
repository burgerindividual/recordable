package com.github.burgerguy.recordable.client.score.play;

import com.github.burgerguy.recordable.client.score.FutureScore;
import com.github.burgerguy.recordable.client.score.PartialSoundInstance;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class BlockRelativeScorePlayer extends ScorePlayer {

    private final BlockPos blockPos;
    private final float distanceFactor;
    private final float volumeFactor;

    public BlockRelativeScorePlayer(FutureScore futureScore, short startTick, SoundManager soundManager, BlockPos blockPos, float distanceFactor, float volumeFactor) {
        super(futureScore, startTick, soundManager);
        this.blockPos = blockPos;
        this.distanceFactor = distanceFactor;
        this.volumeFactor = volumeFactor;
    }

    @Override
    @Nullable
    public SoundInstance createSoundInstance(PartialSoundInstance partialSoundInstance) {
        // actually figure out what sound we're gonna play.
        // this can vary depending on randomness because it's not based on a seed.
        // this is fixed in the latest snapshots.
        ResourceLocation resourceLocation = partialSoundInstance.soundEvent().getLocation();
        WeighedSoundEvents weighedSoundEvents = this.soundManager.getSoundEvent(resourceLocation);
        if (weighedSoundEvents == null) return null;
        Sound sound = weighedSoundEvents.getSound();
        float volume1 = sound.getVolume();

        // why mc uses linear attenuation instead of exponential clamped is beyond me, but
        // I'm going to replicate it here for accuracy
        // TODO: make the linear attenuation actually use OpenAL or something

        // kinda gross, but getting the pos from SoundManager is a pain in the ass with lots of accessors
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        float distance = (float) Math.sqrt(this.blockPos.distToCenterSqr(cameraPos));
        float volume2 = partialSoundInstance.volume() * volume1;
        float volume3 = volume2 * this.volumeFactor;
        // just use the raw distance factor because we're already clamping
        // old line:
         float maxDistance = Math.max(volume3, 1.0F) * this.distanceFactor;
        float volume4 = volume3 * (1 - distance / maxDistance);

        if (volume4 <= 0.0f) return null; // cull sound if not audible

        return new ScoreSoundInstance(
                weighedSoundEvents,
                sound,
                partialSoundInstance.soundEvent().getLocation(),
                volume4,
                partialSoundInstance.pitch(),
                false, // looping
                0, // delay
                SoundInstance.Attenuation.LINEAR,
                partialSoundInstance.relX(),
                partialSoundInstance.relY(),
                partialSoundInstance.relZ(),
                true // relative
        );
    }
}
