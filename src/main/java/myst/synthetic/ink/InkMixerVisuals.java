package myst.synthetic.ink;

import java.util.Map;

public final class InkMixerVisuals {

    private InkMixerVisuals() {
    }

    public static int getMixedColor(Map<String, Float> probabilities) {
        if (probabilities.isEmpty()) {
            return 0x101018;
        }

        float totalWeight = 0.0F;
        float red = 0.0F;
        float green = 0.0F;
        float blue = 0.0F;

        for (Map.Entry<String, Float> entry : probabilities.entrySet()) {
            float weight = Math.max(0.0F, entry.getValue());
            if (weight <= 0.0F) {
                continue;
            }

            int color = getPropertyColor(entry.getKey());
            red += ((color >> 16) & 0xFF) * weight;
            green += ((color >> 8) & 0xFF) * weight;
            blue += (color & 0xFF) * weight;
            totalWeight += weight;
        }

        if (totalWeight <= 0.0F) {
            return 0x101018;
        }

        int r = clamp255(Math.round(red / totalWeight));
        int g = clamp255(Math.round(green / totalWeight));
        int b = clamp255(Math.round(blue / totalWeight));

        return (r << 16) | (g << 8) | b;
    }

    public static int getPropertyColor(String propertyId) {
        return switch (propertyId) {
            case "mystcraft-sc:link_disarm" -> 0xD66A2C;
            case "mystcraft-sc:link_intra" -> 0x7A4FD6;
            case "mystcraft-sc:link_momentum" -> 0x9FD8E8;
            case "mystcraft-sc:link_platform" -> 0xB88A5A;
            case "mystcraft-sc:link_relative" -> 0xC44747;
            case "mystcraft-sc:link_stable" -> 0xC8B85A;
            default -> 0x4C4C64;
        };
    }

    public static int getOverlayAlpha(Map<String, Float> probabilities) {
        if (probabilities.isEmpty()) {
            return 0;
        }

        float totalWeight = 0.0F;
        for (float value : probabilities.values()) {
            totalWeight += Math.max(0.0F, value);
        }

        float clamped = Math.min(totalWeight, 1.0F);
        return clamp255(Math.round(70.0F + (clamped * 110.0F)));
    }

    public static int packArgb(int alpha, int rgb) {
        return (clamp255(alpha) << 24) | (rgb & 0xFFFFFF);
    }

    private static int clamp255(int value) {
        return Math.max(0, Math.min(255, value));
    }
}