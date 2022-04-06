package com.github.burgerguy.recordable.client;

import com.github.burgerguy.recordable.client.score.FutureScore;
import com.github.burgerguy.recordable.client.score.Score;
import com.github.burgerguy.recordable.client.score.cache.ScoreCacheContainer;
import com.github.burgerguy.recordable.client.score.play.BlockScorePlayer;
import com.github.burgerguy.recordable.client.score.play.ScorePlayerRegistry;
import com.github.burgerguy.recordable.client.score.play.ScorePlayerRegistryContainer;
import com.github.burgerguy.recordable.shared.Recordable;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.lwjgl.system.MemoryStack;

public class RecordableClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // register events
        ClientPlayNetworking.registerGlobalReceiver(Recordable.PLAY_SCORE_INSTANCE_AT_POS_ID, (client, handler, buffer, responseSender) -> {
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
                try (MemoryStack memoryStack = MemoryStack.stackPush()) {
                    FriendlyByteBuf newPacketBuffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(memoryStack.malloc(Long.BYTES)));
                    newPacketBuffer.resetWriterIndex();
                    newPacketBuffer.writeLong(scoreId);
                    responseSender.sendPacket(Recordable.REQUEST_SCORE_ID, newPacketBuffer);
                }
            }

            client.execute(() -> {
                ScorePlayerRegistry scorePlayerRegistry = ((ScorePlayerRegistryContainer) client.getConnection()).getScorePlayerRegistry();
                scorePlayerRegistry.play(playId, new BlockScorePlayer(score, currentTick, client.getSoundManager(), blockPos));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(Recordable.STOP_SCORE_INSTANCE_ID, (client, handler, buffer, responseSender) -> {
            int playId = buffer.readInt();

            client.execute(() -> {
                // if we got the packet, we have a connection, so it will never be null
                @SuppressWarnings("ConstantConditions")
                ScorePlayerRegistry scorePlayerRegistry = ((ScorePlayerRegistryContainer) client.getConnection()).getScorePlayerRegistry();
                scorePlayerRegistry.stop(playId);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(Recordable.SET_SCORE_INSTANCE_PAUSED_ID, (client, handler, buffer, responseSender) -> {
            int playId = buffer.readInt();
            boolean paused = buffer.readBoolean();

            client.execute(() -> {
                // if we got the packet, we have a connection, so it will never be null
                @SuppressWarnings("ConstantConditions")
                ScorePlayerRegistry scorePlayerRegistry = ((ScorePlayerRegistryContainer) client.getConnection()).getScorePlayerRegistry();
                scorePlayerRegistry.setPaused(playId, paused);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(Recordable.SEND_SCORE_ID, (client, handler, buffer, responseSender) -> {
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
        });

        // TODO: should this be end world tick or end client tick?
        ClientTickEvents.END_WORLD_TICK.register(cl -> {
            // is this worse or better than making an accessor to ClientLevel?

            // if we got the packet, we have a connection, so it will never be null
            @SuppressWarnings("ConstantConditions")
            ScorePlayerRegistry scorePlayerRegistry = ((ScorePlayerRegistryContainer) Minecraft.getInstance().getConnection()).getScorePlayerRegistry();
            scorePlayerRegistry.tick();
        });
    }
}
