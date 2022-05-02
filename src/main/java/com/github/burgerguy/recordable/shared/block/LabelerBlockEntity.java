package com.github.burgerguy.recordable.shared.block;

import com.github.burgerguy.recordable.shared.Recordable;
import com.github.burgerguy.recordable.shared.menu.LabelerConstants;
import com.github.burgerguy.recordable.shared.menu.LabelerMenu;
import com.github.burgerguy.recordable.shared.util.ImplementedContainer;
import com.github.burgerguy.recordable.shared.util.MenuUtil;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LabelerBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory {
    public static final BlockEntityType<LabelerBlockEntity> INSTANCE = FabricBlockEntityTypeBuilder.create(LabelerBlockEntity::new, LabelerBlock.INSTANCE).build(null);
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(Recordable.MOD_ID, "labeler");

    private final int[] colorLevels;
    // don't save to nbt, should reset on restart/unload. should be stored when sent to other players.
    private boolean inUse;

    public LabelerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(INSTANCE, blockPos, blockState);
        this.colorLevels = new int[LabelerConstants.COLOR_COUNT];
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        this.setInUse(true);
        return new LabelerMenu(
                containerId,
                playerInventory,
                this
        );
    }

    @Override
    public Component getDisplayName() {
        return this.getBlockState().getBlock().getName();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("ColorLevels", Tag.TAG_INT_ARRAY)) {
            System.arraycopy(tag.getIntArray("ColorLevels"), 0, this.colorLevels, 0, this.colorLevels.length);
        }
        if (tag.contains("InUse", Tag.TAG_BYTE)) {
            this.inUse = tag.getBoolean("InUse");
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putIntArray("ColorLevels", this.colorLevels);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = this.saveWithoutMetadata();
        tag.putBoolean("InUse", this.inUse);
        return tag;
    }

    // server to client
    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.getBlockPos());
    }

    // called from server only
    public void setInUse(boolean inUse) {
        this.inUse = inUse;
        // let other clients know of the change
        MenuUtil.updateBlockEntity(this);
    }

    public boolean isInUse() {
        return this.inUse;
    }

    public int[] getColorLevels() {
        return this.colorLevels;
    }

    public int[] getPixelIndexModel() {
        return LabelerConstants.RECORD_PIXEL_INDEX_MODEL;
    }

    public int getPixelModelWidth() {
        return LabelerConstants.RECORD_PIXEL_MODEL_WIDTH;
    }
}
