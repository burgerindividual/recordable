package com.github.burgerguy.recordable.shared.menu;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class PrinterPixel extends Button {

    private final PrinterPalette palette;
    private int color;

    public PrinterPixel(int x, int y, int size, Component component, PrinterPalette palette) {
        super(
                x,
                y,
                size,
                size,
                component,
                (button) -> {

                }
        );
        this.palette = palette;
        this.color = 0xFF000000;
    }

    private static void onPressedAction(Button button) {
        PrinterPixel pixel = (PrinterPixel) button;
        pixel.color = palette.get
    }

    private void setColor(int color) {
        this.color = color;
    }

    private int getColor() {
        return color;
    }
}
