package com.github.burgerguy.recordable.shared.menu;

import com.github.burgerguy.recordable.shared.Recordable;
import com.github.burgerguy.recordable.shared.block.LabelerBlockEntity;
import java.util.Objects;
import net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class LabelerMenu extends AbstractContainerMenu {
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(Recordable.MOD_ID, "labeler");
    public static final MenuType<LabelerMenu> INSTANCE = new ExtendedScreenHandlerType<>(LabelerMenu::new);

    private final LabelerBlockEntity labelerBlockEntity;

    public LabelerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(
                containerId,
                playerInventory,
                (LabelerBlockEntity) Objects.requireNonNull(playerInventory.player.getLevel().getBlockEntity(buffer.readBlockPos())) // any means necessary
        );
    }

    public LabelerMenu(int containerId, Inventory playerInventory, LabelerBlockEntity labelerBlockEntity) {
        super(INSTANCE, containerId);
        // get from BE
        this.labelerBlockEntity = labelerBlockEntity;

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

        // add dye slot

    }

    // only called on server
    public void handleFinish(FriendlyByteBuf buffer) {
        LabelerBlockEntity labeler = this.labelerBlockEntity;

        ItemStack record = ItemStack.EMPTY; // TODO: get item in slot
        String author = buffer.readUtf();
        String title = buffer.readUtf();
        // this will modify the colors of the labeler BE, so we need to sync with the clients
        Painter recreatedPainter = Painter.fromBuffer(
                labeler.getPixelIndexModel(),
                labeler.getPixelModelWidth(),
                labeler.getColorLevels(),
                buffer
        );

        CompoundTag itemTag = record.getOrCreateTag();
        recreatedPainter.applyToTagNoAlpha(itemTag);
        CompoundTag songInfoTag = new CompoundTag();
        songInfoTag.putString("Author", author);
        songInfoTag.putString("Title", title);
        itemTag.put("SongInfo", songInfoTag);

        // update color levels
        this.updateBlockEntity();
        // update record slot
//        this.slotsChanged(this.container);
        this.broadcastChanges();
    }

    // only called on server
    private void updateBlockEntity() {
        LabelerBlockEntity labeler = this.labelerBlockEntity;
        labeler.setChanged();
        BlockState blockState = labeler.getBlockState();
        labeler.getLevel().sendBlockUpdated(labeler.getBlockPos(), blockState, blockState, 2); // or the flag with 1 to cause a block update
    }

    public LabelerBlockEntity getLabelerBlockEntity() {
        return labelerBlockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        BlockPos labelerPos = this.labelerBlockEntity.getBlockPos();
        if (player.level.getBlockEntity(labelerPos) != this.labelerBlockEntity) {
            return false;
        }
        return player.distanceToSqr((double)labelerPos.getX() + 0.5, (double)labelerPos.getY() + 0.5, (double)labelerPos.getZ() + 0.5) <= 64.0;
    }

}
