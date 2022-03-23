package com.github.burgerguy.recordable.shared;

import com.github.burgerguy.recordable.server.database.ScoreDatabase;
import com.github.burgerguy.recordable.server.database.ScoreDatabaseContainer;
import com.github.burgerguy.recordable.server.score.record.EntityScoreRecorder;
import com.github.burgerguy.recordable.server.score.record.RecorderRegistry;
import com.github.burgerguy.recordable.server.score.record.RecorderRegistryContainer;
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

	public static final ResourceLocation PLAY_SCORE_AT_POS_CHANNEL_ID = new ResourceLocation(MOD_ID, "play_score_at_pos");
	public static final ResourceLocation PLAY_SCORE_AT_ENTITY_CHANNEL_ID = new ResourceLocation(MOD_ID, "play_score_at_entity");

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
											((RecorderRegistryContainer) context.getSource().getLevel()).getRecorderRegistry().addRecorder(scoreRecorder);
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
									RecorderRegistry recorderRegistry = ((RecorderRegistryContainer) level).getRecorderRegistry();
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
		ServerLifecycleEvents.SERVER_STARTING.register(s -> {
			// kinda conc, but should be fine for now
			((ScoreDatabaseContainer) s).setScoreDatabase(new ScoreDatabase(s.getWorldPath(LevelResource.ROOT).resolve(SCORE_DATABASE_FILE_NAME)));
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(s -> {
			((ScoreDatabaseContainer) s).getScoreDatabase().close();
		});

		// force stop all recorders, fixing block state
		ServerWorldEvents.UNLOAD.register((s, sw) -> {
			((RecorderRegistryContainer) sw).getRecorderRegistry().closeAll();
		});

		ServerTickEvents.START_WORLD_TICK.register(sw -> ((RecorderRegistryContainer) sw).getRecorderRegistry().beginTick());
		ServerTickEvents.END_WORLD_TICK.register(sw -> ((RecorderRegistryContainer) sw).getRecorderRegistry().endTick());


	}
}
