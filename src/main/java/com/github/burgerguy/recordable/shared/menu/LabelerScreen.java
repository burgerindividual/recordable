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

    public LabelerScreen(LabelerMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
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
