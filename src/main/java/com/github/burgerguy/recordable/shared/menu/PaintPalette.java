package com.github.burgerguy.recordable.shared.menu;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import net.minecraft.world.item.ItemStack;

public record PaintPalette(Int2ObjectSortedMap<Paint> rawColorToPaintMap, Int2ObjectMap<Deque<ItemStack>> rawColorToItemHistory) {
    public Collection<Paint> getPaints() {
        return this.rawColorToPaintMap.values();
    }

    public IntSortedSet getRawColors() {
        return this.rawColorToPaintMap.keySet();
    }

    public Deque<ItemStack> getItemHistory(int rawColor) {
        return this.rawColorToItemHistory.computeIfAbsent(rawColor, rc -> new ArrayDeque<>(8));
    }

    public void clearAllItemHistory() {
        for (Deque<ItemStack> itemHistory : this.rawColorToItemHistory.values()) {
            itemHistory.clear();
        }
    }
}
