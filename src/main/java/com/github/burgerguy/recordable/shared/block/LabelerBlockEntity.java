package com.github.burgerguy.recordable.shared.block;

import com.github.burgerguy.recordable.shared.Recordable;
import com.github.burgerguy.recordable.shared.menu.LabelerConstants;
import com.github.burgerguy.recordable.shared.menu.LabelerMenu;
import com.github.burgerguy.recordable.shared.menu.Paint;
import com.github.burgerguy.recordable.shared.menu.PaintPalette;
import it.unimi.dsi.fastutil.ints.*;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LabelerBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory {
    public static final BlockEntityType<LabelerBlockEntity> INSTANCE = FabricBlockEntityTypeBuilder.create(LabelerBlockEntity::new, LabelerBlock.INSTANCE).build(null);
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(Recordable.MOD_ID, "labeler");

    private final Int2ObjectSortedMap<Paint> rawColorToPaintMap;

    public LabelerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(INSTANCE, blockPos, blockState);
        this.rawColorToPaintMap = new Int2ObjectLinkedOpenHashMap<>(Recordable.getColorPalette().getColorCount());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
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
            int[] colorLevels = tag.getIntArray("ColorLevels");
            Int2IntMap rawColorToLevelMap = new Int2IntOpenHashMap(colorLevels.length / 2);
            for (int i = 0; i < colorLevels.length; i += 2) {
                int rawColor = colorLevels[i];
                int level = colorLevels[i + 1];
                rawColorToLevelMap.put(rawColor, level);
            }
            Recordable.getColorPalette().updatePaints(
                    rawColorToLevelMap,
                    LabelerConstants.PAINT_MAX_CAPACITY,
                    this.rawColorToPaintMap
            );
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        int[] colorLevels = new int[this.rawColorToPaintMap.size() * 2];
        int idx = 0;
        for (Int2ObjectMap.Entry<Paint> entry : this.rawColorToPaintMap.int2ObjectEntrySet()) {
            colorLevels[idx++] = entry.getIntKey();
            colorLevels[idx++] = entry.getValue().getLevel();
        }
        tag.putIntArray("ColorLevels", colorLevels);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    // server to client
    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.getBlockPos());
    }

    public PaintPalette createPaintPalette(Inventory playerInventory, Slot dyeSlot) {
        return new PaintPalette(
                this.rawColorToPaintMap,
                new Int2ObjectOpenHashMap<>(this.rawColorToPaintMap.size()),
                playerInventory,
                dyeSlot
        );
    }

    public int[] getPixelIndexModel() {
        return LabelerConstants.RECORD_PIXEL_INDEX_MODEL;
    }

    public int getPixelModelWidth() {
        return LabelerConstants.RECORD_PIXEL_MODEL_WIDTH;
    }
}
