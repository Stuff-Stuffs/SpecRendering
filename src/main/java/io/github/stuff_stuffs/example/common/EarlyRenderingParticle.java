package io.github.stuff_stuffs.example.common;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public interface EarlyRenderingParticle {
    void renderAsEntity(final MatrixStack matrices, final VertexConsumerProvider vertexConsumers, final Camera camera, final float tickDelta);
}
