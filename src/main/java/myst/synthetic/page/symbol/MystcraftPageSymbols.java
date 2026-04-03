package myst.synthetic.page.symbol;

import myst.synthetic.MystcraftSyntheticCodex;
import net.minecraft.resources.Identifier;

import java.util.List;

public final class MystcraftPageSymbols {

    public static final PageSymbol DEBUG_FULL = register("debug/full_symbol", "symbol.mystcraft-sc.debug.full_symbol", "Mystcraft:SC", "debug", 0, "DEBUG", "DEBUG", "DEBUG", "DEBUG", 0);

// COLOUR MODIFIERS

    public static final PageSymbol RECOLOR_CLOUD = register("recolor_cloud", "symbol.mystcraft-sc.recolor_cloud", "Mystcraft", "color/cloud", 1, "Image","Entropy","Believe","Weave",0);
    public static final PageSymbol RECOLOR_CLOUD_NAT = register("recolor_cloud_nat", "symbol.mystcraft-sc.recolor_cloud_nat", "Mystcraft", "color/cloud", 1, "Image","Entropy","Believe","Nature",0);
    public static final PageSymbol RECOLOR_FOG = register("recolor_fog", "symbol.mystcraft-sc.recolor_fog", "Mystcraft", "color/fog", 1, "Image","Entropy","Explore","Weave",0);
    public static final PageSymbol RECOLOR_FOG_NAT = register("recolor_fog_nat", "symbol.mystcraft-sc.recolor_fog_nat", "Mystcraft", "color/fog", 1, "Image","Entropy","Explore","Nature",0);
    public static final PageSymbol RECOLOR_FOLIAGE = register("recolor_foliage", "symbol.mystcraft-sc.recolor_foliage", "Mystcraft", "color/foliage", 1, "Image","Growth","Elevate","Weave",0);
    public static final PageSymbol RECOLOR_FOLIAGE_NAT = register("recolor_foliage_nat", "symbol.mystcraft-sc.recolor_foliage_nat", "Mystcraft", "color/foliage", 1, "Image","Growth","Elevate","Nature",0);
    public static final PageSymbol RECOLOR_GRASS = register("recolor_grass", "symbol.mystcraft-sc.recolor_grass", "Mystcraft", "color/grass", 1, "Image","Growth","Resilience","Weave",0);
    public static final PageSymbol RECOLOR_GRASS_NAT = register("recolor_grass_nat", "symbol.mystcraft-sc.recolor_grass_nat", "Mystcraft", "color/grass", 1, "Image","Growth","Resilience","Nature",0);
    public static final PageSymbol RECOLOR_SKY = register("recolor_sky", "symbol.mystcraft-sc.recolor_sky", "Mystcraft", "color/sky", 1, "Image","Celestial","Harmony","Weave",0);
    public static final PageSymbol RECOLOR_SKY_NAT = register("recolor_sky_nat", "symbol.mystcraft-sc.recolor_sky_nat", "Mystcraft", "color/sky", 1, "Image","Celestial","Harmony","Nature",0);
    public static final PageSymbol RECOLOR_NIGHT_SKY = register("recolor_night_sky", "symbol.mystcraft-sc.recolor_night_sky", "Mystcraft", "color/sky", 1, "Image","Celestial","Contradict","Weave",0);
    public static final PageSymbol RECOLOR_NIGHT_SKY_NAT = register("recolor_night_sky_nat", "symbol.mystcraft-sc.recolor_night_sky_nat", "Mystcraft:SC", "color/sky", 1, "Image","Celestial","Contradict","Nature",0);
    public static final PageSymbol RECOLOR_WATER = register("recolor_water", "symbol.mystcraft-sc.recolor_water", "Mystcraft", "recolor", 1, "Image","Flow","Constraint","Weave",0);
    public static final PageSymbol RECOLOR_WATER_NAT = register("recolor_water_nat", "symbol.mystcraft-sc.recolor_water_nat", "Mystcraft", "recolor", 1, "Image","Flow","Constraint","Nature",0);

//COLOURS

