package com.github.burgerguy.recordable.client.score.play;

import com.github.burgerguy.recordable.client.score.FutureScore;
import com.github.burgerguy.recordable.client.score.PartialSoundInstance;
import com.github.burgerguy.recordable.client.score.ScheduledSoundGroup;
import com.github.burgerguy.recordable.client.score.Score;
import javax.annotation.Nullable;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;

public abstract class ScorePlayer {
    private final FutureScore futureScore;
    protected final SoundManager soundManager;

    private int currentTick;
    private int arrayIdx;

    protected boolean paused;
    protected boolean done;

    public ScorePlayer(FutureScore futureScore, short startTick, SoundManager soundManager) {
        this.futureScore = futureScore;
        this.soundManager = soundManager;
        this.currentTick = startTick;
    }

    public void tick() {
        if (isDone()) throw new IllegalStateException("Score player ticked after done");

        Score score = futureScore.getScoreOrNull();

        if (score != null && currentTick > score.finalTick()) {
            stop();
            return;
        }

        if (isPaused()) return;

        if (score != null && arrayIdx < score.orderedScheduledSoundGroups().length) {
            ScheduledSoundGroup scheduledSoundGroup = score.orderedScheduledSoundGroups()[arrayIdx];
            if (currentTick == scheduledSoundGroup.tick()) {
                for (PartialSoundInstance partialSoundInstance : scheduledSoundGroup.sounds()) {
                    SoundInstance soundInstance = createSoundInstance(partialSoundInstance);
                    if (soundInstance != null) {
                        soundManager.play(soundInstance);
                    }
                }
                arrayIdx++;
            }
        }
        currentTick++;
    }

    @Nullable
    public abstract SoundInstance createSoundInstance(PartialSoundInstance partialSoundInstance);

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public void stop() {
        done = true;
    }

    public boolean isDone() {
        return done;
    }
}
