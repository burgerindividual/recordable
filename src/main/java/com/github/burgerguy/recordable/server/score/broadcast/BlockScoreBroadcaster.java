package com.github.burgerguy.recordable.server.score.broadcast;

import com.github.burgerguy.recordable.server.database.TickVolumeCache;
import com.github.burgerguy.recordable.shared.Recordable;
import com.github.burgerguy.recordable.shared.block.RecordPlayerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

public class BlockScoreBroadcaster extends VolumeRadiusScoreBroadcaster {

    private final BlockPos blockPos;
    private final ServerLevel level;

    public BlockScoreBroadcaster(long scoreId, TickVolumeCache tickVolumeCache, ServerLevel level, BlockPos blockPos) {
        super(scoreId, tickVolumeCache);
        this.blockPos = blockPos;
        this.level = level;
    }

    @Override
    public boolean isInRange(double x, double y, double z) {
        double radius = getCurrentRadius();
        return blockPos.distToCenterSqr(x, y, z) < radius * radius;
    }

    @Override
    public boolean isPlaying() {
        return level.getBlockState(blockPos).getValue(RecordPlayerBlock.PLAYING);
    }

    @Override
    protected int getPacketSize() {
        // format:
        // long id
        // short starting tick
        // long packed blockpos
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
