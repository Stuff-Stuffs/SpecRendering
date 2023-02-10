package io.github.stuff_stuffs.example.client.particle;

import io.github.stuff_stuffs.example.client.TransparentVertexConsumerProvider;
import io.github.stuff_stuffs.example.common.EarlyRenderingParticle;
import io.github.stuff_stuffs.example.common.ExampleMod;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.List;

public class PastelTransmissionParticle extends SpriteBillboardParticle implements EarlyRenderingParticle {
    private final EntityRenderDispatcher dispatcher;
    private final BufferBuilderStorage bufferStorage;

    private final List<Vec3d> travelNodes;
    private final ItemEntity itemEntity;

    public PastelTransmissionParticle(final EntityRenderDispatcher dispatcher, final BufferBuilderStorage bufferStorage, final ClientWorld world, final double x, final double y, final double z, final List<BlockPos> travelNodes, final ItemStack stack, final int travelTime) {
        super(world, x, y - 0.25, z, 0.0D, 0.0D, 0.0D);
        this.dispatcher = dispatcher;
        this.bufferStorage = bufferStorage;
        scale = 1.0F;

        final List<Vec3d> vecList = new ArrayList<>();
        for (final BlockPos p : travelNodes) {
            vecList.add(new Vec3d(p.getX() + 0.5, p.getY() + 0.25, p.getZ() + 0.5));
        }
        this.travelNodes = vecList;

        itemEntity = new ItemEntity(world, x, y, z, stack);
        maxAge = travelTime;

        // spawning sound & particles
        final Vec3d pos = vecList.get(0);
        //world.playSound(pos.getX(), pos.getY() + 0.25, pos.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS,
        //        0.25F * SpectrumCommon.CONFIG.BlockSoundVolume, 0.9F + world.random.nextFloat() * 0.2F, true);
        world.addParticle(ParticleTypes.BUBBLE_POP, pos.getX(), pos.getY() + 0.25, pos.getZ(), 0, 0, 0);
    }

    @Override
    public void buildGeometry(final VertexConsumer vertexConsumer, final Camera camera, final float tickDelta) {
        final Vec3d cameraPos = camera.getPos();
        final float x = (float) (MathHelper.lerp(tickDelta, prevPosX, this.x) - cameraPos.getX());
        final float y = (float) (MathHelper.lerp(tickDelta, prevPosY, this.y) - cameraPos.getY());
        final float z = (float) (MathHelper.lerp(tickDelta, prevPosZ, this.z) - cameraPos.getZ());
        final int light = getBrightness(tickDelta);

        final Quaternion quaternion = camera.getRotation();
        final Vec3f[] vec3fs = new Vec3f[]{new Vec3f(-1.0F, -1.0F, 0.0F), new Vec3f(-1.0F, 1.0F, 0.0F), new Vec3f(1.0F, 1.0F, 0.0F), new Vec3f(1.0F, -1.0F, 0.0F)};
        final float size = getSize(tickDelta);

        for (int k = 0; k < 4; ++k) {
            final Vec3f vec3f2 = vec3fs[k];
            vec3f2.rotate(quaternion);
            vec3f2.scale(size);
            vec3f2.add(x, y, z);
        }

        final float minU = getMinU();
        final float maxU = getMaxU();
        final float minV = getMinV();
        final float maxV = getMaxV();
        vertexConsumer.vertex(vec3fs[0].getX(), vec3fs[0].getY(), vec3fs[0].getZ()).texture(maxU, maxV).color(red, green, blue, alpha).light(light).next();
        vertexConsumer.vertex(vec3fs[1].getX(), vec3fs[1].getY(), vec3fs[1].getZ()).texture(maxU, minV).color(red, green, blue, alpha).light(light).next();
        vertexConsumer.vertex(vec3fs[2].getX(), vec3fs[2].getY(), vec3fs[2].getZ()).texture(minU, minV).color(red, green, blue, alpha).light(light).next();
        vertexConsumer.vertex(vec3fs[3].getX(), vec3fs[3].getY(), vec3fs[3].getZ()).texture(minU, maxV).color(red, green, blue, alpha).light(light).next();
    }

    @Override
    public int getBrightness(final float tickDelta) {
        return 240;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        age++;

        final int vertexCount = travelNodes.size() - 1;
        final float travelPercent = (float) age / maxAge;
        if (travelPercent >= 1.0F) {
            final Vec3d destination = travelNodes.get(vertexCount);
            //world.playSound(destination.getX(), destination.getY() + 0.25, destination.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS,
            //        0.25F * SpectrumCommon.CONFIG.BlockSoundVolume, 0.9F + world.random.nextFloat() * 0.2F, true);
            world.addParticle(ParticleTypes.BUBBLE_POP, destination.getX(), destination.getY() + 0.25, destination.getZ(), 0, 0, 0);
            markDead();
            return;
        }

        final float progress = travelPercent * vertexCount;
        final int startNodeID = (int) progress;
        prevPosX = x;
        prevPosY = y;
        prevPosZ = z;

        final Vec3d source = travelNodes.get(startNodeID);
        final Vec3d destination = travelNodes.get(startNodeID + 1);

        final float nodeProgress = progress % 1;
        x = MathHelper.lerp(nodeProgress, source.x, destination.x);
        y = MathHelper.lerp(nodeProgress, source.y, destination.y);
        z = MathHelper.lerp(nodeProgress, source.z, destination.z);
    }

    @Override
    public void renderAsEntity(final MatrixStack matrixStack, final VertexConsumerProvider vertexConsumers, final Camera camera, final float tickDelta) {
        final EntityRenderer<? super ItemEntity> entityRenderer = dispatcher.getRenderer(itemEntity);
        final Vec3d positionOffset = entityRenderer.getPositionOffset(itemEntity, tickDelta);

        final Vec3d cameraPos = camera.getPos();
        final float x = (float) (MathHelper.lerp(tickDelta, prevPosX, this.x) + positionOffset.getX());
        final float y = (float) (MathHelper.lerp(tickDelta, prevPosY, this.y) + positionOffset.getY());
        final float z = (float) (MathHelper.lerp(tickDelta, prevPosZ, this.z) + positionOffset.getZ());
        matrixStack.push();
        matrixStack.translate(x - cameraPos.x, y - cameraPos.y, z - cameraPos.z);
        final int light = getBrightness(tickDelta);
        matrixStack.multiply(camera.getRotation());
        matrixStack.translate(0, -0.333, 0);

        ExampleMod.FORCE_TRANSLUCENT = true;
        entityRenderer.render(itemEntity, itemEntity.getYaw(), tickDelta, matrixStack, new TransparentVertexConsumerProvider(vertexConsumers), light);
        ExampleMod.FORCE_TRANSLUCENT = false;
        matrixStack.pop();
    }
}
