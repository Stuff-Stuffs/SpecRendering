package io.github.stuff_stuffs.example.common;

import com.google.common.collect.EvictingQueue;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;

@Environment(EnvType.CLIENT)
public class EarlyRenderingParticleContainer {
    private static final int MAX_PARTICLES = 16384;
    private final Map<ParticleTextureSheet, Queue<EarlyRenderingParticle>> particles = new Object2ReferenceOpenHashMap<>();

    public void add(final Particle particle) {
        if (particle instanceof EarlyRenderingParticle earlyRenderingParticle) {
            particles.computeIfAbsent(particle.getType(), sheet -> EvictingQueue.create(MAX_PARTICLES)).add(earlyRenderingParticle);
        }
    }

    public void removeDead() {
        for (final Queue<EarlyRenderingParticle> particles : particles.values()) {
            particles.removeIf(particle -> !((Particle) particle).isAlive());
        }
    }

    public void render(final MatrixStack matrices, final VertexConsumerProvider vertexConsumers, final Camera camera, final float tickDelta) {
        for (final Queue<EarlyRenderingParticle> particles : particles.values()) {
            for (final EarlyRenderingParticle particle : particles) {
                particle.renderAsEntity(matrices, vertexConsumers, camera, tickDelta);
            }
        }
    }
}
