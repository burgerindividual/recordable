package com.github.burgerguy.recordable.shared.menu;

import com.github.burgerguy.recordable.client.render.util.ScreenRenderUtil;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PrinterColor extends Button {

    private final int color;
    private final Item dyeItem;
    private final DataSlot level;

    private boolean selected;

    public PrinterColor(DyeColor dyeColor, int initialLevel) {
        super(0, 0, 0, 0, new TextComponent(dyeColor.getName()), PrinterColor::onPressedAction);
        this.color = dyeColor.getTextColor(); // TODO: should this use material color?
        this.dyeItem = DyeItem.byColor(dyeColor);
        this.level = DataSlot.standalone();
        this.level.set(initialLevel);
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    private static void onPressedAction(Button button) {
        PrinterColor printerColor = (PrinterColor) button;
        printerColor.selected = !printerColor.selected;
    }

    /**
     * This accepts whatever is in the dye slot to check if it should add to this, and if so, adds to this and removes
     * part of the dye.
     * @return if the itemStack was altered
     */
    public boolean addCapacity(ItemStack itemStack) {
        if (itemStack.is(this.dyeItem)) {
            // integer division truncates, which is what we want
            int currentCapacity = this.level.get();
            int consumed = (LabelerConstants.COLOR_MAX_CAPACITY - currentCapacity) / LabelerConstants.COLOR_CAPACITY_PER_ITEM;
            itemStack.shrink(consumed);
            this.level.set(currentCapacity + (consumed * LabelerConstants.COLOR_CAPACITY_PER_ITEM));
            return true;
        }
        return false;
    }

    public DataSlot getLevelSlot() {
        return level;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTick) {
        float x1 = this.x;
        float x2 = this.x + this.width;
        float y1 = this.y;
        float y2 = this.y + this.height;

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

        float filledPixels = (((y2 - 1) - (y1 + 1)) * this.level.get()) / LabelerConstants.COLOR_MAX_CAPACITY;
        // middle
        ScreenRenderUtil.fill(
                bufferBuilder,
                matrix,
                x1 + 1,
                y2 - 1 - filledPixels,
                x2 - 1,
                y2 - 1,
                this.color | 0xFF000000 // get rid of transparency
        );

        ScreenRenderUtil.endFills(bufferBuilder);
    }

    private int getBorderColor() {
        return this.selected ? LabelerConstants.SELECTED_BORDER_COLOR : LabelerConstants.DEFAULT_BORDER_COLOR;
    }

    /**
     * Mix color if selected and has a high enough level, otherwise don't affect the color.
     * This also decrements the level.
     */
    public int mixColor(int otherColor) {
        int currentLevel = this.level.get();
        if(this.selected && currentLevel > 0) {
            this.level.set(currentLevel - 1);
            if (this.level.get() == 0) {
                this.selected = false;
            }
            return mixColors(this.color, otherColor);
        }
        return otherColor;
    }

    /**
     * Fast color blending algorithm found here: https://stackoverflow.com/a/8440673/4563900
     */
    private static int mixColors(int c1, int c2) {
        return (int) ((((c1 ^ c2) & 0xfefefefeL) >> 1) + (c1 & c2));
    }
}
