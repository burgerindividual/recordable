package com.github.burgerguy.recordable.shared;

import com.github.burgerguy.recordable.mixin.server.score.database.MinecraftServerExtended;
import com.github.burgerguy.recordable.server.database.RecordDatabase;
import com.github.burgerguy.recordable.server.database.RecordDatabaseContainer;
import com.github.burgerguy.recordable.server.score.record.EntityScoreRecorder;
import com.github.burgerguy.recordable.server.score.record.RecorderRegistry;
import com.github.burgerguy.recordable.server.score.record.ScoreRecorder;
import com.github.burgerguy.recordable.shared.block.RecorderBlockEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Recordable implements ModInitializer {
	public static final String MOD_ID = "recordable";
	public static final Logger LOGGER = LoggerFactory.getLogger("recordable");

	public static final String RECORD_DATABASE_FILE_NAME = "records.db";

	public static final Block RECORDER_BLOCK = new Block(FabricBlockSettings.of(Material.METAL).strength(4.0f));
	public static final ResourceLocation RECORDER_BLOCK_RESOURCE = new ResourceLocation(MOD_ID, "recorder");
	public static BlockEntityType<RecorderBlockEntity> RECORDER_BLOCK_ENTITY;
	public static final String RECORDER_BLOCK_ENTITY_ID = MOD_ID + ":recorder_block_entity";

	@Override
	public void onInitialize() {
		// block registry
		Registry.register(Registry.BLOCK, RECORDER_BLOCK_RESOURCE, RECORDER_BLOCK);

		// item registry
		Registry.register(Registry.ITEM, RECORDER_BLOCK_RESOURCE, new BlockItem(RECORDER_BLOCK, new FabricItemSettings().group(CreativeModeTab.TAB_MISC)));

		// block entity registry
		RECORDER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, RECORDER_BLOCK_ENTITY_ID, FabricBlockEntityTypeBuilder.create(RecorderBlockEntity::new, RECORDER_BLOCK).build(null));

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher.register(
					Commands.literal("addstartrecording").then(
							Commands.argument("targets", EntityArgument.entities()).executes(
									context -> {
										for(Entity entity : EntityArgument.getEntities(context, "targets")) {
											ScoreRecorder scoreRecorder = new EntityScoreRecorder(
													entity,
													((RecordDatabaseContainer) context.getSource().getServer()).getRecordDatabase(),
													(sr, id) -> {
														Entity sourceEntity = context.getSource().getEntity();
														if (sourceEntity != null) sourceEntity.sendMessage(new TextComponent("Force stopped recording"), Util.NIL_UUID);
													}
											);
											RecorderRegistry.addRecorder(scoreRecorder);
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
								RecorderRegistry.stopAll();
								RecorderRegistry.removeAll();
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
			((RecordDatabaseContainer) s).setRecordDatabase(new RecordDatabase(s.getWorldPath(LevelResource.ROOT).resolve(RECORD_DATABASE_FILE_NAME)));
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(s -> ((RecordDatabaseContainer) s).getRecordDatabase().close());
		ServerTickEvents.START_WORLD_TICK.register(sw -> RecorderRegistry.beginTick());
		ServerTickEvents.END_WORLD_TICK.register(sw -> RecorderRegistry.endTick());
	}
}
