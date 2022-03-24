package com.github.burgerguy.recordable.mixin.client.score.play;

import com.github.burgerguy.recordable.client.score.play.ScorePlayerRegistry;
import com.github.burgerguy.recordable.client.score.play.ScorePlayerRegistryContainer;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin implements ScorePlayerRegistryContainer {
    private final ScorePlayerRegistry scorePlayerRegistry = new ScorePlayerRegistry();

    @Override
    public ScorePlayerRegistry getScorePlayerRegistry() {
        return scorePlayerRegistry;
    }
}