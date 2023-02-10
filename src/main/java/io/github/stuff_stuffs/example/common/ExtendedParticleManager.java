package io.github.stuff_stuffs.example.common;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public interface ExtendedParticleManager {
    void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Camera camera, float tickDelta);
}
