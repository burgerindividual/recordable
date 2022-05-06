package com.github.burgerguy.recordable.shared.util;

import net.minecraft.util.Mth;

public class ColorUtil {

    public static int blendColors(int... colors) {
        if (colors.length == 0) return 0x00000000;

        int firstColor = colors[0];
        if (colors.length == 1) return firstColor;

        boolean allEqual = true;
        for (int color : colors) {
            if (color != firstColor) {
                allEqual = false;
                break;
            }
        }

        if (allEqual) return firstColor;

        double totalRSquared = 0;
        double totalGSquared = 0;
        double totalBSquared = 0;
        int totalColors = 0;

        for (int color : colors) {
            totalRSquared += Mth.square((color >> 16) & 0xFF);
            totalGSquared += Mth.square((color >> 8) & 0xFF);
            totalBSquared += Mth.square(color & 0xFF);
            totalColors++;
        }

        return (((int) Math.sqrt(totalRSquared / totalColors) & 0xFF) << 16) |
               (((int) Math.sqrt(totalGSquared / totalColors) & 0xFF) << 8)  |
               ((int) Math.sqrt(totalBSquared / totalColors) & 0xFF);
    }

    public static int blendColorsDirect(int... colors) {
        if (colors.length == 0) return 0x00000000;

        int firstColor = colors[0];
        if (colors.length == 1) return firstColor;

        boolean allEqual = true;
        for (int color : colors) {
            if (color != firstColor) {
                allEqual = false;
                break;
            }
        }

        if (allEqual) return firstColor;

        double totalR = 0;
        double totalG = 0;
        double totalB = 0;
        int totalColors = 0;

        for (int color : colors) {
            totalR += ((color >> 16) & 0xFF);
            totalG += ((color >> 8) & 0xFF);
            totalB += (color & 0xFF);
            totalColors++;
        }

        return (((int) Math.round(totalR / totalColors) & 0xFF) << 16) |
               (((int) Math.round(totalG / totalColors) & 0xFF) << 8) |
                ((int) Math.round(totalB / totalColors) & 0xFF);
    }
}
