package com.github.burgerguy.recordable.client.screen;

import com.github.burgerguy.recordable.client.render.util.ScreenRenderUtil;
import com.github.burgerguy.recordable.shared.menu.LabelerConstants;
import com.github.burgerguy.recordable.shared.menu.PaintColor;
import com.github.burgerguy.recordable.shared.util.ColorUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import java.awt.Color;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PaintColorWidget extends Button {

    private final int color;
    private final int colorGrad;
    private final Item dyeItem;
    private final int maxCapacity;
    private final int levelPerItem;

    private int level;
    private boolean selected;

    public PaintColorWidget(PaintColor paintColor, int initialLevel, int maxCapacity, int levelPerItem) {
        super(0, 0, 0, 0, paintColor.name(), PaintColorWidget::onPressedAction);
        this.color = paintColor.color(); // make opaque
        this.colorGrad = new Color(color).darker().getRGB();
        this.dyeItem = paintColor.dyeItem();
        this.maxCapacity = maxCapacity;
        this.levelPerItem = levelPerItem;
        this.level = initialLevel;
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    private static void onPressedAction(Button button) {
        PaintColorWidget paintColorWidget = (PaintColorWidget) button;
        paintColorWidget.selected = !paintColorWidget.selected;
    }

    /**
     * This accepts whatever is in the dye slot to check if it should add to this, and if so, adds to this and removes
     * part of the dye.
     * @return if the itemStack was altered
     */
    public boolean addCapacity(ItemStack itemStack) {
        if (itemStack.is(this.dyeItem)) {
            // integer division truncates, which is what we want
            int consumed = (this.maxCapacity - this.level) / this.levelPerItem;
            itemStack.shrink(consumed);
            this.level += (consumed * this.levelPerItem);
            return true;
        }
        return false;
    }

    @Override
    public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTick) {
        float x1 = this.x;
        float x2 = this.x + this.width;
        float y1 = this.y;
        float y2 = this.y + this.height;

        int borderColor = getBorderColor();

        Matrix4f matrix = matrixStack.last().pose();

        // top border
        ScreenRenderUtil.fill(
                matrix,
                x1,
                y1,
                x2,
                y1 + 1,
                borderColor
        );
        // left border
        ScreenRenderUtil.fill(
                matrix,
                x1,
                y1,
                x1 + 1,
                y2,
                borderColor
        );
        // bottom border
        ScreenRenderUtil.fill(
                matrix,
                x1,
                y2 - 1,
                x2,
                y2,
                borderColor
        );
        // right border
        ScreenRenderUtil.fill(
                matrix,
                x2 - 1,
                y1,
                x2,
                y2,
                borderColor
        );

        float filledPixels = (((y2 - 1) - (y1 + 1)) * this.level) / this.maxCapacity;
        // middle
        ScreenRenderUtil.fillGradient(
                matrix,
                x1 + 1,
                y2 - 1 - filledPixels,
                x2 - 1,
                y2 - 1,
                this.color | 0xFF000000,
                this.colorGrad | 0xFF000000
        );;
    }

    private int getBorderColor() {
        return this.selected ? LabelerConstants.SELECTED_BORDER_COLOR : LabelerConstants.DEFAULT_BORDER_COLOR;
    }

    /**
     * Mix or get color if selected and has a high enough level, otherwise don't affect the color.
     */
    public int applyColor(boolean mix, int otherColor) {
        if(this.selected && this.active) {
            if (mix) {
                return ColorUtil.mixColors(this.color, otherColor);
            } else {
                return this.color;
            }
        }
        return otherColor;
    }

    /**
     * Returns true if the level
     */
    public void decrementLevel() {
        if (isEmpty()) throw new IllegalStateException("Tried to decrement level when already empty");

        this.level--;
        if (isEmpty()) {
            this.selected = false;
            this.active = false;
        }
    }

    public void increaseLevel(int amount) {
        this.level += amount;
        this.active = true;
    }

    public boolean isEmpty() {
        return level == 0;
    }

    public int getMaxCapacity() {
        return this.maxCapacity;
    }

}
