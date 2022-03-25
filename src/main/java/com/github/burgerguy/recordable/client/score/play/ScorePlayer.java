package com.github.burgerguy.recordable.client.score.play;

import com.github.burgerguy.recordable.client.score.PartialSoundInstance;
import com.github.burgerguy.recordable.client.score.ScheduledSoundGroup;
import com.github.burgerguy.recordable.client.score.Score;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;

public abstract class ScorePlayer {
    private final Score score;
    private final SoundManager soundManager;

    private int currentTick;
    private int arrayIdx;

    private boolean playing;
    private boolean done;

    public ScorePlayer(Score score, short startTick, SoundManager soundManager) {
        this.score = score;
        this.soundManager = soundManager;
        this.currentTick = startTick;
    }

    public void tick() {
        if (isDone()) throw new IllegalStateException("Score player ticked after done");

        if (currentTick > score.finalTick()) {
            stop();
            return;
        }

        if (!playing) return; // paused, don't increment tick

        if (arrayIdx < score.orderedScheduledSoundGroups().length) {
            ScheduledSoundGroup scheduledSoundGroup = score.orderedScheduledSoundGroups()[arrayIdx];
            if (currentTick == scheduledSoundGroup.tick()) {
                for (PartialSoundInstance partialSoundInstance : scheduledSoundGroup.sounds()) {
                    soundManager.play(createSoundInstance(partialSoundInstance));
                }
                arrayIdx++;
            }
        }
        currentTick++;
    }

    public abstract SoundInstance createSoundInstance(PartialSoundInstance partialSoundInstance);

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void stop() {
        done = true;
    }

    public boolean isDone() {
        return done;
    }
}
