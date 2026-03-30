package myst.synthetic.page.word;

import myst.synthetic.MystcraftSyntheticCodex;

import java.util.List;

public final class MystcraftPageWords {

    public static final PageWord TEST = register("test", "word.mystcraft-sc.test", 0, 1, 2, 3);
    public static final PageWord DEBUG = register("debug", "word.mystcraft-sc.debug", 4, 5, 6, 7);
    public static final PageWord PAGE = register("page", "word.mystcraft-sc.page", 8, 9, 10, 11);
    public static final PageWord SYMBOL = register("symbol", "word.mystcraft-sc.symbol", 12, 13, 14, 15);

    public static final PageWord CELESTIAL = register("celestial", "word.mystcraft-sc.celestial", 16, 17, 18, 19);
    public static final PageWord LIGHT = register("light", "word.mystcraft-sc.light", 20, 21, 22, 23);
    public static final PageWord DAY = register("day", "word.mystcraft-sc.day", 24, 25, 26, 27);
    public static final PageWord BALANCE = register("balance", "word.mystcraft-sc.balance", 28, 29, 30, 31);

    public static final PageWord NIGHT = register("night", "word.mystcraft-sc.night", 32, 33, 34, 35);
    public static final PageWord PATTERN = register("pattern", "word.mystcraft-sc.pattern", 36, 37, 38, 39);

    public static final PageWord SKY = register("sky", "word.mystcraft-sc.sky", 40, 41, 42, 43);
    public static final PageWord CALM = register("calm", "word.mystcraft-sc.calm", 44, 45, 46, 47);
    public static final PageWord WATER = register("water", "word.mystcraft-sc.water", 48, 49, 50, 51);
    public static final PageWord MOTION = register("motion", "word.mystcraft-sc.motion", 52, 53, 54, 55);
    public static final PageWord SOUND = register("sound", "word.mystcraft-sc.sound", 56, 57, 58, 59);

    public static final PageWord STRUCTURE = register("structure", "word.mystcraft-sc.structure", 60, 61, 62, 63);
    public static final PageWord LIFE = register("life", "word.mystcraft-sc.life", 1, 5, 9, 13);
    public static final PageWord ORDER = register("order", "word.mystcraft-sc.order", 2, 6, 10, 14);
    public static final PageWord SETTLEMENT = register("settlement", "word.mystcraft-sc.settlement", 3, 7, 11, 15);

    public static final PageWord EARTH = register("earth", "word.mystcraft-sc.earth", 4, 8, 12, 16);
    public static final PageWord DEPTH = register("depth", "word.mystcraft-sc.depth", 17, 21, 25, 29);
    public static final PageWord WEALTH = register("wealth", "word.mystcraft-sc.wealth", 18, 22, 26, 30);
    public static final PageWord GROWTH = register("growth", "word.mystcraft-sc.growth", 19, 23, 27, 31);

    private MystcraftPageWords() {
    }

    public static void initialize() {
        MystcraftSyntheticCodex.LOGGER.info("Registered {} Mystcraft page words.", PageWordRegistry.size());
    }

    private static PageWord register(
            String key,
            String translationKey,
            int component0,
            int component1,
            int component2,
            int component3
    ) {
        return PageWordRegistry.register(new PageWord(
                key,
                translationKey,
                List.of(component0, component1, component2, component3)
        ));
    }
}