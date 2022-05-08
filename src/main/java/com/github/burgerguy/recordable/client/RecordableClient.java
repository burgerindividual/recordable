package com.github.burgerguy.recordable.client;

import com.github.burgerguy.recordable.client.network.ClientPacketHandler;
import com.github.burgerguy.recordable.client.render.blockentity.RecorderBlockRenderer;
import com.github.burgerguy.recordable.client.render.blockentity.RecorderItemRenderer;
import com.github.burgerguy.recordable.client.score.play.ScorePlayerRegistry;
import com.github.burgerguy.recordable.client.score.play.ScorePlayerRegistryContainer;
import com.github.burgerguy.recordable.shared.Recordable;
import com.github.burgerguy.recordable.shared.block.RecorderBlock;
import com.github.burgerguy.recordable.shared.block.RecorderBlockEntity;
import com.github.burgerguy.recordable.shared.item.CopperRecordItem;
import com.github.burgerguy.recordable.shared.menu.LabelerMenu;
import com.github.burgerguy.recordable.client.screen.LabelerScreen;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientWorldTickEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class RecordableClient implements ClientModInitializer {

    @Override
    public void onInitializeClient(ModContainer modContainer) {
        //// networking registry
        ClientPlayNetworking.registerGlobalReceiver(Recordable.PLAY_SCORE_INSTANCE_AT_POS_ID, ClientPacketHandler::receivePlayScoreInstancePosPacket);
        ClientPlayNetworking.registerGlobalReceiver(Recordable.STOP_SCORE_INSTANCE_ID, ClientPacketHandler::receiveStopScoreInstancePacket);
        ClientPlayNetworking.registerGlobalReceiver(Recordable.SET_SCORE_INSTANCE_PAUSED_ID, ClientPacketHandler::receiveSetScoreInstancePausedPacket);
        ClientPlayNetworking.registerGlobalReceiver(Recordable.SEND_SCORE_ID, ClientPacketHandler::receiveSentScorePacket);

        //// event registry
        // TODO: should this be end world tick or end client tick?
        ClientWorldTickEvents.END.register((client, level) -> {
            // this probably will never be null. in a single player world the connection still exists, but it's just local
            @SuppressWarnings("ConstantConditions")
            ScorePlayerRegistry scorePlayerRegistry = ((ScorePlayerRegistryContainer) Minecraft.getInstance().getConnection()).getScorePlayerRegistry();
            scorePlayerRegistry.tick();
        });

        //// color provider registry
        ColorProviderRegistry.ITEM.register(CopperRecordItem::getColor, CopperRecordItem.INSTANCE);

        //// BER registry
        BlockEntityRendererRegistry.register(RecorderBlockEntity.INSTANCE, (BlockEntityRendererProvider.Context context) -> new RecorderBlockRenderer());
        BuiltinItemRendererRegistry.INSTANCE.register(RecorderBlock.ITEM_INSTANCE, new RecorderItemRenderer());

        //// screen registry
        MenuScreens.register(LabelerMenu.INSTANCE, LabelerScreen::new);
    }
}
