package com.github.burgerguy.recordable.shared.menu;

import javax.annotation.Nullable;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;

// handle pixel-color interactions in here, but actual gui events and rendering should be handled by the root screen.
// it's just too irritating to do in a wrapper class.
public class PrinterPalette {

    private final PrinterColor[] printerColors;
    @Nullable
    private PrinterColor selectedColor;

    public PrinterPalette(int[] rawColors) {
        this.printerColors = new PrinterColor[rawColors.length];

        for (int i = 0; i < rawColors.length; i++) {
            this.printerColors[i] = new PrinterColor(rawColors[i]);
        }
    }

    // handle rendering and pixel-color interactions in here, but actual gui events should be handled by the root screen.
    public void addToScreen(LabelPrinterScreen screen) {
        screen.a
    }

    public void addToMenu(LabelPrinterMenu menu) {
        menu.addSlotListener();
    }

}
