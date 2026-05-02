package myst.synthetic.world.age;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import myst.synthetic.MystcraftSyntheticCodex;
import myst.synthetic.component.AgebookDataComponent;
import myst.synthetic.config.MystcraftConfig;
import myst.synthetic.page.Page;
import myst.synthetic.page.PageValue;
import myst.synthetic.page.symbol.PageSymbol;
import myst.synthetic.page.symbol.PageSymbolRegistry;
import myst.synthetic.world.biome.layout.BiomeLayoutKind;
import myst.synthetic.world.biome.layout.SingleBiomeLayoutResolver;
import myst.synthetic.world.terrain.BedrockProfile;
import myst.synthetic.world.terrain.FlatTerrainResolver;
import myst.synthetic.world.terrain.FlatTerrainSettings;
import myst.synthetic.world.terrain.TerrainKind;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AgeDataFileCompiler {

    public static final int AGE_DATA_VERSION = 1;
    public static final int COMPILER_VERSION = 1;

    private final SingleBiomeLayoutResolver singleBiomeLayoutResolver = new SingleBiomeLayoutResolver();
    private final FlatTerrainResolver flatTerrainResolver = new FlatTerrainResolver();

    public Result compile(
            AgebookDataComponent agebookData,
            RegistryAccess registryAccess,
            long seed,
            String dimensionUid,
            String levelId,
            @Nullable String bookUuid
    ) {
        boolean trace = MystcraftConfig.getBoolean(
                MystcraftConfig.CATEGORY_DEBUG,
                "age_compiler.trace_pages",
                true
        );

        String title = agebookData == null ? "" : agebookData.displayName();
        List<ItemStack> pages = agebookData == null ? List.of() : agebookData.pages();

        if (trace) {
            MystcraftSyntheticCodex.LOGGER.info(
                    "[MystAgeCompiler] Starting Age data compile: dim={}, level={}, seed={}, title='{}', pages={}",
                    dimensionUid,
                    levelId,
                    seed,
                    title,
                    pages.size()
            );
        }

        List<PageToken> tokens = tokenize(pages, trace);

        Draft draft = new Draft(seed, title, dimensionUid, levelId, bookUuid);

        int cursor = 0;
        if (!tokens.isEmpty() && tokens.get(0).kind == TokenKind.LINK_PANEL) {
            cursor = 1;
            if (trace) {
                MystcraftSyntheticCodex.LOGGER.info("[MystAgeCompiler] Page 0 is a valid linking panel.");
            }
        } else {
            MystcraftSyntheticCodex.LOGGER.warn(
                    "[MystAgeCompiler] Descriptive book did not begin with a link panel. This should not normally happen."
            );
        }

        RegionParseResult overworld = parseRegion(tokens, cursor, RegionKind.OVERWORLD, trace);
        if (overworld.region != null) {
            draft.overworld = overworld.region;
            cursor = overworld.nextIndex;
        } else {
            MystcraftSyntheticCodex.LOGGER.warn(
                    "[MystAgeCompiler] No valid overworld block found. Completing with fallback flat single-biome region."
            );
            draft.overworld = RegionDraft.fallbackOverworld(
                    singleBiomeLayoutResolver.resolveBiome(pages, registryAccess, seed)
            );
        }

        RegionParseResult underworld = parseRegion(tokens, cursor, RegionKind.UNDERWORLD, trace);
        if (underworld.region != null) {
            draft.underworld = underworld.region;
            cursor = underworld.nextIndex;
        }

        parseGlobalTail(tokens, cursor, draft.global, trace);

        JsonObject document = writeDocument(draft);

        Identifier generatorBiome = draft.overworld.firstBiomeOrFallback(
                singleBiomeLayoutResolver.resolveBiome(pages, registryAccess, seed)
        );

        AgeSpec provisional = new AgeSpec.Builder(seed, title)
                .dimensionUid(dimensionUid)
                .biomeLayout(BiomeLayoutKind.SINGLE_BIOME)
                .terrain(TerrainKind.FLAT)
                .resolvedBiome(generatorBiome)
                .bedrockProfile(BedrockProfile.VANILLA_FLOOR)
                .build();

        FlatTerrainSettings flatTerrain = this.flatTerrainResolver.resolve(provisional, registryAccess);
        AgeSpec spec = provisional.toBuilder()
                .resolvedGroundLevel(flatTerrain.groundLevel())
                .bedrockProfile(flatTerrain.bedrockProfile())
                .build();

        if (trace) {
            MystcraftSyntheticCodex.LOGGER.info(
                    "[MystAgeCompiler] Finished Age data compile: overworld={}, underworld={}, generatorBiome={}, ground={}",
                    draft.overworld == null ? "missing" : draft.overworld.describe(),
                    draft.underworld == null ? "absent" : draft.underworld.describe(),
                    generatorBiome,
                    spec.resolvedGroundLevel()
            );
        }

        return new Result(document, spec);
    }

    private List<PageToken> tokenize(List<ItemStack> pages, boolean trace) {
        List<PageToken> tokens = new ArrayList<>();

        for (int i = 0; i < pages.size(); i++) {
            ItemStack stack = pages.get(i);

            if (stack == null || stack.isEmpty()) {
                tokens.add(PageToken.empty(i));
                continue;
            }

            if (Page.isLinkPanel(stack)) {
                tokens.add(PageToken.linkPanel(i));
                continue;
            }

            Identifier symbolId = Page.getSymbol(stack);
            if (symbolId == null) {
                tokens.add(PageToken.empty(i));
                continue;
            }

            PageSymbol symbol = PageSymbolRegistry.get(symbolId);
            PageValue value = Page.getValue(stack);

            String category = symbol == null ? "" : symbol.category();
            String valueText = valueToText(value);
            Float scalar = value == null ? null : value.scalarOrNull();
            List<Float> vector = value == null ? null : value.vectorOrNull();

            PageToken token = new PageToken(
                    i,
                    TokenKind.SYMBOL,
                    symbolId,
                    category == null ? "" : category,
                    valueText,
                    scalar,
                    vector
            );

            tokens.add(token);

            if (trace) {
                MystcraftSyntheticCodex.LOGGER.info(
                        "[MystAgeCompiler] token page={} id={} category='{}' value='{}'",
                        i,
                        symbolId,
                        token.category,
                        token.valueText
                );
            }
        }

        return tokens;
    }

    @Nullable
    private static String valueToText(@Nullable PageValue value) {
        if (value == null) {
            return null;
        }
        if (value.textOrNull() != null) {
            return value.textOrNull();
        }
        if (value.scalarOrNull() != null) {
            return Float.toString(value.scalarOrNull());
        }
        if (value.vectorOrNull() != null) {
            return value.vectorOrNull().toString();
        }
        return null;
    }

    private RegionParseResult parseRegion(
            List<PageToken> tokens,
            int startIndex,
            RegionKind kind,
            boolean trace
    ) {
        PendingModifiers pending = new PendingModifiers();
        RegionDraft region = new RegionDraft();
        region.biomeLayout = "single";
        region.terrain = kind == RegionKind.OVERWORLD ? "flat" : "cave";

        int index = startIndex;
        boolean sawAnyRegionContent = false;

        while (index < tokens.size()) {
            PageToken token = tokens.get(index);

            if (token.kind != TokenKind.SYMBOL) {
                index++;
                continue;
            }

            if (trace) {
                MystcraftSyntheticCodex.LOGGER.info(
                        "[MystAgeCompiler] {} page={} before pending={}",
                        kind.name().toLowerCase(Locale.ROOT),
                        token.pageIndex,
                        pending.describe()
                );
            }

            Identifier biomeId = SingleBiomeLayoutResolver.decodeGeneratedBiomeSymbol(token.symbolId);
            if (biomeId != null) {
                BiomeDraft biome = region.pendingBiomeData.copyForBiome(biomeId.toString());
                applyPendingBiomeModifiers(pending, biome, trace, token);
                region.biomes.add(biome);
                region.pendingBiomeData.clearScopedData();

                sawAnyRegionContent = true;
                if (trace) {
                    MystcraftSyntheticCodex.LOGGER.info(
                            "[MystAgeCompiler] {} consumed biome {} from page {}. biomeData={}",
                            kind.name().toLowerCase(Locale.ROOT),
                            biomeId,
                            token.pageIndex,
                            biome.toJson()
                    );
                }
                index++;
                continue;
            }

            if (isBiomeLayout(token)) {
                region.biomeLayout = normalizeBiomeLayout(token);
                sawAnyRegionContent = true;
                if (trace) {
                    MystcraftSyntheticCodex.LOGGER.info(
                            "[MystAgeCompiler] {} biome_layout='{}' from page {}.",
                            kind.name().toLowerCase(Locale.ROOT),
                            region.biomeLayout,
                            token.pageIndex
                    );
                }
                index++;
                continue;
            }

            if (isTerrain(token)) {
                String terrainValue = token.valueText == null ? token.path() : token.valueText;

                if (kind == RegionKind.OVERWORLD && !isUnderworldTerrainValue(terrainValue)) {
                    region.terrain = normalizeOverworldTerrain(terrainValue);
                    sawAnyRegionContent = true;
                    if (trace) {
                        MystcraftSyntheticCodex.LOGGER.info(
                                "[MystAgeCompiler] Closed overworld block at page {} with terrain='{}'.",
                                token.pageIndex,
                                region.terrain
                        );
                    }
                    return new RegionParseResult(region, index + 1);
                }

                if (kind == RegionKind.UNDERWORLD && isUnderworldTerrainValue(terrainValue)) {
                    region.terrain = normalizeUnderworldTerrain(terrainValue);
                    sawAnyRegionContent = true;
                    if (trace) {
                        MystcraftSyntheticCodex.LOGGER.info(
                                "[MystAgeCompiler] Closed underworld block at page {} with terrain='{}'.",
                                token.pageIndex,
                                region.terrain
                        );
                    }
                    return new RegionParseResult(region, index + 1);
                }

                if (kind == RegionKind.UNDERWORLD && !sawAnyRegionContent) {
                    return new RegionParseResult(null, startIndex);
                }
            }

            if (tryConsumeScopedToken(token, pending, region.pendingBiomeScope(), trace)) {
                sawAnyRegionContent = true;
                index++;
                continue;
            }

            if (isModifier(token)) {
                pending.add(token);
                sawAnyRegionContent = true;
                if (trace) {
                    MystcraftSyntheticCodex.LOGGER.info(
                            "[MystAgeCompiler] {} cached modifier page={} id={} -> pending={}",
                            kind.name().toLowerCase(Locale.ROOT),
                            token.pageIndex,
                            token.symbolId,
                            pending.describe()
                    );
                }
                index++;
                continue;
            }

            if (kind == RegionKind.UNDERWORLD && !sawAnyRegionContent) {
                return new RegionParseResult(null, startIndex);
            }

            MystcraftSyntheticCodex.LOGGER.warn(
                    "[MystAgeCompiler] Unhandled page {} inside {} block: id={}, category='{}', value='{}'",
                    token.pageIndex,
                    kind.name().toLowerCase(Locale.ROOT),
                    token.symbolId,
                    token.category,
                    token.valueText
            );

            index++;
        }

        if (kind == RegionKind.UNDERWORLD) {
            return new RegionParseResult(null, startIndex);
        }

        return new RegionParseResult(region, index);
    }

    private void parseGlobalTail(List<PageToken> tokens, int startIndex, GlobalDraft global, boolean trace) {
        PendingModifiers pending = new PendingModifiers();

        for (int index = startIndex; index < tokens.size(); index++) {
            PageToken token = tokens.get(index);

            if (token.kind != TokenKind.SYMBOL) {
                continue;
            }

            if (trace) {
                MystcraftSyntheticCodex.LOGGER.info(
                        "[MystAgeCompiler] global page={} before pending={}",
                        token.pageIndex,
                        pending.describe()
                );
            }

            if (isClearMods(token)) {
                if (trace) {
                    MystcraftSyntheticCodex.LOGGER.info(
                            "[MystAgeCompiler] clear_mods at page {} cleared pending={}",
                            token.pageIndex,
                            pending.describe()
                    );
                }
                pending.clear();
                continue;
            }

            if (isHorizonColor(token)) {
                ColorObject horizon = pending.consumeColorObject("horizon_color", trace, token);
                if (horizon != null) {
                    pending.horizon = horizon;
                }
                continue;
            }

            if (isGradient(token)) {
                ColorObject gradient = pending.consumeGradient(trace, token);
                if (gradient != null) {
                    pending.addGradient(gradient);
                }
                continue;
            }

            if (isCelestial(token)) {
                consumeCelestial(token, pending, global, trace);
                continue;
            }

            if (tryConsumeScopedToken(token, pending, global, trace)) {
                continue;
            }

            if (isModifier(token)) {
                pending.add(token);
                if (trace) {
                    MystcraftSyntheticCodex.LOGGER.info(
                            "[MystAgeCompiler] global cached modifier page={} id={} -> pending={}",
                            token.pageIndex,
                            token.symbolId,
                            pending.describe()
                    );
                }
                continue;
            }

            MystcraftSyntheticCodex.LOGGER.warn(
                    "[MystAgeCompiler] Unhandled global page {}: id={}, category='{}', value='{}'",
                    token.pageIndex,
                    token.symbolId,
                    token.category,
                    token.valueText
            );
        }

        if (!pending.isEmpty()) {
            MystcraftSyntheticCodex.LOGGER.warn(
                    "[MystAgeCompiler] Compile ended with dangling modifiers: {}",
                    pending.describe()
            );
        }
    }

    private boolean tryConsumeScopedToken(
            PageToken token,
            PendingModifiers pending,
            ScopedDraft scope,
            boolean trace
    ) {
        if (isRecolorConsumer(token)) {
            String channel = normalizeRecolorChannel(token);
            ColorObject color = pending.consumeColorObject("recolor_" + channel, trace, token);
            if (color != null) {
                scope.recolors().put(channel, color);
                if (trace) {
                    MystcraftSyntheticCodex.LOGGER.info(
                            "[MystAgeCompiler] page {} wrote recolor.{} = {}",
                            token.pageIndex,
                            channel,
                            color.describe()
                    );
                }
            }
            return true;
        }

        if (isWeatherType(token)) {
            scope.setWeatherType(normalizeWeatherType(token));
            return true;
        }

        if (isWeatherLength(token)) {
            scope.setWeatherSpeed(normalizeWeatherLength(token));
            return true;
        }

        if (isLighting(token)) {
            if (scope instanceof GlobalDraft global) {
                global.lighting = normalizeSimpleValue(token);
            }
            return true;
        }

        if (isCloudHeight(token)) {
            if (scope instanceof GlobalDraft global) {
                if ("noclouds".equalsIgnoreCase(nullSafe(token.valueText))) {
                    global.cloudHeightNoClouds = true;
                } else if (token.scalar != null) {
                    global.cloudHeight = Math.round(token.scalar);
                }
            }
            return true;
        }

        if (isFeature(token)) {
            FeatureObject feature = buildFeature(token, pending, trace);
            if (feature != null) {
                if (isFormationFeature(token)) {
                    scope.formations().add(feature);
                } else {
                    scope.structures().add(feature);
                }
            }
            return true;
        }

        return false;
    }

    private void applyPendingBiomeModifiers(
            PendingModifiers pending,
            BiomeDraft biome,
            boolean trace,
            PageToken consumer
    ) {
        if (!pending.isEmpty()) {
            MystcraftSyntheticCodex.LOGGER.warn(
                    "[MystAgeCompiler] Biome page {} consumed after loose pending modifiers remained: {}. " +
                            "Only explicit consumers before the biome currently write data.",
                    consumer.pageIndex,
                    pending.describe()
            );
        }
    }

    private void consumeCelestial(
            PageToken token,
            PendingModifiers pending,
            GlobalDraft global,
            boolean trace
    ) {
        JsonObject object = new JsonObject();
        object.addProperty("type", celestialType(token));

        Float direction = pending.consumeNumeric(ModifierKind.DIRECTION, trace, token);
        Float angle = pending.consumeNumeric(ModifierKind.ANGLE, trace, token);
        Float length = pending.consumeNumeric(ModifierKind.LENGTH, trace, token);
        Float tilt = pending.consumeNumeric(ModifierKind.TILT, trace, token);
        Float size = pending.consumeNumeric(ModifierKind.SIZE, trace, token);

        ColorObject color = pending.consumeColorObject("celestial_color", trace, token);

        if (direction != null) {
            addNumber(object, "direction", direction);
        }

        if (angle != null && !token.category.equals("celestial/moon")) {
            addNumber(object, "angle", angle);
        }

        if (length != null) {
            addNumber(object, "length", length);
        }

        if (tilt != null) {
            addNumber(object, "tilt", tilt);
        }

        if (size != null) {
            addNumber(object, "size", size);
        }
        if (color != null) {
            object.add("color", color.toJson());
        }
        if (pending.horizon != null) {
            object.add("horizon", pending.horizon.toJson());
            pending.horizon = null;
        }

        if (token.category.equals("celestial/moon") && angle != null) {
            addNumber(object, "phase", angle);
        }

        switch (token.category) {
            case "celestial/sun" -> global.suns.add(object);
            case "celestial/moon" -> global.moons.add(object);
            case "celestial/stars" -> global.stars.add(object);
            default -> global.others.add(object);
        }

        if (trace) {
            MystcraftSyntheticCodex.LOGGER.info(
                    "[MystAgeCompiler] page {} consumed celestial {} -> pending={}",
                    token.pageIndex,
                    token.symbolId,
                    pending.describe()
            );
        }
    }

    @Nullable
    private FeatureObject buildFeature(PageToken token, PendingModifiers pending, boolean trace) {
        String featureType = normalizeSimpleValue(token);
        if (featureType.isBlank()) {
            featureType = token.path();
        }

        FeatureObject feature = new FeatureObject(featureType);

        Identifier block = pending.consumeBlock(trace, token);
        if (block != null) {
            feature.block = block.toString();
        }

        Float size = pending.consumeNumeric(ModifierKind.SIZE, trace, token);
        if (size != null) {
            feature.size = normalizeNumber(size);
        }

        return feature;
    }

    private JsonObject writeDocument(Draft draft) {
        JsonObject root = new JsonObject();

        root.addProperty("age_data_version", AGE_DATA_VERSION);
        root.addProperty("dimension_uid", draft.dimensionUid);
        root.addProperty("level_id", draft.levelId);
        root.addProperty("seed", draft.seed);
        root.addProperty("book_uuid", draft.bookUuid == null ? "" : draft.bookUuid);
        root.addProperty("book_title", draft.title);
        root.addProperty("compiler_version", COMPILER_VERSION);

        JsonObject global = draft.global.toJson();
        root.add("global", global);

        JsonObject regions = new JsonObject();
        regions.add("overworld", draft.overworld.toJson());
        if (draft.underworld != null) {
            regions.add("underworld", draft.underworld.toJson());
        }
        root.add("regions", regions);

        return root;
    }

    private static boolean isModifier(PageToken token) {
        return modifierKind(token) != null;
    }

    @Nullable
    private static ModifierKind modifierKind(PageToken token) {
        if ("color".equals(token.category)) {
            return ModifierKind.COLOR;
        }
        if (token.category.startsWith("block/")) {
            return ModifierKind.BLOCK;
        }
        return switch (token.category) {
            case "modifier/direction" -> ModifierKind.DIRECTION;
            case "modifier/tilt" -> ModifierKind.TILT;
            case "modifier/size" -> ModifierKind.SIZE;
            case "modifier/length" -> ModifierKind.LENGTH;
            case "modifier/angle" -> ModifierKind.ANGLE;
            default -> null;
        };
    }

    private static boolean isRecolorConsumer(PageToken token) {
        return "recolor".equals(token.category);
    }

    private static String normalizeRecolorChannel(PageToken token) {
        String value = normalizeSimpleValue(token);
        if (value.endsWith("_natural")) {
            value = value.substring(0, value.length() - "_natural".length());
        }
        return value;
    }

    private static boolean isWeatherType(PageToken token) {
        return "weather/type".equals(token.category);
    }

    private static boolean isWeatherLength(PageToken token) {
        return "weather/length".equals(token.category);
    }

    private static boolean isLighting(PageToken token) {
        return "lighting".equals(token.category);
    }

    private static boolean isCloudHeight(PageToken token) {
        return "cloud".equals(token.category);
    }

    private static boolean isFeature(PageToken token) {
        return token.category.startsWith("feature/");
    }

    private static boolean isFormationFeature(PageToken token) {
        return "feature/large".equals(token.category)
                && ("tendrils".equals(normalizeSimpleValue(token))
                || "huge_trees".equals(normalizeSimpleValue(token))
                || "gigantic_trees".equals(normalizeSimpleValue(token))
                || "float_islands".equals(normalizeSimpleValue(token))
                || "spheres".equals(normalizeSimpleValue(token)));
    }

    private static boolean isBiomeLayout(PageToken token) {
        return "biocontrol".equals(token.category);
    }

    private static String normalizeBiomeLayout(PageToken token) {
        String value = normalizeSimpleValue(token);
        return value.isBlank() ? "single" : value;
    }

    private static boolean isTerrain(PageToken token) {
        return "terrain".equals(token.category);
    }

    private static boolean isUnderworldTerrainValue(String value) {
        String normalized = nullSafe(value).toLowerCase(Locale.ROOT);
        return normalized.startsWith("underworld");
    }

    private static String normalizeOverworldTerrain(String value) {
        String normalized = nullSafe(value).toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "normal" -> "normal";
            case "amplified" -> "amplified";
            case "nether" -> "nether";
            case "end" -> "end";
            case "void" -> "void";
            case "island" -> "island";
            case "skylands" -> "skylands";
            case "maze" -> "maze";
            case "backrooms" -> "backrooms";
            default -> "flat";
        };
    }

    private static String normalizeUnderworldTerrain(String value) {
        String normalized = nullSafe(value).toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "underworldvoid" -> "void";
            case "underworldstalactites" -> "stalactites";
            case "underworldcore" -> "core";
            case "underworldflat" -> "flat";
            default -> "cave";
        };
    }

    private static boolean isClearMods(PageToken token) {
        return "misc".equals(token.category) && "clear_mods".equals(token.valueText);
    }

    private static boolean isGradient(PageToken token) {
        return "misc".equals(token.category) && "gradient_color".equals(token.valueText);
    }

    private static boolean isHorizonColor(PageToken token) {
        return "misc".equals(token.category) && "horizon_color".equals(token.valueText);
    }

    private static boolean isCelestial(PageToken token) {
        return token.category.startsWith("celestial/");
    }

    private static String celestialType(PageToken token) {
        String value = normalizeSimpleValue(token);

        if (value.startsWith("sun_")) {
            value = value.substring("sun_".length());
        } else if (value.startsWith("moon_")) {
            value = value.substring("moon_".length());
        } else if (value.startsWith("stars_")) {
            value = value.substring("stars_".length());
        } else if (value.isBlank()) {
            value = token.path();
        }

        if ("natural".equals(value)) {
            return "normal";
        }

        return value;
    }

    private static String normalizeWeatherType(PageToken token) {
        String value = normalizeSimpleValue(token);
        return "standard".equals(value) ? "normal" : value;
    }

    private static String normalizeWeatherLength(PageToken token) {
        String value = normalizeSimpleValue(token);
        return switch (value) {
            case "standard" -> "normal";
            default -> value;
        };
    }

    private static String normalizeSimpleValue(PageToken token) {
        String value = token.valueText == null ? "" : token.valueText.trim();
        return value.toLowerCase(Locale.ROOT);
    }

    private static Number normalizeNumber(float value) {
        if (Math.abs(value - Math.round(value)) < 0.0001F) {
            return Math.round(value);
        }

        return value;
    }

    private static void addNumber(JsonObject object, String key, float value) {
        object.addProperty(key, normalizeNumber(value));
    }

    private static String nullSafe(@Nullable String value) {
        return value == null ? "" : value;
    }

    public record Result(JsonObject document, AgeSpec spec) {
    }

    private enum TokenKind {
        EMPTY,
        LINK_PANEL,
        SYMBOL
    }

    private enum RegionKind {
        OVERWORLD,
        UNDERWORLD
    }

    private enum ModifierKind {
        COLOR,
        DIRECTION,
        TILT,
        SIZE,
        LENGTH,
        ANGLE,
        BLOCK,
        GRADIENT
    }

    private record RegionParseResult(@Nullable RegionDraft region, int nextIndex) {
    }

    private record PageToken(
            int pageIndex,
            TokenKind kind,
            @Nullable Identifier symbolId,
            String category,
            @Nullable String valueText,
            @Nullable Float scalar,
            @Nullable List<Float> vector
    ) {
        static PageToken empty(int pageIndex) {
            return new PageToken(pageIndex, TokenKind.EMPTY, null, "", null, null, null);
        }

        static PageToken linkPanel(int pageIndex) {
            return new PageToken(pageIndex, TokenKind.LINK_PANEL, null, "link_panel", "link_panel", null, null);
        }

        String path() {
            return symbolId == null ? "" : symbolId.getPath();
        }
    }

    private static final class Draft {
        final long seed;
        final String title;
        final String dimensionUid;
        final String levelId;
        final String bookUuid;
        final GlobalDraft global = new GlobalDraft();
        RegionDraft overworld;
        RegionDraft underworld;

        Draft(long seed, String title, String dimensionUid, String levelId, @Nullable String bookUuid) {
            this.seed = seed;
            this.title = title == null ? "" : title;
            this.dimensionUid = dimensionUid;
            this.levelId = levelId;
            this.bookUuid = bookUuid == null ? "" : bookUuid;
        }
    }

    private interface ScopedDraft {
        java.util.Map<String, ColorObject> recolors();

        List<FeatureObject> formations();

        List<FeatureObject> structures();

        void setWeatherType(String weatherType);

        void setWeatherSpeed(String weatherSpeed);
    }

    private static final class GlobalDraft implements ScopedDraft {
        final java.util.Map<String, ColorObject> recolors = new java.util.LinkedHashMap<>();
        final List<FeatureObject> formations = new ArrayList<>();
        final List<FeatureObject> structures = new ArrayList<>();

        final List<JsonObject> suns = new ArrayList<>();
        final List<JsonObject> moons = new ArrayList<>();
        final List<JsonObject> stars = new ArrayList<>();
        final List<JsonObject> others = new ArrayList<>();

        String lighting = null;
        String weatherType = null;
        String weatherSpeed = null;
        Integer cloudHeight = null;
        boolean cloudHeightNoClouds = false;

        @Override
        public java.util.Map<String, ColorObject> recolors() {
            return recolors;
        }

        @Override
        public List<FeatureObject> formations() {
            return formations;
        }

        @Override
        public List<FeatureObject> structures() {
            return structures;
        }

        @Override
        public void setWeatherType(String weatherType) {
            this.weatherType = weatherType;
        }

        @Override
        public void setWeatherSpeed(String weatherSpeed) {
            this.weatherSpeed = weatherSpeed;
        }

        JsonObject toJson() {
            JsonObject json = new JsonObject();

            JsonObject celestials = new JsonObject();
            celestials.add("suns", toArray(suns));
            celestials.add("moons", toArray(moons));
            celestials.add("stars", toArray(stars));
            celestials.add("others", toArray(others));
            json.add("celestials", celestials);

            if (lighting != null && !lighting.isBlank()) {
                json.addProperty("lighting", lighting);
            }

            JsonObject weather = weatherJson(weatherSpeed, weatherType);
            if (weather.size() > 0) {
                json.add("weather", weather);
            }

            if (cloudHeightNoClouds) {
                json.addProperty("cloud_height", "none");
            } else if (cloudHeight != null) {
                json.addProperty("cloud_height", cloudHeight);
            }

            if (!recolors.isEmpty()) {
                json.add("recolors", recolorJson(recolors));
            }

            JsonObject features = featuresJson(formations, structures);
            if (features.size() > 0) {
                json.add("features", features);
            }

            return json;
        }
    }

    private static final class RegionDraft {
        String terrain = "flat";
        String biomeLayout = "single";
        final List<BiomeDraft> biomes = new ArrayList<>();
        final BiomeDraft pendingBiomeData = new BiomeDraft("__pending__");

        ScopedDraft pendingBiomeScope() {
            return pendingBiomeData;
        }

        static RegionDraft fallbackOverworld(Identifier biome) {
            RegionDraft region = new RegionDraft();
            region.terrain = "flat";
            region.biomeLayout = "single";
            region.biomes.add(new BiomeDraft(biome.toString()));
            return region;
        }

        Identifier firstBiomeOrFallback(Identifier fallback) {
            if (biomes.isEmpty()) {
                return fallback;
            }

            Identifier parsed = Identifier.tryParse(biomes.get(0).biome);
            return parsed == null ? fallback : parsed;
        }

        String describe() {
            return "terrain=" + terrain + ", layout=" + biomeLayout + ", biomes=" + biomes.size();
        }

        JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("terrain", terrain);
            json.addProperty("biome_layout", biomeLayout);

            JsonArray biomeArray = new JsonArray();
            for (BiomeDraft biome : biomes) {
                biomeArray.add(biome.toJson());
            }
            json.add("biomes", biomeArray);

            return json;
        }
    }

    private static final class BiomeDraft implements ScopedDraft {
        final String biome;
        final java.util.Map<String, ColorObject> recolors = new java.util.LinkedHashMap<>();
        final List<FeatureObject> formations = new ArrayList<>();
        final List<FeatureObject> structures = new ArrayList<>();
        String weatherType = null;
        String weatherSpeed = null;

        BiomeDraft(String biome) {
            this.biome = biome;
        }

        BiomeDraft copyForBiome(String biomeId) {
            BiomeDraft copy = new BiomeDraft(biomeId);
            copy.recolors.putAll(this.recolors);
            copy.formations.addAll(this.formations);
            copy.structures.addAll(this.structures);
            copy.weatherType = this.weatherType;
            copy.weatherSpeed = this.weatherSpeed;
            return copy;
        }

        void clearScopedData() {
            this.recolors.clear();
            this.formations.clear();
            this.structures.clear();
            this.weatherType = null;
            this.weatherSpeed = null;
        }

        @Override
        public java.util.Map<String, ColorObject> recolors() {
            return recolors;
        }

        @Override
        public List<FeatureObject> formations() {
            return formations;
        }

        @Override
        public List<FeatureObject> structures() {
            return structures;
        }

        @Override
        public void setWeatherType(String weatherType) {
            this.weatherType = weatherType;
        }

        @Override
        public void setWeatherSpeed(String weatherSpeed) {
            this.weatherSpeed = weatherSpeed;
        }

        JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("biome", biome);

            JsonObject weather = weatherJson(weatherSpeed, weatherType);
            if (weather.size() > 0) {
                json.add("weather", weather);
            }

            if (!recolors.isEmpty()) {
                json.add("recolors", recolorJson(recolors));
            }

            JsonObject features = featuresJson(formations, structures);
            if (features.size() > 0) {
                json.add("features", features);
            }

            return json;
        }
    }

    private static final class PendingModifiers {
        final List<ModifierRun> runs = new ArrayList<>();
        ColorObject horizon = null;

        boolean isEmpty() {
            return runs.isEmpty() && horizon == null;
        }

        void add(PageToken token) {
            ModifierKind kind = modifierKind(token);
            if (kind == null) {
                return;
            }

            ModifierValue value = ModifierValue.from(token, kind);
            if (!runs.isEmpty() && runs.get(runs.size() - 1).kind == kind) {
                runs.get(runs.size() - 1).values.add(value);
            } else {
                ModifierRun run = new ModifierRun(kind);
                run.values.add(value);
                runs.add(run);
            }
        }

        void addGradient(ColorObject gradient) {
            ModifierRun run = new ModifierRun(ModifierKind.GRADIENT);
            run.values.add(new ModifierValue(null, null, null, gradient));
            runs.add(run);
        }

        void clear() {
            runs.clear();
            horizon = null;
        }

        @Nullable
        Float consumeNumeric(ModifierKind kind, boolean trace, PageToken consumer) {
            ModifierRun run = consumeUnique(kind, trace, consumer);
            if (run == null || run.values.isEmpty()) {
                return null;
            }

            float total = 0.0F;
            int count = 0;
            for (ModifierValue value : run.values) {
                if (value.scalar != null) {
                    total += value.scalar;
                    count++;
                }
            }

            return count == 0 ? null : total / count;
        }

        @Nullable
        Identifier consumeBlock(boolean trace, PageToken consumer) {
            ModifierRun run = consumeUnique(ModifierKind.BLOCK, trace, consumer);
            if (run == null || run.values.isEmpty()) {
                return null;
            }

            for (int i = run.values.size() - 1; i >= 0; i--) {
                if (run.values.get(i).identifier != null) {
                    return run.values.get(i).identifier;
                }
            }

            return null;
        }

        @Nullable
        ColorObject consumeColorObject(String reason, boolean trace, PageToken consumer) {
            ModifierRun gradient = consumeUnique(ModifierKind.GRADIENT, trace, consumer);
            if (gradient != null && !gradient.values.isEmpty() && gradient.values.get(0).colorObject != null) {
                return gradient.values.get(0).colorObject;
            }

            ModifierRun color = consumeUnique(ModifierKind.COLOR, trace, consumer);
            if (color == null || color.values.isEmpty()) {
                return null;
            }

            float r = 0.0F;
            float g = 0.0F;
            float b = 0.0F;
            int count = 0;

            for (ModifierValue value : color.values) {
                if (value.vector != null && value.vector.size() == 3) {
                    r += value.vector.get(0);
                    g += value.vector.get(1);
                    b += value.vector.get(2);
                    count++;
                }
            }

            if (count == 0) {
                return null;
            }

            return ColorObject.solid(toHex(r / count, g / count, b / count));
        }

        @Nullable
        ColorObject consumeGradient(boolean trace, PageToken consumer) {
            List<ModifierRun> consumed = new ArrayList<>();
            List<ColorObject.GradientStop> stops = new ArrayList<>();

            ColorObject pendingColor = null;
            Float pendingLength = null;

            for (ModifierRun run : runs) {
                if (run.kind == ModifierKind.COLOR) {
                    consumed.add(run);

                    float r = 0.0F;
                    float g = 0.0F;
                    float b = 0.0F;
                    int count = 0;
                    for (ModifierValue value : run.values) {
                        if (value.vector != null && value.vector.size() == 3) {
                            r += value.vector.get(0);
                            g += value.vector.get(1);
                            b += value.vector.get(2);
                            count++;
                        }
                    }
                    if (count > 0) {
                        pendingColor = ColorObject.solid(toHex(r / count, g / count, b / count));
                    }
                } else if (run.kind == ModifierKind.LENGTH) {
                    consumed.add(run);
                    float total = 0.0F;
                    int count = 0;
                    for (ModifierValue value : run.values) {
                        if (value.scalar != null) {
                            total += value.scalar;
                            count++;
                        }
                    }
                    if (count > 0) {
                        pendingLength = total / count;
                    }
                } else {
                    continue;
                }

                if (pendingColor != null && pendingLength != null) {
                    stops.add(new ColorObject.GradientStop(
                            pendingColor.color,
                            normalizeNumber(pendingLength)
                    ));
                    pendingColor = null;
                    pendingLength = null;
                }
            }

            if (stops.isEmpty()) {
                return null;
            }

            runs.removeAll(consumed);
            return ColorObject.gradient("FFFFFFFF", stops);
        }

        @Nullable
        ModifierRun consumeUnique(ModifierKind kind, boolean trace, PageToken consumer) {
            List<ModifierRun> matches = new ArrayList<>();
            for (ModifierRun run : runs) {
                if (run.kind == kind) {
                    matches.add(run);
                }
            }

            if (matches.isEmpty()) {
                return null;
            }

            if (matches.size() > 1) {
                MystcraftSyntheticCodex.LOGGER.warn(
                        "[MystAgeCompiler] Contradictory split modifier {} before page {} {}. Using last run for now.",
                        kind,
                        consumer.pageIndex,
                        consumer.symbolId
                );
            }

            runs.removeAll(matches);
            return matches.get(matches.size() - 1);
        }

        String describe() {
            if (runs.isEmpty() && horizon == null) {
                return "<empty>";
            }

            StringBuilder builder = new StringBuilder();
            for (ModifierRun run : runs) {
                if (!builder.isEmpty()) {
                    builder.append(" | ");
                }
                builder.append(run.kind).append('x').append(run.values.size());
            }

            if (horizon != null) {
                if (!builder.isEmpty()) {
                    builder.append(" | ");
                }
                builder.append("HORIZON");
            }

            return builder.toString();
        }
    }

    private static final class ModifierRun {
        final ModifierKind kind;
        final List<ModifierValue> values = new ArrayList<>();

        ModifierRun(ModifierKind kind) {
            this.kind = kind;
        }
    }

    private record ModifierValue(
            @Nullable Float scalar,
            @Nullable List<Float> vector,
            @Nullable Identifier identifier,
            @Nullable ColorObject colorObject
    ) {
        static ModifierValue from(PageToken token, ModifierKind kind) {
            Identifier identifier = null;
            if (kind == ModifierKind.BLOCK && token.valueText != null) {
                identifier = Identifier.tryParse(token.valueText);
            }

            return new ModifierValue(token.scalar, token.vector, identifier, null);
        }
    }

    private static final class FeatureObject {
        final String type;
        String block = null;
        Number size = null;

        FeatureObject(String type) {
            this.type = type;
        }

        JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("type", type);
            if (block != null) {
                json.addProperty("block", block);
            }
            if (size != null) {
                json.addProperty("size", size);
            }
            return json;
        }
    }

    private static final class ColorObject {
        final String mode;
        final String color;
        final String fallback;
        final List<GradientStop> stops;

        private ColorObject(String mode, String color, String fallback, List<GradientStop> stops) {
            this.mode = mode;
            this.color = color;
            this.fallback = fallback;
            this.stops = stops == null ? List.of() : List.copyOf(stops);
        }

        static ColorObject solid(String color) {
            return new ColorObject("solid", color, null, List.of());
        }

        static ColorObject gradient(String fallback, List<GradientStop> stops) {
            return new ColorObject("gradient", null, fallback, stops);
        }

        JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("mode", mode);

            if ("gradient".equals(mode)) {
                json.addProperty("fallback", fallback == null ? "FFFFFFFF" : fallback);
                JsonArray array = new JsonArray();
                for (GradientStop stop : stops) {
                    JsonObject stopJson = new JsonObject();
                    stopJson.addProperty("color", stop.color);
                    stopJson.addProperty("length", stop.length);
                    array.add(stopJson);
                }
                json.add("stops", array);
            } else {
                json.addProperty("color", color == null ? "FFFFFFFF" : color);
            }

            return json;
        }

        String describe() {
            return toJson().toString();
        }

        record GradientStop(String color, Number length) {
        }
    }

    private static JsonObject recolorJson(java.util.Map<String, ColorObject> recolors) {
        JsonObject json = new JsonObject();
        for (var entry : recolors.entrySet()) {
            json.add(entry.getKey(), entry.getValue().toJson());
        }
        return json;
    }

    private static JsonObject featuresJson(List<FeatureObject> formations, List<FeatureObject> structures) {
        JsonObject features = new JsonObject();

        if (!formations.isEmpty()) {
            JsonArray array = new JsonArray();
            for (FeatureObject formation : formations) {
                array.add(formation.toJson());
            }
            features.add("formations", array);
        }

        if (!structures.isEmpty()) {
            JsonArray array = new JsonArray();
            for (FeatureObject structure : structures) {
                array.add(structure.toJson());
            }
            features.add("structures", array);
        }

        return features;
    }

    private static JsonObject weatherJson(@Nullable String speed, @Nullable String type) {
        JsonObject weather = new JsonObject();
        if (speed != null && !speed.isBlank()) {
            weather.addProperty("speed", speed);
        }
        if (type != null && !type.isBlank()) {
            weather.addProperty("type", type);
        }
        return weather;
    }

    private static JsonArray toArray(List<JsonObject> objects) {
        JsonArray array = new JsonArray();
        for (JsonObject object : objects) {
            array.add(object);
        }
        return array;
    }

    private static String toHex(float r, float g, float b) {
        int ri = clampColor(r);
        int gi = clampColor(g);
        int bi = clampColor(b);
        return String.format(Locale.ROOT, "%02X%02X%02XFF", ri, gi, bi);
    }

    private static int clampColor(float value) {
        int i = Math.round(value * 255.0F);
        return Math.max(0, Math.min(255, i));
    }
}