    public static final PageSymbol COLOR_BLACK = register("color_black", "symbol.mystcraft-sc.color_black", "Mystcraft", "color", 1, "Transform","Image","Weave","Black Color",0);
    public static final PageSymbol COLOR_BLUE = register("color_blue", "symbol.mystcraft-sc.color_blue", "Mystcraft", "color", 1, "Transform","Image","Weave","Blue Color",0);
    public static final PageSymbol COLOR_CYAN = register("color_cyan", "symbol.mystcraft-sc.color_cyan", "Mystcraft", "color", 1, "Transform","Image","Weave","Cyan Color",0);
    public static final PageSymbol COLOR_DARK_GREEN = register("color_dark_green", "symbol.mystcraft-sc.color_dark_green", "Mystcraft", "color", 1, "Transform","Image","Weave","Dark Green Color",0);
    public static final PageSymbol COLOR_GREEN = register("color_green", "symbol.mystcraft-sc.color_green", "Mystcraft", "color", 1, "Transform","Image","Weave","Green Color",0);
    public static final PageSymbol COLOR_GREY = register("color_grey", "symbol.mystcraft-sc.color_grey", "Mystcraft", "color", 1, "Transform","Image","Weave","Grey Color",0);
    public static final PageSymbol COLOR_MAGENTA = register("color_magenta", "symbol.mystcraft-sc.color_magenta", "Mystcraft", "color", 1, "Transform","Image","Weave","Magenta Color",0);
    public static final PageSymbol COLOR_MAROON = register("color_maroon", "symbol.mystcraft-sc.color_maroon", "Mystcraft", "color", 1, "Transform","Image","Weave","Maroon Color",0);
    public static final PageSymbol COLOR_NAVY = register("color_navy", "symbol.mystcraft-sc.color_navy", "Mystcraft", "color", 1, "Transform","Image","Weave","Navy Color",0);
    public static final PageSymbol COLOR_OLIVE = register("color_olive", "symbol.mystcraft-sc.color_olive", "Mystcraft", "color", 1, "Transform","Image","Weave","Olive Color",0);
    public static final PageSymbol COLOR_PURPLE = register("color_purple", "symbol.mystcraft-sc.color_purple", "Mystcraft", "color", 1, "Transform","Image","Weave","Purple Color",0);
    public static final PageSymbol COLOR_RED = register("color_red", "symbol.mystcraft-sc.color_red", "Mystcraft", "color", 1, "Transform","Image","Weave","Red Color",0);
    public static final PageSymbol COLOR_SILVER = register("color_silver", "symbol.mystcraft-sc.color_silver", "Mystcraft", "color", 1, "Transform","Image","Weave","Silver Color",0);
    public static final PageSymbol COLOR_TEAL = register("color_teal", "symbol.mystcraft-sc.color_teal", "Mystcraft", "color", 1, "Transform","Image","Weave","Teal Color",0);
    public static final PageSymbol COLOR_WHITE = register("color_white", "symbol.mystcraft-sc.color_white", "Mystcraft", "color", 1, "Transform","Image","Weave","White Color",0);
    public static final PageSymbol COLOR_YELLOW = register("color_yellow", "symbol.mystcraft-sc.color_yellow", "Mystcraft", "color", 1, "Transform","Image","Weave","Yellow Color",0);
    public static final PageSymbol COLOR_BABY_BLUE = register("color_baby_blue", "symbol.mystcraft-sc.color_baby_blue", "Myst Library", "color", 1, "Transform","Image","Weave","Baby Blue Color",0);
    public static final PageSymbol COLOR_CITRUS = register("color_citrus", "symbol.mystcraft-sc.color_citrus", "Myst Library", "color", 1, "Transform","Image","Weave","Citrus Color",0);
    public static final PageSymbol COLOR_DARK_GREY = register("color_dark_grey", "symbol.mystcraft-sc.color_dark_grey", "Myst Library", "color", 1, "Transform","Image","Weave","Dark Grey Color",0);
    public static final PageSymbol COLOR_LIME = register("color_lime", "symbol.mystcraft-sc.color_lime", "Myst Library", "color", 1, "Transform","Image","Weave","Lime Color",0);
    public static final PageSymbol COLOR_ORANGE = register("color_orange", "symbol.mystcraft-sc.color_orange", "Myst Library", "color", 1, "Transform","Image","Weave","Orange Color",0);
    public static final PageSymbol COLOR_PINK = register("color_pink", "symbol.mystcraft-sc.color_pink", "Myst Library", "color", 1, "Transform","Image","Weave","Pink Color",0);
    public static final PageSymbol COLOR_RAINBOW = register("color_rainbow", "symbol.mystcraft-sc.color_rainbow", "Mystcraft:SC", "color", 1, "Transform","Image","Weave","Rainbow Color",0);

// CELESTIALS

    public static final PageSymbol CELESTIAL_RAINBOW = register("celestial_rainbow", "symbol.mystcraft-sc.celestial_rainbow", "Mystcraft", "celestial/misc", 1, "Celestial","Image","Harmony","Balance",0);
    public static final PageSymbol CELESTIAL_HORIZON = register("celestial_horizon", "symbol.mystcraft-sc.celestial_horizon", "Mystcraft", "celestial/misc", 1, "Celestial","Inhibit","Image","Void",0);
    public static final PageSymbol CELESTIAL_MOON_DARK = register("celestial_moon_dark", "symbol.mystcraft-sc.celestial_moon_dark", "Mystcraft", "celestial/moon", 1, "Celestial","Void","Inhibit","Wisdom",0);
    public static final PageSymbol CELESTIAL_MOON_NAT = register("celestial_moon_nat", "symbol.mystcraft-sc.celestial_moon_nat", "Mystcraft", "celestial/moon", 1, "Celestial","Image","Cycle","Wisdom",0);
    public static final PageSymbol CELESTIAL_STARS_DARK = register("celestial_stars_dark", "symbol.mystcraft-sc.celestial_stars_dark", "Mystcraft", "celestial/stars", 1, "Celestial","Void","Inhibit","Order",0);
    public static final PageSymbol CELESTIAL_STARS_STARFIELD = register("celestial_stars_starfield", "symbol.mystcraft-sc.celestial_stars_starfield", "Mystcraft", "celestial/stars", 1, "Celestial","Image","Chaos","Weave",0);
    public static final PageSymbol CELESTIAL_STARS_NAT = register("celestial_stars_nat", "symbol.mystcraft-sc.celestial_stars_nat", "Mystcraft", "celestial/stars", 1, "Celestial","Harmony","Ethereal","Order",0);
    public static final PageSymbol CELESTIAL_STARS_TWINKLE = register("celestial_stars_twinkle", "symbol.mystcraft-sc.celestial_stars_twinkle", "Mystcraft", "celestial/stars", 1, "Celestial","Harmony","Ethereal","Entropy",0);
    public static final PageSymbol CELESTIAL_SUN_DARK = register("celestial_sun_dark", "symbol.mystcraft-sc.celestial_sun_dark", "Mystcraft", "celestial/sun", 1, "Celestial","Void","Inhibit","Energy",0);
    public static final PageSymbol CELESTIAL_SUN_NAT = register("celestial_sun_nat", "symbol.mystcraft-sc.celestial_sun_nat", "Mystcraft", "celestial/sun", 1, "Celestial","Image","Stimulate","Energy",0);
    public static final PageSymbol CELESTIAL_SUN_TINT = register("celestial_sun_tint", "symbol.mystcraft-sc.celestial_sun_tint", "More Mystcraft", "celestial/sun", 1, "Celestial","Image","Stimulate","Tinted Sun",0);

//CLOUD CONTROLLERS

    public static final PageSymbol CLOUD_DOUBLE = register("cloud_double", "symbol.mystcraft-sc.cloud_double", "More Mystcraft", "biocontrol", 2, "Transform","Motion","Double Cloud Height","Double Cloud Height",0);
    public static final PageSymbol CLOUD_FULL = register("cloud_full", "symbol.mystcraft-sc.cloud_full", "More Mystcraft", "biocontrol", 2, "Transform","Motion","Full Cloud Height","Full Cloud Height",0);
    public static final PageSymbol CLOUD_HALF = register("cloud_half", "symbol.mystcraft-sc.cloud_half", "More Mystcraft", "biocontrol", 2, "Transform","Motion","Half Cloud Height","Half Cloud Height",0);
    public static final PageSymbol CLOUD_ZERO = register("cloud_zero", "symbol.mystcraft-sc.cloud_zero", "More Mystcraft", "biocontrol", 2, "Transform","Motion","Zero Cloud Height","Zero Cloud Height",0);
    public static final PageSymbol CLOUD_DEEP = register("cloud_deep", "symbol.mystcraft-sc.cloud_deep", "More Mystcraft", "biocontrol", 2, "Transform","Motion","Deep Cloud Height","Deep Cloud Height",0);

// TILTS

