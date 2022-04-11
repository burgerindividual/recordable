package com.github.burgerguy.recordable.mixin.client.render.item;

import com.google.common.collect.Lists;
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
    @Mutable
    public static List<String> LAYERS;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void addLayers(CallbackInfo ci) {
        LAYERS = List.of(
                "layer0",
                "layer1",
                "layer2",
                "layer3",
                "layer4",
                "layer5",
                "layer6",
                "layer7",
                "layer8",
                "layer9",
                "layer10",
                "layer11"
        );
    }
}
