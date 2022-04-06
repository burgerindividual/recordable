package com.github.burgerguy.recordable.server.score.broadcast;

import javax.annotation.Nullable;

public interface ScoreBroadcasterContainer {
    @Nullable
    ScoreBroadcaster getScoreBroadcaster();
}
