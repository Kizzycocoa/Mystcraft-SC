package myst.synthetic.page.symbol;

import myst.synthetic.MystcraftSyntheticCodex;
import net.minecraft.resources.Identifier;

import java.util.List;

public final class MystcraftPageSymbols {

    public static final PageSymbol COLOR_CLOUD = register("legacy/color_cloud", "symbol.mystcraft-sc.legacy.color_cloud", 1, "Image", "Entropy", "Believe", "Weave");
    public static final PageSymbol COLOR_CLOUD_NAT = register("legacy/color_cloud_nat", "symbol.mystcraft-sc.legacy.color_cloud_nat", 1, "Image", "Entropy", "Believe", "Nature");
    public static final PageSymbol COLOR_FOG = register("legacy/color_fog", "symbol.mystcraft-sc.legacy.color_fog", 1, "Image", "Entropy", "Explore", "Weave");
    public static final PageSymbol COLOR_FOG_NAT = register("legacy/color_fog_nat", "symbol.mystcraft-sc.legacy.color_fog_nat", 1, "Image", "Entropy", "Explore", "Nature");
    public static final PageSymbol COLOR_FOLIAGE = register("legacy/color_foliage", "symbol.mystcraft-sc.legacy.color_foliage", 1, "Image", "Growth", "Elevate", "Weave");
    public static final PageSymbol COLOR_FOLIAGE_NAT = register("legacy/color_foliage_nat", "symbol.mystcraft-sc.legacy.color_foliage_nat", 1, "Image", "Growth", "Elevate", "Nature");
    public static final PageSymbol COLOR_GRASS = register("legacy/color_grass", "symbol.mystcraft-sc.legacy.color_grass", 1, "Image", "Growth", "Resilience", "Weave");
    public static final PageSymbol COLOR_GRASS_NAT = register("legacy/color_grass_nat", "symbol.mystcraft-sc.legacy.color_grass_nat", 1, "Image", "Growth", "Resilience", "Nature");
    public static final PageSymbol COLOR_SKY = register("legacy/color_sky", "symbol.mystcraft-sc.legacy.color_sky", 1, "Image", "Celestial", "Harmony", "Weave");
    public static final PageSymbol COLOR_SKY_NAT = register("legacy/color_sky_nat", "symbol.mystcraft-sc.legacy.color_sky_nat", 1, "Image", "Celestial", "Harmony", "Nature");
    public static final PageSymbol COLOR_SKY_NIGHT = register("legacy/color_sky_night", "symbol.mystcraft-sc.legacy.color_sky_night", 1, "Image", "Celestial", "Contradict", "Weave");
    public static final PageSymbol COLOR_WATER = register("legacy/color_water", "symbol.mystcraft-sc.legacy.color_water", 1, "Image", "Flow", "Constraint", "Weave");
    public static final PageSymbol COLOR_WATER_NAT = register("legacy/color_water_nat", "symbol.mystcraft-sc.legacy.color_water_nat", 1, "Image", "Flow", "Constraint", "Nature");
    public static final PageSymbol RAINBOW = register("legacy/rainbow", "symbol.mystcraft-sc.legacy.rainbow", 1, "Celestial", "Image", "Harmony", "Balance");
    public static final PageSymbol NO_HORIZON = register("legacy/no_horizon", "symbol.mystcraft-sc.legacy.no_horizon", 1, "Celestial", "Inhibit", "Image", "Void");
    public static final PageSymbol MOON_DARK = register("legacy/moon_dark", "symbol.mystcraft-sc.legacy.moon_dark", 1, "Celestial", "Void", "Inhibit", "Wisdom");
    public static final PageSymbol MOON_NORMAL = register("legacy/moon_normal", "symbol.mystcraft-sc.legacy.moon_normal", 1, "Celestial", "Image", "Cycle", "Wisdom");
    public static final PageSymbol STARS_DARK = register("legacy/stars_dark", "symbol.mystcraft-sc.legacy.stars_dark", 1, "Celestial", "Void", "Inhibit", "Order");
    public static final PageSymbol STARS_END_SKY = register("legacy/stars_end_sky", "symbol.mystcraft-sc.legacy.stars_end_sky", 1, "Celestial", "Image", "Chaos", "Weave");
    public static final PageSymbol STARS_NORMAL = register("legacy/stars_normal", "symbol.mystcraft-sc.legacy.stars_normal", 1, "Celestial", "Harmony", "Ethereal", "Order");
    public static final PageSymbol STARS_TWINKLE = register("legacy/stars_twinkle", "symbol.mystcraft-sc.legacy.stars_twinkle", 1, "Celestial", "Harmony", "Ethereal", "Entropy");
    public static final PageSymbol SUN_DARK = register("legacy/sun_dark", "symbol.mystcraft-sc.legacy.sun_dark", 1, "Celestial", "Void", "Inhibit", "Energy");
    public static final PageSymbol SUN_NORMAL = register("legacy/sun_normal", "symbol.mystcraft-sc.legacy.sun_normal", 2, "Celestial", "Image", "Stimulate", "Energy");

