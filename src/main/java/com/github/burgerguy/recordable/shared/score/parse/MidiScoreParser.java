package com.github.burgerguy.recordable.shared.score.parse;

import com.github.burgerguy.recordable.client.score.FutureScore;
import com.github.burgerguy.recordable.client.score.PartialSoundInstance;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import javax.sound.midi.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class MidiScoreParser {
    private static final int ROOT_NOTE = 66; // F#4
    private static final long SCORE_ID = 43434343L;
    private static final double TICK_MULTIPLIER = 20.0 / 96.0;
    private static final float VOLUME_MULTIPLIER = 1.0f;
    public static final Map<Integer, SoundEvent> channelToSoundEventMap = Map.of(
            0, SoundEvents.NOTE_BLOCK_BASEDRUM,
            1, SoundEvents.NOTE_BLOCK_SNARE,
            2, SoundEvents.NOTE_BLOCK_BIT,
            3, SoundEvents.NOTE_BLOCK_BASS,
            4, SoundEvents.NOTE_BLOCK_HAT,
            5, SoundEvents.SLIME_SQUISH_SMALL,
            6, SoundEvents.NOTE_BLOCK_HARP,
            7, SoundEvents.CHICKEN_EGG,
            8, SoundEvents.COW_AMBIENT,
            9, SoundEvents.NOTE_BLOCK_XYLOPHONE
    );

    public static void addToCache(Long2ObjectMap<FutureScore> scoreCache) {
        try {
            Sequence sequence = MidiSystem.getSequence(
                    Objects.requireNonNull(
                            MidiScoreParser.class.getResourceAsStream("/assets/recordable/music/minecraft.mid")
                    )
            );

            int maxTicks = (int) Math.round(sequence.getTickLength() * TICK_MULTIPLIER);
            ScoreConstructor scoreConstructor = new ScoreConstructor(maxTicks);

            for (Track track : sequence.getTracks()) {
                for (int i = 0; i < track.size(); i++) {
                    MidiEvent event = track.get(i);
                    int currentTick = (int) Math.round(event.getTick() * TICK_MULTIPLIER);
                    MidiMessage message = event.getMessage();
                    if (message instanceof ShortMessage sm && sm.getCommand() == ShortMessage.NOTE_ON) {
                        float pitch = (float) Math.pow(2.0, ((double) (sm.getData1() - ROOT_NOTE)) / 12.0);
                        float volume = (float) sm.getData2() / 100.0f * VOLUME_MULTIPLIER;
                        scoreConstructor.addSound(
                                currentTick,
                                new PartialSoundInstance(
                                        channelToSoundEventMap.get(sm.getChannel()),
                                        0.0f,
                                        0.0f,
                                        0.0f,
                                        volume,
                                        pitch
                                )
                        );
                    }
                }
            }

            FutureScore futureScore = scoreCache.computeIfAbsent(SCORE_ID, unused -> new FutureScore());
            futureScore.request();
            futureScore.setScore(scoreConstructor.createScore());
        } catch (InvalidMidiDataException | IOException e) {
            e.printStackTrace();
        }
    }
}
