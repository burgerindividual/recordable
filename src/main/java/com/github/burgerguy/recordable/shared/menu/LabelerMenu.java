package com.github.burgerguy.recordable.shared.menu;

import com.github.burgerguy.recordable.shared.Recordable;
import com.github.burgerguy.recordable.shared.block.LabelerBlockEntity;
import com.github.burgerguy.recordable.shared.item.CopperRecordItem;
import com.github.burgerguy.recordable.shared.util.MenuUtil;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;

public class LabelerMenu extends AbstractContainerMenu {
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(Recordable.MOD_ID, "labeler");
    public static final MenuType<LabelerMenu> INSTANCE = new ExtendedScreenHandlerType<>(LabelerMenu::new);

    public static final int DYE_SLOT_ID = 0;
    public static final int PAPER_SLOT_ID = 1;
    public static final int RECORD_SLOT_ID = 2;

    private static final int INVENTORY_X = 8;
    private static final int INVENTORY_Y = 100;
    private static final int HOTBAR_X = 8;
    private static final int HOTBAR_Y = 158;

    private final LabelerBlockEntity labelerBlockEntity;
    private final Set<Item> allowedDyeItems;

    private final Container container;
    private final Slot dyeSlot;
    private final Slot recordSlot;
    private final Slot paperSlot;

    public LabelerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(
                containerId,
                playerInventory,
                (LabelerBlockEntity) Objects.requireNonNull(playerInventory.player.getLevel().getBlockEntity(buffer.readBlockPos())) // any means necessary
        );
    }

    public LabelerMenu(int containerId, Inventory playerInventory, LabelerBlockEntity labelerBlockEntity) {
        super(INSTANCE, containerId);
        this.allowedDyeItems = Recordable.getColorPalette().getAllAcceptedItems();
        this.labelerBlockEntity = labelerBlockEntity;
        this.container = new SimpleContainer(3) {
            @Override
            public void setChanged() {
                // handle dyes
                ItemStack dyeItem = this.getItem(DYE_SLOT_ID);

                if (!dyeItem.isEmpty()) {
                    boolean changed = false;
                    for (Paint paint : labelerBlockEntity.getRawColorToPaintMap().values()) {
                        changed |= paint.addLevelFromItem(dyeItem);

                        if (dyeItem.isEmpty()) {
                            // all item count used
                            break;
                        }
                    }

                    if (changed) {
                        // broadcast changes to clients
                        MenuUtil.updateBlockEntity(labelerBlockEntity);
                        // broadcast changes to client screens
                        LabelerMenu.this.slotsChanged(this);
                    }
                }
                super.setChanged();
            }
        };

        // add dye slot
        this.dyeSlot = this.addSlot(new Slot(this.container, DYE_SLOT_ID, 65, 68) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return LabelerMenu.this.allowedDyeItems.contains(stack.getItem());
            }
        });

        // add paper slot
        this.paperSlot = this.addSlot(new Slot(this.container, PAPER_SLOT_ID, 152, 24) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.PAPER);
            }
        });

        // add record slot
        this.recordSlot = this.addSlot(new Slot(this.container, RECORD_SLOT_ID, 110, 46) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(CopperRecordItem.INSTANCE) && (!stack.hasTag() || !stack.getTag().contains("Colors", Tag.TAG_BYTE_ARRAY));
            }
        });

        int i;
        // add player inventory
        for (i = 0; i < 3; ++i) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInventory, k + i * 9 + 9, INVENTORY_X + k * 18, INVENTORY_Y + i * 18));
            }
        }
        // add player hotbar
        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, HOTBAR_X + i * 18, HOTBAR_Y));
        }
    }

    // only called on server
    public void handleFinish(FriendlyByteBuf buffer) {
        LabelerBlockEntity labeler = this.labelerBlockEntity;

        ItemStack paper = this.paperSlot.getItem();
        ItemStack record = this.recordSlot.getItem();
        if (record.isEmpty() || paper.isEmpty()) {
            Recordable.LOGGER.warn("Tried to finish without record/paper");
        }
        paper.shrink(1);

        String author = buffer.readUtf();
        String title = buffer.readUtf();
        // this will modify the colors of the labeler BE, so we need to sync with the clients
        Canvas recreatedCanvas = Canvas.fromBuffer(
                labeler.getPixelIndexModel(),
                labeler.getPixelModelWidth(),
                buffer
        );

        CompoundTag itemTag = record.getOrCreateTag();
        recreatedCanvas.applyToTagNoAlpha(itemTag);
        CompoundTag songInfoTag = new CompoundTag();
        songInfoTag.putString("Author", author);
        songInfoTag.putString("Title", title);
        itemTag.put("SongInfo", songInfoTag);

        // update color levels
        MenuUtil.updateBlockEntity(labeler);
        // update record slot
        this.slotsChanged(this.container);
    }

    public LabelerBlockEntity getLabelerBlockEntity() {
        return this.labelerBlockEntity;
    }

    public Slot getDyeSlot() {
        return this.dyeSlot;
    }

    public Slot getRecordSlot() {
        return this.recordSlot;
    }

    public Slot getPaperSlot() {
        return this.paperSlot;
    }

    @Override
    public boolean stillValid(Player player) {
        BlockPos labelerPos = this.labelerBlockEntity.getBlockPos();
        if (player.level.getBlockEntity(labelerPos) != this.labelerBlockEntity) {
            return false;
        }
        return player.distanceToSqr((double)labelerPos.getX() + 0.5, (double)labelerPos.getY() + 0.5, (double)labelerPos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.clearContainer(player, this.container);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (index != this.dyeSlot.index && index != this.recordSlot.index && index != this.paperSlot.index) {
                if (this.dyeSlot.mayPlace(itemStack2)) {
                    if (!this.moveItemStackTo(itemStack2, this.dyeSlot.index, this.dyeSlot.index + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (this.recordSlot.mayPlace(itemStack2)) {
                    if (!this.moveItemStackTo(itemStack2, this.recordSlot.index, this.recordSlot.index + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (this.paperSlot.mayPlace(itemStack2)) {
                    if (!this.moveItemStackTo(itemStack2, this.paperSlot.index, this.paperSlot.index + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 3 && index < 30) {
                    if (!this.moveItemStackTo(itemStack2, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 30 && index < 39 && !this.moveItemStackTo(itemStack2, 4, 30, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemStack2, 4, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemStack2);
        }

        return itemStack;
    }
}
