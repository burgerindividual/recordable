package com.github.burgerguy.recordable.mixin.client.render.item;

import java.util.List;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemModelGenerator.class)
public class ItemModelGeneratorMixin {
    @Shadow
    @Final
    public static List<String> LAYERS;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void addLayers(CallbackInfo ci) {
        LAYERS.add("layer5");
        LAYERS.add("layer5");
        LAYERS.add("layer6");
        LAYERS.add("layer7");
        LAYERS.add("layer8");
        LAYERS.add("layer9");
        LAYERS.add("layer10");
        LAYERS.add("layer11");
    }
}
