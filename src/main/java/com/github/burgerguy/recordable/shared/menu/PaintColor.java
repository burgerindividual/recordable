package com.github.burgerguy.recordable.shared.menu;

import com.github.burgerguy.recordable.shared.Recordable;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collection;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

public final class PaintColor {
    public static final int ITEM_INVALID = -1;

    private final int rawColor;
    private final Component name;
    private final Object2IntMap<Item> itemsToLevels;

    public PaintColor(int rawColor, Component name) {
        this.rawColor = rawColor;
        this.name = name;
        this.itemsToLevels = new Object2IntOpenHashMap<>(4);
        this.itemsToLevels.defaultReturnValue(ITEM_INVALID);
    }

    public int getRawColor() {
        return this.rawColor;
    }

    public Component getName() {
        return this.name;
    }

    /**
     * @param item The item to be added to the valid items list.
     * @param level The amount of uses the paint should increase by.
     */
    public void addItem(Item item, int level) {
        if (this.itemsToLevels.containsKey(item)) {
            Recordable.LOGGER.info("Level for item " + item.toString() + " overwritten to " + level);
        }

        this.itemsToLevels.put(item, level);
    }

    public int getItemLevelOrInvalid(Item item) {
        return this.itemsToLevels.getInt(item);
    }

    public Collection<Item> getAcceptedItems() {
        return this.itemsToLevels.keySet();
    }

}
