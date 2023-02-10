package io.github.stuff_stuffs.example.client;

import com.mojang.serialization.Codec;
import io.github.stuff_stuffs.example.client.particle.PastelTransmissionParticle;
import io.github.stuff_stuffs.example.common.ExtendedParticleManager;
import io.github.stuff_stuffs.example.common.PastelTransmissionParticleEffect;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

import java.util.function.Function;

public class ExampleModClient implements ClientModInitializer {
    public static final ParticleType<PastelTransmissionParticleEffect> PASTEL_TRANSMISSION = register("pastel_transmission", PastelTransmissionParticleEffect.FACTORY, (particleType) -> PastelTransmissionParticleEffect.CODEC, false);

    @Override
    public void onInitializeClient() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> ((ExtendedParticleManager) MinecraftClient.getInstance().particleManager).render(context.matrixStack(), context.consumers(), context.camera(), context.tickDelta()));
        ParticleFactoryRegistry.getInstance().register(ExampleModClient.PASTEL_TRANSMISSION, provider -> (pastelTransmissionParticleEffect, world, x, y, z, velocityX, velocityY, velocityZ) -> {
            final PastelTransmissionParticle particle = new PastelTransmissionParticle(MinecraftClient.getInstance().getEntityRenderDispatcher(), MinecraftClient.getInstance().getBufferBuilders(),
                    world, x, y, z, pastelTransmissionParticleEffect.getNodePositions(), pastelTransmissionParticleEffect.getStack(), pastelTransmissionParticleEffect.getTravelTime());
            particle.setSprite(provider);
            return particle;
        });
        WorldRenderEvents.AFTER_ENTITIES.register(new WorldRenderEvents.AfterEntities() {
            @Override
            public void afterEntities(final WorldRenderContext context) {
                final MatrixStack matrices = context.matrixStack();
                final Vec3d pos = context.camera().getPos();
                matrices.push();
                matrices.translate(-pos.x, -pos.y, -pos.z);
                //new BlockPos(10, 0, 0), new BlockPos(-10, -10, -10), new BlockPos(0, 10, -10)
                renderLineTo(context.tickDelta(), context.matrixStack(), context.consumers(), LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, new BlockPos(10, 0, 0), new BlockPos(-10, -10, -10));
                renderLineTo(context.tickDelta(), context.matrixStack(), context.consumers(), LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, new BlockPos(-10, -10, -10), new BlockPos(0, 10, -10));
                matrices.pop();
            }
        });
    }

    private static <T extends ParticleEffect> ParticleType<T> register(final String name, final ParticleEffect.Factory<T> factory, final Function<ParticleType<T>, Codec<T>> function, final boolean alwaysShow) {
        return Registry.register(Registry.PARTICLE_TYPE, new Identifier("spectrum", name), new ParticleType<T>(alwaysShow, factory) {
            @Override
            public Codec<T> getCodec() {
                return function.apply(this);
            }
        });
    }

    public static void renderLineTo(final float tickDelta, final MatrixStack matrices, final VertexConsumerProvider vertexConsumers, final int light, final int overlay, final BlockPos thisPos, final BlockPos pos) {
        matrices.push();
        //you might need this because you will be in a block entity renderer
        //matrices.translate(-thisPos.getX(), -thisPos.getY(), -thisPos.getZ());

        final Vec3d vec = Vec3d.ofCenter(pos);
        final Vec3d here = Vec3d.ofCenter(thisPos);
        final Vec3d delta = vec.subtract(here);
        final float dist = (float) vec.length();
        final Vec3d axis = delta.multiply(-1 / dist);

        final Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        final double[] billBoard = billboard((vec.x) * 1, (vec.y) * 1, ( vec.z) * 1, camera.getPos().x, camera.getPos().y, camera.getPos().z, axis.x, axis.y, axis.z);

        final Matrix4f model = matrices.peek().getPositionMatrix();
        final Matrix3f normal = matrices.peek().getNormalMatrix();
        final float width = 0.125F;
        final VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getBeaconBeam(new Identifier("textures/entity/beacon_beam.png"), true));
        renderBeamFace(billBoard, model, normal, buffer, 1, 1, 1, 1, dist, 0, 0, width, -width, 0, 0.5F, 0, 1);
        renderBeamFace(billBoard, model, normal, buffer, 1, 1, 1, 1, dist, -width, -width, 0, 0, 0.5F, 1, 0, 1);

        //ignore need because this isnt in a block entity renderer
        matrices.pop();
    }

    private static void renderBeamFace(final double[] mat, final Matrix4f positionMatrix, final Matrix3f normalMatrix, final VertexConsumer vertices, final float red, final float green, final float blue, final float alpha, final float length, final float x1, final float z1, final float x2, final float z2, final float u1, final float u2, final float v1, final float v2) {
        renderBeamVertex(mat, positionMatrix, normalMatrix, vertices, red, green, blue, alpha, x1, length, z1, u2, v1);
        renderBeamVertex(mat, positionMatrix, normalMatrix, vertices, red, green, blue, alpha, x1, 0, z1, u2, v2);
        renderBeamVertex(mat, positionMatrix, normalMatrix, vertices, red, green, blue, alpha, x2, 0, z2, u1, v2);
        renderBeamVertex(mat, positionMatrix, normalMatrix, vertices, red, green, blue, alpha, x2, length, z2, u1, v1);
    }

    private static void renderBeamVertex(final double[] mat, final Matrix4f positionMatrix, final Matrix3f normalMatrix, final VertexConsumer vertices, final float red, final float green, final float blue, final float alpha, final float x, final float y, final float z, final float u, final float v) {
        final Vec3d transform = transform(new Vec3d(x, y, z), mat);
        vertices.vertex(positionMatrix, (float) transform.x, (float) transform.y, (float) transform.z).color(red, green, blue, alpha).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0.0F, 1.0F, 0.0F).next();
    }

    private static Vec3d transform(final Vec3d vec, final double[] mat) {
        final double x = mat[matIndex(0, 0)] * vec.x + mat[matIndex(0, 1)] * vec.y + mat[matIndex(0, 2)] * vec.z + mat[matIndex(0, 3)];
        final double y = mat[matIndex(1, 0)] * vec.x + mat[matIndex(1, 1)] * vec.y + mat[matIndex(1, 2)] * vec.z + mat[matIndex(1, 3)];
        final double z = mat[matIndex(2, 0)] * vec.x + mat[matIndex(2, 1)] * vec.y + mat[matIndex(2, 2)] * vec.z + mat[matIndex(2, 3)];
        return new Vec3d(x, y, z);
    }

    //treating an array like a matrix because minecrafts matrices aren't mutable
    //also stolen from joml, which is in future versions of minecraft
    private static double[] billboard(final double objX, final double objY, final double objZ, final double targetX, final double targetY, final double targetZ, final double upX, final double upY, final double upZ) {
        double dirX = targetX - objX;
        double dirY = targetY - objY;
        double dirZ = targetZ - objZ;
        // left = up x dir
        double leftX = upY * dirZ - upZ * dirY;
        double leftY = upZ * dirX - upX * dirZ;
        double leftZ = upX * dirY - upY * dirX;
        // normalize left
        final double invLeftLen = 1 / Math.sqrt(leftX * leftX + leftY * leftY + leftZ * leftZ);
        leftX *= invLeftLen;
        leftY *= invLeftLen;
        leftZ *= invLeftLen;
        // recompute dir by constraining rotation around 'up'
        // dir = left x up
        dirX = leftY * upZ - leftZ * upY;
        dirY = leftZ * upX - leftX * upZ;
        dirZ = leftX * upY - leftY * upX;
        // normalize dir
        final double invDirLen = 1 / Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        dirX *= invDirLen;
        dirY *= invDirLen;
        dirZ *= invDirLen;

        final double[] mat = new double[16];
        mat[matIndex(0, 0)] = leftX;
        mat[matIndex(1, 0)] = leftY;
        mat[matIndex(2, 0)] = leftZ;
        mat[matIndex(3, 0)] = 0;

        mat[matIndex(0, 1)] = upX;
        mat[matIndex(1, 1)] = upY;
        mat[matIndex(2, 1)] = upZ;
        mat[matIndex(3, 1)] = 0;

        mat[matIndex(0, 2)] = dirX;
        mat[matIndex(1, 2)] = dirY;
        mat[matIndex(2, 2)] = dirZ;
        mat[matIndex(3, 2)] = 0;

        mat[matIndex(0, 3)] = objX;
        mat[matIndex(1, 3)] = objY;
        mat[matIndex(2, 3)] = objZ;
        mat[matIndex(3, 3)] = 1.0;

        return mat;
    }

    private static int matIndex(final int x, final int y) {
        return x * 4 + y;
    }
}
