package com.github.burgerguy.recordable.server.score.record;

import com.github.burgerguy.recordable.server.database.ScoreDatabase;
import com.github.burgerguy.recordable.shared.score.ScoreConstants;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
    private final ScoreDatabase database;
    private final OnStopCallback onStopCallback;

    private Quaternion currentRotation;
    private ByteBuffer rawScoreBuffer;
    private ByteBuffer tickHeaderPointer;
    private short currentTick;
    private byte currentTickSoundCount;
    private boolean hasTicked;
    private boolean closed;
    private boolean recording;


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
    public abstract Quaternion createRotation();

    public abstract boolean isInRange(double x, double y, double z, float volume);

    public boolean isRecording() {
        return recording;
    }

    private void setRecording(boolean recording) {
        this.recording = recording;
    }

    /**
     * Starts the recording process and allocates the needed memory.
     */
    public void start() {
        if (isRecording()) throw new IllegalStateException("Recorder started while recording");
        setRecording(true);

        // free after storing in DB
        // order is big endian because LMDB likes it
        rawScoreBuffer = MemoryUtil.memAlloc(ScoreConstants.MAX_RECORD_SIZE_BYTES).order(ByteOrder.BIG_ENDIAN);
    }

    /**
     * Stops the recording process and calls the stop callback with the record id.
     * This also stores the recording in the database and frees the allocated memory.
     */
    public void stop() {
        if (!isRecording()) {
            throw new IllegalStateException("Recorder stopped while not recording");
        } else { // why tf do i need this else here??? wtf ij???
            setRecording(false);

            // add blank final tick if no sounds were played on it, and we have enough room
            if (currentTickSoundCount == 0 && rawScoreBuffer.remaining() >= ScoreConstants.TICK_HEADER_SIZE_BYTES) {
                tickHeaderPointer.putShort(currentTick);
                tickHeaderPointer.put(currentTickSoundCount);
            }

            currentTick = 0;
            currentTickSoundCount = 0;
            hasTicked = false;

            long id = database.storeScore(rawScoreBuffer.flip());
            MemoryUtil.memFree(rawScoreBuffer);
            rawScoreBuffer = null;
            tickHeaderPointer = null;

            onStopCallback.onStop(this, id);
        }
    }

    /**
     * Only mixins will call this. You probably don't want to call it manually.
     */
    public void tick() {
        currentRotation = createRotation();

        if (currentTickSoundCount > 0) {
            tickHeaderPointer.putShort(currentTick);
            tickHeaderPointer.put(currentTickSoundCount);
        }

        // keep a pointer so we can write to the previous tick
        if (!hasTicked || currentTickSoundCount > 0) {
            if (rawScoreBuffer.remaining() < ScoreConstants.TICK_HEADER_SIZE_BYTES) stop();
            tickHeaderPointer = MemoryUtil.memSlice(rawScoreBuffer, 0, ScoreConstants.TICK_HEADER_SIZE_BYTES);
            rawScoreBuffer.position(rawScoreBuffer.position() + 3);
            hasTicked = true;
        }

        currentTickSoundCount = 0;
        currentTick++;
    }

    /**
     * Has to be called between beginTick and endTick
     */
    public void recordSound(SoundEvent sound, double x, double y, double z, float volume, float pitch) {
        if (!isRecording()) throw new IllegalStateException("Tried to record sound while not recording");

        if (rawScoreBuffer.remaining() < ScoreConstants.SOUND_SIZE_BYTES) stop();

        rawScoreBuffer.putInt(Registry.SOUND_EVENT.getId(sound)); // sound ID, registry needs to be synced with server

        // rotate around recorder to compensate for orientation
        Vector3f newPos = new Vector3f((float) (x - getXPos()), (float) (y - getYPos()), (float) (z - getZPos()));
        newPos.transform(currentRotation);

        // relative pos to sound source from recording location
        rawScoreBuffer.putFloat(newPos.x());
        rawScoreBuffer.putFloat(newPos.y());
        rawScoreBuffer.putFloat(newPos.z());

        rawScoreBuffer.putFloat(volume);
        rawScoreBuffer.putFloat(pitch);

        if (currentTickSoundCount == (byte) ScoreConstants.MAX_SOUNDS_PER_TICK
                || currentTick == (byte) ScoreConstants.MAX_TICKS
                || rawScoreBuffer.remaining() <= 0) {
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
        if (rawScoreBuffer != null) MemoryUtil.memFree(rawScoreBuffer);
        rawScoreBuffer = null;
        tickHeaderPointer = null;
        closed = true;
    }

    public boolean isClosed() {
        return closed;
    }

    public interface OnStopCallback {
        void onStop(ScoreRecorder recorder, long recordId);
    }

}



