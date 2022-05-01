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
        this.broadcasters.add(broadcaster);
    }

    public void remove(ScoreBroadcaster broadcaster) {
        this.broadcasters.remove(broadcaster);
    }

    public void tick(ServerLevel serverLevel) {
        for (ScoreBroadcaster broadcaster : this.broadcasters) {
            broadcaster.tick(serverLevel);
        }
    }
}
