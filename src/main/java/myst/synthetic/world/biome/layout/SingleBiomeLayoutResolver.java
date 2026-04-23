package myst.synthetic.world.biome.layout;

import myst.synthetic.component.AgebookDataComponent;
import myst.synthetic.page.Page;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SplittableRandom;

public final class SingleBiomeLayoutResolver {

    private static final String GENERATED_BIOME_PREFIX = "mystcraft-sc:generated/biome/";

    public Identifier resolveBiome(AgebookDataComponent agebookData, RegistryAccess registryAccess, long seed) {
        return resolveBiome(agebookData == null ? List.of() : agebookData.pages(), registryAccess, seed);
    }

    public Identifier resolveBiome(List<ItemStack> pages, RegistryAccess registryAccess, long seed) {
        Identifier fromPages = resolveReferencedBiomeFromPages(pages);
        if (fromPages != null) {
            return fromPages;
        }

        return chooseSeededFallbackBiome(registryAccess, seed);
    }

    private Identifier resolveReferencedBiomeFromPages(List<ItemStack> pages) {
        if (pages == null || pages.isEmpty()) {
            return null;
        }

        Identifier firstLooseBiome = null;

        for (int i = 0; i < pages.size(); i++) {
            ItemStack stack = pages.get(i);
            if (stack == null || stack.isEmpty() || !Page.isSymbolPage(stack)) {
                continue;
            }

            Identifier symbolId = Page.getSymbol(stack);
            Identifier looseBiome = decodeGeneratedBiomeSymbol(symbolId);
            if (looseBiome != null && firstLooseBiome == null) {
                firstLooseBiome = looseBiome;
            }

            if (!isSingleBiomeControl(symbolId)) {
                continue;
            }

            Identifier referencedBiome = findPreviousBiomeSymbol(pages, i);
            if (referencedBiome != null) {
                return referencedBiome;
            }
        }

        return firstLooseBiome;
    }

    private Identifier findPreviousBiomeSymbol(List<ItemStack> pages, int fromExclusive) {
        for (int i = fromExclusive - 1; i >= 0; i--) {
            ItemStack previous = pages.get(i);
            if (previous == null || previous.isEmpty() || !Page.isSymbolPage(previous)) {
                continue;
            }

            Identifier symbolId = Page.getSymbol(previous);
            Identifier biomeId = decodeGeneratedBiomeSymbol(symbolId);
            if (biomeId != null) {
                return biomeId;
            }
        }

        return null;
    }

    private Identifier chooseSeededFallbackBiome(RegistryAccess registryAccess, long seed) {
        var biomeLookup = registryAccess.lookupOrThrow(Registries.BIOME);

        List<Identifier> biomeIds = new ArrayList<>();
        biomeLookup.listElementIds().forEach(key -> biomeIds.add(key.identifier()));

        if (biomeIds.isEmpty()) {
            return Identifier.fromNamespaceAndPath("minecraft", "plains");
        }

        biomeIds.sort(Comparator.comparing(Identifier::toString));
        SplittableRandom random = new SplittableRandom(seed);
        return biomeIds.get(random.nextInt(biomeIds.size()));
    }

    public static boolean isSingleBiomeControl(Identifier symbolId) {
        if (symbolId == null) {
            return false;
        }

        String path = symbolId.getPath();
        return path.equals("bioconsingle")
                || path.equals("biome_single")
                || path.equals("biome/single")
                || path.endsWith("/bioconsingle")
                || path.endsWith("/biome_single")
                || path.endsWith("/single_biome");
    }

    public static Identifier decodeGeneratedBiomeSymbol(Identifier symbolId) {
        if (symbolId == null) {
            return null;
        }

        String raw = symbolId.toString();
        if (!raw.startsWith(GENERATED_BIOME_PREFIX)) {
            return null;
        }

        String remainder = raw.substring(GENERATED_BIOME_PREFIX.length());
        int slash = remainder.indexOf('/');
        if (slash <= 0 || slash >= remainder.length() - 1) {
            return null;
        }

        String namespace = remainder.substring(0, slash);
        String path = remainder.substring(slash + 1);
        if (namespace.isBlank() || path.isBlank()) {
            return null;
        }

        return Identifier.fromNamespaceAndPath(namespace, path);
    }
}