package com.github.burgerguy.recordable.shared.menu;

import com.github.burgerguy.recordable.shared.util.ColorUtil;

public class Paint {

    private final PaintColor color;
    private int maxCapacity;
    private int level;
    /**
     * The net change in level caused by canvas edits since the menu was opened, or since the last finalize.
     */
    private int canvasLevelChange;
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
     * @return false if the level change would cause an overflow or underflow
     */
    public boolean tryChangeLevel(int amount) {
        int newLevel = this.level + amount;

        if (newLevel > this.maxCapacity || newLevel < 0) {
            return false;
        } else {
            this.level = newLevel;
            return true;
        }
    }

    public void changeLevel(int amount) {
        if (!this.tryChangeLevel(amount)) {
            throw new IllegalStateException("Tried to change level out of bounds. level: " + this.level + ", change: " + amount);
        }
    }

    public boolean tryChangeLevelNoOverflow(int amount) {
        int newLevel = this.level + amount;

        if (newLevel < 0) {
            return false;
        } else {
            this.level = newLevel;
            return true;
        }
    }

    public void changeLevelNoOverflow(int amount) {
        if (!this.tryChangeLevelNoOverflow(amount)) {
            throw new IllegalStateException("Tried to change level out of bounds. level: " + this.level + ", change: " + amount);
        }
    }

    /**
     * @return false if the level change would cause an underflow
     */
    public boolean tryChangeLevelCanvas(int amount) {
        boolean isChangeValid = this.tryChangeLevelNoOverflow(amount);

        if (isChangeValid) {
            this.canvasLevelChange += amount;
        }

        return isChangeValid;
    }

    public void removeCanvasLevelChange() {
        this.level -= this.canvasLevelChange;
        this.canvasLevelChange = 0;
    }

    public boolean isEmpty() {
        return this.level == 0;
    }

    // only use for loading from NBT
    public void update(int level, int maxCapacity) {
        this.level = level;
        this.maxCapacity = maxCapacity;
    }

}
