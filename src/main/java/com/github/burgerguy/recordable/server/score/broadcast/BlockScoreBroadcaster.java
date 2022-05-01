package com.github.burgerguy.recordable.server.score.broadcast;

import com.github.burgerguy.recordable.shared.Recordable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class BlockScoreBroadcaster extends ScoreBroadcaster {

    private final float radius;
    private final BlockPos blockPos;

    public BlockScoreBroadcaster(float radius, BlockPos blockPos) {
        this.radius = radius;
        this.blockPos = blockPos;
    }

    @Override
    public boolean isInRange(double x, double y, double z) {
        return this.blockPos.distToCenterSqr(x, y, z) < this.radius * this.radius;
    }

    @Override
    protected ResourceLocation getPlayPacketChannelId() {
        return Recordable.PLAY_SCORE_INSTANCE_AT_POS_ID;
    }

    @Override
    protected void writePlayPacket(FriendlyByteBuf buffer) {
        buffer.writeLong(this.scoreId);
        buffer.writeShort(this.currentTick);
        buffer.writeInt(this.playId);
        buffer.writeBlockPos(this.blockPos);
    }
}
