package myst.synthetic.page.loot;

import myst.synthetic.page.Page;
import myst.synthetic.page.symbol.PageSymbol;
import myst.synthetic.page.symbol.PageSymbolRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class PageLootPools {

    private static final Map<String, PageLootPool> POOLS = new HashMap<>();

    private PageLootPools() {
    }

    public static void initialize() {
        registerVillageSlantBoardPools();
    }

    private static void registerVillageSlantBoardPools() {
        register(new PageLootPool(
                "village/desert/specialty",
                1,
                3,
                List.of("desert", "trade"),
                List.of(),
                List.of("effect", "modifier")
        ));

        register(new PageLootPool(
                "village/desert/temperature_biome",
                1,
                3,
                List.of("hot", "trade"),
                List.of(),
                List.of("biome")
        ));

        register(new PageLootPool(
                "village/savanna/specialty",
                1,
                3,
                List.of("savanna", "trade"),
                List.of(),
                List.of("celestial", "modifier")
        ));

        register(new PageLootPool(
                "village/savanna/temperature_biome",
                1,
                3,
                List.of("hot", "trade"),
                List.of(),
                List.of("biome")
        ));

        register(new PageLootPool(
                "village/plains/specialty",
                1,
                3,
                List.of("plains", "trade"),
                List.of(),
                List.of("feature")
        ));

        register(new PageLootPool(
                "village/plains/temperature_biome",
                1,
                3,
                List.of("neutral", "trade"),
                List.of(),
                List.of("biome")
        ));

        register(new PageLootPool(
                "village/taiga/specialty",
                1,
                3,
                List.of("taiga", "trade"),
                List.of(),
                List.of("weather", "modifier")
        ));

        register(new PageLootPool(
                "village/taiga/temperature_biome",
                1,
                3,
                List.of("cold", "trade"),
                List.of(),
                List.of("biome")
        ));

        register(new PageLootPool(
                "village/snowy/specialty",
                1,
                3,
                List.of("snowy", "trade"),
                List.of(),
                List.of("biocontrol", "terrain", "modifier")
        ));

        register(new PageLootPool(
                "village/snowy/temperature_biome",
                1,
                3,
                List.of("cold", "trade"),
                List.of(),
                List.of("biome")
        ));
        register(new PageLootPool(
                "village/common",
                1,
                3,
                List.of("common", "trade"),
                List.of(),
                List.of()
        ));
    }

    private static void register(PageLootPool pool) {
        POOLS.put(pool.id(), pool);
    }

    public static Optional<PageSymbol> pickSymbol(String poolId, RandomSource random) {
        PageLootPool pool = POOLS.get(poolId);
        if (pool == null) {
            return Optional.empty();
        }

        return pool.pick(List.copyOf(PageSymbolRegistry.values()), random);
    }

    public static ItemStack pickPageStack(String poolId, RandomSource random) {
        Optional<PageSymbol> symbol = pickSymbol(poolId, random);
        return symbol.map(pageSymbol -> Page.createSymbolPage(pageSymbol.id()))
                .orElse(ItemStack.EMPTY);
    }

    public static boolean contains(String poolId) {
        return POOLS.containsKey(poolId);
    }

    public static PageLootPool get(String poolId) {
        return POOLS.get(poolId);
    }
}