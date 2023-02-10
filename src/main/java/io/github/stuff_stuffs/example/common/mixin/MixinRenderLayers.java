package io.github.stuff_stuffs.example.common.mixin;

import io.github.stuff_stuffs.example.common.ExampleMod;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderLayers.class)
public class MixinRenderLayers {
    @Inject(method = "getBlockLayer", at = @At("HEAD"), cancellable = true)
    private static void translucentHook(final BlockState state, final CallbackInfoReturnable<RenderLayer> cir) {
        if (ExampleMod.FORCE_TRANSLUCENT) {
            cir.setReturnValue(RenderLayer.getTranslucent());
        }
    }
}