    public static final PageSymbol TILT_FULL = register("tilt_full", "symbol.mystcraft-sc.tilt_full", "More Mystcraft", "biocontrol", 2, "Transform","Motion","Full Tilt","Full Tilt",0);
    public static final PageSymbol TILT_HALF = register("tilt_half", "symbol.mystcraft-sc.tilt_half", "More Mystcraft", "biocontrol", 2, "Transform","Motion","Half Tilt","Half Tilt",0);
    public static final PageSymbol TILT_Zero = register("tilt_zero", "symbol.mystcraft-sc.tilt_zero", "More Mystcraft", "biocontrol", 2, "Transform","Motion","Zero Tilt","Zero Tilt",0);

// SIZES

    public static final PageSymbol SIZE_HUGE = register("size_huge", "symbol.mystcraft-sc.size_huge", "More Mystcraft", "biocontrol", 2, "Control","Form","Huge Size","Huge Size",0);
    public static final PageSymbol SIZE_LARGE = register("size_large", "symbol.mystcraft-sc.size_large", "More Mystcraft", "biocontrol", 2, "Control","Form","Large Size","Large Size",0);
    public static final PageSymbol SIZE_MEDIUM = register("size_medium", "symbol.mystcraft-sc.size_medium", "More Mystcraft", "biocontrol", 2, "Control","Form","Medium Size","Medium Size",0);
    public static final PageSymbol SIZE_SMALL = register("size_small", "symbol.mystcraft-sc.size_small", "More Mystcraft", "biocontrol", 2, "Control","Form","Small Size","Small Size",0);
    public static final PageSymbol SIZE_TINY = register("size_tiny", "symbol.mystcraft-sc.size_tiny", "More Mystcraft", "biocontrol", 2, "Control","Form","Tiny Size","Tiny Size",0);

// BIOME CONTROLLERS

    public static final PageSymbol BIO_CON_GRID = register("bio_con_grid", "symbol.mystcraft-sc.bio_con_grid", "Mystcraft", "biocontrol", 2, "Constraint","Nature","Chain","Mutual",0);
    public static final PageSymbol BIO_CON_NAT = register("bio_con_nat", "symbol.mystcraft-sc.bio_con_nat", "Mystcraft", "biocontrol", 2, "Constraint","Nature","Tradition","Sustain",0);
    public static final PageSymbol BIO_CON_SINGLE = register("bio_con_single", "symbol.mystcraft-sc.bio_con_single", "Mystcraft", "biocontrol", 2, "Constraint","Nature","Infinite","Static",0);
    public static final PageSymbol BIO_CON_TILED = register("bio_con_tiled", "symbol.mystcraft-sc.bio_con_tiled", "Mystcraft", "biocontrol", 2, "Constraint","Nature","Chain","Contradict",0);
    public static final PageSymbol BIO_CON_PATCHY = register("bio_con_patchy", "symbol.mystcraft-sc.bio_con_patchy", "Mystcraft", "biocontrol", 2, "Constraint","Nature","Weave","Tradition",0);
    public static final PageSymbol BIO_CON_BOXES = register("bio_con_boxes", "symbol.mystcraft-sc.bio_con_boxes", "More Mystcraft", "biocontrol", 2, "Constraint","Nature","Cycle","Boxes Biome Distribution",0);
    public static final PageSymbol BIO_CON_ISLAND = register("bio_con_island", "symbol.mystcraft-sc.bio_con_island", "More Mystcraft", "biocontrol", 2, "Constraint","Nature","Weave","Island Biome Distribution",0);
    public static final PageSymbol BIO_CON_EWBAND = register("bio_con_ewband", "symbol.mystcraft-sc.bio_con_ewband", "More Mystcraft", "biocontrol", 2, "Constraint","Nature","Cycle","East-West Bands Biome Distribution",0);
    public static final PageSymbol BIO_CON_NSBAND = register("bio_con_nsband", "symbol.mystcraft-sc.bio_con_nsband", "More Mystcraft", "biocontrol", 2, "Constraint","Nature","Cycle","North-South Bands Biome Distribution",0);
    public static final PageSymbol BIO_CON_RINGS = register("bio_con_rings", "symbol.mystcraft-sc.bio_con_rings", "More Mystcraft", "biocontrol", 2, "Constraint","Nature","Cycle","Rings Biome Distribution",0);
    public static final PageSymbol BIO_CON_MAZE = register("bio_con_maze", "symbol.mystcraft-sc.bio_con_maze", "More Mystcraft", "biocontrol", 2, "Constraint","Nature","Weave","Maze Biome Distribution",0);

// EFFECTS

    public static final PageSymbol EFF_ACCELERATED = register("eff_accelerated", "symbol.mystcraft-sc.eff_accelerated", "Mystcraft", "effect", 1, "Survival","Dynamic","Change","Spur",0);
    public static final PageSymbol EFF_EXPLOSIONS = register("eff_explosions", "symbol.mystcraft-sc.eff_explosions", "Mystcraft", "effect", 3, "Survival","Sacrifice","Power","Force",0);
    public static final PageSymbol EFF_LIGHTNING = register("eff_lightning", "symbol.mystcraft-sc.eff_lightning", "Mystcraft", "effect", 3, "Survival","Sacrifice","Power","Energy",0);
    public static final PageSymbol EFF_METEOR = register("eff_meteor", "symbol.mystcraft-sc.eff_meteor", "Mystcraft", "effect", 3, "Survival","Sacrifice","Power","Momentum",0);
    public static final PageSymbol EFF_SCORCH = register("eff_scorch", "symbol.mystcraft-sc.eff_scorch", "Mystcraft", "effect", 3, "Survival","Sacrifice","Power","Chaos",0);

// LIGHTING

    public static final PageSymbol LIGHT_BRIGHT = register("light_bright", "symbol.mystcraft-sc.light_bright", "Mystcraft", "lighting", 1, "Ethereal","Power","Infinite","Spur",0);
    public static final PageSymbol LIGHT_DARK = register("light_dark", "symbol.mystcraft-sc.light_dark", "Mystcraft", "lighting", 1, "Ethereal","Void","Constraint","Inhibit",0);
    public static final PageSymbol LIGHT_NAT = register("light_nat", "symbol.mystcraft-sc.light_nat", "Mystcraft", "lighting", 1, "Ethereal","Dynamic","Cycle","Balance",0);

// DIRECTIONS

