package com.github.burgerguy.recordable.client.score.cache;

import com.github.burgerguy.recordable.client.score.FutureScore;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class ScoreCache {
    private final Long2ObjectMap<FutureScore> scoreIdToScoreFutures;

    public ScoreCache() {
        this.scoreIdToScoreFutures = new Long2ObjectOpenHashMap<>();
    }

    public FutureScore getFutureScore(long scoreId) {
        return scoreIdToScoreFutures.computeIfAbsent(scoreId, unused -> new FutureScore());
    }
}
