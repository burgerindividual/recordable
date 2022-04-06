package com.github.burgerguy.recordable.server.score.broadcast;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;

public class ScoreBroadcasterRegistry {
    private final Set<ScoreBroadcaster> broadcasters;

    public ScoreBroadcasterRegistry() {
        this.broadcasters = new ObjectOpenHashSet<>();
    }

    public void add(ScoreBroadcaster broadcaster) {
        broadcasters.add(broadcaster);
    }

    public void remove(ScoreBroadcaster broadcaster) {
        broadcasters.remove(broadcaster);
    }

    public void tick(ServerLevel serverLevel) {
        for (ScoreBroadcaster broadcaster : broadcasters) {
            broadcaster.tick(serverLevel);
        }
    }
}
