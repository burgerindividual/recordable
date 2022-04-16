package com.github.burgerguy.recordable.shared.menu;

import com.github.burgerguy.recordable.client.render.util.ScreenRenderUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PrinterColor extends Button {
    private static final int DEFAULT_BORDER_COLOR = 0xFF000000;
    private static final int SELECTED_BORDER_COLOR = 0xFFFF0000;

    private static final int MAX_CAPACITY = 16;
    private static final int CAPACITY_PER_ITEM = 4;

    private final int color;
    private final Item dyeItem;
    private int x;
    private int y;
    private int width;
    private int height;

    private boolean selected;
    private int capacity;

    public PrinterColor(int x, int y, int width, int height, int color, Item dyeItem) {
        super(x, y, width, height, );
        this.color = color;
        this.dyeItem = dyeItem;
    }

    public PrinterColor(int x, int y, int width, int height, DyeItem dyeItem) {
        super(x, y, width, height);
        this.color = dyeItem.getDyeColor().getMaterialColor().col; // TODO: should this use text color?
        this.dyeItem = dyeItem;
    }

    void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * This accepts whatever's in the dye slot to check if it should add to this, and if so, adds to this and removes
     * part of the dye.
     * @return if the itemStack was altered
     */
    public boolean addCapacity(ItemStack itemStack) {
        if (itemStack.is(this.dyeItem)) {

        }
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTick) {
        float x1 = x;
        float x2 = x + width;
        float y1 = y;
        float y2 = y + height;

        int borderColor = getBorderColor();

        Matrix4f matrix = matrixStack.last().pose();
        BufferBuilder bufferBuilder = ScreenRenderUtil.startFills();

        // top border
        ScreenRenderUtil.fill(
                bufferBuilder,
                matrix,
                x1,
                y1,
                x2,
                y1 + 1,
                borderColor
        );
        // left border
        ScreenRenderUtil.fill(
                bufferBuilder,
                matrix,
                x1,
                y1,
                x1 + 1,
                y2,
                borderColor
        );
        // bottom border
        ScreenRenderUtil.fill(
                bufferBuilder,
                matrix,
                x1,
                y2 - 1,
                x2,
                y2,
                borderColor
        );
        // right border
        ScreenRenderUtil.fill(
                bufferBuilder,
                matrix,
                x2 - 1,
                y1,
                x2,
                y2,
                borderColor
        );

        float filledPixels = ((y2 - 1) - (y1 + 1) * (float) capacity) / MAX_CAPACITY;
        // middle
        ScreenRenderUtil.fill(
                bufferBuilder,
                matrix,
                x1 + 1,
                y2 - 1 - filledPixels,
                x2 - 1,
                y2 - 1,
                this.color
        );

        ScreenRenderUtil.endFills(bufferBuilder);
    }

    private int getBorderColor() {
        return selected ? SELECTED_BORDER_COLOR : DEFAULT_BORDER_COLOR;
    }

    public int mixColor(int otherColor) {
        return mixColors(this.color, otherColor);
    }

    /**
     * Fast color blending algorithm found here: https://stackoverflow.com/a/8440673/4563900
     */
    private static int mixColors(int c1, int c2) {
        return (int) ((((c1 ^ c2) & 0xfefefefeL) >> 1) + (c1 & c2));
    }
}