    public static final PageSymbol DIR_NORTH = register("dir_north", "symbol.mystcraft-sc.dir_north", "Mystcraft", "modifier/direction", 1, "Transform","Flow","Motion","Control",0);
    public static final PageSymbol DIR_EAST = register("dir_east", "symbol.mystcraft-sc.dir_east", "Mystcraft", "modifier/direction", 1, "Transform","Flow","Motion","Tradition",0);
    public static final PageSymbol DIR_SOUTH = register("dir_south", "symbol.mystcraft-sc.dir_south", "Mystcraft", "modifier/direction", 1, "Transform","Flow","Motion","Chaos",0);
    public static final PageSymbol DIR_WEST = register("dir_west", "symbol.mystcraft-sc.dir_west", "Mystcraft", "modifier/direction", 1, "Transform","Flow","Motion","Change",0) ;
    public static final PageSymbol DIR_NORTHEAST = register("dir_northeast", "symbol.mystcraft-sc.dir_northeast", "Myst Library", "modifier/direction", 1, "Transform","Motion","Northeast Direction","Northeast Direction",0) ;
    public static final PageSymbol DIR_NORTHWEST = register("dir_northwest", "symbol.mystcraft-sc.dir_northwest", "Myst Library", "modifier/direction", 1, "Transform","Motion","Northwest Direction","Northwest Direction",0) ;
    public static final PageSymbol DIR_SOUTHEAST = register("dir_southeast", "symbol.mystcraft-sc.dir_southeast", "Myst Library", "modifier/direction", 1, "Transform","Motion","Southeast Direction","Southeast Direction",0) ;
    public static final PageSymbol DIR_SOUTHWEST = register("dir_southwest", "symbol.mystcraft-sc.dir_southwest", "Myst Library", "modifier/direction", 1, "Transform","Motion","Southwest Direction","Southwest Direction",0) ;

// CELESTIAL LENGTH

    public static final PageSymbol LENGTH_ZERO = register("length_zero", "symbol.mystcraft-sc.length_zero", "Mystcraft", "modifier/length", 1, "Transform","Time","System","Inhibit",0);
    public static final PageSymbol LENGTH_HALF = register("length_half", "symbol.mystcraft-sc.length_half", "Mystcraft", "modifier/length", 1, "Transform","Time","System","Stimulate",0);
    public static final PageSymbol LENGTH_FULL = register("length_full", "symbol.mystcraft-sc.length_full", "Mystcraft", "modifier/length", 1, "Transform","Time","System","Balance",0);
    public static final PageSymbol LENGTH_DOUBLE = register("length_double", "symbol.mystcraft-sc.length_double", "Mystcraft", "modifier/length", 1, "Transform","Time","System","Sacrifice",0);
    public static final PageSymbol LENGTH_FIFTH = register("length_fifth", "symbol.mystcraft-sc.length_fifth", "Myst Library", "modifier/length", 1, "Transform","Time","System","One Fifth Length",0);
    public static final PageSymbol LENGTH_THIRD = register("length_third", "symbol.mystcraft-sc.length_third", "Myst Library", "modifier/length", 1, "Transform","Time","System","One Third Length",0);
    public static final PageSymbol LENGTH_TRIPLE = register("length_triple", "symbol.mystcraft-sc.length_triple", "Myst Library", "modifier/length", 1, "Transform","Time","System","Triple Length",0);
    public static final PageSymbol LENGTH_QUINTUPLE = register("length_quintuple", "symbol.mystcraft-sc.length_quintuple", "Myst Library", "modifier/length", 1, "Transform","Time","System","Quintuple Length",0);
    public static final PageSymbol LENGTH_REAL = register("length_real", "symbol.mystcraft-sc.length_real", "Myst Library", "modifier/length", 1, "Transform","Time","System","Real Length",0);

// CELESTIAL ANGLE

    public static final PageSymbol ANGLE_NADIR = register("angle_nadir", "symbol.mystcraft-sc.angle_nadir", "Mystcraft", "modifier/angle", 1, "Transform","Cycle","System","Rebirth",0);
    public static final PageSymbol ANGLE_RISING = register("angle_rising", "symbol.mystcraft-sc.angle_rising", "Mystcraft", "modifier/angle", 1, "Transform","Cycle","System","Growth",0);
    public static final PageSymbol ANGLE_NOON = register("angle_noon", "symbol.mystcraft-sc.angle_noon", "Mystcraft", "modifier/angle", 1, "Transform","Cycle","System","Harmony",0);
    public static final PageSymbol ANGLE_SETTING = register("angle_setting", "symbol.mystcraft-sc.angle_setting", "Mystcraft", "modifier/angle", 1, "Transform","Cycle","System","Future",0);
    public static final PageSymbol ANGLE_WANING_DAY = register("angle_waning_day", "symbol.mystcraft-sc.angle_waning_day", "Myst Library", "modifier/angle", 1, "Transform","Cycle","Waning Day Phase","Waning Day Phase",0);
    public static final PageSymbol ANGLE_WANING_NIGHT = register("angle_waning_night", "symbol.mystcraft-sc.angle_waning_night", "Myst Library", "modifier/angle", 1, "Transform","Cycle","Waning Night Phase","Waning Night Phase",0);
    public static final PageSymbol ANGLE_WAXING_DAY = register("angle_waxing_day", "symbol.mystcraft-sc.angle_waxing_day", "Myst Library", "modifier/angle", 1, "Transform","Cycle","Waxing Day Phase","Waxing Day Phase",0);
    public static final PageSymbol ANGLE_WAXING_NIGHT = register("angle_waxing_night", "symbol.mystcraft-sc.angle_waxing_night", "Myst Library", "modifier/angle", 1, "Transform","Cycle","Waxing Night Phase","Waxing Night Phase",0);

//FEATURES

