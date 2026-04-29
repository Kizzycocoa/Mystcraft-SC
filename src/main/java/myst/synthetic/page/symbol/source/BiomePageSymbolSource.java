package myst.synthetic.page.symbol.source;

import myst.synthetic.page.symbol.PageSymbol;
import myst.synthetic.page.symbol.PageSymbolRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.List;
import java.util.Set;

public final class BiomePageSymbolSource {

    private static final Set<String> END_BIOMES = Set.of(
            "the_end",
            "end_highlands",
            "end_midlands",
            "end_barrens",
            "small_end_islands"
    );

    private static final Set<String> NETHER_BIOMES = Set.of(
            "nether_wastes",
            "crimson_forest",
            "warped_forest",
            "soul_sand_valley",
            "basalt_deltas"
    );

    public void registerSymbols(RegistryAccess registryAccess) {
        var biomeRegistry = registryAccess.lookupOrThrow(Registries.BIOME);

        for (ResourceKey<Biome> biomeKey : biomeRegistry.listElementIds().toList()) {
            Identifier biomeId = biomeKey.identifier();

            Biome biome = biomeRegistry.get(biomeKey)
                    .map(Holder::value)
                    .orElse(null);

            PageSymbolRegistry.register(new PageSymbol(
                    Identifier.fromNamespaceAndPath(
                            "mystcraft-sc",
                            "generated/biome/" + biomeId.getNamespace() + "/" + biomeId.getPath()
                    ),
                    null,
                    "Minecraft",
                    "biome",
                    cardRankForSpecial(biomeId),
                    lootTagsForBiome(biomeId, biome),
                    GeneratedPageSymbolUtil.biomeWords(biomeId),
                    1
            ));
        }
    }

    private static Integer cardRankForSpecial(Identifier biomeId) {
        if (biomeId.getNamespace().equals("minecraft") && END_BIOMES.contains(biomeId.getPath())) {
            return 6;
        }
        if (biomeId.getNamespace().equals("minecraft") && NETHER_BIOMES.contains(biomeId.getPath())) {
            return 6;
        }
        return 3;
    }

    private static List<String> lootTagsForBiome(Identifier biomeId, Biome biome) {
        if (biomeId.getNamespace().equals("minecraft") && biomeId.getPath().equals("the_void")) {
            return List.of("special");
        }

        if (biomeId.getNamespace().equals("minecraft") && END_BIOMES.contains(biomeId.getPath())) {
            return List.of("end");
        }

        if (biomeId.getNamespace().equals("minecraft") && NETHER_BIOMES.contains(biomeId.getPath())) {
            return List.of("nether");
        }

        if (biome == null) {
            return List.of("neutral");
        }

        float temperature = biome.getBaseTemperature();

        if (temperature <= 0.2F) {
            return List.of("cold");
        }

        if (temperature >= 1.0F) {
            return List.of("hot");
        }

        return List.of("neutral");
    }
}