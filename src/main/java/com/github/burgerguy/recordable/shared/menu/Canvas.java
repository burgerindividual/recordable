package com.github.burgerguy.recordable.shared.menu;

import com.github.burgerguy.recordable.shared.Recordable;
import com.github.burgerguy.recordable.shared.util.ColorUtil;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.Arrays;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

public class Canvas {

    public static final int EMPTY_INDEX = -1;
    public static final int OUT_OF_BOUNDS_INDEX = -2;
    public static final int CLEAR_COLOR = 0xFFFFFFFF;
    protected static final int PAINT_STEP_HEADER_BYTES = Integer.BYTES + Integer.BYTES + Byte.BYTES;

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

    public static Canvas fromBuffer(int[] pixelIndexModel, int width, FriendlyByteBuf buffer) {
        Canvas canvas = new Canvas(pixelIndexModel, width);
        while (buffer.isReadable() && buffer.readableBytes() >= PAINT_STEP_HEADER_BYTES) {
            int pixelIdx = buffer.readInt();
            boolean isMixed = buffer.readBoolean();
            int rawColorCount = buffer.readInt();

            if (!canvas.isIndexValid(pixelIdx)) {
                Recordable.LOGGER.warn("Pixel index out of bounds: " + pixelIdx);
                break;
            } else if (buffer.readableBytes() < rawColorCount * Integer.BYTES) {
                Recordable.LOGGER.warn("Color count too large for buffer: " + rawColorCount);
            }

            int[] rawColors = new int[rawColorCount];
            for (int i = 0; i < rawColorCount; i++) {
                rawColors[i] = buffer.readInt();
            }

            int oldColor = canvas.getColor(pixelIdx);
            int blendedColor = ColorUtil.blendColorsDirect(rawColors);
            int newColor = isMixed ? ColorUtil.blendColorsDirect(oldColor, blendedColor) : blendedColor;
            canvas.setColor(pixelIdx, newColor);
        }
        return canvas;
    }

    public static Canvas fromBufferVerified(int[] pixelIndexModel, int width, FriendlyByteBuf buffer, Int2IntOpenHashMap levelsSnapshot, PaintPalette paintPalette) {
        Int2IntMap levelsSnapshotCopy = levelsSnapshot.clone();
        Canvas canvas = new Canvas(pixelIndexModel, width);
        while (buffer.isReadable() && buffer.readableBytes() >= PAINT_STEP_HEADER_BYTES) {
            int pixelIdx = buffer.readInt();
            boolean isMixed = buffer.readBoolean();
            int rawColorCount = buffer.readInt();

            if (!canvas.isIndexValid(pixelIdx)) {
                Recordable.LOGGER.warn("Pixel index out of bounds: " + pixelIdx);
                break;
            } else if (buffer.readableBytes() < rawColorCount * Integer.BYTES) {
                Recordable.LOGGER.warn("Color count too large for buffer: " + rawColorCount);
            }

            int[] rawColors = new int[rawColorCount];
            for (int i = 0; i < rawColorCount; i++) {
                rawColors[i] = buffer.readInt();
            }

            int oldColor = canvas.getColor(pixelIdx);
            int blendedColor = ColorUtil.blendColorsDirect(rawColors);
            int newColor = isMixed ? ColorUtil.blendColorsDirect(oldColor, blendedColor) : blendedColor;
            canvas.setColor(pixelIdx, newColor);
            // decrement the existing level for each color by one
            for (int rawColor : rawColors) {
                levelsSnapshotCopy.computeIfPresent(rawColor, (rc, lvl) -> lvl -= 1);
            }
        }
        if (!paintPalette.compareWithSnapshot(levelsSnapshotCopy)) {
            // validation failed
            canvas.clear();
            Recordable.LOGGER.warn("Canvas validation failed");
        }
        return canvas;
    }

    public void applyToTagNoAlpha(CompoundTag tag) {
        byte[] byteColors = new byte[this.colors.length * 3];
        for (int i = 0; i < this.colors.length; i++) {
            int color = this.colors[i];
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
        return 0 <= index && index < this.colors.length;
    }

    public boolean isCoordInBounds(int x, int y) {
        return (0 <= x && x < this.width) && (0 <= y && y < this.height);
    }

    public int coordsToIndex(int x, int y) {
        return this.isCoordInBounds(x, y) ? this.pixelIndexModel[y * this.width + x] : OUT_OF_BOUNDS_INDEX;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}
