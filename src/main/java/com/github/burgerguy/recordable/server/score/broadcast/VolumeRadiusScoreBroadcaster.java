package com.github.burgerguy.recordable.server.score.broadcast;

import com.github.burgerguy.recordable.server.database.TickVolumeCache;

public abstract class VolumeRadiusScoreBroadcaster extends ScoreBroadcaster {

    private final TickVolumeCache tickVolumeCache;
    private float[] tickVolumes;

    protected VolumeRadiusScoreBroadcaster(TickVolumeCache tickVolumeCache) {
        super();
        this.tickVolumeCache = tickVolumeCache;
    }

    @Override
    public void play(long scoreId) {
        this.tickVolumes = tickVolumeCache.getTickVolumes(scoreId);
    }

    public double getCurrentRadius() {
        float currentVolume = tickVolumes[currentTick];
        return currentVolume > 1.0F ? 16.0D * currentVolume : 16.0;
    }
}
