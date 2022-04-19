package com.github.burgerguy.recordable.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;

public class PaintWidget extends AbstractWidget {
    private final PaintColorWidget[] paintColorWidgets;
    private final ClientPaintArray paintArray;
    private final int pixelSize;

    private boolean mouseEventEdited;
    private boolean mix;

    public PaintWidget(int x, int y, int pixelSize, ClientPaintArray paintArray, PaintColorWidget[] paintColorWidgets) {
        super(
                x,
                y,
                paintArray.getWidth() * pixelSize,
                paintArray.getHeight() * pixelSize,
                new TranslatableComponent("widget.recordable.paint_area")
        );
        this.paintArray = paintArray;
        this.paintColorWidgets = paintColorWidgets;
        this.pixelSize = pixelSize;
    }

    public void setMix(boolean mix) {
        this.mix = mix;
    }

    private void tryPaint(double mouseX, double mouseY) {
        int x = ((int) Math.round(mouseX)) / this.pixelSize;
        int y = ((int) Math.round(mouseY) - this.x) / this.pixelSize;
        if (!this.mouseEventEdited) {
            this.paintArray.paint(x, y, this.paintColorWidgets, this.mix);
            this.mouseEventEdited = true;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean superResult = super.keyPressed(keyCode, scanCode, modifiers);
        if (!superResult && Screen.hasControlDown() && keyCode == GLFW.GLFW_KEY_Z) {
            return this.paintArray.undo();
        }
        return false;
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        this.mouseEventEdited = false;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        tryPaint(mouseX, mouseY);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        tryPaint(mouseX, mouseY);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        // TODO: implement
        super.renderButton(poseStack, mouseX, mouseY, partialTick);
        if (!this.isHovered) {
            this.mouseEventEdited = false;
        }
    }

    @Override
    public void playDownSound(SoundManager handler) {
        // no sound should be played
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}
