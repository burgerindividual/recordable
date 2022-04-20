package com.github.burgerguy.recordable.shared.menu;

import net.minecraft.world.item.DyeColor;

public class LabelerConstants {
    public static final int CONTAINER_SIZE = 4;

    public static final int PALETTE_X = 4;
    public static final int PALETTE_Y = 13;
    public static final int PALETTE_COLUMNS_WRAP = 4;
    public static final int COLOR_WIDTH = 12;
    public static final int COLOR_HEIGHT = 12;
    public static final int COLOR_MARGIN_X = 2;
    public static final int COLOR_MARGIN_Y = 2;

    public static final int DEFAULT_BORDER_COLOR = 0xFF000000;
    public static final int SELECTED_BORDER_COLOR = 0xFFFF0000;

    public static final int COLOR_MAX_CAPACITY = 20;
    public static final int COLOR_CAPACITY_PER_ITEM = 5;

    // sorted by hue because it looks nice
    public static final PaintColor[] DEFINED_COLORS = {
            PaintColor.fromDyeColor(DyeColor.WHITE),
            PaintColor.fromDyeColor(DyeColor.LIGHT_GRAY),
            PaintColor.fromDyeColor(DyeColor.GRAY),
            PaintColor.fromDyeColor(DyeColor.BLACK),
            PaintColor.fromDyeColor(DyeColor.RED),
            PaintColor.fromDyeColor(DyeColor.PINK),
            PaintColor.fromDyeColor(DyeColor.MAGENTA),
            PaintColor.fromDyeColor(DyeColor.PURPLE),
            PaintColor.fromDyeColor(DyeColor.BLUE),
            PaintColor.fromDyeColor(DyeColor.LIGHT_BLUE),
            PaintColor.fromDyeColor(DyeColor.CYAN),
            PaintColor.fromDyeColor(DyeColor.GREEN),
            PaintColor.fromDyeColor(DyeColor.LIME),
            PaintColor.fromDyeColor(DyeColor.YELLOW),
            PaintColor.fromDyeColor(DyeColor.BROWN),
            PaintColor.fromDyeColor(DyeColor.ORANGE)
    };
    public static final int COLOR_COUNT = DEFINED_COLORS.length;

    public static final int[] RECORD_PIXEL_INDEX_MODEL = {
            -1,  0,  1,  2, -1,
             3,  4, -1,  5,  6,
            -1,  7,  8,  9, -1
    };
    public static final int RECORD_PIXEL_MODEL_WIDTH = 5;


}
