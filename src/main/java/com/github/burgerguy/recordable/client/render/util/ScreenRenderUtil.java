package com.github.burgerguy.recordable.client.render.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.GameRenderer;

/**
 * Should be used to batch fill calls when rendering screens.
 * KEEP TRACK OF START AND END CALLS!!!!
 */
public class ScreenRenderUtil {

    public static BufferBuilder startFills() {
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        return bufferBuilder;
    }

    public static void fill(BufferBuilder bufferBuilder, Matrix4f matrix, float x1, float y1, float x2, float y2, int color) {
        bufferBuilder.vertex(matrix, x1, y2, 0.0F).color(color).endVertex();
        bufferBuilder.vertex(matrix, x2, y2, 0.0F).color(color).endVertex();
        bufferBuilder.vertex(matrix, x2, y1, 0.0F).color(color).endVertex();
        bufferBuilder.vertex(matrix, x1, y1, 0.0F).color(color).endVertex();
    }

    public static void endFills(BufferBuilder bufferBuilder) {
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}
