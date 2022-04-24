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
        if (!isBroadcasting() || isPaused()) return;

        // clean out set so reconnected players can hear
        sentTargets.removeIf(ServerPlayer::hasDisconnected);

        for (ServerPlayer player : serverLevel.players()) {
            if (!sentTargets.contains(player) && isInRange(player.getX(), player.getY(), player.getZ())) {
                FriendlyByteBuf buffer = new FriendlyByteBuf(PacketByteBufs.create());
                buffer.resetWriterIndex();
                writePlayPacket(buffer);
                // TODO: this will be really bad if the packet is scheduled to be sent for later
                ServerPlayNetworking.send(player, getPlayPacketChannelId(), buffer);
                sentTargets.add(player);
            }
        }
        currentTick++;
    }

    public void play(long scoreId) {
        this.scoreId = scoreId;
        sentTargets.clear();
        currentTick = 0;
        playId = ThreadLocalRandom.current().nextInt(); // meh...
        setBroadcasting(true);
    }

    public void stop() {
        setBroadcasting(false);

        FriendlyByteBuf buffer = new FriendlyByteBuf(PacketByteBufs.create());
        buffer.resetWriterIndex();
        buffer.writeInt(playId);
        for (ServerPlayer player : sentTargets) {
            ServerPlayNetworking.send(player, Recordable.STOP_SCORE_INSTANCE_ID, buffer);
        }
        sentTargets.clear();
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        if (this.paused != paused) {
            this.paused = paused;

            // All clients that are currently playing it will be sent the pause packet.
            // From there, the sentTargets set will be frozen, and when unpaused, all
            // the users that were sent the pause packet will be sent the unpaused packet.
            for(ServerPlayer player : sentTargets) {
                FriendlyByteBuf buffer = new FriendlyByteBuf(PacketByteBufs.create());
                buffer.resetWriterIndex();
                buffer.writeInt(playId);
                buffer.writeBoolean(paused); // byte disguised as boolean
                // TODO: this will be really bad if the packet is scheduled to be sent for later
                ServerPlayNetworking.send(player, getPlayPacketChannelId(), buffer);
            }
        }
    }

    public boolean isBroadcasting() {
        return broadcasting;
    }

    public void setBroadcasting(boolean broadcasting) {
        this.broadcasting = broadcasting;
    }

}
