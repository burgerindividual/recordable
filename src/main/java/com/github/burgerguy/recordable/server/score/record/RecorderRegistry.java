package com.github.burgerguy.recordable.server.score.record;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;

public class RecorderRegistry {
    private final Set<ScoreRecorder> recorders;

    public RecorderRegistry() {
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

    public void beginTick() {
        for (ScoreRecorder recorder : recorders) {
            if (recorder.isRecording()) {
                recorder.beginTick();
            }
        }
    }

    public void endTick() {
        for (ScoreRecorder recorder : recorders) {
            if (recorder.isRecording()) {
                recorder.endTick();
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
