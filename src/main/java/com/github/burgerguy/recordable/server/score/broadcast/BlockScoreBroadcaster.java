package com.github.burgerguy.recordable.server.score.broadcast;

import com.github.burgerguy.recordable.server.database.TickVolumeCache;
import com.github.burgerguy.recordable.shared.Recordable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class BlockScoreBroadcaster extends VolumeRadiusScoreBroadcaster {

    private final BlockPos blockPos;

    public BlockScoreBroadcaster(TickVolumeCache tickVolumeCache, BlockPos blockPos) {
        super(tickVolumeCache);
        this.blockPos = blockPos;
    }

    @Override
    public boolean isInRange(double x, double y, double z) {
        double radius = getCurrentRadius();
        return blockPos.distToCenterSqr(x, y, z) < radius * radius;
    }

    @Override
    protected int getPlayPacketSize() {
        return 8 + 2 + 4 + 8;
    }

    @Override
    protected ResourceLocation getPlayPacketChannelId() {
        return Recordable.PLAY_SCORE_AT_POS_ID;
    }

    @Override
    protected void writePlayPacket(FriendlyByteBuf buffer) {
        buffer.writeLong(scoreId);
        buffer.writeShort(currentTick);
        buffer.writeInt(playId);
        buffer.writeBlockPos(blockPos);
    }
}
