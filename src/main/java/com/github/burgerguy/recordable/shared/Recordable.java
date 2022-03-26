package com.github.burgerguy.recordable.shared;

import com.github.burgerguy.recordable.server.database.ScoreDatabase;
import com.github.burgerguy.recordable.server.database.ScoreDatabaseContainer;
import com.github.burgerguy.recordable.server.score.record.EntityScoreRecorder;
import com.github.burgerguy.recordable.server.score.record.ScoreRecorderRegistry;
import com.github.burgerguy.recordable.server.score.record.ScoreRecorderRegistryContainer;
import com.github.burgerguy.recordable.server.score.record.ScoreRecorder;
import com.github.burgerguy.recordable.shared.block.*;
import io.netty.buffer.Unpooled;
import java.nio.ByteBuffer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Recordable implements ModInitializer {
	public static final String MOD_ID = "recordable";
	public static final Logger LOGGER = LoggerFactory.getLogger("recordable");

	public static final String SCORE_DATABASE_FILE_NAME = "scores.db";

	public static final ResourceLocation PLAY_SCORE_AT_POS_ID = new ResourceLocation(MOD_ID, "play_score_at_pos");
	public static final ResourceLocation PLAY_SCORE_AT_ENTITY_ID = new ResourceLocation(MOD_ID, "play_score_at_entity");
	public static final ResourceLocation STOP_SCORE_ID = new ResourceLocation(MOD_ID, "stop_score");
	public static final ResourceLocation REQUEST_SCORE_ID = new ResourceLocation(MOD_ID, "request_score");
	public static final ResourceLocation SEND_SCORE_ID = new ResourceLocation(MOD_ID, "send_score");

	@Override
	public void onInitialize() {
		// block registry
		Registry.register(Registry.BLOCK, RecorderBlock.IDENTIFIER, RecorderBlock.INSTANCE);
		Registry.register(Registry.BLOCK, RecordPlayerBlock.IDENTIFIER, RecordPlayerBlock.INSTANCE);

		// item registry
		Registry.register(Registry.ITEM, RecorderBlock.IDENTIFIER, new BlockItem(RecorderBlock.INSTANCE, new FabricItemSettings().group(CreativeModeTab.TAB_MISC)));
		Registry.register(Registry.ITEM, RecordPlayerBlock.IDENTIFIER, new BlockItem(RecordPlayerBlock.INSTANCE, new FabricItemSettings().group(CreativeModeTab.TAB_MISC)));
		Registry.register(Registry.ITEM, CopperRecordItem.IDENTIFIER, CopperRecordItem.INSTANCE);

		// block entity registry
		Registry.register(Registry.BLOCK_ENTITY_TYPE, RecorderBlockEntity.IDENTIFIER, RecorderBlockEntity.INSTANCE);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, RecordPlayerBlockEntity.IDENTIFIER, RecordPlayerBlockEntity.INSTANCE);

		// event registry
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			// kinda conc, but should be fine for now
			((ScoreDatabaseContainer) server).setScoreDatabase(new ScoreDatabase(server.getWorldPath(LevelResource.ROOT).resolve(SCORE_DATABASE_FILE_NAME)));
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			((ScoreDatabaseContainer) server).getScoreDatabase().close();
		});

		// force stop all recorders, fixing block state
		ServerWorldEvents.UNLOAD.register((server, serverLevel) -> {
			((ScoreRecorderRegistryContainer) serverLevel).getScoreRecorderRegistry().closeAll();
		});

		// these are in the server tick because some packet handling is done outside the world tick
		// (irr)
		ServerTickEvents.START_SERVER_TICK.register(server -> {
			for (ServerLevel serverLevel : server.getAllLevels()) {
				((ScoreRecorderRegistryContainer) serverLevel).getScoreRecorderRegistry().tick();
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(REQUEST_SCORE_ID, (server, player, handler, buffer, responseSender) -> {
			long scoreId = buffer.readLong();

			ScoreDatabase scoreDatabase = ((ScoreDatabaseContainer) server).getScoreDatabase();

			try (ScoreDatabase.ScoreRequest scoreRequest = scoreDatabase.requestScore(scoreId);
				 MemoryStack memoryStack = MemoryStack.stackPush()) {
				ByteBuffer scoreData = scoreRequest.getData();
				int packetSize = scoreData != null ? Long.BYTES + scoreData.capacity() : Long.BYTES;

				FriendlyByteBuf newPacketBuffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(memoryStack.malloc(packetSize)));
				newPacketBuffer.resetWriterIndex();
				newPacketBuffer.writeLong(scoreId);

				if (scoreData != null) {
					newPacketBuffer.writeBytes(scoreData);
				} else {
					LOGGER.info("Player " + player + " requested invalid score id " + scoreId);
				}

				responseSender.sendPacket(Recordable.SEND_SCORE_ID, newPacketBuffer);
			}
		});
	}
}
