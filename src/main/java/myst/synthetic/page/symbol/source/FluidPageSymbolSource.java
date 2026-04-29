package myst.synthetic.page.symbol.source;

import myst.synthetic.page.symbol.PageSymbol;
import myst.synthetic.page.symbol.PageSymbolRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

import java.util.List;

public final class FluidPageSymbolSource implements PageSymbolSource {

    @Override
    public void registerSymbols() {
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            Identifier fluidId = BuiltInRegistries.FLUID.getKey(fluid);
            if (fluidId == null) {
                continue;
            }

            if (!(fluid instanceof FlowingFluid)) {
                continue;
            }

            if (fluidId.getPath().startsWith("flowing_")) {
                continue;
            }

            PageSymbolRegistry.register(new PageSymbol(
                    Identifier.fromNamespaceAndPath(
                            "mystcraft-sc",
                            "generated/fluid/" + fluidId.getNamespace() + "/" + fluidId.getPath()
                    ),
                    null,
                    "Minecraft",
                    "fluid",
                    cardRankForFluid(fluidId),
                    lootTagsForFluid(fluidId),
                    GeneratedPageSymbolUtil.fluidWords(fluidId),
                    0
            ));
        }
    }

    private static int cardRankForFluid(Identifier fluidId) {
        return isInk(fluidId) ? 6 : 2;
    }

    private static List<String> lootTagsForFluid(Identifier fluidId) {
        if (isInk(fluidId)) {
            return List.of("special");
        }

        if (fluidId.getNamespace().equals("minecraft") && fluidId.getPath().equals("water")) {
            return List.of("cold");
        }

        if (fluidId.getNamespace().equals("minecraft") && fluidId.getPath().equals("lava")) {
            return List.of("hot");
        }

        return List.of("neutral");
    }

    private static boolean isInk(Identifier fluidId) {
        return fluidId.getNamespace().equals("mystcraft-sc")
                && fluidId.getPath().equals("black_ink");
    }
}