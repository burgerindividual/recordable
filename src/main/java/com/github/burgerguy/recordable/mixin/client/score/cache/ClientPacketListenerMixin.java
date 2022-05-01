package com.github.burgerguy.recordable.mixin.client.score.cache;

import com.github.burgerguy.recordable.client.score.FutureScore;
import com.github.burgerguy.recordable.client.score.cache.ScoreCacheContainer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin implements ScoreCacheContainer {
    private final Long2ObjectMap<FutureScore> scoreCache = new Long2ObjectOpenHashMap<>();

    @Override
    public Long2ObjectMap<FutureScore> getScoreCache() {
        return this.scoreCache;
    }
}
