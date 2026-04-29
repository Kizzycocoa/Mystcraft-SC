package myst.synthetic.page.symbol;

import myst.synthetic.MystcraftSyntheticCodex;
import net.minecraft.resources.Identifier;

import java.util.List;

public final class MystcraftPageSymbols {

    public static final PageSymbol DEBUG_FULL = register("debug/full_symbol", "symbol.mystcraft-sc.debug.full_symbol", "Mystcraft:SC", "debug", 0, "DEBUG", "DEBUG", "DEBUG", "DEBUG", 0);

    public static void initialize() {
        MystcraftSyntheticCodex.LOGGER.info("Registered {} Mystcraft page symbols.", PageSymbolRegistry.size());
    }

    private static PageSymbol register(
            String path,
            String translationKey,
            String origin,
            String category,
            Integer cardRank,
            String word1,
            String word2,
            String word3,
            String word4,
            Integer tested
    ) {
        return PageSymbolRegistry.register(new PageSymbol(
                Identifier.fromNamespaceAndPath("mystcraft-sc", path),
                translationKey,
                origin,
                category,
                cardRank == null ? 0 : cardRank,
                List.of(),
                List.of(word1, word2, word3, word4),
                tested == null ? 0 : tested
        ));
    }
}