    public static final PageSymbol FEATURE_SMALL_CRYSTALS = register("feature_small_crystals", "symbol.mystcraft-sc.feature_small_crystals", "Mystcraft", "feature/small", 1, "Nature","Encourage","Growth","Static",0);
    public static final PageSymbol FEATURE_SMALL_STAR_FISSURE = register("feature_small_star_fissure", "symbol.mystcraft-sc.feature_small_star_fissure", "Mystcraft", "feature/small", 1, "Nature","Harmony","Mutual","Void",0);
    public static final PageSymbol FEATURE_SMALL_SURFACE_LAKES = register("feature_small_surface_lakes", "symbol.mystcraft-sc.feature_small_surface_lakes", "Mystcraft", "feature/small", 1, "Nature","Flow","Static","Elevate",0);
    public static final PageSymbol FEATURE_SMALL_OBELISKS = register("feature_small_obelisks", "symbol.mystcraft-sc.feature_small_obelisks", "Mystcraft", "feature/small", 1, "Civilization","Resilience","Static","Form",0);
    public static final PageSymbol FEATURE_SMALL_DEEP_LAKES = register("feature_small_deep_lakes", "symbol.mystcraft-sc.feature_small_deep_lakes", "Mystcraft", "feature/small", 1, "Nature","Flow","Static","Explore",0);
    public static final PageSymbol FEATURE_SMALL_WITCH_HUTS = register("feature_small_witch_huts", "symbol.mystcraft-sc.feature_small_witch_huts", "Mystcraft:SC", "feature/small", 1, "Nature","Flow","Static","Explore",0);
    public static final PageSymbol FEATURE_SMALL_RUINED_PORTALS = register("feature_small_ruined_portals", "symbol.mystcraft-sc.feature_small_ruined_portals", "Mystcraft:SC", "feature/small", 1, "Nature","Flow","Static","Explore",0);
    public static final PageSymbol FEATURE_SMALL_FOSSILS = register("feature_small_fossils", "symbol.mystcraft-sc.feature_small_fossils", "Mystcraft:SC", "feature/small", 1, "Nature","Flow","Static","Explore",0);
    public static final PageSymbol FEATURE_SMALL_IGLOOS = register("feature_small_igloos", "symbol.mystcraft-sc.feature_small_igloos", "Mystcraft:SC", "feature/small", 1, "Nature","Flow","Static","Explore",0);
    public static final PageSymbol FEATURE_SMALL_BURIED_TREASURE = register("feature_small_buried_treasure", "symbol.mystcraft-sc.feature_small_buried_treasure", "Mystcraft:SC", "feature/small", 1, "Nature","Flow","Static","Explore",0);

    public static final PageSymbol FEATURE_MEDIUM_DUNGEONS = register("feature_medium_dungeons", "symbol.mystcraft-sc.feature_medium_dungeons", "Mystcraft", "feature/medium", 1, "Civilization","Constraint","Chain","Resurrect",0);
    public static final PageSymbol FEATURE_MEDIUM_RAVINES = register("feature_medium_ravines", "symbol.mystcraft-sc.feature_medium_ravines", "Mystcraft", "terrain/medium", 1, "Terrain","Transform","Void","Weave",0);
    public static final PageSymbol FEATURE_MEDIUM_STRONGHOLDS = register("feature_medium_strongholds", "symbol.mystcraft-sc.feature_medium_strongholds", "Mystcraft", "feature/medium", 1, "Civilization","Wisdom","Future","Honor",0);
    public static final PageSymbol FEATURE_MEDIUM_VILLAGES = register("feature_medium_villages", "symbol.mystcraft-sc.feature_medium_villages", "Mystcraft", "feature/medium", 1, "Civilization","Society","Harmony","Nurture",0);
    public static final PageSymbol FEATURE_MEDIUM_SPIKES = register("feature_medium_spikes", "symbol.mystcraft-sc.feature_medium_spikes", "Mystcraft", "feature/medium", 1, "Nature","Encourage","Entropy","Static",0);
    public static final PageSymbol FEATURE_MEDIUM_MINESHAFTS = register("feature_medium_mineshafts", "symbol.mystcraft-sc.feature_medium_mineshafts", "Mystcraft", "feature/medium", 1, "Civilization","Machine","Motion","Tradition",0);
    public static final PageSymbol FEATURE_MEDIUM_SPHERES = register("feature_medium_spheres", "symbol.mystcraft-sc.feature_medium_spheres", "Mystcraft", "feature_medium", 1, "Terrain","Transform","Form","Cycle",0);
    public static final PageSymbol FEATURE_MEDIUM_NETHER_FORTRESSES = register("feature_medium_nether_fortresses", "symbol.mystcraft-sc.feature_medium_nether_fortresses", "Mystcraft", "feature/medium", 1, "Civilization","Machine","Power","Entropy",0);
    public static final PageSymbol FEATURE_MEDIUM_ABANDONED_STUDIES = register("feature_medium_abandoned_studies", "symbol.mystcraft-sc.feature_medium_abandoned_studies", "More Mystcraft", "feature/medium", 1, "Civilization","Possibility","Power","Wisdom,",0);
    public static final PageSymbol FEATURE_MEDIUM_OCEAN_MONUMENTS = register("feature_medium_ocean_monuments", "symbol.mystcraft-sc.feature_medium_ocean_monuments", "More Mystcraft", "feature/medium", 1, "Civilization","Machine","Power","Entropy",0);
    public static final PageSymbol FEATURE_MEDIUM_PYRAMIDS = register("feature_medium_pyramids", "symbol.mystcraft-sc.feature_medium_pyramids", "More Mystcraft", "feature/medium", 1, "Civilization","Constraint","Creativity","Elevate",0);
    public static final PageSymbol FEATURE_MEDIUM_TRIAL_CHAMBERS = register("feature_medium_trial_chambers", "symbol.mystcraft-sc.feature_medium_trial_chambers", "Mystcraft:SC", "feature/medium", 1, "Nature","Flow","Static","Explore",0);
    public static final PageSymbol FEATURE_MEDIUM_WOODLAND_MANSIONS = register("feature_medium_woodland_mansions", "symbol.mystcraft-sc.feature_medium_woodland_mansions", "Mystcraft:SC", "feature/medium", 1, "Nature","Flow","Static","Explore",0);
    public static final PageSymbol FEATURE_MEDIUM_BASTIONS = register("feature_medium_bastions", "symbol.mystcraft-sc.feature_medium_bastions", "Mystcraft:SC", "feature/medium", 1, "Nature","Flow","Static","Explore",0);
    public static final PageSymbol FEATURE_MEDIUM_ANCIENT_CITIES = register("feature_medium_ancient_cities", "symbol.mystcraft-sc.feature_medium_ancient_cities", "Mystcraft:SC", "feature/medium", 1, "Nature","Flow","Static","Explore",0);
    public static final PageSymbol FEATURE_MEDIUM_END_CITIES = register("feature_medium_end_cities", "symbol.mystcraft-sc.feature_medium_end_cities", "Mystcraft:SC", "feature/medium", 1, "Nature","Flow","Static","Explore",0);
    public static final PageSymbol FEATURE_MEDIUM_TEMPLES = register("feature_medium_temples", "symbol.mystcraft-sc.feature_medium_temples", "Mystcraft:SC", "feature/medium", 1, "Nature","Flow","Static","Explore",0);
    public static final PageSymbol FEATURE_MEDIUM_PILLAGER_OUTPOSTS = register("feature_medium_pillager_outposts", "symbol.mystcraft-sc.feature_medium_pillager_outposts", "Mystcraft:SC", "feature/medium", 1, "Nature","Flow","Static","Explore",0);
    public static final PageSymbol FEATURE_MEDIUM_SHIPWRECKS = register("feature_medium_shipwrecks", "symbol.mystcraft-sc.feature_medium_shipwrecks", "Mystcraft:SC", "feature/medium", 1, "Nature","Flow","Static","Explore",0);
    public static final PageSymbol FEATURE_MEDIUM_OCEAN_RUINS = register("feature_medium_ocean_ruins", "symbol.mystcraft-sc.feature_medium_ocean_ruins", "Mystcraft:SC", "feature/medium", 1, "Nature","Flow","Static","Explore",0);
    public static final PageSymbol FEATURE_MEDIUM_TRAIL_RUINS = register("feature_medium_trail_ruins", "symbol.mystcraft-sc.feature_medium_trail_ruins", "Mystcraft:SC", "feature/medium", 1, "Nature","Flow","Static","Explore",0);
    public static final PageSymbol FEATURE_MEDIUM_GEODES = register("feature_medium_geodes", "symbol.mystcraft-sc.feature_medium_geodes", "Mystcraft:SC", "feature/medium", 1, "Nature","Flow","Static","Explore",0);
    public static final PageSymbol FEATURE_MEDIUM_LIBRARIES = register("feature_medium_libraries", "symbol.mystcraft-sc.feature_medium_libraries", "Mystcraft:SC", "feature/medium", 1, "Civilization","Power","Wisdom","Tradition",0);

