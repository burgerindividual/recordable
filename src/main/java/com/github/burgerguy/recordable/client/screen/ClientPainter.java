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
    private boolean erasing = false;
    private boolean mixing = false;

    private PaintStep[] paintSteps;
    private int lastPaintStepIdx = EMPTY_INDEX;
    private final IntList[] pixelPaintStepIdxs;

    public ClientPainter(int[] pixelIndexModel, int width, PaintColorWidget[] paintColorWidgets) {
        super(pixelIndexModel, width);
        this.paintColorWidgets = paintColorWidgets;
        this.paintSteps = new PaintStep[32]; // seems like a good starting number
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

            this.ensureStepsCapacity();
            int newStepIdx = ++this.lastPaintStepIdx;
            this.paintSteps[newStepIdx] = new PaintStep(oldColor, pixelIdx, events);
            IntList stepIndices = this.pixelPaintStepIdxs[pixelIdx];
            stepIndices.add(newStepIdx);
        }
    }

    public void erase(int pixelIdx) {
        IntList stepIndices = this.pixelPaintStepIdxs[pixelIdx];
        for (int stepIdx : stepIndices) {
            PaintStep paintStep = this.paintSteps[stepIdx];
            this.paintSteps[stepIdx] = null;
            for (PixelPaintEvent event : paintStep.events) this.paintColorWidgets[event.colorIndex].incrementLevel();
        }
        this.updateLastIdx();
        stepIndices.clear();
        // set back to white
        this.setColor(pixelIdx, CLEAR_COLOR);
    }

    public boolean undo() {
        this.updateLastIdx();
        if (this.lastPaintStepIdx == EMPTY_INDEX) {
            // nothing to remove
            return false;
        } else {
            // remove from both lists
            PaintStep lastStep = this.paintSteps[this.lastPaintStepIdx]; // FIXME check if this returns decremented or original
            this.paintSteps[this.lastPaintStepIdx--] = null;
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
        Arrays.fill(this.paintSteps, null);
        for (IntList list : this.pixelPaintStepIdxs) list.clear();
    }

    private void ensureStepsCapacity() {
        if (this.lastPaintStepIdx == this.paintSteps.length - 1) {
            this.compact();
            // compact didn't help need to grow array
            if (this.lastPaintStepIdx == this.paintSteps.length - 1) {
                this.paintSteps = ObjectArrays.grow(this.paintSteps, this.paintSteps.length + 1); // will increase in size by 50%
            }
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
            for (PixelPaintEvent ignored : step.events) {
                size += PER_PAINT_EVENT_BYTES;
            }
        }
        return size;
    }

    private record PixelPaintEvent(int colorIndex, boolean isMixed) {}
    private record PaintStep(int previousColorState, int pixelIndex, List<PixelPaintEvent> events) {}
}
