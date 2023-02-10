package io.github.stuff_stuffs.example.common;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ExampleMod implements ModInitializer {
    public static final String MOD_ID = "modid";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static boolean FORCE_TRANSLUCENT = false;

    @Override
    public void onInitialize() {
        ServerTickEvents.START_WORLD_TICK.register(world -> {
            if (world.getTime() % 128 == 0) {
                world.spawnParticles(new PastelTransmissionParticleEffect(List.of(new BlockPos(10, 0, 0), new BlockPos(-10, -10, -10), new BlockPos(0, 10, -10)), new ItemStack(Items.ANVIL, 1), 512), 0, 0, 0, 1, 0, 0, 0, 0);
            }
        });
    }

    public static Identifier id(final String path) {
        return new Identifier(MOD_ID, path);
    }
}