    public static final PageSymbol FEATURE_LARGE_CAVES = register("feature_large_caves", "symbol.mystcraft-sc.feature_large_caves", "Mystcraft", "feature/large", 1, "Terrain","Transform","Void","Flow",0);
    public static final PageSymbol FEATURE_LARGE_FLOAT_ISLANDS = register("feature_large_float_islands", "symbol.mystcraft-sc.feature_large_float_islands", "Mystcraft", "feature/large", 1, "Terrain","Transform","Form","Celestial",0);
    public static final PageSymbol FEATURE_LARGE_TENDRILS = register("feature_large_tendrils", "symbol.mystcraft-sc.feature_large_tendrils", "Mystcraft", "feature/large", 1, "Terrain","Transform","Growth","Flow",0);
    public static final PageSymbol FEATURE_LARGE_HUGE_TREES = register("feature_large_huge_trees", "symbol.mystcraft-sc.feature_large_huge_trees", "Mystcraft", "feature/large", 1, "Nature","Stimulate","Spur","Elevate",0);
    public static final PageSymbol FEATURE_LARGE_GIGANTIC_TREES = register("feature_large_gigantic_trees", "symbol.mystcraft-sc.feature_large_gigantic_trees", "More Mystcraft", "feature/large", 1, "Nature","Stimulate","Spur","Elevate",0);

    public static final PageSymbol FEATURE_SMALL_DUMMY = register("feature_small_dummy", "symbol.mystcraft-sc.feature_small_dummy", "Mystcraft", "feature/small", 1, "Contradict","Chaos","Exist","Form",0);
    public static final PageSymbol FEATURE_MEDIUM_DUMMY = register("feature_medium_dummy", "symbol.mystcraft-sc.feature_medium_dummy", "Mystcraft", "feature/medium", 1, "Contradict","Chaos","Exist","Balance",0);
    public static final PageSymbol FEATURE_LARGE_DUMMY = register("feature_large_dummy", "symbol.mystcraft-sc.feature_large_dummy", "Mystcraft", "feature/large", 1, "Contradict","Chaos","Exist","Terrain",0);

    public static final PageSymbol FEATURE_LARGE_DENSE_ORE = register("feature_large_dense_ore", "symbol.mystcraft-sc.feature_large_dense_ore", "Mystcraft", "feature/large", 3, "Survival","Stimulate","Machine","Chaos",0);
    public static final PageSymbol FEATURE_LARGE_LACKING_ORE = register("feature_large_lacking_ore", "symbol.mystcraft-sc.feature_large_lacking_ore", "More Mystcraft", "feature/large", 3, "Nature","Possibility","Form","Void",0);

// WEATHER

    public static final PageSymbol WEATHER_CONSTANT = register("weather_constant", "symbol.mystcraft-sc.weather_constant", "Mystcraft", "weather", 1, "Sustain","Static","Tradition","Stimulate",0);
    public static final PageSymbol WEATHER_OVERCAST = register("weather_overcast", "symbol.mystcraft-sc.weather_overcast", "Mystcraft", "weather", 1, "Sustain","Static","Believe","Motion",0);
    public static final PageSymbol WEATHER_FAST = register("weather_fast", "symbol.mystcraft-sc.weather_fast", "Mystcraft", "weather", 1, "Sustain","Dynamic","Tradition","Spur",0);
    public static final PageSymbol WEATHER_NAT = register("weather_nat", "symbol.mystcraft-sc.weather_nat", "Mystcraft", "weather", 1, "Sustain","Dynamic","Tradition","Balance",0);
    public static final PageSymbol WEATHER_OFF = register("weather_off", "symbol.mystcraft-sc.weather_off", "Mystcraft", "weather", 1, "Sustain","Static","Stimulate","Energy",0);
    public static final PageSymbol WEATHER_RAIN = register("weather_rain", "symbol.mystcraft-sc.weather_rain", "Mystcraft", "weather", 1, "Sustain","Static","Rebirth","Growth",0);
    public static final PageSymbol WEATHER_SLOW = register("weather_slow", "symbol.mystcraft-sc.weather_slow", "Mystcraft", "weather", 1, "Sustain","Dynamic","Tradition","Inhibit",0);
    public static final PageSymbol WEATHER_SNOW = register("weather_snow", "symbol.mystcraft-sc.weather_snow", "Mystcraft", "weather", 1, "Sustain","Static","Inhibit","Energy",0);
    public static final PageSymbol WEATHER_STORM = register("weather_storm", "symbol.mystcraft-sc.weather_storm", "Mystcraft", "weather", 1, "Sustain","Static","Nature","Power",0);

// TERRAIN

