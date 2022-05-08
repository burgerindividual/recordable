package com.github.burgerguy.recordable.shared.menu;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
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
    }

    /**
     * Adds items to existing entries of the palette
     */
    public void addItemToRawColor(int rawColor, Item dyeItem, int level) {
        this.rawColorToPaintColorMap.get(rawColor).addItem(dyeItem, level);
    }

    /**
     * Helper method for {@link ColorPalette#addItemToRawColor(int, Item, int)}
     */
    public void addItemToDye(DyeColor dyeColor, Item dyeItem, int level) {
        this.addItemToRawColor(dyeColor.getTextColor(), dyeItem, level);
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