    public static final PageSymbol BIO_CON_GRID = register("legacy/bio_con_grid", "symbol.mystcraft-sc.legacy.bio_con_grid", 3, "Constraint", "Nature", "Chain", "Mutual");
    public static final PageSymbol BIO_CON_NATIVE = register("legacy/bio_con_native", "symbol.mystcraft-sc.legacy.bio_con_native", 3, "Constraint", "Nature", "Tradition", "Sustain");
    public static final PageSymbol BIO_CON_SINGLE = register("legacy/bio_con_single", "symbol.mystcraft-sc.legacy.bio_con_single", 3, "Constraint", "Nature", "Infinite", "Static");
    public static final PageSymbol BIO_CON_TILED = register("legacy/bio_con_tiled", "symbol.mystcraft-sc.legacy.bio_con_tiled", 3, "Constraint", "Nature", "Chain", "Contradict");
    public static final PageSymbol BIO_CON_HUGE = register("legacy/bio_con_huge", "symbol.mystcraft-sc.legacy.bio_con_huge", 3, "Constraint", "Nature", "Weave", "Huge");
    public static final PageSymbol BIO_CON_LARGE = register("legacy/bio_con_large", "symbol.mystcraft-sc.legacy.bio_con_large", 3, "Constraint", "Nature", "Weave", "Large");
    public static final PageSymbol BIO_CON_MEDIUM = register("legacy/bio_con_medium", "symbol.mystcraft-sc.legacy.bio_con_medium", 3, "Constraint", "Nature", "Weave", "Medium");
    public static final PageSymbol BIO_CON_SMALL = register("legacy/bio_con_small", "symbol.mystcraft-sc.legacy.bio_con_small", 3, "Constraint", "Nature", "Weave", "Small");
    public static final PageSymbol BIO_CON_TINY = register("legacy/bio_con_tiny", "symbol.mystcraft-sc.legacy.bio_con_tiny", 3, "Constraint", "Nature", "Weave", "Tiny");

    public static final PageSymbol NO_SEA = register("legacy/no_sea", "symbol.mystcraft-sc.legacy.no_sea", 2, "Modifier", "Constraint", "Flow", "Inhibit");
    public static final PageSymbol PV_P_OFF = register("legacy/pv_p_off", "symbol.mystcraft-sc.legacy.pv_p_off", null, "Chain", "Chaos", "Encourage", "Harmony");

    public static final PageSymbol ENV_ACCEL = register("legacy/env_accel", "symbol.mystcraft-sc.legacy.env_accel", 3, "Environment", "Dynamic", "Change", "Spur");
    public static final PageSymbol ENV_EXPLOSIONS = register("legacy/env_explosions", "symbol.mystcraft-sc.legacy.env_explosions", 3, "Environment", "Sacrifice", "Power", "Force");
    public static final PageSymbol ENV_LIGHTNING = register("legacy/env_lightning", "symbol.mystcraft-sc.legacy.env_lightning", 3, "Environment", "Sacrifice", "Power", "Energy");
    public static final PageSymbol ENV_METEOR = register("legacy/env_meteor", "symbol.mystcraft-sc.legacy.env_meteor", 3, "Environment", "Sacrifice", "Power", "Momentum");
    public static final PageSymbol ENV_SCORCH = register("legacy/env_scorch", "symbol.mystcraft-sc.legacy.env_scorch", 3, "Environment", "Sacrifice", "Power", "Chaos");

