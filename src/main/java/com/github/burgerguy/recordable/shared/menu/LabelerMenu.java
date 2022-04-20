package com.github.burgerguy.recordable.shared.menu;

import com.github.burgerguy.recordable.shared.Recordable;
import com.github.burgerguy.recordable.shared.block.LabelerBlockEntity;
import java.util.Objects;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class LabelerMenu extends AbstractContainerMenu {
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(Recordable.MOD_ID, "labeler");
    public static final MenuType<LabelerMenu> INSTANCE = new ExtendedScreenHandlerType<>(LabelerMenu::new);

    private final Container container;
    private final LabelerBlockEntity labelerBlockEntity;

    private final int[] pixelIndexModel;
    private final int pixelModelWidth;

    public LabelerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        // TODO: does this work with multiple dimensions? i'd hope that the user is accessing the block from the same dimension they're in, but what if not?
        this(
                containerId,
                playerInventory,
                new SimpleContainer(LabelerConstants.CONTAINER_SIZE),
                (LabelerBlockEntity) Objects.requireNonNull(playerInventory.player.getLevel().getBlockEntity(buffer.readBlockPos()))
        );
    }

    public LabelerMenu(int containerId, Inventory playerInventory, Container container, LabelerBlockEntity labelerBlockEntity) {
        super(INSTANCE, containerId);
        // get from BE
        this.labelerBlockEntity = labelerBlockEntity;
        this.pixelIndexModel = labelerBlockEntity.getPixelIndexModel();
        this.pixelModelWidth = labelerBlockEntity.getPixelModelWidth();
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

    public void handleFinish(FriendlyByteBuf buffer) {
        ItemStack record = ItemStack.EMPTY; // TODO: get item in slot
        String author = buffer.readUtf();
        String title = buffer.readUtf();
        PaintArray recreatedPaintArray = PaintArray.fromBuffer(this.pixelIndexModel, this.pixelModelWidth, this.labelerBlockEntity.getColorLevels(), buffer);

        CompoundTag itemTag = record.getOrCreateTag();
        recreatedPaintArray.applyToTagNoAlpha(itemTag);
        CompoundTag songInfoTag = new CompoundTag();
        songInfoTag.putString("Author", author);
        songInfoTag.putString("Title", title);
        itemTag.put("SongInfo", songInfoTag);

        // this is kinda slow for some reason, but eventually should get to other clients
        this.labelerBlockEntity.setChanged();

        // or should i do this?
        for (ServerPlayer player : PlayerLookup.tracking(this.labelerBlockEntity)) {
            // i know for sure this will never be null due to our implementation of the method
            //noinspection ConstantConditions
            player.connection.send(this.labelerBlockEntity.getUpdatePacket());
        }

        // or both?
    }

    public int[] getPixelIndexModel() {
        return pixelIndexModel;
    }

    public int getPixelModelWidth() {
        return pixelModelWidth;
    }

    public LabelerBlockEntity getLabelerBlockEntity() {
        return labelerBlockEntity;
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
