package com.github.burgerguy.recordable.shared.menu;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public class LabelPrinterScreen extends AbstractContainerScreen<LabelPrinterMenu> {
    private static final ResourceLocation COLOR_ICON_LOCATION = new ResourceLocation("textures/gui/container/loom.png");

    public LabelPrinterScreen(LabelPrinterMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);

        //The player inventory
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this..addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
            }
        }
        //The player Hotbar
        for (m = 0; m < 9; ++m) {
            this.addSlot(new Slot(inventory, m, 8 + m * 18, 142));
        }
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        // TODO: noop for now
    }

    // override access
    @Override
    public <T extends GuiEventListener & Widget & NarratableEntry> T addRenderableWidget(T widget) {
        return super.addRenderableWidget(widget);
    }
}
