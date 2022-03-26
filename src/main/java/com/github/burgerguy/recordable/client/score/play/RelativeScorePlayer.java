package com.github.burgerguy.recordable.client.score.play;

import com.github.burgerguy.recordable.client.score.FutureScore;
import com.github.burgerguy.recordable.client.score.PartialSoundInstance;
import com.github.burgerguy.recordable.client.score.Score;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundSource;

public class RelativeScorePlayer extends ScorePlayer {

    public RelativeScorePlayer(FutureScore score, short startTick, SoundManager soundManager) {
        super(score, startTick, soundManager);
    }

    @Override
    public SoundInstance createSoundInstance(PartialSoundInstance partialSoundInstance) {
        return new SimpleSoundInstance(
                partialSoundInstance.soundEvent().getLocation(),
                SoundSource.RECORDS,
                partialSoundInstance.volume(),
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
