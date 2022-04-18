package com.github.burgerguy.recordable.client.screen;

import com.github.burgerguy.recordable.shared.menu.PaintArray;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;

public class ClientPaintArray extends PaintArray {
    private final List<PaintStep> paintSteps;
    private final IntList[] pixelPaintStepIdxs;

    public ClientPaintArray(int width, boolean[] model) {
        super(width, model);
        this.paintSteps = new ArrayList<>();
        // use compressed model
        this.pixelPaintStepIdxs = new IntList[];
    }

    public void paint(int x, int y, PaintColor[] paintColors) {
        for (PaintColor paintColor : paintColors) {
            pixel.color = paintColor.applyColor(pixel.color);
        }
    }

    private record PaintStep(int color, List<PaintEvent> events) {}
    private record PaintEvent(int colorIndex, int pixelIndex, boolean isMixed) {}
}
