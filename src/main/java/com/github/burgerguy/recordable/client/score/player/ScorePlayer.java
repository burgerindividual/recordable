package com.github.burgerguy.recordable.client.score.player;

import com.github.burgerguy.recordable.client.score.PartialSoundInstance;
import com.github.burgerguy.recordable.client.score.ScheduledSoundGroup;
import com.github.burgerguy.recordable.client.score.Score;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;

public abstract class ScorePlayer {
    private int currentTick = 0;
    private int arrayIdx = 0;
    private Score score;

    public ScorePlayer() {
    }

    public void tick() {
        if (isDone()) return;

        if (currentTick >= score.endTick()) {
            stop();
            return;
        }

        if (arrayIdx < score.orderedScheduledSoundGroups().length) {
            ScheduledSoundGroup scheduledSoundGroup = score.orderedScheduledSoundGroups()[arrayIdx];
            if (currentTick == scheduledSoundGroup.tick()) {
                for (PartialSoundInstance partialSoundInstance : scheduledSoundGroup.sounds()) {
                    Minecraft.getInstance().getSoundManager().play(createSoundInstance(partialSoundInstance));
                }
                arrayIdx++;
            }
        }
        currentTick++;
    }

    public abstract SoundInstance createSoundInstance(PartialSoundInstance partialSoundInstance);

    public void play(Score score, short startTick) {
        this.score = score;
        this.currentTick = startTick;
    }

    public void play(Score score) {
        play(score, (short) 0);
    }

    public void stop() {
//        serverWorld.getServer().getPlayerManager().sendToAround(
//                null,
//                x,
//                y,
//                z,
//                volume > 1.0F ? (double)(16.0F * volume) : 16.0,
//                serverWorld.getRegistryKey(),
//                new StopSoundS2CPacket(null, SoundCategory.RECORDS) // TODO: send a bunch of packets for all played types of sounds
//        );
        score = null;
    }

    public boolean isDone() {
        return score == null;
    }
}
