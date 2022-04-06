package com.github.burgerguy.recordable.server.score;

import com.github.burgerguy.recordable.server.score.broadcast.ScoreBroadcasterRegistry;
import com.github.burgerguy.recordable.server.score.record.ScoreRecorderRegistry;

public interface ServerScoreRegistriesContainer {
    ScoreRecorderRegistry getScoreRecorderRegistry();
    ScoreBroadcasterRegistry getScoreBroadcasterRegistry();
}
