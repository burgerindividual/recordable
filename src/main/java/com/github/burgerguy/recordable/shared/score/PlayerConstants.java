package com.github.burgerguy.recordable.shared.score;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class PlayerConstants {

    /**
     * The vanilla sound factor before it doesn't broadcast to players.
     * @see net.minecraft.server.level.ServerLevel#playSound(Player, double, double, double, SoundEvent, SoundSource, float, float) ServerLevel.playSound
     */
    public static final float DISTANCE_FACTOR = 16.0f;

    /**
     * The volume of built-in record playing.
     * @see net.minecraft.client.resources.sounds.SimpleSoundInstance#forRecord
     */
    public static final float VOLUME = 4.0f;
}
