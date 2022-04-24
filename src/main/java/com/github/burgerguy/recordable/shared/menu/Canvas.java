package com.github.burgerguy.recordable.shared.menu;

import com.github.burgerguy.recordable.shared.Recordable;
import com.github.burgerguy.recordable.shared.util.ColorUtil;
import java.util.Arrays;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

public class Canvas {

    public static final int EMPTY_INDEX = -1;
    public static final int OUT_OF_BOUNDS_INDEX = -2;
    public static final int CLEAR_COLOR = 0xFFFFFFFF;
    protected static final int PER_PAINT_EVENT_BYTES = Integer.BYTES + Integer.BYTES + Byte.BYTES;

    private final int width;
    private final int height;
    protected final int[] pixelIndexModel;
    protected final int[] colors;

    public Canvas(int[] pixelIndexModel, int width) {
        this.pixelIndexModel = pixelIndexModel;
        this.width = width;
        // get maximum rows
        this.height = Mth.positiveCeilDiv(pixelIndexModel.length, width);

        int maxIdx = -1;
        for (int idx : pixelIndexModel) {
            if (idx > maxIdx) {
                maxIdx = idx;
            }
        }
        if (maxIdx == -1) throw new IllegalArgumentException("model needs to have at least 1 valid index");

        this.colors = new int[maxIdx + 1];
        // all pixels default to clear color (white)
        Arrays.fill(this.colors, CLEAR_COLOR);
    }

    /**
     * This also reflects the side effects on the color levels from coloring.
     */
    public static Canvas fromBuffer(int[] pixelIndexModel, int width, int[] colorLevels, FriendlyByteBuf buffer) {
        Canvas canvas = new Canvas(pixelIndexModel, width);
        while (buffer.isReadable() && buffer.readableBytes() >= PER_PAINT_EVENT_BYTES) {
            int colorIdx = buffer.readInt();
            int pixelIdx = buffer.readInt();
            boolean isMixed = buffer.readBoolean();
            if (0 > colorIdx || colorIdx >= LabelerConstants.COLOR_COUNT) {
                Recordable.LOGGER.warn("Color index out of bounds: " + colorIdx);
            } else if (colorLevels[colorIdx] == 0) {
                Recordable.LOGGER.warn("Tried to use color which is already at 0 level (idx: " + colorIdx + ")");
            } else if (!canvas.isIndexValid(pixelIdx)) {
                Recordable.LOGGER.warn("Pixel index out of bounds: " + pixelIdx);
            } else {
                int resolvedColor = LabelerConstants.DEFINED_COLORS[colorIdx].rawColor();
                int newColor = isMixed ? ColorUtil.mixColors(canvas.getColor(pixelIdx), resolvedColor) : resolvedColor;
                canvas.setColor(pixelIdx, newColor);
                colorLevels[colorIdx] -= 1;
            }
        }
        return canvas;
    }

    public void applyToTagNoAlpha(CompoundTag tag) {
        byte[] byteColors = new byte[colors.length * 3];
        for (int i = 0; i < colors.length; i++) {
            int color = colors[i];
            int firstIdx = i * 3;
            byteColors[firstIdx] = (byte) ((color >> 16) & 0xFF);
            byteColors[firstIdx + 1] = (byte) ((color >> 8) & 0xFF);
            byteColors[firstIdx + 2] = (byte) (color & 0xFF);
        }
        tag.putByteArray("Colors", byteColors);
    }

    public void setColor(int index, int color) {
        this.colors[index] = color;
    }

    public int getColor(int index) {
        return this.colors[index];
    }

    public void clear() {
        Arrays.fill(this.colors, CLEAR_COLOR);
    }

    public boolean isIndexValid(int index) {
        return 0 <= index && index < colors.length;
    }

    public boolean isCoordInBounds(int x, int y) {
        return (0 <= x && x < this.width) && (0 <= y && y < this.height);
    }

    public int coordsToIndex(int x, int y) {
        return isCoordInBounds(x, y) ? this.pixelIndexModel[y * this.width + x] : OUT_OF_BOUNDS_INDEX;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}
