package com.github.burgerguy.recordable.shared.menu;

import com.github.burgerguy.recordable.shared.Recordable;
import com.github.burgerguy.recordable.shared.util.ColorUtil;
import java.util.Arrays;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class PaintArray {

    private static final int PER_PAINT_EVENT_BYTES = Integer.BYTES + Integer.BYTES + Byte.BYTES;

    private final int width;
    private final int height;
    protected final int[] pixelIndexModel;
    protected final int[] colors;

    public PaintArray(int[] pixelIndexModel, int width) {
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
        // all pixels default to white
        Arrays.fill(this.colors, 0xFFFFFFFF);
    }

    /**
     * This also reflects the side effects on the color levels from coloring.
     */
    public static PaintArray fromBuffer(int[] pixelIndexModel, int width, int[] colorLevels, FriendlyByteBuf buffer) {
        PaintArray paintArray = new PaintArray(pixelIndexModel, width);
        while (buffer.isReadable() && buffer.readableBytes() >= PER_PAINT_EVENT_BYTES) {
            int colorIdx = buffer.readInt();
            int pixelIdx = buffer.readInt();
            boolean isMixed = buffer.readBoolean();
            if (0 > colorIdx || colorIdx >= LabelerConstants.COLOR_COUNT) {
                Recordable.LOGGER.warn("Color index out of bounds: " + colorIdx);
            } else if (colorLevels[colorIdx] == 0) {
                Recordable.LOGGER.warn("Tried to use color which is already at 0 level (idx: " + colorIdx + ")");
            } else if (paintArray.isIndexValid(pixelIdx)) {
                Recordable.LOGGER.warn("Pixel index out of bounds: " + pixelIdx);
            } else {
                int resolvedColor = LabelerConstants.DEFINED_COLORS[colorIdx].color();
                int newColor = isMixed ? ColorUtil.mixColors(paintArray.getColor(pixelIdx), resolvedColor) : resolvedColor;
                paintArray.setColor(pixelIdx, newColor);
                colorLevels[colorIdx] -= 1;
            }
        }
        return paintArray;
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

    public boolean isIndexValid(int index) {
        return 0 < index && index < colors.length;
    }

    public int coordsToIndex(int x, int y) {
        return this.pixelIndexModel[y * this.width + x];
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}
