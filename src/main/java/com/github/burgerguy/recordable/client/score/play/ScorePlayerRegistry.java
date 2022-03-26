package com.github.burgerguy.recordable.client.score.play;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Iterator;

public class ScorePlayerRegistry {
    private final Int2ObjectMap<ScorePlayer> playIdToPlayerMap;

    public ScorePlayerRegistry() {
        playIdToPlayerMap = new Int2ObjectOpenHashMap<>();
    }

    public void play(int playId, ScorePlayer scorePlayer) {
        scorePlayer.setPlaying(true);
        playIdToPlayerMap.put(playId, scorePlayer);
    }

    public void setPlaying(int playId, boolean playing) {
        ScorePlayer scorePlayer = playIdToPlayerMap.get(playId);
        scorePlayer.setPlaying(playing);
    }

    public void stop(int playId) {
        ScorePlayer scorePlayer = playIdToPlayerMap.remove(playId);
        if (scorePlayer != null) scorePlayer.stop();
    }

    // should probably be called at the end of the tick, so newly added players will be ticked the same tick they're added.
    public void tick() {
        Iterator<ScorePlayer> scorePlayerIterator = playIdToPlayerMap.values().iterator();
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
