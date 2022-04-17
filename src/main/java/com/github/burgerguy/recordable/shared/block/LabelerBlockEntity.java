package com.github.burgerguy.recordable.shared.block;

import com.github.burgerguy.recordable.shared.Recordable;
import com.github.burgerguy.recordable.shared.menu.LabelerConstants;
import com.github.burgerguy.recordable.shared.menu.LabelerMenu;
import com.github.burgerguy.recordable.shared.util.ImplementedContainer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
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

public class LabelerBlockEntity extends BlockEntity implements ImplementedContainer, ExtendedScreenHandlerFactory {
    public static final BlockEntityType<LabelerBlockEntity> INSTANCE = FabricBlockEntityTypeBuilder.create(LabelerBlockEntity::new, LabelerBlock.INSTANCE).build(null);
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(Recordable.MOD_ID, "labeler");

    private final NonNullList<ItemStack> items;
    private final int[] colorLevels;

    public LabelerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(INSTANCE, blockPos, blockState);
        this.items = NonNullList.withSize(LabelerConstants.CONTAINER_SIZE, ItemStack.EMPTY);
        this.colorLevels = new int[LabelerConstants.COLOR_COUNT];
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new LabelerMenu(containerId, playerInventory, this, this.colorLevels);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, this.items);
        if (tag.contains("ColorLevels", Tag.TAG_INT_ARRAY)) {
            System.arraycopy(tag.getIntArray("ColorLevels"), 0, this.colorLevels, 0, this.colorLevels.length);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, this.items);
        tag.putIntArray("ColorLevels", this.colorLevels);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeVarIntArray(this.colorLevels);
    }
}
