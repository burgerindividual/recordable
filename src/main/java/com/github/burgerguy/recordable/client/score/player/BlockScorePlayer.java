package com.github.burgerguy.recordable.client.score.player;

import com.github.burgerguy.recordable.client.score.PartialSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;

public class BlockScorePlayer extends ScorePlayer {

    private final BlockPos blockPos;

    public BlockScorePlayer(BlockPos blockPos) {
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
