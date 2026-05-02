package myst.synthetic.client.age;

import myst.synthetic.MystcraftSyntheticCodex;
import myst.synthetic.network.AgeRenderDataPayload;
import org.jetbrains.annotations.Nullable;

public final class MystClientAgeRenderData {

    private static @Nullable String currentDimensionId = null;
    private static boolean hasMoonDirection = false;
    private static float moonDirection = 0.0F;

    private MystClientAgeRenderData() {
    }

    public static void apply(AgeRenderDataPayload payload) {
        currentDimensionId = payload.dimensionId();
        hasMoonDirection = payload.hasMoonDirection();
        moonDirection = normalizeDegrees(payload.moonDirection());

        MystcraftSyntheticCodex.LOGGER.info(
                "[MystAgeRenderClient] Applied render data: dimension={}, hasMoonDirection={}, moonDirection={}",
                currentDimensionId,
                hasMoonDirection,
                moonDirection
        );
    }

    public static void reset() {
        currentDimensionId = null;
        hasMoonDirection = false;
        moonDirection = 0.0F;
    }

    public static boolean hasMoonDirection() {
        return hasMoonDirection;
    }

    public static float moonDirection() {
        return moonDirection;
    }

    public static float adjustMoonAngle(float vanillaMoonAngle) {
        if (!hasMoonDirection) {
            return vanillaMoonAngle;
        }

        /*
         * First-pass interpretation:
         * direction is treated as an orbit-angle offset.
         *
         * If testing shows the visual direction is rotated, this is the single
         * place to tune sign/offset without touching the packet/parser path.
         */
        return vanillaMoonAngle + moonDirection;
    }

    private static float normalizeDegrees(float degrees) {
        float value = degrees % 360.0F;
        if (value < 0.0F) {
            value += 360.0F;
        }
        return value;
    }
}