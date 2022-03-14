package com.github.burgerguy.recordable.score;

import net.minecraft.server.world.ServerWorld;

public interface Score {
    Player createPlayer();

    interface Player {
        void tick(ServerWorld serverWorld);
        void stop(ServerWorld serverWorld);
        boolean isDone();
    }

    interface Broadcaster {
        void tick(ServerWorld serverWorld);
        void stop(ServerWorld serverWorld);
        boolean isDone();
    }
}
