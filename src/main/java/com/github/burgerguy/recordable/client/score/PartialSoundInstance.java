package com.github.burgerguy.recordable.client.score;

import net.minecraft.sounds.SoundEvent;

public record PartialSoundInstance(SoundEvent soundEvent, float relX, float relY, float relZ, float volume, float pitch) {
}
