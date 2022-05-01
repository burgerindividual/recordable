package com.github.burgerguy.recordable.client.network;

import com.github.burgerguy.recordable.client.score.FutureScore;
import com.github.burgerguy.recordable.client.score.Score;
import com.github.burgerguy.recordable.client.score.cache.ScoreCacheContainer;
import com.github.burgerguy.recordable.client.score.play.BlockMonoScorePlayer;
import com.github.burgerguy.recordable.client.score.play.ScorePlayerRegistry;
import com.github.burgerguy.recordable.client.score.play.ScorePlayerRegistryContainer;
import com.github.burgerguy.recordable.shared.Recordable;
import com.github.burgerguy.recordable.shared.score.PlayerConstants;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class ClientPacketHandler {

    public static void receivePlayScoreInstancePosPacket(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender responseSender) {
        try {
            long scoreId = buffer.readLong();
            short currentTick = buffer.readShort();
            int playId = buffer.readInt();
            BlockPos blockPos = BlockPos.of(buffer.readLong());

            // if we got the packet, we have a connection, so it will never be null
            @SuppressWarnings("ConstantConditions")
            Long2ObjectMap<FutureScore> scoreCache = ((ScoreCacheContainer) client.getConnection()).getScoreCache();
            FutureScore score = scoreCache.computeIfAbsent(scoreId, unused -> new FutureScore());

            if (score.request()) {
                // hasn't been previously requested
                FriendlyByteBuf newPacketBuffer = PacketByteBufs.create();
                newPacketBuffer.resetWriterIndex();
                newPacketBuffer.writeLong(scoreId);
                responseSender.sendPacket(Recordable.REQUEST_SCORE_ID, newPacketBuffer);
            }

            client.execute(() -> {
                ScorePlayerRegistry scorePlayerRegistry = ((ScorePlayerRegistryContainer) client.getConnection()).getScorePlayerRegistry();
                scorePlayerRegistry.play(playId, new BlockMonoScorePlayer(score, currentTick, client.getSoundManager(), blockPos, PlayerConstants.DISTANCE_FACTOR * PlayerConstants.VOLUME));
            });
        } catch (Exception e) {
            Recordable.LOGGER.warn("Error processing score stop at pos packet", e);
        }
    }

    public static void receiveStopScoreInstancePacket(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender responseSender) {
        try {
            int playId = buffer.readInt();

            client.execute(() -> {
                // if we got the packet, we have a connection, so it will never be null
                @SuppressWarnings("ConstantConditions")
                ScorePlayerRegistry scorePlayerRegistry = ((ScorePlayerRegistryContainer) client.getConnection()).getScorePlayerRegistry();
                scorePlayerRegistry.stop(playId);
            });
        } catch (Exception e) {
            Recordable.LOGGER.warn("Error processing score stop packet", e);
        }
    }

    public static void receiveSetScoreInstancePausedPacket(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender responseSender) {
        try {
            int playId = buffer.readInt();
            boolean paused = buffer.readBoolean();

            client.execute(() -> {
                // if we got the packet, we have a connection, so it will never be null
                @SuppressWarnings("ConstantConditions")
                ScorePlayerRegistry scorePlayerRegistry = ((ScorePlayerRegistryContainer) client.getConnection()).getScorePlayerRegistry();
                scorePlayerRegistry.setPaused(playId, paused);
            });
        } catch (Exception e) {
            Recordable.LOGGER.warn("Error processing score pause packet", e);
        }
    }

    public static void receiveSentScorePacket(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender responseSender) {
        try {
            long scoreId = buffer.readLong();

            if (buffer.isReadable()) {
                Score score = Score.fromBuffer(buffer);

                // if we got the packet, we have a connection, so it will never be null
                @SuppressWarnings("ConstantConditions")
                Long2ObjectMap<FutureScore> scoreCache = ((ScoreCacheContainer) client.getConnection()).getScoreCache();
                scoreCache.computeIfAbsent(scoreId, unused -> new FutureScore()).setScore(score);
            } else {
                throw new IllegalArgumentException("Requested score of id " + scoreId + ", but score did not exist on the server.");
            }
        } catch (Exception e) {
            Recordable.LOGGER.warn("Error processing score data packet", e);
        }
    }
}
