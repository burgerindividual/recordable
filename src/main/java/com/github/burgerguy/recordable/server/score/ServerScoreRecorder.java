package com.github.burgerguy.recordable.server.score;

import com.github.burgerguy.recordable.server.database.RecordDB;
import java.io.Closeable;
import java.nio.ByteBuffer;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.registry.Registry;
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
public abstract class ServerScoreRecorder implements Closeable {
    public static final int MAX_TICKS = 65535;
    public static final int MAX_SOUNDS_PER_TICK = 255;
    public static final int SOUND_SIZE_BYTES = 24;
    public static final int MAX_RECORD_SIZE_BYTES = 524288; // 512 KiB

    private final RecordDB database;
    private final RecordingCallback recordingCallback;

    private ByteBuffer rawScoreBuffer;
    private ByteBuffer tickHeaderPointer;
    private boolean recording;
    private short currentTick;
    private byte currentTickSoundCount;

    /**
     * The start callback should be used for recorder registration, etc.
     * The stop callback should be used for recorder de-registration, saving the disk item, etc.
     */
    public ServerScoreRecorder(RecordDB database, RecordingCallback recordingCallback) {
        this.database = database;
        this.recordingCallback = recordingCallback;
    }

    public abstract double getXPos();
    public abstract double getYPos();
    public abstract double getZPos();

    /**
     * Starts the recording process and allocates the needed memory.
     */
    public void start() {
        if (recording) throw new IllegalStateException("Recorder started while recording");
        recording = true;

        rawScoreBuffer = MemoryUtil.memAlloc(MAX_RECORD_SIZE_BYTES); // free after storing in DB

        recordingCallback.onStart(this);
    }

    /**
     * Stops the recording process and calls the stop callback with the record id.
     */
    public void stop() {
        if (!recording) throw new IllegalStateException("Recorder stopped while not recording");

        // add blank final tick directly to the end of the buffer if no sounds were played on it
        if (currentTickSoundCount == 0) {
            rawScoreBuffer.putShort(currentTick);
            rawScoreBuffer.put(currentTickSoundCount);
        }

        currentTick = 0;
        currentTickSoundCount = 0;
        recording = false;

        long id = database.storeRecord(rawScoreBuffer);
        MemoryUtil.memFree(rawScoreBuffer);
        rawScoreBuffer = null;

        recordingCallback.onStop(this, id);
    }

    public boolean isRecording() {
        return recording;
    }

    /**
     * Only mixins will call this. You probably don't want to call it manually.
     */
    public void beginTick() {
        currentTickSoundCount = 0;
        // keep a pointer so when the tick ends we can write the tick header after
        tickHeaderPointer = MemoryUtil.memSlice(rawScoreBuffer, 0, 3);
        rawScoreBuffer.position(rawScoreBuffer.position() + 3);
    }

    /**
     * Only mixins will call this. You probably don't want to call it manually.
     */
    public void endTick() {
        // this probably happened because we force stopped
        if (!recording) return;

        if (currentTickSoundCount > 0) {
            tickHeaderPointer.putShort(currentTick);
            tickHeaderPointer.put(currentTickSoundCount);
        }
        tickHeaderPointer = null;
        currentTick++;
    }

    /**
     * Has to be called between beginTick and endTick
     */
    public void recordSound(SoundEvent sound, double x, double y, double z, float volume, float pitch) {
        if (!recording) throw new IllegalStateException("Tried to record sound while not recording");

        rawScoreBuffer.putInt(Registry.SOUND_EVENT.getRawId(sound)); // sound ID, registry needs to be synced with server

        // relative pos to sound source from recording location
        rawScoreBuffer.putFloat((float) (x - getXPos()));
        rawScoreBuffer.putFloat((float) (y - getYPos()));
        rawScoreBuffer.putFloat((float) (z - getZPos()));

        rawScoreBuffer.putFloat(volume);
        rawScoreBuffer.putFloat(pitch);

        // sound count = unsigned byte max or if tick count = unsigned short max
        if (currentTickSoundCount == -1 || currentTick == -1) {
            stop();
        } else {
            currentTickSoundCount++;
        }
    }

    /**
     * This immediately removes the potentially staging score in memory.
     */
    @Override
    public void close() {
        MemoryUtil.memFree(rawScoreBuffer);
        rawScoreBuffer = null;
        tickHeaderPointer = null;
    }

    public interface RecordingCallback {
        void onStart(ServerScoreRecorder recorder);
        void onStop(ServerScoreRecorder recorder, long recordId);
    }

}



