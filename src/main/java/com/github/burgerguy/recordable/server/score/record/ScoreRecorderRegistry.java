package com.github.burgerguy.recordable.server.score.record;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
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

    public void removeAll() {
        recorders.clear();
    }

    public void stopAll() {
        for (ScoreRecorder recorder : recorders) {
            if (recorder.isRecording()) {
                recorder.stop();
            }
        }
    }

    /**
     * Should be called when the server is shutting down, or
     */
    public void closeAll() {
        for (ScoreRecorder recorder : recorders) {
            if (recorder.isRecording()) {
                recorder.close();
            }
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
            // TODO: maybe check if recorder is in loaded chunk somehow? maybe check last tick? idk
            if (recorder.isRecording() && recorder.isInRange(x, y, z, volume)) {
                recorder.recordSound(sound, x, y, z, volume, pitch);
            }
        }
    }
}
