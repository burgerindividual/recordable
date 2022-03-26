package com.github.burgerguy.recordable.server.score.broadcast;

import com.github.burgerguy.recordable.shared.Recordable;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.lwjgl.system.MemoryStack;

// This does not manage sending scores themselves, only play packets
public abstract class ScoreBroadcaster {

    private final Set<ServerPlayer> sentPlayPlayers;
    protected long scoreId;
    protected short currentTick;
    protected int playId;
    private boolean playing;

    protected ScoreBroadcaster() {
        this.sentPlayPlayers = new ObjectOpenHashSet<>();
    }

    public abstract boolean isInRange(double x, double y, double z);

    protected abstract int getPlayPacketSize();
    protected abstract ResourceLocation getPlayPacketChannelId();
    protected abstract void writePlayPacket(FriendlyByteBuf buffer);

    public void tick(MinecraftServer server) {
        if (isPlaying()) {
            // clean out set so reconnected can hear
            sentPlayPlayers.removeIf(ServerPlayer::hasDisconnected);

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (!sentPlayToPlayer(player) && isInRange(player.getX(), player.getY(), player.getZ())) {
                    sendPlayToPlayer(player);
                }
            }
            currentTick++;
        }
    }

    public void play(long scoreId) {
        this.scoreId = scoreId;
        sentPlayPlayers.clear();
        currentTick = 0;
        playId = ThreadLocalRandom.current().nextInt(); // meh...
        setPlaying(true);
    }

    public void stop() {
        setPlaying(false);

        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(memoryStack.malloc(Integer.BYTES)));
            buffer.resetWriterIndex();
            buffer.writeInt(playId);
            for (ServerPlayer player : sentPlayPlayers) {
                ServerPlayNetworking.send(player, Recordable.STOP_SCORE_ID, buffer);
            }
        }
        sentPlayPlayers.clear();
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean sentPlayToPlayer(ServerPlayer player) {
        return sentPlayPlayers.contains(player);
    }

    // call this when a player goes into the broadcast radius
    public void sendPlayToPlayer(ServerPlayer player) {
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(memoryStack.malloc(getPlayPacketSize())));
            buffer.resetWriterIndex();
            writePlayPacket(buffer);
            // TODO: this will be really bad if the packet is scheduled to be sent for later
            ServerPlayNetworking.send(player, getPlayPacketChannelId(), buffer);
            sentPlayPlayers.add(player);
        }
    }

}
