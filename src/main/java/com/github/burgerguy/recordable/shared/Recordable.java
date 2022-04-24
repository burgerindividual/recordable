package com.github.burgerguy.recordable.shared;

import com.github.burgerguy.recordable.server.database.ScoreDatabase;
import com.github.burgerguy.recordable.server.database.ScoreDatabaseContainer;
import com.github.burgerguy.recordable.server.score.ServerScoreRegistriesContainer;
import com.github.burgerguy.recordable.shared.block.LabelerBlock;
import com.github.burgerguy.recordable.shared.block.LabelerBlockEntity;
import com.github.burgerguy.recordable.shared.block.RecorderBlock;
import com.github.burgerguy.recordable.shared.block.RecorderBlockEntity;
import com.github.burgerguy.recordable.shared.item.CopperRecordItem;
import com.github.burgerguy.recordable.shared.menu.LabelerMenu;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Map;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib3.GeckoLib;

public class Recordable implements ModInitializer {
	public static final String MOD_ID = "recordable";
	public static final Logger LOGGER = LoggerFactory.getLogger("recordable");

	public static final String SCORE_DATABASE_FILE_NAME = "scores.db";

	public static final ResourceLocation PLAY_SCORE_INSTANCE_AT_POS_ID = new ResourceLocation(MOD_ID, "play_score_at_pos");
	public static final ResourceLocation PLAY_SCORE_INSTANCE_AT_ENTITY_ID = new ResourceLocation(MOD_ID, "play_score_at_entity");
	public static final ResourceLocation SET_SCORE_INSTANCE_PAUSED_ID = new ResourceLocation(MOD_ID, "set_score_instance_paused");
	public static final ResourceLocation STOP_SCORE_INSTANCE_ID = new ResourceLocation(MOD_ID, "stop_score_instance");
	public static final ResourceLocation REQUEST_SCORE_ID = new ResourceLocation(MOD_ID, "request_score");
	public static final ResourceLocation SEND_SCORE_ID = new ResourceLocation(MOD_ID, "send_score");
	public static final ResourceLocation FINALIZE_LABEL_ID = new ResourceLocation(MOD_ID, "finalize_label");

	@Override
	public void onInitialize() {
		try {
			// add the entire LMDB code source to the path, so all subsequent classes are loaded by Knot
			// and all assigned mixins are applied to them
			URI lmdbJarUri = this.getClass().getClassLoader().getResource("org/lmdbjava").toURI();
			try (FileSystem fs = FileSystems.newFileSystem(lmdbJarUri, Map.of("create", "true"))) {
				for (Path jarRootDir : fs.getRootDirectories()) {
					FabricLauncherBase.getLauncher().addToClassPath(jarRootDir);
				}
			}
		} catch (Throwable t) {
			LOGGER.error("Unable to force load LMDB into Knot", t);
		}

		GeckoLib.initialize();

		//// block registry
		Registry.register(Registry.BLOCK, RecorderBlock.IDENTIFIER, RecorderBlock.INSTANCE);
		Registry.register(Registry.BLOCK, LabelerBlock.IDENTIFIER, LabelerBlock.INSTANCE);

		//// item registry
		Registry.register(Registry.ITEM, RecorderBlock.IDENTIFIER, RecorderBlock.ITEM_INSTANCE);
		Registry.register(Registry.ITEM, LabelerBlock.IDENTIFIER, LabelerBlock.ITEM_INSTANCE);
		Registry.register(Registry.ITEM, CopperRecordItem.IDENTIFIER, CopperRecordItem.INSTANCE);

		//// block entity registry
		Registry.register(Registry.BLOCK_ENTITY_TYPE, RecorderBlockEntity.IDENTIFIER, RecorderBlockEntity.INSTANCE);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, LabelerBlockEntity.IDENTIFIER, LabelerBlockEntity.INSTANCE);

		//// menu/screen handler registry
		Registry.register(Registry.MENU, LabelerMenu.IDENTIFIER, LabelerMenu.INSTANCE);

		//// event registry
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			// kinda conc, but should be fine for now
			((ScoreDatabaseContainer) server).setScoreDatabase(new ScoreDatabase(server.getWorldPath(LevelResource.ROOT).resolve(SCORE_DATABASE_FILE_NAME)));
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			((ScoreDatabaseContainer) server).getScoreDatabase().close();
		});

		// force stop all recorders, fixing block state
		ServerWorldEvents.UNLOAD.register((server, serverLevel) -> {
			((ServerScoreRegistriesContainer) serverLevel).getScoreRecorderRegistry().removeAndCloseAll();
		});

		// these are in the server tick because some packet handling is done outside the world tick
		// (irr)
		ServerTickEvents.START_SERVER_TICK.register(server -> {
			for (ServerLevel serverLevel : server.getAllLevels()) {
				((ServerScoreRegistriesContainer) serverLevel).getScoreRecorderRegistry().tick();
				((ServerScoreRegistriesContainer) serverLevel).getScoreBroadcasterRegistry().tick(serverLevel);
			}
		});

		//// networking registry
		ServerPlayNetworking.registerGlobalReceiver(REQUEST_SCORE_ID, (server, player, handler, buffer, responseSender) -> {
			long scoreId = buffer.readLong();

			ScoreDatabase scoreDatabase = ((ScoreDatabaseContainer) server).getScoreDatabase();

			try (ScoreDatabase.ScoreRequest scoreRequest = scoreDatabase.requestScore(scoreId)) {
				ByteBuffer scoreData = scoreRequest.getData();
				FriendlyByteBuf newPacketBuffer = new FriendlyByteBuf(PacketByteBufs.create());
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

		ServerPlayNetworking.registerGlobalReceiver(FINALIZE_LABEL_ID, (server, player, handler, buffer, responseSender) -> {
			if (player.containerMenu instanceof LabelerMenu labelerMenu) {
				labelerMenu.handleFinish(buffer);
			}
		});
	}
}
