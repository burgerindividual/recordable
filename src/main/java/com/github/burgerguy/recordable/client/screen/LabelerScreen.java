package com.github.burgerguy.recordable.client.screen;

import com.github.burgerguy.recordable.client.render.util.ScreenRenderUtil;
import com.github.burgerguy.recordable.shared.Recordable;
import com.github.burgerguy.recordable.shared.menu.LabelerConstants;
import com.github.burgerguy.recordable.shared.menu.LabelerMenu;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;

public class LabelerScreen extends AbstractContainerScreen<LabelerMenu> {
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(Recordable.MOD_ID, "labeler");
    private static final ResourceLocation COLOR_ICON_LOCATION = new ResourceLocation("textures/gui/container/loom.png");

    private final PaintColor[] paintColors;

    public LabelerScreen(LabelerMenu labelerMenu, Inventory inventory, Component component) {
        super(labelerMenu, inventory, component);

        DyeColor[] dyeColors = DyeColor.values();
        this.paintColors = new PaintColor[dyeColors.length];
        for (int i = 0; i < dyeColors.length; i++) {
            this.paintColors[i] = new PaintColor(dyeColors[i], labelerMenu.getColorLevels()[i]);
        }
    }

    @Override
    protected void init() {
        super.init();

//        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        // gets ScreenRenderUtil ready for render
        this.addRenderableOnly((poseStack, mouseX, mouseY, partialTick) -> ScreenRenderUtil.startFills());
        for (int i = 0; i < this.paintColors.length; i++) {
            int xIdx = i / LabelerConstants.PALETTE_COLUMNS_WRAP;
            int yIdx = i % LabelerConstants.PALETTE_COLUMNS_WRAP;
            PaintColor paintColor = this.paintColors[i];
            paintColor.setBounds(
                this.leftPos + LabelerConstants.PALETTE_X + xIdx * (LabelerConstants.COLOR_WIDTH + LabelerConstants.COLOR_MARGIN_X),
                this.topPos + LabelerConstants.PALETTE_Y + yIdx * (LabelerConstants.COLOR_HEIGHT + LabelerConstants.COLOR_MARGIN_Y),
                LabelerConstants.COLOR_WIDTH,
                LabelerConstants.COLOR_HEIGHT
            );
            this.addRenderableWidget(paintColor);
        }
        // renders everything that widgets drew using ScreenRenderUtil
        this.addRenderableOnly((poseStack, mouseX, mouseY, partialTick) -> ScreenRenderUtil.endAndRenderFills());
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTick, int mouseX, int mouseY) {
        // TODO: noop for now
    }

}
