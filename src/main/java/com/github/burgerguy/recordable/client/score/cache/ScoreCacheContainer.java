package com.github.burgerguy.recordable.client.score.cache;

import com.github.burgerguy.recordable.client.score.FutureScore;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

public interface ScoreCacheContainer {
    Long2ObjectMap<FutureScore> getScoreCache();
}