    public static final PageSymbol LIGHTING_BRIGHT = register("legacy/lighting_bright", "symbol.mystcraft-sc.legacy.lighting_bright", 3, "Ethereal", "Power", "Infinite", "Spur");
    public static final PageSymbol LIGHTING_DARK = register("legacy/lighting_dark", "symbol.mystcraft-sc.legacy.lighting_dark", 3, "Ethereal", "Void", "Constraint", "Inhibit");
    public static final PageSymbol LIGHTING_NORMAL = register("legacy/lighting_normal", "symbol.mystcraft-sc.legacy.lighting_normal", 2, "Ethereal", "Dynamic", "Cycle", "Balance");

    public static final PageSymbol MOD_NORTH = register("legacy/mod_north", "symbol.mystcraft-sc.legacy.mod_north", 0, "Modifier", "Flow", "Motion", "Control");
    public static final PageSymbol MOD_EAST = register("legacy/mod_east", "symbol.mystcraft-sc.legacy.mod_east", 0, "Modifier", "Flow", "Motion", "Tradition");
    public static final PageSymbol MOD_SOUTH = register("legacy/mod_south", "symbol.mystcraft-sc.legacy.mod_south", 0, "Modifier", "Flow", "Motion", "Chaos");
    public static final PageSymbol MOD_WEST = register("legacy/mod_west", "symbol.mystcraft-sc.legacy.mod_west", 0, "Modifier", "Flow", "Motion", "Change");
    public static final PageSymbol MOD_CLEAR = register("legacy/mod_clear", "symbol.mystcraft-sc.legacy.mod_clear", 0, "Contradict", "Transform", "Change", "Void");
    public static final PageSymbol MOD_GRADIENT = register("legacy/mod_gradient", "symbol.mystcraft-sc.legacy.mod_gradient", 1, "Modifier", "Image", "Merge", "Weave");
    public static final PageSymbol COLOR_HORIZON = register("legacy/color_horizon", "symbol.mystcraft-sc.legacy.color_horizon", 0, "Modifier", "Image", "Celestial", "Change");
    public static final PageSymbol MOD_ZERO = register("legacy/mod_zero", "symbol.mystcraft-sc.legacy.mod_zero", 0, "Modifier", "Time", "System", "Inhibit");
    public static final PageSymbol MOD_HALF = register("legacy/mod_half", "symbol.mystcraft-sc.legacy.mod_half", 0, "Modifier", "Time", "System", "Stimulate");
    public static final PageSymbol MOD_FULL = register("legacy/mod_full", "symbol.mystcraft-sc.legacy.mod_full", 0, "Modifier", "Time", "System", "Balance");
    public static final PageSymbol MOD_DOUBLE = register("legacy/mod_double", "symbol.mystcraft-sc.legacy.mod_double", 0, "Modifier", "Time", "System", "Sacrifice");
    public static final PageSymbol MOD_END = register("legacy/mod_end", "symbol.mystcraft-sc.legacy.mod_end", 0, "Modifier", "Cycle", "System", "Rebirth");
    public static final PageSymbol MOD_RISING = register("legacy/mod_rising", "symbol.mystcraft-sc.legacy.mod_rising", 0, "Modifier", "Cycle", "System", "Growth");
    public static final PageSymbol MOD_NOON = register("legacy/mod_noon", "symbol.mystcraft-sc.legacy.mod_noon", 0, "Modifier", "Cycle", "System", "Harmony");
    public static final PageSymbol MOD_SETTING = register("legacy/mod_setting", "symbol.mystcraft-sc.legacy.mod_setting", 0, "Modifier", "Cycle", "System", "Future");

