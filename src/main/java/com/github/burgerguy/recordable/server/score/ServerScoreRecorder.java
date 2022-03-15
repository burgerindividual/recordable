package com.github.burgerguy.recordable.server.score;

import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryUtil;

/**
 * byte format:
 *
 * per tick:
 * option 1:
 * 2 bytes tick no. (unsigned)
 * 1 byte sound count (unsigned)
 * (count * 24) byte sound list
 * option 2 (final tick):
 * 2 bytes tick no. (unsigned)
 * 1 byte sound count (value of 0)
 *
 * per sound:
 * 4 byte id (unsigned)
 * 4 byte float relative x pos
 * 4 byte float relative y pos
 * 4 byte float relative z pos
 * 4 byte float volume
 * 4 byte float pitch
 *
 * if no final tick is provided, the final tick is the last tick with sounds played
 */
// TODO: add good equals and hashcode methods
public abstract class ServerScoreRecorder {
    public static final int MAX_TICKS = 65535;
    public static final int MAX_SOUNDS_PER_TICK = 255;
    public static final int SOUND_SIZE_BYTES = 24;
    public static final int MAX_RECORD_SIZE_BYTES = 524288; // 512 KiB

    private ByteBuffer rawScoreBuffer;

    public ServerScoreRecorder() {
        this.rawScoreBuffer = MemoryUtil.memAlloc()
    }

    public abstract double getXPos();
    public abstract double getYPos();
    public abstract double getZPos();

}



