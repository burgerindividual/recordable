package com.github.burgerguy.recordable.client.screen;

import com.github.burgerguy.recordable.client.render.util.ScreenRenderUtil;
import com.github.burgerguy.recordable.shared.menu.LabelerConstants;
import com.github.burgerguy.recordable.shared.menu.Paint;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import java.awt.Color;
import net.minecraft.client.gui.components.Button;

public class PaintWidget extends Button {

    private final Paint paint;
    private final int rawColor;
    private final int rawColorGrad;

    private boolean selected;
    private boolean forceInactive;

    public PaintWidget(int x, int y, int width, int height, Paint paint) {
        super(x, y, width, height, paint.getColor().getName(), PaintWidget::onPressedAction);
        this.paint = paint;
        this.rawColor = paint.getColor().getRawColor();
        this.rawColorGrad = new Color(this.rawColor).darker().getRGB();
    }

    private static void onPressedAction(Button button) {
        PaintWidget paintWidget = (PaintWidget) button;
        paintWidget.selected = !paintWidget.selected;
        paintWidget.update();
    }

    @Override
    public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTick) {
        float x1 = this.x;
        float x2 = this.x + this.width;
        float y1 = this.y;
        float y2 = this.y + this.height;

        int borderColor = this.getBorderColor();

        Matrix4f matrix = matrixStack.last().pose();

        ScreenRenderUtil.innerBorder(
                ScreenRenderUtil.FILL_BUFFER_1,
                matrix,
                x1,
                y1,
                x2,
                y2,
                0.0f,
                1.0f,
                borderColor
        );

        float filledPixels = (((y2 - 1) - (y1 + 1)) * this.paint.getLevel()) / this.paint.getMaxCapacity();
        // middle
        ScreenRenderUtil.fillGradient(
                ScreenRenderUtil.FILL_BUFFER_1,
                matrix,
                x1 + 1,
                y2 - 1 - filledPixels,
                x2 - 1,
                y2 - 1,
                0.0f,
                this.rawColor | 0xFF000000, // make opaque
                this.rawColorGrad | 0xFF000000 // make opaque
        );

        // darken if inactive
        if (!this.active) {
            ScreenRenderUtil.fill(
                    ScreenRenderUtil.FILL_BUFFER_2,
                    matrix,
                    x1,
                    y1,
                    x2,
                    y2,
                    10.0f,
                    LabelerConstants.INACTIVE_COLOR
            );
        }
    }

    private int getBorderColor() {
        return this.selected ? LabelerConstants.SELECTED_BORDER_COLOR : LabelerConstants.DEFAULT_BORDER_COLOR;
    }

    public void setForceInactive(boolean forceInactive) {
        this.forceInactive = forceInactive;
        this.update();
    }

    public void update() {
        this.active = !this.paint.isEmpty() && !this.forceInactive;
        this.selected &= !this.paint.isEmpty();
        this.paint.setCanApply(this.selected && this.active);
    }

}
