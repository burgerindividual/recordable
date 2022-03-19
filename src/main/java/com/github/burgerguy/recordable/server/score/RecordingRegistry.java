package com.github.burgerguy.recordable.server.score;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import net.minecraft.sound.SoundEvent;

public class RecordingRegistry {
    private static final Set<ServerScoreRecorder> activeRecorders = new ObjectOpenHashSet<>();

    public static void startRecorder(ServerScoreRecorder recorder) {
        activeRecorders.add(recorder);
    }

    public static void finishRecorder(ServerScoreRecorder recorder) {
        activeRecorders.remove(recorder);
    }

    public static void captureSound(SoundEvent sound, double x, double y, double z, float volume, float pitch) {
        for (ServerScoreRecorder recorder : activeRecorders) {
            recorder.recordSound(sound, x, y, z, volume, pitch);
        }
    }
}
