package com.github.burgerguy.recordable.shared.menu;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;

public class ColorPalette {
    private final Int2ObjectSortedMap<PaintColor> rawColorToPaintColorMap;

    public ColorPalette() {
        this.rawColorToPaintColorMap = new Int2ObjectLinkedOpenHashMap<>(16);
    }

    PaintColor addColor(int rawColor, Component name) {
        PaintColor paintColor = new PaintColor(rawColor, name);
        this.rawColorToPaintColorMap.put(rawColor, paintColor);
        return paintColor;
    }

    void addDye(DyeColor dyeColor) {
        PaintColor paintColor = this.addColor(dyeColor.getTextColor(), new TextComponent(dyeColor.getName()));
        paintColor.addItem(DyeItem.byColor(dyeColor), LabelerConstants.PAINT_LEVEL_PER_ITEM);
    }

    public void setToDefaults() {
        // ordered by hue because it looks nice
        this.addDye(DyeColor.WHITE);
        this.addDye(DyeColor.LIGHT_GRAY);
        this.addDye(DyeColor.GRAY);
        this.addDye(DyeColor.BLACK);
        this.addDye(DyeColor.RED);
        this.addDye(DyeColor.PINK);
        this.addDye(DyeColor.MAGENTA);
        this.addDye(DyeColor.PURPLE);
        this.addDye(DyeColor.BLUE);
        this.addDye(DyeColor.LIGHT_BLUE);
        this.addDye(DyeColor.CYAN);
        this.addDye(DyeColor.GREEN);
        this.addDye(DyeColor.LIME);
        this.addDye(DyeColor.YELLOW);
        this.addDye(DyeColor.BROWN);
        this.addDye(DyeColor.ORANGE);
        // add all the items under the typical dye tags
        this.addItemTagToDye(DyeColor.WHITE, ConventionalItemTags.WHITE_DYES);
        this.addItemTagToDye(DyeColor.LIGHT_GRAY, ConventionalItemTags.LIGHT_GRAY_DYES);
        this.addItemTagToDye(DyeColor.GRAY, ConventionalItemTags.GRAY_DYES);
        this.addItemTagToDye(DyeColor.BLACK, ConventionalItemTags.BLACK_DYES);
        this.addItemTagToDye(DyeColor.RED, ConventionalItemTags.RED_DYES);
        this.addItemTagToDye(DyeColor.PINK, ConventionalItemTags.PINK_DYES);
        this.addItemTagToDye(DyeColor.MAGENTA, ConventionalItemTags.MAGENTA_DYES);
        this.addItemTagToDye(DyeColor.PURPLE, ConventionalItemTags.PURPLE_DYES);
        this.addItemTagToDye(DyeColor.BLUE, ConventionalItemTags.BLUE_DYES);
        this.addItemTagToDye(DyeColor.LIGHT_BLUE, ConventionalItemTags.LIGHT_BLUE_DYES);
        this.addItemTagToDye(DyeColor.CYAN, ConventionalItemTags.CYAN_DYES);
        this.addItemTagToDye(DyeColor.GREEN, ConventionalItemTags.GREEN_DYES);
        this.addItemTagToDye(DyeColor.LIME, ConventionalItemTags.LIME_DYES);
        this.addItemTagToDye(DyeColor.YELLOW, ConventionalItemTags.YELLOW_DYES);
        this.addItemTagToDye(DyeColor.BROWN, ConventionalItemTags.BROWN_DYES);
        this.addItemTagToDye(DyeColor.ORANGE, ConventionalItemTags.ORANGE_DYES);
    }

    /**
     * Adds items to existing entries of the palette
     */
    public void addItemToRawColor(int rawColor, Item dyeItem, int level) {
        this.rawColorToPaintColorMap.get(rawColor).addItem(dyeItem, level);
    }

    public void addItemToRawColor(int rawColor, Item dyeItem) {
        this.addItemToRawColor(rawColor, dyeItem, LabelerConstants.PAINT_LEVEL_PER_ITEM);
    }

    /**
     * Helper method for {@link ColorPalette#addItemToRawColor(int, Item, int)}
     */
    public void addItemToDye(DyeColor dyeColor, Item dyeItem, int level) {
        this.addItemToRawColor(dyeColor.getTextColor(), dyeItem, level);
    }

    public void addItemToDye(DyeColor dyeColor, Item dyeItem) {
        this.addItemToDye(dyeColor, dyeItem, LabelerConstants.PAINT_LEVEL_PER_ITEM);
    }

    public void addItemTagToDye(DyeColor dyeColor, TagKey<Item> itemTag, int level) {
        for(Holder<Item> holder : Registry.ITEM.getTagOrEmpty(itemTag)) {
            this.addItemToDye(dyeColor, holder.value(), level);
        }
    }

    public void addItemTagToDye(DyeColor dyeColor, TagKey<Item> itemTag) {
        this.addItemTagToDye(dyeColor, itemTag, LabelerConstants.PAINT_LEVEL_PER_ITEM);
    }

    public void addItemTagToRawColor(int rawColor, TagKey<Item> itemTag, int level) {
        for(Holder<Item> holder : Registry.ITEM.getTagOrEmpty(itemTag)) {
            this.addItemToRawColor(rawColor, holder.value(), level);
        }
    }

    public void addItemTagToRawColor(int rawColor, TagKey<Item> itemTag) {
        this.addItemTagToRawColor(rawColor, itemTag, LabelerConstants.PAINT_LEVEL_PER_ITEM);
    }

    public int getColorCount() {
        return this.rawColorToPaintColorMap.size();
    }

    public Set<Item> getAllAcceptedItems() {
        Set<Item> items = new ObjectOpenHashSet<>();
        for (PaintColor paintColor : this.rawColorToPaintColorMap.values()) {
            items.addAll(paintColor.getAcceptedItems());
        }
        return items;
    }

    public void updatePaints(Int2IntMap rawColorToLevelMap, int maxPaintCapacity, Int2ObjectSortedMap<Paint> rawColorToPaintMap) {
        for (PaintColor paintColor : this.rawColorToPaintColorMap.values()) {
            int rawColor = paintColor.getRawColor();
            int newLevel = rawColorToLevelMap.get(paintColor.getRawColor());
            Paint existingPaint = rawColorToPaintMap.get(rawColor);
            if (existingPaint != null) {
                existingPaint.update(newLevel, maxPaintCapacity);
            } else {
                rawColorToPaintMap.put(rawColor, new Paint(paintColor, newLevel, maxPaintCapacity));
            }
        }
    }

    public Int2ObjectSortedMap<Paint> createPaintMap(int maxPaintCapacity) {
        Int2ObjectSortedMap<Paint> rawColorToPaintMap = new Int2ObjectLinkedOpenHashMap<>(this.getColorCount());
        for (PaintColor paintColor : this.rawColorToPaintColorMap.values()) {
            int rawColor = paintColor.getRawColor();
            rawColorToPaintMap.put(rawColor, new Paint(paintColor, maxPaintCapacity));
        }
        return rawColorToPaintMap;
    }

}
