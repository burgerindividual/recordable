package com.github.burgerguy.recordable.server.score.parse;

import com.github.burgerguy.recordable.client.score.PartialSoundInstance;
import com.github.burgerguy.recordable.client.score.ScheduledSoundGroup;
import com.github.burgerguy.recordable.client.score.Score;
import java.util.ArrayList;
import java.util.List;

public class ScoreConstructor {

    private final List<PartialSoundInstance>[] scheduledSoundGroupArray;

    public ScoreConstructor(int maxTicks) {
        //noinspection unchecked
        this.scheduledSoundGroupArray = (List<PartialSoundInstance>[]) new List<?>[maxTicks];
    }

    public void addSound(int tick, PartialSoundInstance partialSoundInstance) {
        List<PartialSoundInstance> scheduledSoundGroup = this.scheduledSoundGroupArray[tick];
        if (scheduledSoundGroup == null) {
            scheduledSoundGroup = new ArrayList<>(4);
            this.scheduledSoundGroupArray[tick] = scheduledSoundGroup;
        }
        scheduledSoundGroup.add(partialSoundInstance);
    }

    public Score createScore() {
        List<ScheduledSoundGroup> newScheduledSoundGroups = new ArrayList<>();
        int lastNonNullTick = 0;

        for (int tick = 0; tick < this.scheduledSoundGroupArray.length; tick++) {
            List<PartialSoundInstance> scheduledSoundGroup = this.scheduledSoundGroupArray[tick];
            if (scheduledSoundGroup != null) {
                newScheduledSoundGroups.add(new ScheduledSoundGroup(tick, scheduledSoundGroup.toArray(PartialSoundInstance[]::new)));
                lastNonNullTick = tick;
            }
        }


        return new Score(newScheduledSoundGroups.toArray(ScheduledSoundGroup[]::new), lastNonNullTick);
    }

}
