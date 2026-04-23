package myst.synthetic.world.age;

import myst.synthetic.component.AgebookDataComponent;
import myst.synthetic.page.Page;
import myst.synthetic.world.biome.layout.BiomeLayoutKind;
import myst.synthetic.world.biome.layout.SingleBiomeLayoutResolver;
import myst.synthetic.world.terrain.BedrockProfile;
import myst.synthetic.world.terrain.FlatTerrainResolver;
import myst.synthetic.world.terrain.FlatTerrainSettings;
import myst.synthetic.world.terrain.TerrainKind;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class AgeSpecCompiler {

    private final SingleBiomeLayoutResolver singleBiomeLayoutResolver;
    private final FlatTerrainResolver flatTerrainResolver;

    public AgeSpecCompiler() {
        this(new SingleBiomeLayoutResolver(), new FlatTerrainResolver());
    }

    public AgeSpecCompiler(
            SingleBiomeLayoutResolver singleBiomeLayoutResolver,
            FlatTerrainResolver flatTerrainResolver
    ) {
        this.singleBiomeLayoutResolver = singleBiomeLayoutResolver;
        this.flatTerrainResolver = flatTerrainResolver;
    }

    public AgeSpec compile(AgebookDataComponent agebookData, RegistryAccess registryAccess, long seed) {
        String displayName = agebookData == null ? "" : agebookData.displayName();
        List<ItemStack> pages = agebookData == null ? List.of() : agebookData.pages();

        AgeSpec.Builder builder = new AgeSpec.Builder(seed, displayName)
                .biomeLayout(BiomeLayoutKind.SINGLE_BIOME)
                .terrain(TerrainKind.FLAT)
                .bedrockProfile(BedrockProfile.VANILLA_FLOOR);

        List<Identifier> ignoredSymbols = new ArrayList<>();

        if (pages != null && !pages.isEmpty()) {
            for (ItemStack page : pages) {
                if (page == null || page.isEmpty() || !Page.isSymbolPage(page)) {
                    continue;
                }

                Identifier symbolId = Page.getSymbol(page);
                if (symbolId == null) {
                    continue;
                }

                if (SingleBiomeLayoutResolver.decodeGeneratedBiomeSymbol(symbolId) != null) {
                    continue;
                }

                if (SingleBiomeLayoutResolver.isSingleBiomeControl(symbolId)) {
                    builder.biomeLayout(BiomeLayoutKind.SINGLE_BIOME);
                    continue;
                }

                if (isFlatTerrainSymbol(symbolId)) {
                    builder.terrain(TerrainKind.FLAT);
                    continue;
                }

                ignoredSymbols.add(symbolId);
            }
        }

        Identifier resolvedBiome = this.singleBiomeLayoutResolver.resolveBiome(
                pages == null ? List.of() : pages,
                registryAccess,
                seed
        );
        builder.resolvedBiome(resolvedBiome);
        builder.ignoredSymbols(ignoredSymbols);

        AgeSpec provisional = builder.build();

        FlatTerrainSettings flatTerrain = this.flatTerrainResolver.resolve(provisional, registryAccess);
        return provisional.toBuilder()
                .resolvedGroundLevel(flatTerrain.groundLevel())
                .bedrockProfile(flatTerrain.bedrockProfile())
                .build();
    }

    private static boolean isFlatTerrainSymbol(Identifier symbolId) {
        if (symbolId == null) {
            return false;
        }

        String path = symbolId.getPath();
        return path.equals("terrain_flat")
                || path.equals("flat")
                || path.endsWith("/terrain_flat")
                || path.endsWith("/flat");
    }
}