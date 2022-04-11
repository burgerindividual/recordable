package com.github.burgerguy.recordable.client.render.blockentity;

import com.github.burgerguy.recordable.shared.Recordable;
import com.github.burgerguy.recordable.shared.block.RecorderBlock;
import com.github.burgerguy.recordable.shared.block.RecorderBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RecorderBlockEntityRenderer implements BlockEntityRenderer<RecorderBlockEntity> {
    private static final float TICKS_PER_REVOLUTION = 20.0f / ((100.0f/3.0f) / 60.0f); // 33 1/3 rpm to ticks per revolution

    public RecorderBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(new ModelLayerLocation(RecorderBlock.IDENTIFIER, "main"));
    }

    @Override
    public void render(RecorderBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Quaternion rotation = Vector3f.YP.rotation(((partialTick % TICKS_PER_REVOLUTION) / TICKS_PER_REVOLUTION) * 2.0f * (float) Math.PI);
    }

}
