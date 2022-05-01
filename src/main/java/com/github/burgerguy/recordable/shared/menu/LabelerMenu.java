package com.github.burgerguy.recordable.shared.menu;

import com.github.burgerguy.recordable.shared.Recordable;
import com.github.burgerguy.recordable.shared.block.LabelerBlockEntity;
import com.github.burgerguy.recordable.shared.item.CopperRecordItem;
import com.github.burgerguy.recordable.shared.util.MenuUtil;
import java.util.Objects;
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
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
    private final Paint[] paints;

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

        // create paints from constants
        this.paints = new Paint[LabelerConstants.COLOR_COUNT];
        for (int i = 0; i < this.paints.length; i++) {
            this.paints[i] = new Paint(
                    LabelerConstants.DEFINED_COLORS[i],
                    labelerBlockEntity.getColorLevels()[i],
                    LabelerConstants.COLOR_MAX_CAPACITY,
                    LabelerConstants.COLOR_LEVEL_PER_ITEM
            );
        }
        this.labelerBlockEntity = labelerBlockEntity;
        this.container = new SimpleContainer(3) {
            @Override
            public void setChanged() {
                // handle dyes
                ItemStack dyeItem = this.getItem(DYE_SLOT_ID);

                if (!dyeItem.isEmpty()) {
                    for (int i = 0; i < LabelerMenu.this.paints.length; i++) {
                        Paint paint = LabelerMenu.this.paints[i];
                        paint.addLevelFromItem(dyeItem);
                        LabelerMenu.this.getLabelerBlockEntity().getColorLevels()[i] = paint.getLevel();
                        // all item count used
                        if (dyeItem.isEmpty()) break;
                    }
                }
                super.setChanged();
                // broadcast changes to client
                LabelerMenu.this.slotsChanged(this);
            }
        };

        // add dye slot
        this.dyeSlot = this.addSlot(new Slot(this.container, DYE_SLOT_ID, 65, 68) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof DyeItem;
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
                labeler.getColorLevels(),
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

    public Paint[] getPaints() {
        return this.paints;
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
    public ItemStack quickMoveStack(Player player, int index) {
        System.out.println(index);
        return ItemStack.EMPTY;
    }
}
