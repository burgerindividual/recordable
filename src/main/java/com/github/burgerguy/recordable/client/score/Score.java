package com.github.burgerguy.recordable.client.score;

import com.github.burgerguy.recordable.shared.score.ScoreConstants;
import java.util.Arrays;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;

public record Score(ScheduledSoundGroup[] orderedScheduledSoundGroups, int finalTick) {

    /**
     * See ScoreRecorder docs for buffer format.
     */
    public static Score fromBuffer(FriendlyByteBuf buffer) {
        int currentTick = 0;
        ScheduledSoundGroup[] soundGroups = new ScheduledSoundGroup[ScoreConstants.MAX_TICKS];
        short currentSoundGroup = 0;

        while (buffer.isReadable()) {
            currentTick = Short.toUnsignedInt(buffer.readShort());
            int soundInstancesCount = Byte.toUnsignedInt(buffer.readByte());
            if (soundInstancesCount == 0) break;

            PartialSoundInstance[] soundInstancesArray = new PartialSoundInstance[soundInstancesCount];
            for (int i = 0; i < soundInstancesCount; i++) {
                SoundEvent soundEvent = Registry.SOUND_EVENT.byId(buffer.readInt());
                float relX = buffer.readFloat();
                float relY = buffer.readFloat();
                float relZ = buffer.readFloat();
                float volume = buffer.readFloat();
                float pitch = buffer.readFloat();
                soundInstancesArray[i] = new PartialSoundInstance(soundEvent, relX, relY, relZ, volume, pitch);
            }
            soundGroups[currentSoundGroup] = new ScheduledSoundGroup(currentTick, soundInstancesArray);

            currentSoundGroup++;
        }

        return new Score(Arrays.copyOf(soundGroups, currentSoundGroup), currentTick);
    }
}
