package myst.synthetic.page.symbol.source;

import myst.synthetic.page.symbol.PageSymbol;
import myst.synthetic.page.symbol.PageSymbolRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

public final class BiomePageSymbolSource {

    public void registerSymbols(RegistryAccess registryAccess) {
        var biomeRegistry = registryAccess.lookupOrThrow(Registries.BIOME);

        for (var biomeEntry : biomeRegistry.listElements().toList()) {
            Identifier biomeId = biomeEntry.key().identifier();

            PageSymbolRegistry.register(new PageSymbol(
                    Identifier.fromNamespaceAndPath(
                            "mystcraft-sc",
                            "generated/biome/" + biomeId.getNamespace() + "/" + biomeId.getPath()
                    ),
                    null,
                    1,
                    GeneratedPageSymbolUtil.biomeWords(biomeId)
            ));
        }
    }
}