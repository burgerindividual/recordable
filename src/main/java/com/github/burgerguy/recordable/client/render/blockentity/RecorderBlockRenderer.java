package com.github.burgerguy.recordable.client.render.blockentity;

import com.github.burgerguy.recordable.shared.block.RecorderBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

public class RecorderBlockRenderer extends GeoBlockRenderer<RecorderBlockEntity> {

    public RecorderBlockRenderer() {
        super(new RecorderModel());
    }

    public void render(RecorderBlockEntity recorderBlockEntity, float partialTicks, PoseStack stack, MultiBufferSource bufferIn, int packedLightIn) {
        this.setRecordHidden(!recorderBlockEntity.hasRecord());
        super.render(recorderBlockEntity, partialTicks, stack, bufferIn, packedLightIn);
    }

    public void setRecordHidden(boolean hidden) {
        IBone recordBone = this.getGeoModelProvider().getAnimationProcessor().getBone("record");
        if (recordBone != null) recordBone.setHidden(hidden);
    }

}