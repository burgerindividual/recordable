package com.github.burgerguy.recordable.server.score;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.List;
import java.util.Set;

public class RecordingRegistry {
    private static final Set<ServerScoreRecorder> activeRecorders = new ObjectOpenHashSet<>();

    public static void startRecorder(ServerScoreRecorder recorder) {
        activeRecorders.add(recorder);
    }

    public static void finishRecorder(ServerScoreRecorder recorder) {
        activeRecorders.remove(recorder);
    }
}
