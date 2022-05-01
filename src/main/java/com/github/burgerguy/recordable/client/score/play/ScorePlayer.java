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
        if (this.isDone()) throw new IllegalStateException("Score player ticked after done");

        Score score = this.futureScore.getScoreOrNull();

        if (score != null && this.currentTick > score.finalTick()) {
            this.stop();
            return;
        }

        if (this.isPaused()) return;

        if (score != null && this.arrayIdx < score.orderedScheduledSoundGroups().length) {
            ScheduledSoundGroup scheduledSoundGroup = score.orderedScheduledSoundGroups()[this.arrayIdx];
            if (this.currentTick == scheduledSoundGroup.tick()) {
                for (PartialSoundInstance partialSoundInstance : scheduledSoundGroup.sounds()) {
                    SoundInstance soundInstance = this.createSoundInstance(partialSoundInstance);
                    if (soundInstance != null) {
                        this.soundManager.play(soundInstance);
                    }
                }
                this.arrayIdx++;
            }
        }
        this.currentTick++;
    }

    @Nullable
    public abstract SoundInstance createSoundInstance(PartialSoundInstance partialSoundInstance);

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return this.paused;
    }

    public void stop() {
        this.done = true;
    }

    public boolean isDone() {
        return this.done;
    }
}
