package com.github.burgerguy.recordable.client.screen;

import com.github.burgerguy.recordable.client.render.util.ScreenRenderUtil;
import com.github.burgerguy.recordable.shared.menu.Painter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;

public class PaintWidget extends AbstractWidget {
    private static final int GRID_COLOR = 0xFF000000;
    private static final float GRID_HALF_LINE_WIDTH = 0.5f;

    private final ClientPainter clientPainter;
    private final int pixelSize;

    private int exitEventPixelIdx = Painter.EMPTY_INDEX;

    public PaintWidget(int x, int y, int pixelSize, ClientPainter clientPainter) {
        super(
                x,
                y,
                clientPainter.getWidth() * pixelSize,
                clientPainter.getHeight() * pixelSize,
                new TranslatableComponent("screen.recordable.labeler.paint_area")
        );
        this.clientPainter = clientPainter;
        this.pixelSize = pixelSize;
    }

    /**
     * Returns true if any paint color widget runs out.
     */
    private boolean tryApply(double mouseX, double mouseY) {
        int x = (int) ((mouseX - this.x) / this.pixelSize);
        int y = (int) ((mouseY - this.y) / this.pixelSize);
        int pixelIdx = this.clientPainter.coordsToIndex(x, y);
        if (this.exitEventPixelIdx != pixelIdx) {
            if (pixelIdx != Painter.EMPTY_INDEX && pixelIdx != Painter.OUT_OF_BOUNDS_INDEX) {
                this.clientPainter.apply(pixelIdx);
                this.exitEventPixelIdx = pixelIdx;
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean superResult = super.keyPressed(keyCode, scanCode, modifiers);
        if (!superResult && Screen.hasControlDown() && keyCode == GLFW.GLFW_KEY_Z && this.clientPainter.canUndo()) {
            this.clientPainter.undo();
            return true;
        }
        return false;
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        this.exitEventPixelIdx = Painter.EMPTY_INDEX;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        tryApply(mouseX, mouseY);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        tryApply(mouseX, mouseY);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        Matrix4f matrix = poseStack.last().pose();
        float widgetX = this.x;
        float widgetY = this.y;
        float pixelSize = this.pixelSize;
        int paHeight = this.clientPainter.getHeight();
        int paWidth = this.clientPainter.getWidth();

        // pixels
        for (int pxYCoord = 0; pxYCoord < paHeight; pxYCoord++) {
            for (int pxXCoord = 0; pxXCoord < paWidth; pxXCoord++) {
                int pxIndex = this.clientPainter.coordsToIndex(pxXCoord, pxYCoord);
                if (pxIndex != Painter.EMPTY_INDEX) {
                    ScreenRenderUtil.fill(
                            matrix,
                            widgetX + (pxXCoord * pixelSize),
                            widgetY + (pxYCoord * pixelSize),
                            widgetX + (pxXCoord * pixelSize) + pixelSize,
                            widgetY + (pxYCoord * pixelSize) + pixelSize,
                            this.clientPainter.getColor(pxIndex) | 0xFF000000 // make opaque
                    );
                }
            }
        }

        // grid lines
        for (int pxYCoord = 0; pxYCoord <= paHeight; pxYCoord++) {
            for (int pxXCoord = 0; pxXCoord <= paWidth; pxXCoord++) {
                float lineX = pxXCoord * pixelSize;
                float lineY = pxYCoord * pixelSize;
                int currentIdx = this.clientPainter.coordsToIndex(pxXCoord, pxYCoord);
                int prevIdxX = this.clientPainter.coordsToIndex(pxXCoord - 1, pxYCoord);
                int prevIdxY = this.clientPainter.coordsToIndex(pxXCoord, pxYCoord - 1);

                // horizontal lines, drawn in vertical order
                if ((currentIdx != Painter.EMPTY_INDEX &&
                    currentIdx != Painter.OUT_OF_BOUNDS_INDEX)
                    ||
                    (pxXCoord != paWidth &&
                    prevIdxY != Painter.EMPTY_INDEX &&
                    prevIdxY != Painter.OUT_OF_BOUNDS_INDEX)) {
                    ScreenRenderUtil.fill(
                            matrix,
                            widgetX + lineX,
                            widgetY + lineY - GRID_HALF_LINE_WIDTH,
                            widgetX + lineX + pixelSize,
                            widgetY + lineY + GRID_HALF_LINE_WIDTH,
                            GRID_COLOR
                    );
                }

                // vertical lines, drawn in horizontal order
                if ((currentIdx != Painter.EMPTY_INDEX &&
                    currentIdx != Painter.OUT_OF_BOUNDS_INDEX)
                    ||
                    (pxYCoord != paHeight &&
                    prevIdxX != Painter.EMPTY_INDEX &&
                    prevIdxX != Painter.OUT_OF_BOUNDS_INDEX)) {
                    ScreenRenderUtil.fill(
                            matrix,
                            widgetX + lineX - GRID_HALF_LINE_WIDTH,
                            widgetY + lineY,
                            widgetX + lineX + GRID_HALF_LINE_WIDTH,
                            widgetY + lineY + pixelSize,
                            GRID_COLOR
                    );
                }
            }
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
