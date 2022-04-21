package com.github.burgerguy.recordable.client.screen;

import com.github.burgerguy.recordable.shared.menu.Painter;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;

// TODO: rename to ClientPainter and Painter? not sure yet
public class ClientPainter extends Painter {

    private final PaintColorWidget[] paintColorWidgets;
    private boolean erasing = false;
    private boolean mixing = false;

    private final List<PaintStep> paintSteps;
    private final IntList[] pixelPaintStepIdxs;

    public ClientPainter(int[] pixelIndexModel, int width, PaintColorWidget[] paintColorWidgets) {
        super(pixelIndexModel, width);
        this.paintColorWidgets = paintColorWidgets;
        this.paintSteps = new ObjectArrayList<>();
        this.pixelPaintStepIdxs = new IntList[this.colors.length];
        // 16 seems like a reasonable amount of edits before a resize is needed
        Arrays.setAll(this.pixelPaintStepIdxs, (idx) -> new IntArrayList(16));
    }

    public void apply(int pixelIdx) {
        if (this.erasing) {
            this.erase(pixelIdx);
        } else {
            this.paint(pixelIdx, this.mixing);
        }
    }

    public void paint(int pixelIdx, boolean mix) {
        List<PixelPaintEvent> events = new ObjectArrayList<>();
        int oldColor = this.getColor(pixelIdx);
        int currentColor = oldColor;

        for (int colorIdx = 0; colorIdx < this.paintColorWidgets.length; colorIdx++) {
            PaintColorWidget paintColorWidget = this.paintColorWidgets[colorIdx];
            int newColor = paintColorWidget.applyColor(mix, currentColor);
            if (currentColor != newColor) {
                currentColor = newColor;
                events.add(new PixelPaintEvent(colorIdx, mix));
                // the input mix variable should only apply to the first applied color, after it should always be true
                mix = true;
            }
        }

        if (oldColor != currentColor) {
            this.setColor(pixelIdx, currentColor);
            // actually decrement applied colors now
            for (PixelPaintEvent event : events) this.paintColorWidgets[event.colorIndex].decrementLevel();

            this.paintSteps.add(new PaintStep(oldColor, pixelIdx, events));
            IntList stepIndices = this.pixelPaintStepIdxs[pixelIdx];
            stepIndices.add(this.paintSteps.size() - 1);
        }
    }

    public void erase(int pixelIdx) {
        // reverse because removal will cause a shift of the ones in front.
        // this is avoided if we go in reverse.
        IntList stepIndices = this.pixelPaintStepIdxs[pixelIdx];
        for (int stepIdx : Lists.reverse(stepIndices)) {
            // FIXME: THIS IS BAD AND RUINS THE WHOLE ALGORITHM!!! all other SxS step indices get screwed when this happens
            PaintStep paintStep = this.paintSteps.remove(stepIdx);
            for (PixelPaintEvent event : paintStep.events) this.paintColorWidgets[event.colorIndex].incrementLevel();
        }
        stepIndices.clear();
        // set back to white
        this.setColor(pixelIdx, Painter.CLEAR_COLOR);
    }

    public boolean undo() {
        int paintStepsSize = this.paintSteps.size();
        if (paintStepsSize == 0) {
            // nothing to remove
            return false;
        } else {
            // remove from both lists
            PaintStep lastStep = this.paintSteps.remove(paintStepsSize - 1);
            IntList stepIndices = this.pixelPaintStepIdxs[lastStep.pixelIndex];
            stepIndices.removeInt(stepIndices.size() - 1);
            // actually set back variables for user
            this.setColor(lastStep.pixelIndex, lastStep.previousColorState);
            for (PixelPaintEvent event : lastStep.events) this.paintColorWidgets[event.colorIndex].incrementLevel();
            return true;
        }
    }

    @Override
    public void clear() {
        super.clear();
        this.paintSteps.clear();
        for (IntList list : this.pixelPaintStepIdxs) list.clear();
    }

    public void toggleErase() {
        this.erasing = !this.erasing;
    }

    public void toggleMix() {
        this.mixing = !this.mixing;
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

    public int getSizeBytes() {
        int size = 0;
        for (PaintStep step : this.paintSteps) {
            for (PixelPaintEvent event : step.events) {
                size += Painter.PER_PAINT_EVENT_BYTES;
            }
        }
        return size;
    }

    private record PixelPaintEvent(int colorIndex, boolean isMixed) {}
    private record PaintStep(int previousColorState, int pixelIndex, List<PixelPaintEvent> events) {}
}
