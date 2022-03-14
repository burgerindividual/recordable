package com.github.burgerguy.recordable.score;

import net.minecraft.client.sound.SoundInstance;

public record ScheduledSoundGroup(int tick, SoundInstance[] sounds) {}
