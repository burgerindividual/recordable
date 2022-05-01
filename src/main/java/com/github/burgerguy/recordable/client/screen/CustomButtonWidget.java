package com.github.burgerguy.recordable.client.screen;

import com.github.burgerguy.recordable.client.render.util.ScreenRenderUtil;
import com.github.burgerguy.recordable.shared.menu.LabelerConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

// WARNING: Only use in places with proper ScreenRenderUtil support.
public class CustomButtonWidget extends Button {
    private final float buttonU;
    private final float buttonV;
    private final float iconU;
    private final float iconV;

    public CustomButtonWidget(int x, int y, int width, int height, float buttonU, float buttonV, float iconU, float iconV, Component name, OnPress onPress) {
        super(x, y, width, height, name, onPress);
        this.buttonU = buttonU;
        this.buttonV = buttonV;
        this.iconU = iconU;
        this.iconV = iconV;
    }

    @Override
    public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTick) {
        float x1 = this.x;
        float x2 = this.x + this.width;
        float y1 = this.y;
        float y2 = this.y + this.height;

        Matrix4f matrix = matrixStack.last().pose();

        ScreenRenderUtil.blit(
                ScreenRenderUtil.BLIT_BUFFER_1,
                matrix,
                x1,
                y1,
                0.0f,
                this.buttonU,
                this.buttonV,
                this.width,
                this.height,
                LabelerConstants.LABELER_GUI_TEX_WIDTH,
                LabelerConstants.LABELER_GUI_TEX_HEIGHT
        );

        ScreenRenderUtil.blit(
                ScreenRenderUtil.BLIT_BUFFER_1,
                matrix,
                x1,
                y1,
                1.0f,
                this.iconU,
                this.iconV,
                this.width,
                this.height,
                LabelerConstants.LABELER_GUI_TEX_WIDTH,
                LabelerConstants.LABELER_GUI_TEX_HEIGHT
        );

        if (!this.active) {
            // darken if inactive
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
        } else if (this.isHoveredOrFocused()) {
            // draw hover border
            ScreenRenderUtil.innerBorder(
                    ScreenRenderUtil.FILL_BUFFER_2,
                    matrix,
                    x1,
                    y1,
                    x2,
                    y2,
                    10.0f,
                    LabelerConstants.BUTTON_BORDER_WIDTH,
                    LabelerConstants.BUTTON_BORDER_COLOR
            );
        }
    }
}
