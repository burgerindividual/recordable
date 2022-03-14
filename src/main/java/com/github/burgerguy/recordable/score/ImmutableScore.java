package com.github.burgerguy.recordable.score;

import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.registry.Registry;

public class ImmutableScore implements Score {
    private final ScheduledSoundGroup[] orderedScheduledSoundGroups;

    public ImmutableScore(ScheduledSoundGroup[] orderedScheduledSoundGroups) {
        this.orderedScheduledSoundGroups = orderedScheduledSoundGroups;
    }


    @Override
    public Player createPlayer() {
        return new ImmutableServerPlayer();
    }

    public class ImmutableServerPlayer implements Score.Player {
        private int currentTick = 0;
        private int arrayIdx = 0;
        private boolean isDone = false;

        private final Set<ServerPlayerEntity>

        private ImmutableServerPlayer() {

        }

        @Override
        public void tick(ServerWorld serverWorld) {
            if (isDone()) throw new IllegalStateException("Score ticked when done");

            ScheduledSoundGroup scheduledSoundGroup = orderedScheduledSoundGroups[arrayIdx];

            if (currentTick == scheduledSoundGroup.tick()) {
                for (SoundInstance soundInstance : scheduledSoundGroup.sounds()) {
                    serverWorld.playSound(
                            null,
                            soundInstance.getX(),
                            soundInstance.getY(),
                            soundInstance.getZ(),
                            Registry.SOUND_EVENT.get(soundInstance.getId()), // TODO: not sure about this one chief
                            soundInstance.getCategory(),
                            soundInstance.getVolume(),
                            soundInstance.getPitch()
                    );
                }
                arrayIdx++;
                isDone = arrayIdx == orderedScheduledSoundGroups.length;
            }

            currentTick++;
        }

        @Override
        public void stop(ServerWorld serverWorld) {
            serverWorld.getServer().getPlayerManager().sendToAround(
                    null,
                    x,
                    y,
                    z,
                    volume > 1.0F ? (double)(16.0F * volume) : 16.0,
                    serverWorld.getRegistryKey(),
                    new StopSoundS2CPacket(null, SoundCategory.RECORDS) // TODO: send a bunch of packets for all played types of sounds
            );
            isDone = true;
        }

        @Override
        public boolean isDone() {
            return isDone;
        }
    }
}
