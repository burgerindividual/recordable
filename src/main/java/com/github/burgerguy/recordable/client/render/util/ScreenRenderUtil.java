package com.github.burgerguy.recordable.client.render.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Should be used to batch draw calls when rendering screens.
 * KEEP TRACK OF START AND END CALLS!!!!
 */
public class ScreenRenderUtil {
    public static final BufferBuilder FILL_BUFFER_1 = new BufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    public static final BufferBuilder BLIT_BUFFER_1 = new BufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    public static final BufferBuilder FILL_BUFFER_2 = new BufferBuilder(RenderType.SMALL_BUFFER_SIZE);

    public static void startFills(BufferBuilder fillBuffer) {
        fillBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
    }

    public static void fill(BufferBuilder fillBuffer, Matrix4f matrix, float x1, float y1, float x2, float y2, float z, int color) {
        fillBuffer.vertex(matrix, x1, y2, z).color(color).endVertex();
        fillBuffer.vertex(matrix, x2, y2, z).color(color).endVertex();
        fillBuffer.vertex(matrix, x2, y1, z).color(color).endVertex();
        fillBuffer.vertex(matrix, x1, y1, z).color(color).endVertex();
    }

    public static void fillGradient(BufferBuilder fillBuffer, Matrix4f matrix, float x1, float y1, float x2, float y2, float z, int color1, int color2) {
        fillBuffer.vertex(matrix, x2, y1, z).color(color1).endVertex();
        fillBuffer.vertex(matrix, x1, y1, z).color(color1).endVertex();
        fillBuffer.vertex(matrix, x1, y2, z).color(color2).endVertex();
        fillBuffer.vertex(matrix, x2, y2, z).color(color2).endVertex();
    }

    public static void endAndRenderFills(BufferBuilder fillBuffer) {
        fillBuffer.end();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferUploader.end(fillBuffer);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void startBlits(BufferBuilder blitBuffer) {
        blitBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
    }

    public static void blit(BufferBuilder blitBuffer, Matrix4f matrix, float x, float y, float z, float u, float v, float size) {
        blit(
                blitBuffer,
                matrix,
                x,
                y,
                z,
                u,
                v,
                size,
                size,
                256,
                256
        );
    }

    public static void blit(BufferBuilder blitBuffer, Matrix4f matrix, float x, float y, float z, float u, float v, float width, float height, int texWidth, int texHeight) {
        blit(
                blitBuffer,
                matrix,
                x,
                y,
                x + width,
                y + height,
                z,
                u / (float) texWidth,
                v / (float) texHeight,
                (u + width) / (float) texWidth,
                (v + height) / (float) texHeight
        );
    }

    public static void blit(BufferBuilder blitBuffer, Matrix4f matrix, float x1, float y1, float x2, float y2, float z, float minU, float minV, float maxU, float maxV) {
        blitBuffer.vertex(matrix, x1, y2, z).uv(minU, maxV).endVertex();
        blitBuffer.vertex(matrix, x2, y2, z).uv(maxU, maxV).endVertex();
        blitBuffer.vertex(matrix, x2, y1, z).uv(maxU, minV).endVertex();
        blitBuffer.vertex(matrix, x1, y1, z).uv(minU, minV).endVertex();
    }

    public static void endAndRenderBlits(BufferBuilder blitBuffer, ResourceLocation texture, int texBindingSlot) {
        blitBuffer.end();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(texBindingSlot, texture);
        BufferUploader.end(blitBuffer);
    }

}
