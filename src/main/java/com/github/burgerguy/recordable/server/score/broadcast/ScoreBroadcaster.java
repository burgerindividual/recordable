package com.github.burgerguy.recordable.server.score.broadcast;

import com.github.burgerguy.recordable.shared.Recordable;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.lwjgl.system.MemoryStack;

// This does not manage sending scores themselves, only play packets
public abstract class ScoreBroadcaster {

    private final Set<ServerPlayer> sentPlayers;
    protected final long scoreId;
    protected short currentTick;

    protected ScoreBroadcaster(long scoreId) {
        this.sentPlayers = new ObjectOpenHashSet<>();
        this.scoreId = scoreId;
    }

    public abstract boolean isInRange(double x, double y, double z);

    public abstract boolean isPlaying();

    protected abstract int getPacketSize();
    protected abstract ResourceLocation getPacketChannelId();
    protected abstract void writePacket(FriendlyByteBuf buffer);

    public void tick(MinecraftServer server) {
        if (isPlaying()) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (isInRange(player.getX(), player.getY(), player.getZ())) {
                    sendToPlayer(player);
                }
            }
            currentTick++;
        }
    }

    public boolean sentToPlayer(ServerPlayer player) {
        return sentPlayers.contains(player);
    }

    // call this when a player goes into the broadcast radius
    public void sendToPlayer(ServerPlayer player) {
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(memoryStack.malloc(getPacketSize())));
            writePacket(buffer);
            // this will be really bad if the packet is scheduled to be sent for later
            ServerPlayNetworking.send(player, getPacketChannelId(), buffer);
            sentPlayers.add(player);
        }
    }

    // call this when a player leaves the server / disconnects, or the score ends
    public void removeSentPlayer(ServerPlayer player) {
        sentPlayers.remove(player);
    }
}
