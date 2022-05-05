package com.github.burgerguy.recordable.shared.menu;

import com.github.burgerguy.recordable.shared.Recordable;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

// We use the new fabric transaction api here, which is considered unstable.
// I honestly couldn't care less as long as quilt has support for it.
@SuppressWarnings("UnstableApiUsage")
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
                int consumedItemCount = Math.min(
                        (paint.getMaxCapacity() - paint.getLevel()) / itemLevel,
                        itemStack.getCount()
                );

                if (consumedItemCount != 0) {
                    paint.changeLevel(consumedItemCount * itemLevel);

                    ItemHistoryElement lastHistory = itemHistory.peekLast();
                    if (lastHistory != null && lastHistory.itemStack.sameItem(itemStack)) {
                        // merge with existing top item in deque
                        itemStack.shrink(consumedItemCount);
                        lastHistory.itemStack.grow(consumedItemCount);
                    } else {
                        itemHistory.addLast(new ItemHistoryElement(itemStack.split(consumedItemCount), itemLevel));
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
    public boolean returnExcess(Paint paint, PlayerInventoryStorage playerInventoryStorage, TransactionContext transactionContext) {
        if (paint.getLevel() > paint.getMaxCapacity()) {
            Deque<ItemHistoryElement> itemHistory = this.rawColorToItemHistory.get(paint.getColor().getRawColor());
            if (itemHistory == null) return false;

            boolean changed = false;

            // this is a do while because we want to obtain the item history on only the first iteration
            do {
                ItemHistoryElement lastHistoryElement = itemHistory.peekLast();
                if (lastHistoryElement == null) return changed;
                ItemStack itemStack = lastHistoryElement.itemStack;
                int itemLevel = lastHistoryElement.itemLevel;

                int maxReturnedItemCount = Mth.positiveCeilDiv(
                        paint.getLevel() - paint.getMaxCapacity(),
                        itemLevel
                );

                if (maxReturnedItemCount > 0) {
                    int returnedItemCount = Math.min(
                            Mth.positiveCeilDiv(
                                    paint.getLevel() - paint.getMaxCapacity(),
                                    itemLevel
                            ),
                            itemStack.getCount()
                    );

                    if (returnedItemCount != 0) {
                        // it needs to have no overflow checks because we can be out of bounds still if we need to go through multiple item stacks.
                        // also, we have our own checks in this method
                        paint.changeLevelNoOverflow(-returnedItemCount * itemLevel);

                        playerInventoryStorage.offerOrDrop(ItemVariant.of(itemStack.getItem()), returnedItemCount, transactionContext);
                        itemStack.shrink(returnedItemCount);

                        if (itemStack.isEmpty()) {
                            itemHistory.remove();
                        }

                        changed = true;
                    }
                }
            } while (paint.getLevel() > paint.getMaxCapacity());

            return changed;
        }

        return false;
    }

    // (c) -> s
    public void sendCanvasLevelChange(Paint paint, int amount, PlayerInventoryStorage playerInventoryStorage, TransactionContext transactionContext) {
        if (paint.tryChangeLevelCanvas(amount)) {
            this.returnExcess(paint, playerInventoryStorage, transactionContext);
            FriendlyByteBuf buffer = PacketByteBufs.create();
            buffer.resetWriterIndex();
            buffer.writeInt(paint.getColor().getRawColor());
            buffer.writeInt(amount);
            ClientPlayNetworking.send(Recordable.CANVAS_LEVEL_CHANGE_ID, buffer);
        } else {
            throw new IllegalStateException("Tried to change level out of bounds. level: " + paint.getLevel() + ", change: " + amount);
        }
    }

    public void sendCanvasLevelChange(Paint paint, int amount, Inventory playerInventory) {
        try (Transaction transaction = Transaction.openOuter()) {
            this.sendCanvasLevelChange(
                    paint,
                    amount,
                    PlayerInventoryStorage.of(playerInventory),
                    transaction
            );
        }
    }

    // c -> (s)
    public boolean tryReceiveCanvasLevelChange(Paint paint, int amount, PlayerInventoryStorage playerInventoryStorage, TransactionContext transactionContext) {
        boolean isChangeValid = paint.tryChangeLevelCanvas(amount);

        if (isChangeValid) {
            this.returnExcess(paint, playerInventoryStorage, transactionContext);
        }

        return isChangeValid;
    }

    public boolean onMenuExit(Inventory playerInventory) {
        boolean changed = false;
        PlayerInventoryStorage playerInventoryStorage = PlayerInventoryStorage.of(playerInventory);
        try (Transaction transaction = Transaction.openOuter()) {
            for (Paint paint : this.getPaints()) {
                paint.removeCanvasLevelChange();
                changed |= this.returnExcess(paint, playerInventoryStorage, transaction);
            }
        }

        for (Deque<ItemHistoryElement> itemHistory : this.rawColorToItemHistory.values()) {
            itemHistory.clear();
        }

        return changed;
    }

    public record ItemHistoryElement(ItemStack itemStack, int itemLevel) {}

}
