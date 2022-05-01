package com.github.burgerguy.recordable.client.render.blockentity;

import com.github.burgerguy.recordable.shared.block.RecorderBlock;
import com.github.burgerguy.recordable.shared.block.RecorderBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class RecorderItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    private final RecorderBlockEntity renderedInstance;

    public RecorderItemRenderer() {
        this.renderedInstance = new RecorderBlockEntity(BlockPos.ZERO, RecorderBlock.INSTANCE.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH));
    }

    @Override
    public void render(ItemStack itemStack, ItemTransforms.TransformType transformType, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(this.renderedInstance, matrices, vertexConsumers, light, overlay);
    }
}
