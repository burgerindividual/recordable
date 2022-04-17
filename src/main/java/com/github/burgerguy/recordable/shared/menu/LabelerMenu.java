package com.github.burgerguy.recordable.shared.menu;

import com.github.burgerguy.recordable.shared.Recordable;
import net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;

public class LabelerMenu extends AbstractContainerMenu {
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(Recordable.MOD_ID, "labeler");
    public static final MenuType<LabelerMenu> INSTANCE = new ExtendedScreenHandlerType<>(LabelerMenu::new);

    private final Container container;
    protected final PrinterColor[] printerColors;

    public LabelerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, new SimpleContainer(LabelerConstants.CONTAINER_SIZE), buf.readVarIntArray(LabelerConstants.COLOR_COUNT));
    }

    public LabelerMenu(int containerId, Inventory playerInventory, Container container, int[] colorLevels) {
        super(INSTANCE, containerId);

        DyeColor[] dyeColors = DyeColor.values();
        this.printerColors = new PrinterColor[dyeColors.length];
        for (int i = 0; i < dyeColors.length; i++) {
            PrinterColor printerColor = new PrinterColor(dyeColors[i], colorLevels[i]);
            this.addDataSlot(printerColor.getLevelSlot());
            this.printerColors[i] = printerColor;
        }

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