    public static final PageSymbol TERRAIN_AMPLIFIED = register("terrain_amplified", "symbol.mystcraft-sc.terrain_amplified", "Mystcraft", "terrain", 1, "Terrain","Form","Tradition","Spur",0);
    public static final PageSymbol TERRAIN_END = register("terrain_end", "symbol.mystcraft-sc.terrain_end", "Mystcraft", "terrain", 1, "Terrain","Form","Ethereal","Flow",0);
    public static final PageSymbol TERRAIN_SKYLANDS = register("terrain_skylands", "symbol.mystcraft-sc.terrain_skylands", "Mystcraft", "terrain", 1, "Terrain","Transform","Void","Elevate",0);
    public static final PageSymbol TERRAIN_FLAT = register("terrain_flat", "symbol.mystcraft-sc.terrain_flat", "Mystcraft", "terrain", 1, "Terrain","Form","Inhibit","Motion",0);
    public static final PageSymbol TERRAIN_NETHER = register("terrain_nether", "symbol.mystcraft-sc.terrain_nether", "Mystcraft", "terrain", 1, "Terrain","Form","Constraint","Entropy",0);
    public static final PageSymbol TERRAIN_NORMAL = register("terrain_normal", "symbol.mystcraft-sc.terrain_normal", "Mystcraft", "terrain", 1, "Terrain","Form","Tradition","Flow",0);
    public static final PageSymbol TERRAIN_VOID = register("terrain_void", "symbol.mystcraft-sc.terrain_void", "Mystcraft", "terrain", 1, "Terrain","Form","Infinite","Void",0);
    public static final PageSymbol TERRAIN_ISLAND = register("terrain_island", "symbol.mystcraft-sc.terrain_island", "Mystcraft", "terrain", 1, "Terrain","Form","Ethereal","Flow",0);
    public static final PageSymbol TERRAIN_MAZE = register("terrain_maze", "symbol.mystcraft-sc.terrain_maze", "More Mystcraft", "terrain", 1, "Terrain","Form","Infinite","Maze",0);
    public static final PageSymbol TERRAIN_BACKROOMS = register("terrain_backrooms", "symbol.mystcraft-sc.terrain_backrooms", "Mystcraft:SC", "terrain", 1, "Terrain","Form","Infinite","Maze",0);

// MISC

    public static final PageSymbol CLEAR_MODS = register("clear_mods", "symbol.mystcraft-sc.clear_mods", "Mystcraft", "misc", 1, "Contradict","Transform","Change","Void",0);
    public static final PageSymbol GRADIENT_COLOR = register("gradient_color", "symbol.mystcraft-sc.gradient_color", "Mystcraft", "misc", 1, "Transform","Image","Merge","Weave",0);
    public static final PageSymbol HORIZON_COLOR = register("horizon_color", "symbol.mystcraft-sc.horizon_color", "Mystcraft", "misc", 1, "Transform","Image","Celestial","Change",0);

// BLOCKS
    //// TERRAIN BLOCKS

    public static final PageSymbol BLOCK_T_DIRT = register("block_t_dirt", "symbol.mystcraft-sc.block_t_dirt", "Mystcraft", "block/terrain", 1, "Modifier","Constraint","Terrain","Dirt Block",0);
    public static final PageSymbol BLOCK_T_STONE = register("block_t_stone", "symbol.mystcraft-sc.block_t_stone", "Mystcraft", "block/terrain", 1, "Modifier","Constraint","Terrain","Stone Block_0",0);
    public static final PageSymbol BLOCK_T_ANDESITE = register("block_t_andesite", "symbol.mystcraft-sc.block_t_andesite", "Mystcraft", "block/terrain", 1, "Modifier","Constraint","Terrain","Andesite Block",0);
    public static final PageSymbol BLOCK_T_GRANITE = register("block_t_granite", "symbol.mystcraft-sc.block_t_granite", "Mystcraft", "block/terrain", 1, "Modifier","Constraint","Terrain","Granite Block",0);
    public static final PageSymbol BLOCK_T_DIORITE = register("block_t_diorite", "symbol.mystcraft-sc.block_t_diorite", "Mystcraft", "block/terrain", 1, "Modifier","Constraint","Terrain","Diorite Block",0);
    public static final PageSymbol BLOCK_T_SANDSTONE = register("block_t_sandstone", "symbol.mystcraft-sc.block_t_sandstone", "Mystcraft", "block/terrain", 1, "Modifier","Constraint","Terrain","Sandstone Block",0);
    public static final PageSymbol BLOCK_T_NETHERRACK = register("block_t_netherrack", "symbol.mystcraft-sc.block_t_netherrack", "Mystcraft", "block/terrain", 1, "Modifier","Constraint","Terrain","Netherrack Block",0);
    public static final PageSymbol BLOCK_T_END_STONE = register("block_t_end_stone", "symbol.mystcraft-sc.block_t_end_stone", "Mystcraft", "block/terrain", 1, "Modifier","Constraint","Terrain","End Stone Block",0);

    //// CRYSTAL BLOCKS

