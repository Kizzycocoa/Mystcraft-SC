package myst.synthetic.page.symbol.source;

import myst.synthetic.MystcraftSyntheticCodex;
import net.minecraft.core.RegistryAccess;

import java.util.List;

public final class MystcraftGeneratedPageSymbols {

    private static final FluidPageSymbolSource FLUID_SOURCE = new FluidPageSymbolSource();
    private static final CelestialPageSymbolSource CELESTIAL_SOURCE = new CelestialPageSymbolSource();
    private static final BiomePageSymbolSource BIOME_SOURCE = new BiomePageSymbolSource();

    private static boolean staticInitialized = false;
    private static boolean biomeInitialized = false;

    private MystcraftGeneratedPageSymbols() {
    }

    public static void initializeStatic() {
        if (staticInitialized) {
            return;
        }
        staticInitialized = true;

        FLUID_SOURCE.registerSymbols();
        CELESTIAL_SOURCE.registerSymbols();

        MystcraftSyntheticCodex.LOGGER.info("Registered static generated Mystcraft page symbols.");
    }

    public static void initializeBiomes(RegistryAccess registryAccess) {
        if (biomeInitialized) {
            return;
        }
        biomeInitialized = true;

        BIOME_SOURCE.registerSymbols(registryAccess);

        MystcraftSyntheticCodex.LOGGER.info("Registered biome-generated Mystcraft page symbols.");
    }
}