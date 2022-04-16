package com.github.burgerguy.recordable.shared.block;

import com.github.burgerguy.recordable.shared.Recordable;
import com.github.burgerguy.recordable.shared.util.ImplementedContainer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class LabelPrinterBlockEntity extends BaseContainerBlockEntity implements ImplementedContainer {
    public static final BlockEntityType<LabelPrinterBlockEntity> INSTANCE = FabricBlockEntityTypeBuilder.create(LabelPrinterBlockEntity::new, LabelPrinterBlock.INSTANCE).build(null);
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(Recordable.MOD_ID, "label_printer");

    private final NonNullList<ItemStack> items;

    public LabelPrinterBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(INSTANCE, blockPos, blockState);
        this.items = NonNullList.withSize(3, ItemStack.EMPTY);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);

        if (tag.contains("record")) {
            recordItem = ItemStack.of(tag.getCompound("record"));
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        // Save the current value of the number to the tag
        ContainerHelper.saveAllItems(tag, items);
        super.saveAdditional(tag);
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("menu.recordable.label_printer");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return null;
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return items;
    }

}
