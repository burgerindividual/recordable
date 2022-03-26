package com.github.burgerguy.recordable.server.score.record;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.sounds.SoundEvent;

public class ScoreRecorderRegistry {
    private final Set<ScoreRecorder> recorders;

    public ScoreRecorderRegistry() {
        this.recorders = new ObjectOpenHashSet<>();
    }

    public void addRecorder(ScoreRecorder recorder) {
        recorders.add(recorder);
    }

    public void removeRecorder(ScoreRecorder recorder) {
        recorders.remove(recorder);
    }

    /**
     * Should be called when the server is shutting down or if the world is unloading
     */
    public void removeAndCloseAll() {
        Iterator<ScoreRecorder> scoreRecorderIterator = recorders.iterator();
        while (scoreRecorderIterator.hasNext()) {
            ScoreRecorder scoreRecorder = scoreRecorderIterator.next();
            scoreRecorderIterator.remove();
            scoreRecorder.close();
        }
    }

    public void tick() {
        for (ScoreRecorder recorder : recorders) {
            if (recorder.isRecording()) {
                recorder.tick();
            }
        }
    }

    public void captureSound(SoundEvent sound, double x, double y, double z, float volume, float pitch) {
        for (ScoreRecorder recorder : recorders) {
            if (recorder.isRecording() && recorder.isInRange(x, y, z, volume)) {
                recorder.recordSound(sound, x, y, z, volume, pitch);
            }
        }
    }
}
