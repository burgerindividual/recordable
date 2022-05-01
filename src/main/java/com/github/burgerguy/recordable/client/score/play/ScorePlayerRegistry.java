package com.github.burgerguy.recordable.client.score.play;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Iterator;

public class ScorePlayerRegistry {
    private final Int2ObjectMap<ScorePlayer> playIdToPlayerMap;

    public ScorePlayerRegistry() {
        this.playIdToPlayerMap = new Int2ObjectOpenHashMap<>();
    }

    public void play(int playId, ScorePlayer scorePlayer) {
        this.playIdToPlayerMap.put(playId, scorePlayer);
    }

    public void setPaused(int playId, boolean paused) {
        ScorePlayer scorePlayer = this.playIdToPlayerMap.get(playId);
        scorePlayer.setPaused(paused);
    }

    public void stop(int playId) {
        ScorePlayer scorePlayer = this.playIdToPlayerMap.remove(playId);
        if (scorePlayer != null) scorePlayer.stop();
    }

    // should probably be called at the end of the tick, so newly added players will be ticked the same tick they're added.
    public void tick() {
        Iterator<ScorePlayer> scorePlayerIterator = this.playIdToPlayerMap.values().iterator();
        while (scorePlayerIterator.hasNext()) {
            ScorePlayer scorePlayer = scorePlayerIterator.next();
            if (scorePlayer.isDone()) {
                scorePlayerIterator.remove();
            } else {
                scorePlayer.tick();
            }
        }
    }
}
