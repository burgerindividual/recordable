package com.github.burgerguy.recordable.shared.menu;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;

public class ColorPalette {
    private final Int2ObjectSortedMap<Color> rawColorMap;

    public ColorPalette() {
        this.rawColorMap = new Int2ObjectLinkedOpenHashMap<>(16);
    }

    public void addColor(int rawColor, Component name) {
        this.getOrCreateKey(rawColor, name).dyeItems.add(dyeItem);
    }

    /**
     * Helper method for {@link ColorPalette#add(int, Component, Item)} to use built in
     * values from {@link DyeColor}
     */
    public void addFromDyeColor(DyeColor dyeColor) {
        this.add(dyeColor.getTextColor(), new TextComponent(dyeColor.getName()), DyeItem.byColor(dyeColor));
    }

    /**
     * Adds items to existing DyeColor entries of the palette
     */
    public void addItemsToDye(DyeColor dyeColor, Set<Item> dyeItems) {
        this.rawColorMap.get(dyeColor.getTextColor()).dyeItems.addAll(dyeItems);
    }

    /**
     * Helper method for {@link ColorPalette#addItemsToDye(DyeColor, Set)}
     */
    public void addItemToDye(DyeColor dyeColor, Item dyeItem) {
        this.rawColorMap.get(dyeColor.getTextColor()).dyeItems.add(dyeItem);
    }

    private Color getOrCreateKey(int rawColor, Component name) {
        return this.rawColorMap.computeIfAbsent(rawColor, rc -> {
            Set<Item> itemSet = new HashSet<>();
            return new Color(rc, name, itemSet);
        });
    }

    public Collection<Color> createPaintPalette() {

    }

    public record Color(int rawColor, Component name, Set<Item> colorItems) {}
    public record ColorItem(Item item, int levelAdd) {

    }
}
