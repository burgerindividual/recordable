package com.github.burgerguy.recordable.shared.menu;

import java.util.Arrays;
import net.minecraft.util.Mth;

public class PaintArray {

    private final int width;
    private final int height;
    private final int[] currentColors;

    public PaintArray(int width, boolean[] model) {
        this.width = width;
        // get maximum rows
        int height = Mth.positiveCeilDiv(model.length, width);
        this.height = height;
        this.currentColors = new int[width * height];
        // all pixels default to white
        Arrays.fill(this.currentColors, 0xFFFFFFFF);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
