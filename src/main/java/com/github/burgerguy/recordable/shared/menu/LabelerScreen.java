package com.github.burgerguy.recordable.shared.menu;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class LabelerScreen extends AbstractContainerScreen<LabelerMenu> {
    private static final ResourceLocation COLOR_ICON_LOCATION = new ResourceLocation("textures/gui/container/loom.png");

    public LabelerScreen(LabelerMenu labelerMenu, Inventory inventory, Component component) {
        super(labelerMenu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();

        PrinterColor[] printerColors = this.getMenu().printerColors;
        for (int i = 0; i < printerColors.length; i++) {
            int xIdx = i / LabelerConstants.PALETTE_COLUMNS_WRAP;
            int yIdx = i % LabelerConstants.PALETTE_COLUMNS_WRAP;
            PrinterColor printerColor = printerColors[i];
            printerColor.setBounds(
                LabelerConstants.PALETTE_X + xIdx * LabelerConstants.COLOR_WIDTH,
                LabelerConstants.PALETTE_Y + yIdx * LabelerConstants.COLOR_HEIGHT,
                LabelerConstants.COLOR_WIDTH,
                LabelerConstants.COLOR_HEIGHT
            );
            this.addRenderableWidget(printerColor);
        }
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTick, int mouseX, int mouseY) {
        // TODO: noop for now
    }

    // override access
    @Override
    public <T extends GuiEventListener & Widget & NarratableEntry> T addRenderableWidget(T widget) {
        return super.addRenderableWidget(widget);
    }
}
