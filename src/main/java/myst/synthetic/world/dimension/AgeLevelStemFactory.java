package myst.synthetic.world.dimension;

import myst.synthetic.MystcraftSyntheticCodex;
import myst.synthetic.world.age.AgeSpec;
import myst.synthetic.world.gen.MystChunkGenerator;
import myst.synthetic.world.gen.MystChunkGeneratorSettings;
import myst.synthetic.world.terrain.FlatTerrainResolver;
import myst.synthetic.world.terrain.FlatTerrainSettings;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.server.level.ServerLevel;

public final class AgeLevelStemFactory {

    private final FlatTerrainResolver flatTerrainResolver = new FlatTerrainResolver();

    public LevelStem create(ServerLevel templateLevel, AgeSpec spec) {
        RegistryAccess.Frozen registryAccess = templateLevel.getServer().registryAccess();

        Holder<DimensionType> dimensionType = templateLevel.dimensionTypeRegistration();

        Holder<Biome> biomeHolder = resolveBiomeHolder(registryAccess, spec);

        FlatTerrainSettings flatTerrain = this.flatTerrainResolver.resolve(spec, registryAccess);

        MystChunkGeneratorSettings settings = MystChunkGeneratorSettings.create(
                spec.seed(),
                biomeHolder,
                spec.resolvedBiome(),
                flatTerrain.groundLevel(),
                flatTerrain.seaLevel(),
                flatTerrain.bedrockProfile()
        );

        return new LevelStem(dimensionType, new MystChunkGenerator(settings));
    }

    private Holder<Biome> resolveBiomeHolder(RegistryAccess.Frozen registryAccess, AgeSpec spec) {
        ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, spec.resolvedBiome());

        return registryAccess.lookupOrThrow(Registries.BIOME)
                .get(biomeKey)
                .orElseGet(() -> {
                    MystcraftSyntheticCodex.LOGGER.warn(
                            "Mystcraft age compiler resolved missing biome '{}', falling back to plains.",
                            spec.resolvedBiome()
                    );

                    return registryAccess.lookupOrThrow(Registries.BIOME)
                            .getOrThrow(ResourceKey.create(Registries.BIOME,
                                    net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", "plains")));
                });
    }
}