package com.github.burgerguy.recordable.client.screen;

import com.github.burgerguy.recordable.client.render.util.ScreenRenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;

public class PaintWidget extends AbstractWidget {
    private static final int GRID_COLOR = 0xFFFFFFFF;
    private static final float GRID_HALF_LINE_WIDTH = 0.5f;

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
                new TranslatableComponent("screen.recordable.labeler.paint_area")
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
        Matrix4f matrix = poseStack.last().pose();
        float x1 = this.x;
        float x2 = this.x + this.width;
        float y1 = this.y;
        float y2 = this.y + this.height;

        // pixels
        for (int pxY = 0; pxY < this.paintArray.getHeight(); pxY++) {
            for (int pxX = 0; pxX < this.paintArray.getWidth(); pxX++) {
                ScreenRenderUtil.fill(
                        matrix,
                        x1 + (pxX * this.pixelSize),
                        y1 + (pxY * this.pixelSize),
                        x1 + (pxX * this.pixelSize) + this.pixelSize,
                        y1 + (pxY * this.pixelSize) + this.pixelSize,
                        paintArray.getColor(pxX, pxY)
                );
            }
        }

        // horizontal lines
        for (int i = 0; i <= this.paintArray.getHeight(); i++) {
            float lineY = i * this.pixelSize;
            ScreenRenderUtil.fill(
                    matrix,
                    x1,
                    y1 + lineY - GRID_HALF_LINE_WIDTH,
                    x2,
                    y1 + lineY + GRID_HALF_LINE_WIDTH,
                    GRID_COLOR
            );
        }

        // vertical lines
        for (int i = 0; i <= this.paintArray.getWidth(); i++) {
            float lineX = i * this.pixelSize;
            ScreenRenderUtil.fill(
                    matrix,
                    x1 + lineX - GRID_HALF_LINE_WIDTH,
                    y1,
                    x1 + lineX + GRID_HALF_LINE_WIDTH,
                    y2,
                    GRID_COLOR
            );
        }

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
