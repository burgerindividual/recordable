package com.github.burgerguy.recordable.client.screen;

import com.github.burgerguy.recordable.client.render.util.ScreenRenderUtil;
import com.github.burgerguy.recordable.shared.Recordable;
import com.github.burgerguy.recordable.shared.menu.LabelerConstants;
import com.github.burgerguy.recordable.shared.menu.LabelerMenu;
import com.mojang.blaze3d.vertex.PoseStack;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.system.MemoryStack;

public class LabelerScreen extends AbstractContainerScreen<LabelerMenu> {
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(Recordable.MOD_ID, "labeler");
    private static final ResourceLocation COLOR_ICON_LOCATION = new ResourceLocation("textures/gui/container/loom.png");

    private final PaintColorWidget[] paintColorWidgets;
    private final ClientPaintArray clientPaintArray;
    private final PaintWidget paintWidget;

    public LabelerScreen(LabelerMenu labelerMenu, Inventory inventory, Component component) {
        super(labelerMenu, inventory, component);

        this.paintColorWidgets = new PaintColorWidget[LabelerConstants.COLOR_COUNT];
        for (int i = 0; i < LabelerConstants.COLOR_COUNT; i++) {
            this.paintColorWidgets[i] = new PaintColorWidget(LabelerConstants.DEFINED_COLORS[i], labelerMenu.getLabelerBlockEntity().getColorLevels()[i]);
        }

        this.clientPaintArray = new ClientPaintArray(labelerMenu.getPixelIndexModel(), labelerMenu.getPixelModelWidth());
        this.paintWidget = new PaintWidget();
    }

    @Override
    protected void init() {
        super.init();

        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        // gets ScreenRenderUtil ready for render
        this.addRenderableOnly((poseStack, mouseX, mouseY, partialTick) -> ScreenRenderUtil.startFills());
        for (int i = 0; i < this.paintColorWidgets.length; i++) {
            int xIdx = i / LabelerConstants.PALETTE_COLUMNS_WRAP;
            int yIdx = i % LabelerConstants.PALETTE_COLUMNS_WRAP;
            PaintColorWidget paintColorWidget = this.paintColorWidgets[i];
            paintColorWidget.setBounds(
                this.leftPos + LabelerConstants.PALETTE_X + xIdx * (LabelerConstants.COLOR_WIDTH + LabelerConstants.COLOR_MARGIN_X),
                this.topPos + LabelerConstants.PALETTE_Y + yIdx * (LabelerConstants.COLOR_HEIGHT + LabelerConstants.COLOR_MARGIN_Y),
                LabelerConstants.COLOR_WIDTH,
                LabelerConstants.COLOR_HEIGHT
            );
            this.addRenderableWidget(paintColorWidget);
        }
        // renders everything that widgets drew using ScreenRenderUtil
        this.addRenderableOnly((poseStack, mouseX, mouseY, partialTick) -> ScreenRenderUtil.endAndRenderFills());
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTick, int mouseX, int mouseY) {
        // TODO: noop for now
    }

    public void sendFinish() {
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(memoryStack.malloc(Integer.BYTES)));
            buffer.resetWriterIndex();
            buffer.writeUtf(null); // artist
            buffer.writeUtf(null); // title
            this.clientPaintArray.writeToPacket(buffer);
            ClientPlayNetworking.send(Recordable.FINALIZE_LABEL_ID, buffer);
        }
    }

}
