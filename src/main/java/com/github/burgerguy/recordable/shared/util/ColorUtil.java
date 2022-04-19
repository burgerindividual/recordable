package com.github.burgerguy.recordable.shared.util;

public class ColorUtil {

    /**
     * Fast color blending algorithm found here: https://stackoverflow.com/a/8440673/4563900
     */
    public static int mixColors(int c1, int c2) {
        return (int) ((((c1 ^ c2) & 0xfefefefeL) >> 1) + (c1 & c2));
    }
}
