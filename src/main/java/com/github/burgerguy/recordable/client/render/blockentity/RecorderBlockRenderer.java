package com.github.burgerguy.recordable.client.render.blockentity;

import com.github.burgerguy.recordable.shared.block.RecorderBlockEntity;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

public class RecorderBlockRenderer extends GeoBlockRenderer<RecorderBlockEntity> {

    public RecorderBlockRenderer(AnimatedGeoModel<RecorderBlockEntity> modelProvider) {
        super(modelProvider);
    }
}