    public static final PageSymbol CAVES = register("legacy/caves", "symbol.mystcraft-sc.legacy.caves", 2, "Terrain", "Transform", "Void", "Flow");
    public static final PageSymbol DUNGEONS = register("legacy/dungeons", "symbol.mystcraft-sc.legacy.dungeons", 2, "Civilization", "Constraint", "Chain", "Resurrect");
    public static final PageSymbol FLOAT_ISLANDS = register("legacy/float_islands", "symbol.mystcraft-sc.legacy.float_islands", 3, "Terrain", "Transform", "Form", "Celestial");
    public static final PageSymbol FEATURE_LARGE_DUMMY = register("legacy/feature_large_dummy", "symbol.mystcraft-sc.legacy.feature_large_dummy", 4, "Contradict", "Chaos", "Exist", "Terrain");
    public static final PageSymbol FEATURE_MEDIUM_DUMMY = register("legacy/feature_medium_dummy", "symbol.mystcraft-sc.legacy.feature_medium_dummy", 4, "Contradict", "Chaos", "Exist", "Balance");
    public static final PageSymbol FEATURE_SMALL_DUMMY = register("legacy/feature_small_dummy", "symbol.mystcraft-sc.legacy.feature_small_dummy", 5, "Contradict", "Chaos", "Exist", "Form");
    public static final PageSymbol HUGE_TREES = register("legacy/huge_trees", "symbol.mystcraft-sc.legacy.huge_trees", 2, "Nature", "Stimulate", "Spur", "Elevate");
    public static final PageSymbol LAKES_DEEP = register("legacy/lakes_deep", "symbol.mystcraft-sc.legacy.lakes_deep", 3, "Nature", "Flow", "Static", "Explore");
    public static final PageSymbol LAKES_SURFACE = register("legacy/lakes_surface", "symbol.mystcraft-sc.legacy.lakes_surface", 3, "Nature", "Flow", "Static", "Elevate");
    public static final PageSymbol MINESHAFTS = register("legacy/mineshafts", "symbol.mystcraft-sc.legacy.mineshafts", 3, "Civilization", "Machine", "Motion", "Tradition");
    public static final PageSymbol NETHER_FORT = register("legacy/nether_fort", "symbol.mystcraft-sc.legacy.nether_fort", 3, "Civilization", "Machine", "Power", "Entropy");
    public static final PageSymbol OBELISKS = register("legacy/obelisks", "symbol.mystcraft-sc.legacy.obelisks", 3, "Civilization", "Resilience", "Static", "Form");
    public static final PageSymbol RAVINES = register("legacy/ravines", "symbol.mystcraft-sc.legacy.ravines", 2, "Terrain", "Transform", "Void", "Weave");
    public static final PageSymbol TER_MOD_SPHERES = register("legacy/ter_mod_spheres", "symbol.mystcraft-sc.legacy.ter_mod_spheres", 2, "Terrain", "Transform", "Form", "Cycle");
    public static final PageSymbol GEN_SPIKES = register("legacy/gen_spikes", "symbol.mystcraft-sc.legacy.gen_spikes", 3, "Nature", "Encourage", "Entropy", "Structure");
    public static final PageSymbol STRONGHOLDS = register("legacy/strongholds", "symbol.mystcraft-sc.legacy.strongholds", 3, "Civilization", "Wisdom", "Future", "Honor");
    public static final PageSymbol TENDRILS = register("legacy/tendrils", "symbol.mystcraft-sc.legacy.tendrils", 3, "Terrain", "Transform", "Growth", "Flow");
    public static final PageSymbol VILLAGES = register("legacy/villages", "symbol.mystcraft-sc.legacy.villages", 3, "Civilization", "Society", "Harmony", "Nurture");
    public static final PageSymbol CRY_FORM = register("legacy/cry_form", "symbol.mystcraft-sc.legacy.cry_form", 3, "Nature", "Encourage", "Growth", "Structure");
    public static final PageSymbol SKYLANDS = register("legacy/skylands", "symbol.mystcraft-sc.legacy.skylands", 3, "Terrain", "Transform", "Void", "Elevate");
    public static final PageSymbol STAR_FISSURE = register("legacy/star_fissure", "symbol.mystcraft-sc.legacy.star_fissure", 3, "Nature", "Harmony", "Mutual", "Void");
    public static final PageSymbol DENSE_ORES = register("legacy/dense_ores", "symbol.mystcraft-sc.legacy.dense_ores", 5, "Environment", "Stimulate", "Machine", "Chaos");

