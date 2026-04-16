package myst.synthetic.page.symbol.source;

import myst.synthetic.page.symbol.PageSymbol;
import myst.synthetic.page.symbol.PageSymbolRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

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
                    "fluid",
                    1,
                    GeneratedPageSymbolUtil.fluidWords(fluidId),
                    0
            ));
        }
    }
}