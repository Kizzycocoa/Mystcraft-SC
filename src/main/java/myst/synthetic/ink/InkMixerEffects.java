package myst.synthetic.ink;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.item.ItemStack;

/**
 * Central helper for Ink Mixer ingredient -> link property mixing.
 *
 * This is intentionally small and easy to expand.
 * Legacy Mystcraft had a much larger item effect map; this modern port starts
 * with a clean helper so you can add more ingredients later without bloating
 * the block entity.
 */
public final class InkMixerEffects {

    private InkMixerEffects() {
    }

    /**
     * Returns the property weights contributed by a consumed ingredient.
     *
     * Keys are link-property ids.
     * Values are probabilities in the 0.0 - 1.0 range.
     */
    public static Map<String, Float> getItemEffects(ItemStack stack) {
        Map<String, Float> result = new HashMap<>();

        if (stack.isEmpty()) {
            return result;
        }

        /*
         * Legacy-style ingredient mappings
         *
         * Values represent probability weights (0.0 – 1.0)
         */

        // Mushroom Stew → Disarm (5%)
        if (stack.is(net.minecraft.world.item.Items.MUSHROOM_STEW)) {
            result.put("mystcraft-sc:link_disarm", 0.05F);
        }

        // Bottle o' Enchanting → Intra Linking (15%)
        if (stack.is(net.minecraft.world.item.Items.EXPERIENCE_BOTTLE)) {
            result.put("mystcraft-sc:link_intra", 0.15F);
        }

        // Ender Pearl → Intra Linking (15%) + Disarm (15%)
        if (stack.is(net.minecraft.world.item.Items.ENDER_PEARL)) {
            result.put("mystcraft-sc:link_intra", 0.15F);
            result.put("mystcraft-sc:link_disarm", 0.15F);
        }

        // Feather → Maintain Momentum (15%)
        if (stack.is(net.minecraft.world.item.Items.FEATHER)) {
            result.put("mystcraft-sc:link_momentum", 0.15F);
        }

        // Gunpowder → Disarm (20%)
        if (stack.is(net.minecraft.world.item.Items.GUNPOWDER)) {
            result.put("mystcraft-sc:link_disarm", 0.20F);
        }

        // Clay Ball → Generate Platform (25%)
        if (stack.is(net.minecraft.world.item.Items.CLAY_BALL)) {
            result.put("mystcraft-sc:link_platform", 0.25F);
        }

        // Fire Charge → Disarm (25%)
        if (stack.is(net.minecraft.world.item.Items.FIRE_CHARGE)) {
            result.put("mystcraft-sc:link_disarm", 0.25F);
        }

        return result;
    }

    /**
     * Whether the given property is currently allowed in the mixer.
     *
     * This mirrors the legacy "is property allowed" gate.
     * For now this is permissive; later you can forbid unfinished properties here.
     */
    public static boolean isPropertyAllowed(String propertyId) {
        return propertyId != null && !propertyId.isBlank();
    }

    /**
     * Legacy-style probability blending:
     *
     * 1) Sum all valid incoming probabilities
     * 2) Scale existing probabilities by (1 - totalIncoming)
     * 3) Add incoming probabilities on top
     */
    public static void blendInto(Map<String, Float> existing, Map<String, Float> incoming) {
        if (incoming.isEmpty()) {
            return;
        }

        float totalIncoming = 0.0F;
        Map<String, Float> filtered = new HashMap<>();

        for (Map.Entry<String, Float> entry : incoming.entrySet()) {
            String key = entry.getKey();
            float value = clamp(entry.getValue());

            if (!isPropertyAllowed(key) || value <= 0.0F) {
                continue;
            }

            filtered.put(key, value);
            totalIncoming += value;
        }

        if (filtered.isEmpty()) {
            return;
        }

        totalIncoming = Math.min(totalIncoming, 1.0F);
        float existingScale = 1.0F - totalIncoming;

        for (Map.Entry<String, Float> entry : new HashMap<>(existing).entrySet()) {
            existing.put(entry.getKey(), clamp(entry.getValue() * existingScale));
        }

        for (Map.Entry<String, Float> entry : filtered.entrySet()) {
            existing.merge(entry.getKey(), entry.getValue(), Float::sum);
            existing.put(entry.getKey(), clamp(existing.get(entry.getKey())));
        }
    }

    public static boolean canConsumeIngredient(ItemStack stack) {
        return !getItemEffects(stack).isEmpty();
    }

    private static float clamp(float value) {
        if (value < 0.0F) {
            return 0.0F;
        }
        if (value > 1.0F) {
            return 1.0F;
        }
        return value;
    }
}