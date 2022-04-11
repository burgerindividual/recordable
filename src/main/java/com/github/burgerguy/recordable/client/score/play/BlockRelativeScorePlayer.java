package com.github.burgerguy.recordable.client.score.play;

import com.github.burgerguy.recordable.client.score.FutureScore;
import com.github.burgerguy.recordable.client.score.PartialSoundInstance;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
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
        // why mc uses linear attenuation instead of exponential clamped is beyond me, but
        // I'm going to replicate it here for accuracy
        // TODO: make the linear attenuation actually use OpenAL or something

        // kinda gross, but getting the pos from SoundManager is a pain in the ass with lots of accessors
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        float distance = (float) Math.sqrt(blockPos.distToCenterSqr(cameraPos));
        float newVolume = ((-1.0f / distanceFactor) * distance) + (volumeFactor * partialSoundInstance.volume());

        if (newVolume <= 0.0f) return null; // cull sound if not audible

        return new SimpleSoundInstance(
                partialSoundInstance.soundEvent().getLocation(),
                SoundSource.RECORDS,
                newVolume,
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
