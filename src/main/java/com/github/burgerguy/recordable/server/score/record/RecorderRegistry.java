package com.github.burgerguy.recordable.server.score.record;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;

// TODO: move an object of this to RecordableServer or smth and make it not static
public class RecorderRegistry {
    private static final Set<ScoreRecorder> recorders = new ObjectOpenHashSet<>();

    public static void addRecorder(ScoreRecorder recorder) {
        recorders.add(recorder);
    }

    public static void removeRecorder(ScoreRecorder recorder) {
        recorders.remove(recorder);
    }

    public static void removeAll() {
        recorders.clear();
    }

    public static void stopAll() {
        for (ScoreRecorder recorder : recorders) {
            if (recorder.isRecording()) {
                recorder.stop();
            }
        }
    }

    public static void beginTick() {
        for (ScoreRecorder recorder : recorders) {
            if (recorder.isRecording()) {
                recorder.beginTick();
            }
        }
    }

    public static void endTick() {
        for (ScoreRecorder recorder : recorders) {
            if (recorder.isRecording()) {
                recorder.endTick();
            }
        }
    }

    public static void captureSound(SoundEvent sound, double x, double y, double z, ResourceKey<Level> dimension, float volume, float pitch) {
        for (ScoreRecorder recorder : recorders) {
            // TODO: maybe check if recorder is in loaded chunk somehow? maybe check last tick? idk
            if (recorder.isRecording() && recorder.isInRange(x, y, z, dimension, volume)) {
                recorder.recordSound(sound, x, y, z, volume, pitch);
            }
        }
    }
}
