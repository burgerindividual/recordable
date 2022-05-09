package com.github.burgerguy.recordable.shared;

import com.github.burgerguy.recordable.server.database.ScoreDatabase;
import com.github.burgerguy.recordable.server.database.ScoreDatabaseContainer;
import com.github.burgerguy.recordable.server.score.ServerScoreRegistriesContainer;
import com.github.burgerguy.recordable.shared.block.LabelerBlock;
import com.github.burgerguy.recordable.shared.block.LabelerBlockEntity;
import com.github.burgerguy.recordable.shared.block.RecorderBlock;
import com.github.burgerguy.recordable.shared.block.RecorderBlockEntity;
import com.github.burgerguy.recordable.shared.entrypoint.RecordableApi;
import com.github.burgerguy.recordable.shared.item.CopperRecordItem;
import com.github.burgerguy.recordable.shared.menu.ColorPalette;
import com.github.burgerguy.recordable.shared.menu.LabelerMenu;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.fabricmc.loader.launch.common.FabricLauncherBase;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.LevelResource;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.lifecycle.api.event.ServerTickEvents;
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldLoadEvents;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib3q.GeckoLib;

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
	public static final ResourceLocation CANVAS_LEVEL_CHANGE_ID = new ResourceLocation(MOD_ID, "canvas_level_change");

	public static ColorPalette COLOR_PALETTE;

	@Override
	public void onInitialize(ModContainer modContainer) {
		try {
			// add the entire LMDB code source to the path, so all subsequent classes are loaded by Knot
			// and all assigned mixins are applied to them
			URI lmdbJarUri = this.getClass().getClassLoader().getResource("org/lmdbjava").toURI();

			boolean fsNeedsCreation = false;
			FileSystem fs = null;
			try {
				fs = FileSystems.getFileSystem(lmdbJarUri);
				if (fs == null || !fs.isOpen()) {
					fsNeedsCreation = true;
				}
			} catch (FileSystemNotFoundException e) {
				fsNeedsCreation = true;
			}
			if (fsNeedsCreation) {
				fs = FileSystems.newFileSystem(lmdbJarUri, Map.of("create", "true"));
			}

			for (Path jarRootDir : fs.getRootDirectories()) {
				FabricLauncherBase.getLauncher().addToClassPath(jarRootDir);
				LOGGER.info("Loaded LMDB dir " + fs + jarRootDir.toAbsolutePath() + " into Knot");
			}
		} catch (Throwable t) {
			LOGGER.error("Unable to force load LMDB into Knot", t);
		}

		GeckoLib.initialize();

		ColorPalette colorPalette = new ColorPalette();
		colorPalette.setToDefaults();
		QuiltLoader.getEntrypointContainers(MOD_ID, RecordableApi.class).forEach(apiImpl -> apiImpl.getEntrypoint().modifyColorPalette(colorPalette));
		COLOR_PALETTE = colorPalette;

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
		ServerLifecycleEvents.STARTING.register(server -> {
			// kinda conc, but should be fine for now
			((ScoreDatabaseContainer) server).setScoreDatabase(new ScoreDatabase(server.getWorldPath(LevelResource.ROOT).resolve(SCORE_DATABASE_FILE_NAME)));
		});
		ServerLifecycleEvents.STOPPING.register(server -> ((ScoreDatabaseContainer) server).getScoreDatabase().close());

		// force stop all recorders, fixing block state
		ServerWorldLoadEvents.UNLOAD.register((server, serverLevel) -> ((ServerScoreRegistriesContainer) serverLevel).getScoreRecorderRegistry().removeAndCloseAll());

		// these are in the server tick because some packet handling is done outside the world tick
		// (irr)
		ServerTickEvents.START.register(server -> {
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
				FriendlyByteBuf newPacketBuffer = PacketByteBufs.create();
				newPacketBuffer.resetWriterIndex();
				newPacketBuffer.writeLong(scoreId);

				if (scoreData != null) {
					newPacketBuffer.writeBytes(scoreData);
				} else {
					LOGGER.warn("Player " + player + " requested invalid score id " + scoreId);
				}

				responseSender.sendPacket(Recordable.SEND_SCORE_ID, newPacketBuffer);
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(FINALIZE_LABEL_ID, (server, player, handler, buffer, responseSender) -> {
			if (player.containerMenu instanceof LabelerMenu labelerMenu) {
				labelerMenu.handleFinish(buffer);
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(CANVAS_LEVEL_CHANGE_ID, (server, player, handler, buffer, responseSender) -> {
			if (player.containerMenu instanceof LabelerMenu labelerMenu) {
				if (buffer.readableBytes() % (Integer.BYTES * 2) != 0) {
					LOGGER.warn("Player " + player + " requested canvas level change with bad byte multiple: " + buffer.readableBytes());
				}

				int[] levelChanges = new int[buffer.readableBytes() / Integer.BYTES];

				for (int i = 0; i < levelChanges.length; i++) {
					levelChanges[i] = buffer.readInt();
				}

				// update sender's menu server copy
				labelerMenu.handleCanvasLevelChange(levelChanges);

				// update other users who have a menu associated with the same labeler
				LabelerBlockEntity labelerBlockEntity = labelerMenu.getLabelerBlockEntity();
				List<ServerPlayer> trackingPlayers = ((ServerLevel) labelerBlockEntity.getLevel()).getChunkSource().chunkMap.getPlayers(new ChunkPos(labelerBlockEntity.getBlockPos()), false);
				for (ServerPlayer trackingPlayer : trackingPlayers) {
					if (!trackingPlayer.equals(player) && trackingPlayer.containerMenu instanceof LabelerMenu trackingLabelerMenu) {
						trackingLabelerMenu.handleSnapshotLevelChange(levelChanges);
					}
				}
			}
		});
	}

	public static ColorPalette getColorPalette() {
		return COLOR_PALETTE;
	}
}
