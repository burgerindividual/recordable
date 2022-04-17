package com.github.burgerguy.recordable.shared.menu;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;

public class PrinterPixel extends Button {

    private final LabelerMenu labelerMenu;
    private int color;

    public PrinterPixel(int x, int y, int size, int idx, LabelerMenu labelerMenu) {
        super(
                x,
                y,
                size,
                size,
                new TextComponent("Pixel #" + idx),
                (button) -> {

                }
        );
        this.labelerMenu = labelerMenu;
        this.color = 0xFFFFFFFF;
    }

    private static void onPressedAction(Button button) {
        PrinterPixel pixel = (PrinterPixel) button;
        for (PrinterColor printerColor : pixel.labelerMenu.printerColors) {
            pixel.color = printerColor.mixColor(pixel.color);
        }
    }

    public int getColor() {
        return color;
    }
}
