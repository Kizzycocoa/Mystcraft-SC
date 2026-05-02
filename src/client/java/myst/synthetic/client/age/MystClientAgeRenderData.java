package myst.synthetic.client.age;

import myst.synthetic.MystcraftSyntheticCodex;
import myst.synthetic.network.AgeRenderDataPayload;
import org.jetbrains.annotations.Nullable;

public final class MystClientAgeRenderData {

    /*
     * Modern vanilla-ish baseline for the Age data format.
     *
     * We do NOT force clouds to exactly this number. Instead:
     *
     * renderedHeight = vanillaRuntimeHeight + (ageHeight - 192)
     *
     * That way, if the game's actual current default is slightly above 192,
     * that trailing vanilla offset is preserved.
     */
    public static final float AGE_DATA_DEFAULT_CLOUD_HEIGHT = 192.0F;

    private static @Nullable String currentDimensionId = null;
    private static boolean hasCloudHeight = false;
    private static float cloudHeight = AGE_DATA_DEFAULT_CLOUD_HEIGHT;

    private MystClientAgeRenderData() {
    }

    public static void apply(AgeRenderDataPayload payload) {
        currentDimensionId = payload.dimensionId();
        hasCloudHeight = payload.hasCloudHeight();
        cloudHeight = payload.cloudHeight();

        MystcraftSyntheticCodex.LOGGER.info(
                "[MystAgeRenderClient] Applied render data: dimension={}, hasCloudHeight={}, cloudHeight={}",
                currentDimensionId,
                hasCloudHeight,
                cloudHeight
        );
    }

    public static void reset() {
        currentDimensionId = null;
        hasCloudHeight = false;
        cloudHeight = AGE_DATA_DEFAULT_CLOUD_HEIGHT;
    }

    public static boolean hasCloudHeight() {
        return hasCloudHeight;
    }

    public static float cloudHeight() {
        return cloudHeight;
    }

    public static float adjustCloudHeight(float vanillaCloudHeight) {
        if (!hasCloudHeight) {
            return vanillaCloudHeight;
        }

        return vanillaCloudHeight + (cloudHeight - AGE_DATA_DEFAULT_CLOUD_HEIGHT);
    }
}