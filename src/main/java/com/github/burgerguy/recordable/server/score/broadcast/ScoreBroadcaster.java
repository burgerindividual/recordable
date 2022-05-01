package com.github.burgerguy.recordable.server.score.broadcast;

import com.github.burgerguy.recordable.shared.Recordable;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

// This does not manage sending scores themselves, only their instances
public abstract class ScoreBroadcaster {

    private final Set<ServerPlayer> sentTargets;
    protected long scoreId;
    protected short currentTick;
    protected int playId;
    private boolean paused;
    private boolean broadcasting;

    protected ScoreBroadcaster() {
        this.sentTargets = new ObjectOpenHashSet<>();
    }

    public abstract boolean isInRange(double x, double y, double z);

    protected abstract ResourceLocation getPlayPacketChannelId();
    protected abstract void writePlayPacket(FriendlyByteBuf buffer);

    public void tick(ServerLevel serverLevel) {
        if (!this.isBroadcasting() || this.isPaused()) return;

        // clean out set so reconnected players can hear
        this.sentTargets.removeIf(ServerPlayer::hasDisconnected);

        for (ServerPlayer player : serverLevel.players()) {
            if (!this.sentTargets.contains(player) && this.isInRange(player.getX(), player.getY(), player.getZ())) {
                FriendlyByteBuf buffer = PacketByteBufs.create();
                buffer.resetWriterIndex();
                this.writePlayPacket(buffer);
                ServerPlayNetworking.send(player, this.getPlayPacketChannelId(), buffer);
                this.sentTargets.add(player);
            }
        }
        this.currentTick++;
    }

    public void play(long scoreId) {
        this.scoreId = scoreId;
        this.sentTargets.clear();
        this.currentTick = 0;
        this.playId = ThreadLocalRandom.current().nextInt(); // meh...
        this.setBroadcasting(true);
    }

    public void stop() {
        this.setBroadcasting(false);

        FriendlyByteBuf buffer = PacketByteBufs.create();
        buffer.resetWriterIndex();
        buffer.writeInt(this.playId);
        for (ServerPlayer player : this.sentTargets) {
            ServerPlayNetworking.send(player, Recordable.STOP_SCORE_INSTANCE_ID, buffer);
        }
        this.sentTargets.clear();
    }

    public boolean isPaused() {
        return this.paused;
    }

    public void setPaused(boolean paused) {
        if (this.paused != paused) {
            this.paused = paused;

            // All clients that are currently playing it will be sent the pause packet.
            // From there, the sentTargets set will be frozen, and when unpaused, all
            // the users that were sent the pause packet will be sent the unpaused packet.
            for(ServerPlayer player : this.sentTargets) {
                FriendlyByteBuf buffer = PacketByteBufs.create();
                buffer.resetWriterIndex();
                buffer.writeInt(this.playId);
                buffer.writeBoolean(paused); // byte disguised as boolean
                ServerPlayNetworking.send(player, Recordable.SET_SCORE_INSTANCE_PAUSED_ID, buffer);
            }
        }
    }

    public boolean isBroadcasting() {
        return this.broadcasting;
    }

    public void setBroadcasting(boolean broadcasting) {
        this.broadcasting = broadcasting;
    }

}
