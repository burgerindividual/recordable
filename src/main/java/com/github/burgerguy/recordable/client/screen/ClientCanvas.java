package com.github.burgerguy.recordable.client.screen;

import com.github.burgerguy.recordable.shared.menu.Canvas;
import com.github.burgerguy.recordable.shared.menu.Paint;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import java.util.Arrays;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;

public class ClientCanvas extends Canvas {

    private final IntList[] pixelPaintStepIdxs;
    private final Int2ObjectMap<Paint> rawColorToPaintMap;
    private PaintStep[] paintSteps;

    private int lastPaintStepIdx = EMPTY_INDEX;
    private boolean erasing = false;
    private boolean mixing = false;

    public ClientCanvas(int[] pixelIndexModel, int width, Int2ObjectMap<Paint> rawColorToPaintMap) {
        super(pixelIndexModel, width);
        this.rawColorToPaintMap = rawColorToPaintMap;
        // this isn't the true maximum because dyes can be refilled as you're drawing, but it's a good starting point
        int maximumSteps = rawColorToPaintMap.values().stream().mapToInt(Paint::getMaxCapacity).sum();
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

        for (Paint paint : this.rawColorToPaintMap.values()) {
            int newColor = paint.applyColor(mix, currentColor);
            if (currentColor != newColor) {
                currentColor = newColor;
                events.add(new PixelPaintEvent(paint.getColor().getRawColor(), mix));
                // the input mix variable should only apply to the first applied color, after it should always be true
                mix = true;
            }
        }

        if (oldColor != currentColor) {
            this.setColor(pixelIdx, currentColor);
            // actually decrement applied colors now
            for (PixelPaintEvent event : events) {
                Paint paint = this.rawColorToPaintMap.get(event.rawColor);
                paint.decrementLevel();
                anyPaintEmpty |= paint.isEmpty();
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
            for (PixelPaintEvent event : paintStep.events) {
                Paint paint = this.rawColorToPaintMap.get(event.rawColor);
                if (!paint.isFull()) paint.incrementLevel();
            }
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
        for (PixelPaintEvent event : lastStep.events) this.rawColorToPaintMap.get(event.rawColor).incrementLevel();
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
        while (this.canUndo()) this.undo();
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

    public void setErasing(boolean erasing) {
        this.erasing = erasing;
    }

    public void setMixing(boolean mixing) {
        this.mixing = mixing;
    }

    public boolean isErasing() {
        return this.erasing;
    }

    public boolean isMixing() {
        return this.mixing;
    }

    public void writeToPacket(FriendlyByteBuf buffer) {
        for (PaintStep step : this.paintSteps) {
            if (step != null) {
                for (PixelPaintEvent event : step.events) {
                    buffer.writeInt(event.rawColor);
                    buffer.writeInt(step.pixelIndex);
                    buffer.writeBoolean(event.isMixed);
                }
            }
        }
    }

    private record PixelPaintEvent(int rawColor, boolean isMixed) {}
    private record PaintStep(int previousColorState, int pixelIndex, List<PixelPaintEvent> events) {}
}
