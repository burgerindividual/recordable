package com.github.burgerguy.recordable.client.screen;

import com.github.burgerguy.recordable.client.render.util.ScreenRenderUtil;
import com.github.burgerguy.recordable.shared.menu.LabelerConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

// WARNING: Only use in places with proper ScreenRenderUtil support.
public class CustomToggleButtonWidget extends AbstractButton {
    private final OnToggle onToggleAction;
    private final float buttonU;
    private final float buttonV;
    private final float iconU;
    private final float iconV;

    private boolean pressed;

    public CustomToggleButtonWidget(int x, int y, int width, int height, float buttonU, float buttonV, float iconU, float iconV, boolean initialToggled, Component name, OnToggle onToggleAction) {
        super(x, y, width, height, name);
        this.buttonU = buttonU;
        this.buttonV = buttonV;
        this.iconU = iconU;
        this.iconV = iconV;
        this.onToggleAction = onToggleAction;
        this.pressed = initialToggled;

        // update with initial
        onToggleAction.onToggle(initialToggled);
    }

    @Override
    public void onPress() {
        this.pressed = !this.pressed;
        this.onToggleAction.onToggle(this.pressed);
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
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
                this.buttonU + this.getUOffset(),
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
                this.iconU + this.getUOffset(),
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

    private float getUOffset() {
        return this.pressed ? this.width : 0.0f;
    }

    public interface OnToggle {
        void onToggle(boolean pressed);
    }
}
