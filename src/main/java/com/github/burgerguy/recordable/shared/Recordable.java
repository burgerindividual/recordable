package com.github.burgerguy.recordable.shared;

import com.github.burgerguy.recordable.server.database.ScoreDatabase;
import com.github.burgerguy.recordable.server.database.ScoreDatabaseContainer;
import com.github.burgerguy.recordable.server.score.record.EntityScoreRecorder;
import com.github.burgerguy.recordable.server.score.record.ScoreRecorderRegistry;
import com.github.burgerguy.recordable.server.score.record.ScoreRecorderRegistryContainer;
import com.github.burgerguy.recordable.server.score.record.ScoreRecorder;
import com.github.burgerguy.recordable.shared.block.CopperRecordItem;
import com.github.burgerguy.recordable.shared.block.RecordPlayerBlock;
import com.github.burgerguy.recordable.shared.block.RecorderBlock;
import com.github.burgerguy.recordable.shared.block.RecorderBlockEntity;
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
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Recordable implements ModInitializer {
	public static final String MOD_ID = "recordable";
	public static final Logger LOGGER = LoggerFactory.getLogger("recordable");

	public static final String SCORE_DATABASE_FILE_NAME = "scores.db";

	public static final ResourceLocation PLAY_SCORE_AT_POS_ID = new ResourceLocation(MOD_ID, "play_score_at_pos");
	public static final ResourceLocation PLAY_SCORE_AT_ENTITY_ID = new ResourceLocation(MOD_ID, "play_score_at_entity");
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

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher.register(
					Commands.literal("addstartrecording").then(
							Commands.argument("targets", EntityArgument.entities()).executes(
									context -> {
										for(Entity entity : EntityArgument.getEntities(context, "targets")) {
											ScoreRecorder scoreRecorder = new EntityScoreRecorder(
													entity,
													((ScoreDatabaseContainer) context.getSource().getServer()).getScoreDatabase(),
													(sr, id) -> {
														Entity sourceEntity = context.getSource().getEntity();
														if (sourceEntity != null) sourceEntity.sendMessage(new TextComponent("Force stopped recording"), Util.NIL_UUID);
													}
											);
											((ScoreRecorderRegistryContainer) context.getSource().getLevel()).getScoreRecorderRegistry().addRecorder(scoreRecorder);
											scoreRecorder.start();
										}
										Entity sourceEntity = context.getSource().getEntity();
										if (sourceEntity != null) sourceEntity.sendMessage(new TextComponent("Added and started recorder"), Util.NIL_UUID);
										return 1;
									}
							)
					)
			);
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher.register(
					Commands.literal("stopremoverecordingall").executes(
							context -> {
								for (ServerLevel level : context.getSource().getServer().getAllLevels()) {
									ScoreRecorderRegistry recorderRegistry = ((ScoreRecorderRegistryContainer) level).getScoreRecorderRegistry();
									recorderRegistry.stopAll();
									recorderRegistry.removeAll();
								}
								Entity sourceEntity = context.getSource().getEntity();
								if (sourceEntity != null) sourceEntity.sendMessage(new TextComponent("Stopped and removed all recorders"), Util.NIL_UUID);
								return 1;
							}
					)
			);
		});

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

		ServerTickEvents.START_WORLD_TICK.register(serverLevel -> ((ScoreRecorderRegistryContainer) serverLevel).getScoreRecorderRegistry().beginTick());
		ServerTickEvents.END_WORLD_TICK.register(serverLevel -> ((ScoreRecorderRegistryContainer) serverLevel).getScoreRecorderRegistry().endTick());

		ServerPlayNetworking.registerGlobalReceiver(REQUEST_SCORE_ID, (server, player, handler, buffer, responseSender) -> {
			// TODO: pass error to client, crash theirs
		});
	}
}