    public static final PageSymbol WEATHER_ON = register("legacy/weather_on", "symbol.mystcraft-sc.legacy.weather_on", 3, "Sustain", "Static", "Tradition", "Stimulate");
    public static final PageSymbol WEATHER_CLOUDY = register("legacy/weather_cloudy", "symbol.mystcraft-sc.legacy.weather_cloudy", 3, "Sustain", "Static", "Believe", "Motion");
    public static final PageSymbol WEATHER_FAST = register("legacy/weather_fast", "symbol.mystcraft-sc.legacy.weather_fast", 3, "Sustain", "Dynamic", "Tradition", "Spur");
    public static final PageSymbol WEATHER_NORM = register("legacy/weather_norm", "symbol.mystcraft-sc.legacy.weather_norm", 2, "Sustain", "Dynamic", "Tradition", "Balance");
    public static final PageSymbol WEATHER_OFF = register("legacy/weather_off", "symbol.mystcraft-sc.legacy.weather_off", 3, "Sustain", "Static", "Stimulate", "Energy");
    public static final PageSymbol WEATHER_RAIN = register("legacy/weather_rain", "symbol.mystcraft-sc.legacy.weather_rain", 3, "Sustain", "Static", "Rebirth", "Growth");
    public static final PageSymbol WEATHER_SLOW = register("legacy/weather_slow", "symbol.mystcraft-sc.legacy.weather_slow", 3, "Sustain", "Dynamic", "Tradition", "Inhibit");
    public static final PageSymbol WEATHER_SNOW = register("legacy/weather_snow", "symbol.mystcraft-sc.legacy.weather_snow", 3, "Sustain", "Static", "Inhibit", "Energy");
    public static final PageSymbol WEATHER_STORM = register("legacy/weather_storm", "symbol.mystcraft-sc.legacy.weather_storm", 3, "Sustain", "Static", "Nature", "Power");

    public static final PageSymbol TERRAIN_AMPLIFIED = register("legacy/terrain_amplified", "symbol.mystcraft-sc.legacy.terrain_amplified", 3, "Terrain", "Form", "Tradition", "Spur");
    public static final PageSymbol TERRAIN_END = register("legacy/terrain_end", "symbol.mystcraft-sc.legacy.terrain_end", 4, "Terrain", "Form", "Ethereal", "Flow");
    public static final PageSymbol TERRAIN_FLAT = register("legacy/terrain_flat", "symbol.mystcraft-sc.legacy.terrain_flat", 3, "Terrain", "Form", "Inhibit", "Motion");
    public static final PageSymbol TERRAIN_NETHER = register("legacy/terrain_nether", "symbol.mystcraft-sc.legacy.terrain_nether", 4, "Terrain", "Form", "Constraint", "Entropy");
    public static final PageSymbol TERRAIN_NORMAL = register("legacy/terrain_normal", "symbol.mystcraft-sc.legacy.terrain_normal", 2, "Terrain", "Form", "Tradition", "Flow");
    public static final PageSymbol TERRAIN_VOID = register("legacy/terrain_void", "symbol.mystcraft-sc.legacy.terrain_void", 4, "Terrain", "Form", "Infinite", "Void");

    private MystcraftPageSymbols() {
    }

    public static void initialize() {
        MystcraftSyntheticCodex.LOGGER.info("Registered {} Mystcraft page symbols.", PageSymbolRegistry.size());
    }

    private static PageSymbol register(
            String path,
            String translationKey,
            Integer cardRank,
            String word1,
            String word2,
            String word3,
            String word4
    ) {
        return PageSymbolRegistry.register(new PageSymbol(
                Identifier.fromNamespaceAndPath("mystcraft-sc", path),
                translationKey,
                cardRank == null ? 0 : cardRank,
                List.of(word1, word2, word3, word4)
        ));
    }
}