package myst.synthetic.world.dimension;

import myst.synthetic.world.age.AgeStoragePaths;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public final class AgeDimensionKeys {

    public static final String DIMENSION_NAMESPACE = "mystcraft-sc";

    private AgeDimensionKeys() {
    }

    public static ResourceKey<Level> levelKey(String dimensionUid) {
        return ResourceKey.create(Registries.DIMENSION, location(dimensionUid));
    }

    public static ResourceKey<LevelStem> levelStemKey(String dimensionUid) {
        return ResourceKey.create(Registries.LEVEL_STEM, location(dimensionUid));
    }

    public static Identifier location(String dimensionUid) {
        String normalized = AgeStoragePaths.normalizeDimensionUid(dimensionUid).toLowerCase(Locale.ROOT);
        return Identifier.fromNamespaceAndPath(DIMENSION_NAMESPACE, normalized);
    }

    public static String levelIdString(String dimensionUid) {
        return location(dimensionUid).toString();
    }

    public static @Nullable String toDimensionUid(@Nullable String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        Identifier parsed = Identifier.tryParse(raw.trim());
        if (parsed != null && DIMENSION_NAMESPACE.equals(parsed.getNamespace())) {
            return AgeStoragePaths.normalizeDimensionUid(parsed.getPath());
        }

        return AgeStoragePaths.normalizeDimensionUid(raw);
    }
}