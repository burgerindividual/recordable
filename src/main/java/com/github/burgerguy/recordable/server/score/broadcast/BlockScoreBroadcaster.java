package com.github.burgerguy.recordable.server.score.broadcast;

import com.github.burgerguy.recordable.server.database.TickVolumeCache;
import com.github.burgerguy.recordable.shared.Recordable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class BlockScoreBroadcaster extends VolumeRadiusScoreBroadcaster {

    private final BlockPos blockPos;
    private boolean playing;

    public BlockScoreBroadcaster(long scoreId, TickVolumeCache tickVolumeCache, BlockPos blockPos) {
        super(scoreId, tickVolumeCache);
        this.blockPos = blockPos;
    }

    @Override
    public boolean isInRange(double x, double y, double z) {
        double radius = getCurrentRadius();
        return blockPos.distToCenterSqr(x, y, z) < radius * radius;
    }

    @Override
    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    @Override
    protected int getPacketSize() {
        // format:
        // long id
        // short starting tick
        // long packed blockpos
        // long hashed timestamp
        return 8 + 2 + 8;
    }

    @Override
    protected ResourceLocation getPacketChannelId() {
        return Recordable.PLAY_SCORE_AT_POS_ID;
    }

    @Override
    protected void writePacket(FriendlyByteBuf buffer) {
        buffer.writeVarLong(scoreId);
        buffer.writeShort(currentTick);
        buffer.writeBlockPos(blockPos);
    }
}
