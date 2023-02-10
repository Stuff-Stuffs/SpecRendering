package io.github.stuff_stuffs.example.common;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.example.client.ExampleModClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PastelTransmissionParticleEffect implements ParticleEffect {

    public static final Codec<PastelTransmissionParticleEffect> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Codec.list(BlockPos.CODEC).fieldOf("positions").forGetter((particleEffect) -> particleEffect.nodePositions),
            ItemStack.CODEC.fieldOf("stack").forGetter((effect) -> effect.stack),
            Codec.INT.fieldOf("travel_time").forGetter((particleEffect) -> particleEffect.travelTime)
    ).apply(instance, PastelTransmissionParticleEffect::new));

    public static final Factory<PastelTransmissionParticleEffect> FACTORY = new Factory<>() {
        public PastelTransmissionParticleEffect read(ParticleType<PastelTransmissionParticleEffect> particleType, StringReader stringReader) throws CommandSyntaxException {
            List<BlockPos> posList = new ArrayList<>();

            stringReader.expect(' ');
            int travelTime = stringReader.readInt();

            // TODO I don't care, really
            stringReader.expect(' ');
            int x1 = stringReader.readInt();
            stringReader.expect(' ');
            int y1 = stringReader.readInt();
            stringReader.expect(' ');
            int z1 = stringReader.readInt();

            stringReader.expect(' ');
            int x2 = stringReader.readInt();
            stringReader.expect(' ');
            int y2 = stringReader.readInt();
            stringReader.expect(' ');
            int z2 = stringReader.readInt();

            BlockPos sourcePos = new BlockPos(x1, y1, z1);
            BlockPos destinationPos = new BlockPos(x2, y2, z2);
            posList.add(sourcePos);
            posList.add(destinationPos);
            return new PastelTransmissionParticleEffect(posList, Items.STONE.getDefaultStack(), travelTime);
        }

        public PastelTransmissionParticleEffect read(ParticleType<PastelTransmissionParticleEffect> particleType, PacketByteBuf buf) {
            int posCount = buf.readInt();
            List<BlockPos> posList = new ArrayList<>();
            for (int i = 0; i < posCount; i++) {
                posList.add(buf.readBlockPos());
            }
            ItemStack stack = buf.readItemStack();
            int travelTime = buf.readInt();
            return new PastelTransmissionParticleEffect(posList, stack, travelTime);
        }
    };

    private final List<BlockPos> nodePositions;
    private final ItemStack stack;
    private final int travelTime;

    public PastelTransmissionParticleEffect(List<BlockPos> nodePositions, ItemStack stack, int travelTime) {
        this.nodePositions = nodePositions;
        this.stack = stack;
        this.travelTime = travelTime;
    }

    public ParticleType<?> getType() {
        return ExampleModClient.PASTEL_TRANSMISSION;
    }

    @Override
    public String asString() {
        int nodeCount = this.nodePositions.size();
        BlockPos source = this.nodePositions.get(0);
        BlockPos destination = this.nodePositions.get(this.nodePositions.size() - 1);
        int d = source.getX();
        int e = source.getY();
        int f = source.getZ();
        int g = destination.getX();
        int h = destination.getY();
        int i = destination.getZ();
        return String.format(Locale.ROOT, "%s %d %d %d %d %d %d %d %d %d", Registry.PARTICLE_TYPE.getId(this.getType()), this.travelTime, nodeCount, d, e, f, g, h, i);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(nodePositions.size());
        for (BlockPos pos : nodePositions) {
            buf.writeBlockPos(pos);
        }
        buf.writeItemStack(stack);
        buf.writeInt(travelTime);
    }

    public List<BlockPos> getNodePositions() {
        return nodePositions;
    }

    public ItemStack getStack() {
        return stack;
    }

    public int getTravelTime() {
        return travelTime;
    }

}
