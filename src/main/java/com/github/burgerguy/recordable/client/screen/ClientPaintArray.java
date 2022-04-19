package com.github.burgerguy.recordable.client.screen;

import com.github.burgerguy.recordable.shared.menu.PaintArray;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;

public class ClientPaintArray extends PaintArray {

    private final List<PaintStep> paintSteps;
    private final IntList[] pixelPaintStepIdxs;

    public ClientPaintArray(int[] pixelIndexModel, int width) {
        super(pixelIndexModel, width);
        this.paintSteps = new ObjectArrayList<>();
        this.pixelPaintStepIdxs = new IntList[this.colors.length];
        // 16 seems like a reasonable amount of edits before a resize is needed
        Arrays.setAll(this.pixelPaintStepIdxs, (idx) -> new IntArrayList(16));
    }

    public void paint(int x, int y, PaintColorWidget[] paintColorWidgets, boolean mix) {
        List<PixelPaintEvent> events = new ObjectArrayList<>();
        int pixelIdx = this.coordsToIndex(x, y);
        for (int colorIdx = 0; colorIdx < paintColorWidgets.length; colorIdx++) {
            PaintColorWidget paintColorWidget = paintColorWidgets[colorIdx];
            int oldColor = this.getColor(pixelIdx);
            int newColor = paintColorWidget.applyColor(mix, oldColor);
            if (oldColor != newColor) {
                this.setColor(pixelIdx, newColor);
                events.add(new PixelPaintEvent(colorIdx, mix));
                // the input mix variable should only apply to the first applied color, after it should always be true
                mix = true;
            }
        }
        this.paintSteps.add(new PaintStep(this.getColor(pixelIdx), pixelIdx, events));

        IntList stepIndices = this.pixelPaintStepIdxs[pixelIdx];
        stepIndices.add(this.paintSteps.size() - 1);
    }

    public void erase(int x, int y) {
        int index = this.coordsToIndex(x, y);
        // reverse because removal will cause a shift of the ones in front.
        // this is avoided if we go in reverse.
        IntList stepIndices = pixelPaintStepIdxs[index];
        for (int stepIdx : Lists.reverse(stepIndices)) {
            paintSteps.remove(stepIdx);
        }
        stepIndices.clear();
        // set back to white
        setColor(index, 0xFFFFFFFF);
    }

    public boolean undo() {
        int paintStepsSize = this.paintSteps.size();
        if (paintStepsSize <= 0) {
            // nothing to remove
            return false;
        } else {
            // remove from both lists
            PaintStep lastStep = this.paintSteps.remove(paintStepsSize - 1);
            IntList stepIndices = this.pixelPaintStepIdxs[lastStep.pixelIndex];
            stepIndices.removeInt(stepIndices.size() - 1);
            return true;
        }
    }

    public void writeToPacket(FriendlyByteBuf buffer) {
        for (PaintStep step : this.paintSteps) {
            for (PixelPaintEvent event : step.events) {
                buffer.writeInt(event.colorIndex);
                buffer.writeInt(step.pixelIndex);
                buffer.writeBoolean(event.isMixed);
            }
        }
    }

    private record PixelPaintEvent(int colorIndex, boolean isMixed) {}
    private record PaintStep(int colorState, int pixelIndex, List<PixelPaintEvent> events) {}
}
