package com.github.burgerguy.recordable.shared.menu;

import com.github.burgerguy.recordable.shared.Recordable;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class LabelerMenu extends AbstractContainerMenu {
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(Recordable.MOD_ID, "labeler");
    public static final MenuType<LabelerMenu> INSTANCE = new MenuType<>(LabelerMenu::new);

    private final Container container;
    private final int[] colorLevels;

    private final int[] pixelIndexModel;
    private final int pixelModelWidth;

    public LabelerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(containerId, playerInventory, new SimpleContainer(LabelerConstants.CONTAINER_SIZE), buffer.readVarIntArray(), buffer.readVarIntArray(), buffer.readVarInt());
    }

    public LabelerMenu(int containerId, Inventory playerInventory, Container container, int[] colorLevels, int[] pixelIndexModel, int pixelModelWidth) {
        super(INSTANCE, containerId);
        this.colorLevels = colorLevels;
        this.pixelIndexModel = pixelIndexModel;
        this.pixelModelWidth = pixelModelWidth;
        checkContainerSize(container, LabelerConstants.CONTAINER_SIZE);
        this.container = container;
        container.startOpen(playerInventory.player);

        // add player inventory
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
            }
        }

        // add player hotbar
        for (int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(playerInventory, x, 8 + x * 18, 142));
        }
    }

    public int[] getColorLevels() {
        return this.colorLevels;
    }

    public void handleFinish(FriendlyByteBuf buffer) {
        ItemStack record = ItemStack.EMPTY; // TODO: get item in slot
        String artist = buffer.readUtf();
        String title = buffer.readUtf();
        PaintArray recreatedPaintArray = PaintArray.fromBuffer(this.pixelIndexModel, this.pixelModelWidth, this.colorLevels, buffer);

        CompoundTag itemTag = record.getOrCreateTag();
        recreatedPaintArray.applyToTagNoAlpha(itemTag);
        CompoundTag songInfoTag = new CompoundTag();


        PlayerLookup.tracking((BlockEntity) container);
    }

    public int[] getPixelIndexModel() {
        return pixelIndexModel;
    }

    public int getPixelModelWidth() {
        return pixelModelWidth;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }
}