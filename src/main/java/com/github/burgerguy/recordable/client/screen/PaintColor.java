package com.github.burgerguy.recordable.client.screen;

import com.github.burgerguy.recordable.client.render.util.ScreenRenderUtil;
import com.github.burgerguy.recordable.shared.menu.LabelerConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import java.awt.Color;
import java.util.OptionalInt;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PaintColor extends Button {

    private final int color;
    private final int colorGrad;
    private final Item dyeItem;

    private int level;
    private boolean selected;

    public PaintColor(DyeColor dyeColor, int initialLevel) {
        super(0, 0, 0, 0, new TextComponent(dyeColor.getName()), PaintColor::onPressedAction);
        this.color = dyeColor.getTextColor() | 0xFF000000; // make opaque
        this.colorGrad = new Color(color).darker().getRGB();
        this.dyeItem = DyeItem.byColor(dyeColor);
        this.level = initialLevel;
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    private static void onPressedAction(Button button) {
        PaintColor paintColor = (PaintColor) button;
        paintColor.selected = !paintColor.selected;
    }

    /**
     * This accepts whatever is in the dye slot to check if it should add to this, and if so, adds to this and removes
     * part of the dye.
     * @return if the itemStack was altered
     */
    public boolean addCapacity(ItemStack itemStack) {
        if (itemStack.is(this.dyeItem)) {
            // integer division truncates, which is what we want
            int consumed = (LabelerConstants.COLOR_MAX_CAPACITY - this.level) / LabelerConstants.COLOR_CAPACITY_PER_ITEM;
            itemStack.shrink(consumed);
            this.level += (consumed * LabelerConstants.COLOR_CAPACITY_PER_ITEM);
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

        float filledPixels = (((y2 - 1) - (y1 + 1)) * this.level) / LabelerConstants.COLOR_MAX_CAPACITY;
        // middle
        ScreenRenderUtil.fillGradient(
                matrix,
                x1 + 1,
                y2 - 1 - filledPixels,
                x2 - 1,
                y2 - 1,
                this.color,
                this.colorGrad
        );;
    }

    private int getBorderColor() {
        return this.selected ? LabelerConstants.SELECTED_BORDER_COLOR : LabelerConstants.DEFAULT_BORDER_COLOR;
    }

    /**
     * Mix or get color if selected and has a high enough level, otherwise don't affect the color.
     * If not mixing, and the color can't be used, return an empty.
     * This also decrements the level.
     */
    public OptionalInt applyColor(boolean mix, int otherColor) {
        if(this.selected && this.level > 0) {
            this.level -= 1;
            if (this.level == 0) {
                this.selected = false;
            }
            if (mix) {
                return OptionalInt.of(mixColors(this.color, otherColor));
            } else {
                return OptionalInt.of(this.color);
            }
        }
        return mix ? OptionalInt.of(otherColor) : OptionalInt.empty();
    }

    /**
     * Fast color blending algorithm found here: https://stackoverflow.com/a/8440673/4563900
     */
    private static int mixColors(int c1, int c2) {
        return (int) ((((c1 ^ c2) & 0xfefefefeL) >> 1) + (c1 & c2));
    }
}
