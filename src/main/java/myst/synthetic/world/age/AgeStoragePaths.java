package myst.synthetic.world.age;

import java.nio.file.Path;
import java.util.Locale;

public final class AgeStoragePaths {

    public static final String ROOT_FOLDER_NAME = "MYSTDIMS";
    public static final String DIMENSION_PREFIX = "DIM";

    private AgeStoragePaths() {
    }

    public static String buildDimensionUid(int index) {
        int safeIndex = Math.max(1, index);
        return DIMENSION_PREFIX + safeIndex;
    }

    public static String normalizeDimensionUid(String dimensionUid) {
        if (dimensionUid == null || dimensionUid.isBlank()) {
            return buildDimensionUid(1);
        }

        String trimmed = dimensionUid.trim().toUpperCase(Locale.ROOT);
        if (trimmed.startsWith(DIMENSION_PREFIX)) {
            return trimmed;
        }

        return DIMENSION_PREFIX + trimmed;
    }

    public static Path root(Path worldRoot) {
        return worldRoot.resolve(ROOT_FOLDER_NAME);
    }

    public static Path dimensionFolder(Path worldRoot, String dimensionUid) {
        return root(worldRoot).resolve(normalizeDimensionUid(dimensionUid));
    }

    public static Path ageSpecFile(Path worldRoot, String dimensionUid) {
        return ageDataFile(worldRoot, dimensionUid);
    }

    public static Path ageDataFile(Path worldRoot, String dimensionUid) {
        return dimensionFolder(worldRoot, dimensionUid).resolve("age_data.json");
    }

    public static Path metadataFile(Path worldRoot, String dimensionUid) {
        return dimensionFolder(worldRoot, dimensionUid).resolve("metadata.json");
    }
}