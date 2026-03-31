package myst.synthetic.page.symbol;

import myst.synthetic.MystcraftSyntheticCodex;
import net.minecraft.resources.Identifier;

import java.util.List;

public final class MystcraftPageSymbols {

    public static final PageSymbol DEBUG_TEST_SYMBOL = register(
            "debug/test_symbol",
            "symbol.mystcraft-sc.debug.test_symbol",
            0,
            "Test",
            "Debug",
            "Page",
            "Symbol"
    );

    public static final PageSymbol SUNS_NORMAL = register(
            "celestial/suns_normal",
            "symbol.mystcraft-sc.celestial.suns_normal",
            2,
            "Celestial",
            "Image",
            "Stimulate",
            "Energy"
    );

    public static final PageSymbol STARS_NORMAL = register(
            "celestial/stars_normal",
            "symbol.mystcraft-sc.celestial.stars_normal",
            1,
            "Celestial",
            "Harmony",
            "Ethereal",
            "Order"
    );

    public static final PageSymbol WEATHER_CLEAR = register(
            "weather/clear",
            "symbol.mystcraft-sc.weather.clear",
            0,
            "Contradict",
            "Transform",
            "Change",
            "Void"
    );

    public static final PageSymbol WEATHER_RAIN = register(
            "weather/rain",
            "symbol.mystcraft-sc.weather.rain",
            3,
            "Sustain",
            "Static",
            "Rebirth",
            "Growth"
    );

    public static final PageSymbol VILLAGES = register(
            "structure/villages",
            "symbol.mystcraft-sc.structure.villages",
            3,
            "Civilization",
            "Society",
            "Harmony",
            "Nurture"
    );

    public static final PageSymbol DENSE_ORES = register(
            "terrain/dense_ores",
            "symbol.mystcraft-sc.terrain.dense_ores",
            5,
            "Survival",
            "Stimulate",
            "Machine",
            "Chaos"
    );

    private MystcraftPageSymbols() {
    }

    public static void initialize() {
        MystcraftSyntheticCodex.LOGGER.info("Registered {} Mystcraft page symbols.", PageSymbolRegistry.size());
    }

    private static PageSymbol register(
            String path,
            String translationKey,
            int cardRank,
            String word1,
            String word2,
            String word3,
            String word4
    ) {
        return PageSymbolRegistry.register(new PageSymbol(
                Identifier.fromNamespaceAndPath("mystcraft-sc", path),
                translationKey,
                cardRank,
                List.of(word1, word2, word3, word4)
        ));
    }
}