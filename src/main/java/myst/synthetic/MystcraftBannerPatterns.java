package myst.synthetic;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BannerPattern;

public final class MystcraftBannerPatterns {

    public static final TagKey<BannerPattern> MYST_POETRY_PATTERN_ITEM = TagKey.create(
            Registries.BANNER_PATTERN,
            Identifier.fromNamespaceAndPath("mystcraft-sc", "myst_poetry_pattern_item")
    );

    public static final TagKey<BannerPattern> MYST_NUMEROLOGY_PATTERN_ITEM = TagKey.create(
            Registries.BANNER_PATTERN,
            Identifier.fromNamespaceAndPath("mystcraft-sc", "myst_numerology_pattern_item")
    );

    private MystcraftBannerPatterns() {
    }

    public static void initialize() {
    }
}