package com.github.burgerguy.recordable.client.score.play;

import com.github.burgerguy.recordable.client.score.PartialSoundInstance;
import com.github.burgerguy.recordable.client.score.Score;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;

public class BlockScorePlayer extends ScorePlayer {

    private final BlockPos blockPos;

    public BlockScorePlayer(Score score, short startTick, SoundManager soundManager, BlockPos blockPos) {
        super(score, startTick, soundManager);
        this.blockPos = blockPos;
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
                blockPos.getX() + .5 + partialSoundInstance.relX(),
                blockPos.getY() + .5 + partialSoundInstance.relY(),
                blockPos.getZ() + .5 + partialSoundInstance.relZ(),
                false // relative
        );
    }
}
