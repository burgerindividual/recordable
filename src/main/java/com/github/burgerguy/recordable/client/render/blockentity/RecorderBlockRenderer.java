package com.github.burgerguy.recordable.client.render.blockentity;

import com.github.burgerguy.recordable.shared.block.RecorderBlockEntity;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

public class RecorderBlockRenderer extends GeoBlockRenderer<RecorderBlockEntity> {

    public RecorderBlockRenderer() {
        super(new RecorderModel());
    }

    public void setRecordHidden(boolean hidden) {
        getGeoModelProvider().getBone("record").setHidden(hidden);
    }

}