package myst.synthetic.page.symbol.source;

import myst.synthetic.page.symbol.PageSymbol;
import myst.synthetic.page.symbol.PageSymbolRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public final class BiomePageSymbolSource {

    public void registerSymbols(RegistryAccess registryAccess) {
        var biomeRegistry = registryAccess.lookupOrThrow(Registries.BIOME);

        for (ResourceKey<Biome> biomeKey : biomeRegistry.listElementIds().toList()) {
            Identifier biomeId = biomeKey.identifier();

            PageSymbolRegistry.register(new PageSymbol(
                    Identifier.fromNamespaceAndPath(
                            "mystcraft-sc",
                            "generated/biome/" + biomeId.getNamespace() + "/" + biomeId.getPath()
                    ),
                    null,
                    "biome",
                    1,
                    GeneratedPageSymbolUtil.biomeWords(biomeId),
                    0
            ));
        }
    }
}