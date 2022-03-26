package com.github.burgerguy.recordable.server.database;

import com.github.burgerguy.recordable.shared.score.ScoreConstants;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class TickVolumeCache {
    private final Long2ObjectMap<float[]> idToVolumesMap;
    private final ScoreDatabase scoreDatabase;

    public TickVolumeCache(ScoreDatabase scoreDatabase) {
        this.idToVolumesMap = new Long2ObjectOpenHashMap<>();
        this.scoreDatabase = scoreDatabase;
    }

    public float[] getTickVolumes(long scoreId) {
        return idToVolumesMap.computeIfAbsent(scoreId, id -> {
            try(ScoreDatabase.ScoreRequest rawScoreData = scoreDatabase.requestScore(id)) {
                ByteBuffer buffer = rawScoreData.getData();

                float[] volumeArray = new float[ScoreConstants.MAX_TICKS];
                int tick = 0;
                while (buffer.hasRemaining()) {
                    tick = Short.toUnsignedInt(buffer.getShort());
                    int soundCount = Byte.toUnsignedInt(buffer.get());
                    float loudestVolume = 0.0F;

                    // skip if tick has no sounds
                    if (soundCount > 0) {
                        buffer.position(buffer.position() + 16); // advance past id and relative pos first
                        for (int i = 0; i < soundCount - 1; i++) { // loop through all but last sound
                            float soundVolume = buffer.getFloat();
                            if (soundVolume > loudestVolume) {
                                loudestVolume = soundVolume;
                            }
                            buffer.position(buffer.position() + ScoreConstants.SOUND_SIZE_BYTES - 4); // negate 4 because we just read a float
                        }
                        // final sound, need to bump position less
                        float soundVolume = buffer.getFloat();
                        if (soundVolume > loudestVolume) {
                            loudestVolume = soundVolume;
                        }
                        buffer.position(buffer.position() + 4);

                        volumeArray[tick] = loudestVolume;
                    }
                }
                return Arrays.copyOf(volumeArray, tick + 1);
            }
        });
    }
}
