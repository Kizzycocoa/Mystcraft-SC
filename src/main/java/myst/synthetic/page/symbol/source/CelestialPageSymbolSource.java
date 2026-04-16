package myst.synthetic.page.symbol.source;

import myst.synthetic.page.symbol.PageSymbol;
import myst.synthetic.page.symbol.PageSymbolRegistry;
import net.minecraft.resources.Identifier;

import java.util.List;

public final class CelestialPageSymbolSource implements PageSymbolSource {

    @Override
    public void registerSymbols() {
        register("sun", 2, "Celestial", "Image", "Stimulate", "Energy");
        register("moon", 1, "Celestial", "Image", "Cycle", "Wisdom");
        register("stars", 1, "Celestial", "Harmony", "Ethereal", "Order");
        register("twinkling_stars", 1, "Celestial", "Harmony", "Ethereal", "Entropy");
        register("dark_sun", 1, "Celestial", "Void", "Inhibit", "Energy");
        register("dark_moon", 1, "Celestial", "Void", "Inhibit", "Wisdom");
        register("dark_stars", 1, "Celestial", "Void", "Inhibit", "Order");
        register("sky", 1, "Image", "Celestial", "Harmony", "Weave");
        register("night_sky", 1, "Image", "Celestial", "Contradict", "Weave");
        register("rainbow", 1, "Celestial", "Image", "Harmony", "Balance");
        register("no_horizon", 1, "Celestial", "Inhibit", "Image", "Void");
    }

    private static void register(String path, int rank, String w1, String w2, String w3, String w4) {
        PageSymbolRegistry.register(new PageSymbol(
                Identifier.fromNamespaceAndPath("mystcraft-sc", "generated/celestial/" + path),
                null,
                "celestial",
                rank,
                List.of(w1, w2, w3, w4),
                0
        ));
    }
}