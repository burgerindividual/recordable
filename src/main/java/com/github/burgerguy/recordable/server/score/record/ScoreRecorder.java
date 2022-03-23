package com.github.burgerguy.recordable.server.score.record;

import com.github.burgerguy.recordable.server.database.ScoreDatabase;
import java.io.Closeable;
import java.nio.ByteBuffer;
import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvent;
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
public abstract class ScoreRecorder implements Closeable {
    public static final int MAX_TICKS = 65535;
    public static final int MAX_SOUNDS_PER_TICK = 255;
    public static final int SOUND_SIZE_BYTES = 24;
    public static final int MAX_RECORD_SIZE_BYTES = 524288; // 512 KiB

    private final ScoreDatabase database;
    private final OnStopCallback onStopCallback;

    private ByteBuffer rawScoreBuffer;
    private ByteBuffer tickHeaderPointer;
    private short currentTick;
    private byte currentTickSoundCount;

    /**
     * The stop callback should be used for saving the disk item, etc and can happen even when stop isn't invoked by the user.
     */
    public ScoreRecorder(ScoreDatabase database, OnStopCallback onStopCallback) {
        this.database = database;
        this.onStopCallback = onStopCallback;
    }

    public abstract double getXPos();
    public abstract double getYPos();
    public abstract double getZPos();

    public abstract boolean isInRange(double x, double y, double z, float volume);

    public abstract boolean isRecording();
    protected abstract void setRecording(boolean recording);

    /**
     * Starts the recording process and allocates the needed memory.
     */
    public void start() {
        if (isRecording()) throw new IllegalStateException("Recorder started while recording");
        setRecording(true);

        rawScoreBuffer = MemoryUtil.memAlloc(MAX_RECORD_SIZE_BYTES); // free after storing in DB
    }

    /**
     * Stops the recording process and calls the stop callback with the record id.
     * This also stores the recording in the database and frees the allocated memory.
     */
    public void stop() {
        if (!isRecording()) throw new IllegalStateException("Recorder stopped while not recording");

        // add blank final tick directly to the end of the buffer if no sounds were played on it
        if (currentTickSoundCount == 0) {
            rawScoreBuffer.putShort(currentTick);
            rawScoreBuffer.put(currentTickSoundCount);
        }

        currentTick = 0;
        currentTickSoundCount = 0;
        setRecording(false);

        long id = database.storeScore(rawScoreBuffer.flip());
        MemoryUtil.memFree(rawScoreBuffer);
        rawScoreBuffer = null;

        onStopCallback.onStop(this, id);
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
        if (!isRecording()) throw new IllegalStateException("Tried to record sound while not recording");

        rawScoreBuffer.putInt(Registry.SOUND_EVENT.getId(sound)); // sound ID, registry needs to be synced with server

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
        setRecording(false);
        MemoryUtil.memFree(rawScoreBuffer);
        rawScoreBuffer = null;
        tickHeaderPointer = null;
    }

    public interface OnStopCallback {
        void onStop(ScoreRecorder recorder, long recordId);
    }

}



