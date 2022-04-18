package com.github.burgerguy.recordable.client.render.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;

/**
 * Should be used to batch fill calls when rendering screens.
 * KEEP TRACK OF START AND END CALLS!!!!
 */
public class ScreenRenderUtil {

    private static final BufferBuilder GLOBAL_SCREEN_BUFFER = new BufferBuilder(RenderType.SMALL_BUFFER_SIZE);

    public static void startFills() {
        GLOBAL_SCREEN_BUFFER.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
    }

    public static void fill(Matrix4f matrix, float x1, float y1, float x2, float y2, int color) {
        GLOBAL_SCREEN_BUFFER.vertex(matrix, x1, y2, 0.0F).color(color).endVertex();
        GLOBAL_SCREEN_BUFFER.vertex(matrix, x2, y2, 0.0F).color(color).endVertex();
        GLOBAL_SCREEN_BUFFER.vertex(matrix, x2, y1, 0.0F).color(color).endVertex();
        GLOBAL_SCREEN_BUFFER.vertex(matrix, x1, y1, 0.0F).color(color).endVertex();
    }

    public static void fillGradient(Matrix4f matrix, float x1, float y1, float x2, float y2, int color1, int color2) {
        GLOBAL_SCREEN_BUFFER.vertex(matrix, x2, y1, 0.0F).color(color1).endVertex();
        GLOBAL_SCREEN_BUFFER.vertex(matrix, x1, y1, 0.0F).color(color1).endVertex();
        GLOBAL_SCREEN_BUFFER.vertex(matrix, x1, y2, 0.0F).color(color2).endVertex();
        GLOBAL_SCREEN_BUFFER.vertex(matrix, x2, y2, 0.0F).color(color2).endVertex();
    }

    public static void endAndRenderFills() {
        GLOBAL_SCREEN_BUFFER.end();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferUploader.end(GLOBAL_SCREEN_BUFFER);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}
