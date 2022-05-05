package com.github.burgerguy.recordable.shared.menu;

import it.unimi.dsi.fastutil.ints.*;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class PaintPalette {
    private final Int2ObjectSortedMap<Paint> rawColorToPaintMap;
    private final Int2ObjectMap<Deque<ItemHistoryElement>> rawColorToItemHistory;

    public PaintPalette(Int2ObjectSortedMap<Paint> rawColorToPaintMap, Int2ObjectMap<Deque<ItemHistoryElement>> rawColorToItemHistory) {
        this.rawColorToPaintMap = rawColorToPaintMap;
        this.rawColorToItemHistory = rawColorToItemHistory;
    }

    public Paint getPaint(int rawColor) {
        return this.rawColorToPaintMap.get(rawColor);
    }

    public Collection<Paint> getPaints() {
        return this.rawColorToPaintMap.values();
    }

    public Int2IntOpenHashMap createLevelsSnapshot() {
        Int2IntOpenHashMap rawColorToLevelMap = new Int2IntOpenHashMap();
        for (Paint paint : this.getPaints()) {
            rawColorToLevelMap.put(paint.getColor().getRawColor(), paint.getLevel());
        }
        return rawColorToLevelMap;
    }

    public boolean compareWithSnapshot(Int2IntMap rawColorToLevelMap) {
        if (rawColorToLevelMap.size() != this.rawColorToPaintMap.size()) return false;
        for (Int2IntMap.Entry entry : rawColorToLevelMap.int2IntEntrySet()) {
            Paint paint = this.rawColorToPaintMap.get(entry.getIntKey());
            if (paint == null) return false;
            if (paint.getLevel() != entry.getIntValue()) return false;
        }
        return true;
    }

    /**
     * @return if the item stack was altered
     */
    public boolean acceptItemStack(ItemStack itemStack) {
        if (itemStack.isEmpty()) return false;

        boolean stackChanged = false;

        for (Paint paint : this.getPaints()) {
            Deque<ItemHistoryElement> itemHistory = this.rawColorToItemHistory.computeIfAbsent(paint.getColor().getRawColor(), ignored -> new ArrayDeque<>(8));

            // make sure item applies to this color
            int itemLevel = paint.getColor().getItemLevelOrInvalid(itemStack.getItem());
            if (itemLevel != PaintColor.ITEM_INVALID) {
                // integer division truncates, which is what we want
                int consumedItemCount = Math.min((paint.getMaxCapacity() - paint.getLevel()) / itemLevel, itemStack.getCount());

                if (consumedItemCount != 0) {
                    int levelAmount = consumedItemCount * itemLevel;
                    paint.changeLevel(levelAmount);

                    ItemHistoryElement lastHistory = itemHistory.peekLast();
                    if (lastHistory != null && lastHistory.itemStack.sameItem(itemStack)) {
                        // merge with existing top item in deque
                        itemStack.shrink(consumedItemCount);
                        lastHistory.itemStack.grow(consumedItemCount);
                    } else {
                        itemHistory.addLast(new ItemHistoryElement(itemStack.split(consumedItemCount), levelAmount));
                    }

                    stackChanged = true;
                }
            }

            if (itemStack.isEmpty()) {
                // all item count used
                break;
            }
        }

        return stackChanged;
    }

    /**
     * Called when labeler menu closes on both client and server
     */
    public void returnExcess(Inventory playerInventory) {
        for (Paint paint : this.getPaints()) {
            while (paint.getLevel())
            Deque<ItemHistoryElement> itemHistory = this.rawColorToItemHistory.get(paint.getColor().getRawColor());

            // integer division truncates, which is what we want
            int consumedItemCount = Math.min((paint.getMaxCapacity() - paint.getLevel()) / itemLevel, itemStack.getCount());

            if (consumedItemCount != 0) {
                paint.changeLevel(consumedItemCount * itemLevel);

                ItemStack lastItemStack = itemHistory.peekLast();
                if (lastItemStack != null && lastItemStack.sameItem(itemStack)) {
                    // merge with existing top item in deque
                    itemStack.shrink(consumedItemCount);
                    lastItemStack.grow(consumedItemCount);
                }
            }

            if (itemStack.isEmpty()) {
                // all item count used
                break;
            }
        }
    }

    public void clearAllItemHistory() {
        for (Deque<ItemHistoryElement> itemHistory : this.rawColorToItemHistory.values()) {
            itemHistory.clear();
        }
    }

    public void clearAllCanvasLevelChanges() {
        for (Paint paint : this.getPaints()) {
            paint.resetCanvasLevelChange();
        }
    }

    public record ItemHistoryElement(ItemStack itemStack, int levelAmount) {}

}