    public static final PageSymbol BLOCK_C_ICE = register("block_c_ice", "symbol.mystcraft-sc.block_c_ice", "Mystcraft", "block/crystal", 1, "Modifier","Constraint","Chain","Ice Block",0);
    public static final PageSymbol BLOCK_C_PACKED_ICE = register("block_c_packed_ice", "symbol.mystcraft-sc.block_c_packed_ice", "Mystcraft", "block/crystal", 1, "Modifier","Constraint","Chain","Packed Ice Block",0);
    public static final PageSymbol BLOCK_C_GLASS = register("block_c_glass", "symbol.mystcraft-sc.block_c_glass", "Mystcraft", "block/crystal", 1, "Modifier","Constraint","Chain","Glass Block",0);
    public static final PageSymbol BLOCK_C_SNOW = register("block_c_snow", "symbol.mystcraft-sc.block_c_snow", "Mystcraft", "block/crystal", 1, "Modifier","Constraint","Chain","Snow Block",0);
    public static final PageSymbol BLOCK_C_OBSIDIAN = register("block_c_obsidian", "symbol.mystcraft-sc.block_c_obsidian", "Mystcraft", "block/crystal", 1, "Modifier","Constraint","Chain","Obsidian Block",0);
    public static final PageSymbol BLOCK_C_GLOWSTONE = register("block_c_glowstone", "symbol.mystcraft-sc.block_c_glowstone", "Mystcraft", "block/crystal", 1, "Modifier","Constraint","Chain","Glowstone Block",0);
    public static final PageSymbol BLOCK_C_CRYSTAL = register("block_c_crystal", "symbol.mystcraft-sc.block_c_crystal", "Mystcraft", "block/crystal", 1, "Modifier","Constraint","Chain","Crystal Block",0);

    //// ORE BLOCKS

    public static final PageSymbol BLOCK_O_DIAMOND = register("block_o_diamond", "symbol.mystcraft-sc.block_o_diamond", "Mystcraft", "block/ore", 1, "Modifier","Constraint","Ore","Diamond Ore",0);
    public static final PageSymbol BLOCK_O_GOLD = register("block_o_gold", "symbol.mystcraft-sc.block_o_gold", "Mystcraft", "block/ore", 1, "Modifier","Constraint","Ore","Gold Ore",0);
    public static final PageSymbol BLOCK_O_IRON = register("block_o_iron", "symbol.mystcraft-sc.block_o_iron", "Mystcraft", "block/ore", 1, "Modifier","Constraint","Ore","Iron Ore",0);
    public static final PageSymbol BLOCK_O_COAL = register("block_o_coal", "symbol.mystcraft-sc.block_o_coal", "Mystcraft", "block/ore", 1, "Modifier","Constraint","Ore","Coal Ore",0);
    public static final PageSymbol BLOCK_O_REDSTONE = register("block_o_redstone", "symbol.mystcraft-sc.block_o_redstone", "Mystcraft", "block/ore", 1, "Modifier","Constraint","Ore","Redstone Ore",0);
    public static final PageSymbol BLOCK_O_LAPIS = register("block_o_lapis", "symbol.mystcraft-sc.block_o_lapis", "Mystcraft", "block/ore", 1, "Modifier","Constraint","Ore","Lapis Lazuli Ore",0);
    public static final PageSymbol BLOCK_O_EMERALD = register("block_o_emerald", "symbol.mystcraft-sc.block_o_emerald", "Mystcraft", "block/ore", 1, "Modifier","Constraint","Ore","Emerald Ore",0);
    public static final PageSymbol BLOCK_O_QUARTZ = register("block_o_quartz", "symbol.mystcraft-sc.block_o_quartz", "Mystcraft", "block/ore", 1, "Modifier","Constraint","Chain","Quartz Ore",0);

    //// FLUID BLOCKS

    public static final PageSymbol BLOCK_F_GRAVEL = register("block_f_gravel", "symbol.mystcraft-sc.block_f_gravel", "Mystcraft", "block/fluid", 1, "Modifier","Constraint","Sea","Gravel Block",0);

    //// STRUCTURE BLOCKS

    public static final PageSymbol BLOCK_S_ANDESITE_BLOCK = register("block_s_andesite_block", "symbol.mystcraft-sc.block_s_andesite_block", "Mystcraft", "block/structure", 1, "Modifier","Constraint","Structure","Polished Andesite Block",0);
    public static final PageSymbol BLOCK_S_GRANITE_BLOCK = register("block_s_granite_block", "symbol.mystcraft-sc.block_s_granite_block", "Mystcraft", "block/structure", 1, "Modifier","Constraint","Structure","Polished Granite Block",0);
    public static final PageSymbol BLOCK_S_DIORITE_BLOCK = register("block_s_diorite_block", "symbol.mystcraft-sc.block_s_diorite_block", "Mystcraft", "block/structure", 1, "Modifier","Constraint","Structure","Polished Diorite Block",0);
    public static final PageSymbol BLOCK_S_OAK_LOG = register("block_s_oak_log", "symbol.mystcraft-sc.block_s_oak_log", "Mystcraft", "block/structure", 1, "Modifier","Constraint","Structure","Oak Log",0);
    public static final PageSymbol BLOCK_S_DARK_OAK_LOG = register("block_s_dark_oak_log", "symbol.mystcraft-sc.block_s_dark_oak_log", "Mystcraft", "block/structure", 1, "Modifier","Constraint","Structure","Dark Oak Log",0);
    public static final PageSymbol BLOCK_S_SPRUCE_LOG = register("block_s_spruce_log", "symbol.mystcraft-sc.block_s_spruce_log", "Mystcraft", "block/structure", 1, "Modifier","Constraint","Structure","Spruce Log",0);
    public static final PageSymbol BLOCK_S_JUNGLE_LOG = register("block_s_jungle_log", "symbol.mystcraft-sc.block_s_jungle_log", "Mystcraft", "block/structure", 1, "Modifier","Constraint","Structure","Jungle Log",0);
    public static final PageSymbol BLOCK_S_BIRCH_LOG = register("block_s_birch_log", "symbol.mystcraft-sc.block_s_birch_log", "Mystcraft", "block/structure", 1, "Modifier","Constraint","Structure","Birch Log",0);
    public static final PageSymbol BLOCK_S_ACACIA_LOG = register("block_s_acacia_log", "symbol.mystcraft-sc.block_s_acacia_log", "Mystcraft", "block/structure", 1, "Modifier","Constraint","Structure","Acacia Log",0);

    private MystcraftPageSymbols() {
    }

    public static void initialize() {
        MystcraftSyntheticCodex.LOGGER.info("Registered {} Mystcraft page symbols.", PageSymbolRegistry.size());
    }

    private static PageSymbol register(
            String path,
            String translationKey,
            String origin,
            String category,
            Integer cardRank,
            String word1,
            String word2,
            String word3,
            String word4,
            Integer tested
    ) {
        return PageSymbolRegistry.register(new PageSymbol(
                Identifier.fromNamespaceAndPath("mystcraft-sc", path),
                translationKey,
                cardRank == null ? 0 : cardRank,
                List.of(word1, word2, word3, word4),
                tested == null ? 0 : tested
        ));
    }
}