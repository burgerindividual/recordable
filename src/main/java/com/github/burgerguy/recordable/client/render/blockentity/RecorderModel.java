package com.github.burgerguy.recordable.client.render.blockentity;

import com.github.burgerguy.recordable.shared.Recordable;
import com.github.burgerguy.recordable.shared.block.RecorderBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class RecorderModel extends AnimatedGeoModel<RecorderBlockEntity> {
    @Override
    public ResourceLocation getModelLocation(RecorderBlockEntity object) {
        return new ResourceLocation(Recordable.MOD_ID, "geo/jack.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(RecorderBlockEntity object) {
        return new ResourceLocation(Recordable.MOD_ID, "textures/item/jack.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(RecorderBlockEntity object) {
        return new ResourceLocation(Recordable.MOD_ID, "animations/jackinthebox.animation.json");
    }
}
