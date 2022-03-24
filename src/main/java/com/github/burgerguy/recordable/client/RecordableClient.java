package com.github.burgerguy.recordable.client;

import com.github.burgerguy.recordable.shared.Recordable;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class RecordableClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(Recordable.PLAY_SCORE_AT_POS_ID, (client, handler, buf, responseSender) -> {
//            responseSender.createPacket()
        });
    }
}
