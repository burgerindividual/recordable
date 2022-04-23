package com.github.burgerguy.recordable.client.screen;

import com.github.burgerguy.recordable.shared.menu.Painter;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import java.util.Arrays;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;

// TODO: rename to ClientPainter and Painter? not sure yet
public class ClientPainter extends Painter {

    private final PaintColorWidget[] paintColorWidgets;
    private final PaintStep[] paintSteps;
    private final IntList[] pixelPaintStepIdxs;

    private int lastPaintStepIdx = EMPTY_INDEX;
    private boolean erasing = false;
    private boolean mixing = false;

    public ClientPainter(int[] pixelIndexModel, int width, PaintColorWidget[] paintColorWidgets) {
        super(pixelIndexModel, width);
        this.paintColorWidgets = paintColorWidgets;
        int maximumSteps = Arrays.stream(paintColorWidgets).mapToInt(PaintColorWidget::getMaxCapacity).sum();
        this.paintSteps = new PaintStep[maximumSteps];
        this.pixelPaintStepIdxs = new IntList[this.colors.length];
        // 16 seems like a reasonable amount of edits before a resize is needed
        Arrays.setAll(this.pixelPaintStepIdxs, (idx) -> new IntArrayList(16));
    }

    public boolean apply(int pixelIdx) {
        if (this.erasing) {
            this.erase(pixelIdx);
            return false;
        } else {
            return this.paint(pixelIdx, this.mixing);
        }
    }

    /**
     * Returns true if any paint color runs out.
     */
    public boolean paint(int pixelIdx, boolean mix) {
        List<PixelPaintEvent> events = new ObjectArrayList<>();
        boolean anyPaintEmpty = false;
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
            for (PixelPaintEvent event : events) {
                PaintColorWidget paintColorWidget = this.paintColorWidgets[event.colorIndex];
                paintColorWidget.decrementLevel();
                anyPaintEmpty |= paintColorWidget.isEmpty();
            }

            this.ensureStepsCapacity();
            int newStepIdx = ++this.lastPaintStepIdx;
            this.paintSteps[newStepIdx] = new PaintStep(oldColor, pixelIdx, events);
            IntList stepIndices = this.pixelPaintStepIdxs[pixelIdx];
            stepIndices.add(newStepIdx);
        }

        return anyPaintEmpty;
    }

    public void erase(int pixelIdx) {
        IntList stepIndices = this.pixelPaintStepIdxs[pixelIdx];
        for (int stepIdx : stepIndices) {
            PaintStep paintStep = this.paintSteps[stepIdx];
            this.paintSteps[stepIdx] = null;
            for (PixelPaintEvent event : paintStep.events) this.paintColorWidgets[event.colorIndex].increaseLevel(1);
        }
        this.updateLastIdx();
        stepIndices.clear();
        // set back to white
        this.setColor(pixelIdx, CLEAR_COLOR);
    }

    public void undo() {
        this.updateLastIdx();
        // remove from both lists
        PaintStep lastStep = this.paintSteps[this.lastPaintStepIdx]; // FIXME check if this returns decremented or original
        this.paintSteps[this.lastPaintStepIdx--] = null;
        IntList stepIndices = this.pixelPaintStepIdxs[lastStep.pixelIndex];
        stepIndices.removeInt(stepIndices.size() - 1);
        // actually set back variables for user
        this.setColor(lastStep.pixelIndex, lastStep.previousColorState);
        for (PixelPaintEvent event : lastStep.events) this.paintColorWidgets[event.colorIndex].increaseLevel(1);
    }

    public boolean canUndo() {
        return this.lastPaintStepIdx != EMPTY_INDEX;
    }

    @Override
    public void clear() {
        super.clear();
        Arrays.fill(this.paintSteps, null);
        for (IntList list : this.pixelPaintStepIdxs) list.clear();
    }

    public void reset() {
        while (canUndo()) undo();
    }

    private void ensureStepsCapacity() {
        if (this.lastPaintStepIdx == this.paintSteps.length - 1) {
            // because we calculate the total possible amount of steps at the start,
            // we know that compacting will always give us enough space.
            this.compact();
        }
    }

    private void compact() {
        for (IntList list : this.pixelPaintStepIdxs) list.clear();

        int lastIdx = EMPTY_INDEX;
        for(int i = 0; i < this.paintSteps.length; i++){
            PaintStep step = this.paintSteps[i];
            if (step != null){
                lastIdx++;
                this.paintSteps[lastIdx] = step;
                this.pixelPaintStepIdxs[step.pixelIndex].add(lastIdx);
            }
        }

        int previousLastIndex = this.lastPaintStepIdx;
        if (lastIdx != EMPTY_INDEX && lastIdx != previousLastIndex) {
            for (int i = lastIdx; i <= previousLastIndex; i++) {
                this.paintSteps[i] = null;
            }
            this.lastPaintStepIdx = lastIdx;
        }
    }

    private void updateLastIdx() {
        int newIdx = EMPTY_INDEX;
        for (int i = this.lastPaintStepIdx; i >= 0; i--) {
            if (this.paintSteps[i] != null) {
                newIdx = i;
                break;
            }
        }
        this.lastPaintStepIdx = newIdx;
    }

    public void toggleErase() {
        this.erasing = !this.erasing;
    }

    public void toggleMix() {
        this.mixing = !this.mixing;
    }

    public void writeToPacket(FriendlyByteBuf buffer) {
        for (PaintStep step : this.paintSteps) {
            if (step != null) {
                for (PixelPaintEvent event : step.events) {
                    buffer.writeInt(event.colorIndex);
                    buffer.writeInt(step.pixelIndex);
                    buffer.writeBoolean(event.isMixed);
                }
            }
        }
    }

    public int getSizeBytes() {
        int size = 0;
        for (PaintStep step : this.paintSteps) {
            if (step != null) {
                for (PixelPaintEvent ignored : step.events) {
                    size += PER_PAINT_EVENT_BYTES;
                }
            }
        }
        return size;
    }

    private record PixelPaintEvent(int colorIndex, boolean isMixed) {}
    private record PaintStep(int previousColorState, int pixelIndex, List<PixelPaintEvent> events) {}
}
