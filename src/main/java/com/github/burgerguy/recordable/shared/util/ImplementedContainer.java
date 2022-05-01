package com.github.burgerguy.recordable.shared.util;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A simple {@code Container} implementation with only default methods + an item list getter.
 *
 * Originally by Juuz
 *
 * Refactored to use Mojang mappings
 */
public interface ImplementedContainer extends Container {

    /**
     * Retrieves the item list of this container.
     * Must return the same instance every time it's called.
     */
    NonNullList<ItemStack> getItems();

    /**
     * Creates a container from the item list.
     */
    static ImplementedContainer of(NonNullList<ItemStack> items) {
        return () -> items;
    }

    /**
     * Creates a new inventory with the specified size.
     */
    static ImplementedContainer ofSize(int size) {
        return of(NonNullList.withSize(size, ItemStack.EMPTY));
    }

    /**
     * Returns the inventory size.
     */
    @Override
    default int getContainerSize() {
        return this.getItems().size();
    }

    /**
     * Checks if the inventory is empty.
     * @return true if this inventory has only empty stacks, false otherwise.
     */
    @Override
    default boolean isEmpty() {
        for (int i = 0; i < this.getContainerSize(); i++) {
            ItemStack stack = this.getItem(i);
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Retrieves the item in the given index.
     */
    @Override
    default ItemStack getItem(int index) {
        return this.getItems().get(index);
    }

    /**
     * Removes items from an inventory slot.
     * @param index The index to remove from.
     * @param count How many items to remove. If there are less items in the slot than what are requested,
     *              takes all items in that slot.
     */
    @Override
    default ItemStack removeItem(int index, int count) {
        ItemStack result = ContainerHelper.removeItem(this.getItems(), index, count);
        if (!result.isEmpty()) {
            this.setChanged();
        }
        return result;
    }

    /**
     * Removes all items from an inventory slot.
     * @param index The slot to remove from.
     */
    @Override
    default ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(this.getItems(), index);
    }

    /**
     * Replaces the current stack in a container index with the provided stack.
     * @param index  The inventory index of which to replace the itemstack.
     * @param stack The replacing itemstack. If the stack is too big for
     *              this inventory ({@link Container#getMaxStackSize()},
     *              it gets resized to this inventory's maximum amount.
     */
    @Override
    default void setItem(int index, ItemStack stack) {
        this.getItems().set(index, stack);
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
    }

    /**
     * Clears the inventory.
     */
    @Override
    default void clearContent() {
        this.getItems().clear();
    }

    /**
     * Marks the state as dirty.
     * Must be called after changes in the container, so that the game can properly save
     * the container contents and notify neighboring blocks of container changes.
     */
    @Override
    default void setChanged() {
        // Override if you want behavior.
    }

    /**
     * @return true if the player can use the inventory, false otherwise.
     */
    @Override
    default boolean stillValid(Player player) {
        return true;
    }
}