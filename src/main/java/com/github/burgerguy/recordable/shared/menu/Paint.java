package com.github.burgerguy.recordable.shared.menu;

import com.github.burgerguy.recordable.shared.util.ColorUtil;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.world.item.ItemStack;

public class Paint {

    private final PaintColor color;
    private final int maxCapacity;

    private int level;
    private boolean canApply;

    public Paint(PaintColor color, int initialLevel, int maxCapacity) {
        this.color = color;
        this.maxCapacity = maxCapacity;
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
                return ColorUtil.mixColors(this.color.getRawColor(), otherColor);
            } else {
                return this.color.getRawColor();
            }
        }
        return otherColor;
    }

    /**
     * This accepts whatever is in the dye slot to check if it should add to this, and if so, adds to this and removes
     * part of the dye.
     * @return if the itemStack was altered
     */
    public boolean addLevelFromItem(ItemStack itemStack, Deque<ItemStack> paintItemHistory) {
        int level = this.color.getItemLevelOrInvalid(itemStack.getItem());
        if (level != PaintColor.ITEM_INVALID) {
            // integer division truncates, which is what we want
            int consumed = Math.min((this.maxCapacity - this.level) / level, itemStack.getCount());
            this.level += (consumed * level);

            ItemStack lastItemStack = paintItemHistory.peekLast();
            if (lastItemStack != null && lastItemStack.sameItem(itemStack)) {
                // merge with existing top item in deque
                itemStack.shrink(consumed);
                lastItemStack.grow(consumed);
            } else {
                paintItemHistory.addLast(itemStack.split(consumed));
            }
            return true;
        }
        return false;
    }

    public void decrementLevel() {
        if (this.isEmpty()) throw new IllegalStateException("Tried to decrement level when already empty");
        this.level--;
    }

    public void incrementLevel() {
        if (this.isFull()) throw new IllegalStateException("Tried to increment level when already full");
        this.level++;
    }

    public boolean isEmpty() {
        return this.level == 0;
    }

    public boolean isFull() {
        return this.level == this.maxCapacity;
    }

}
