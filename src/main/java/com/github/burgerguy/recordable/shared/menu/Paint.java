package com.github.burgerguy.recordable.shared.menu;

import com.github.burgerguy.recordable.shared.util.ColorUtil;
import net.minecraft.world.item.ItemStack;

public class Paint {

    private final PaintColor color;
    private final int maxCapacity;
    private final int levelPerItem;

    private int level;
    private boolean canApply;

    public Paint(PaintColor color, int initialLevel, int maxCapacity, int levelPerItem) {
        this.color = color;
        this.maxCapacity = maxCapacity;
        this.levelPerItem = levelPerItem;
        this.level = initialLevel;
    }

    public PaintColor getColor() {
        return this.color;
    }

    public int getMaxCapacity() {
        return this.maxCapacity;
    }

    public int getLevel() {
        return this.level;
    }

    public void setCanApply(boolean canApply) {
        this.canApply = canApply;
    }

    public int applyColor(boolean mix, int otherColor) {
        if (this.canApply) {
            if (mix) {
                return ColorUtil.mixColors(this.color.rawColor(), otherColor);
            } else {
                return this.color.rawColor();
            }
        }
        return otherColor;
    }

    /**
     * This accepts whatever is in the dye slot to check if it should add to this, and if so, adds to this and removes
     * part of the dye.
     * @return if the itemStack was altered
     */
    public boolean addLevelFromItem(ItemStack itemStack) {
        if (itemStack.is(this.color.dyeItem())) {
            // integer division truncates, which is what we want
            int consumed = Math.min((this.maxCapacity - this.level) / this.levelPerItem, itemStack.getCount());
            itemStack.shrink(consumed);
            this.level += (consumed * this.levelPerItem);
            return true;
        }
        return false;
    }

    public void decrementLevel() {
        if (this.isEmpty()) throw new IllegalStateException("Tried to decrement level when already empty");
        this.level--;
    }

    public void incrementLevel() {
        if (this.isFull()) throw new IllegalStateException("Tried to decrement level when already empty");
        this.level++;
    }

    public boolean isEmpty() {
        return this.level == 0;
    }

    public boolean isFull() {
        return this.level == this.maxCapacity;
    }

}
