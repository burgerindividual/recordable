package com.github.burgerguy.recordable.shared.menu;

import com.github.burgerguy.recordable.shared.util.ColorUtil;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

public class Paint {

    private final PaintColor color;
    private final int maxCapacity;

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
     * @return false if the level change is not possible
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

    public boolean tryChangeLevelCanvas(int amount) {
        boolean isLevelChangeValid = this.tryChangeLevel(amount);
        if (isLevelChangeValid) {
            this.canvasLevelChange += amount;
        }
        return isLevelChangeValid;
    }

    public void changeLevelCanvas(int amount) {
        if (!this.tryChangeLevelCanvas(amount)) {
            throw new IllegalStateException("Tried to change level out of bounds. level: " + this.level + ", change: " + amount);
        }
    }

    public void resetCanvasLevelChange() {
        this.canvasLevelChange = 0;
    }

    public int getCanvasLevelChange() {
        return this.canvasLevelChange;
    }

    public boolean isEmpty() {
        return this.level == 0;
    }

}
