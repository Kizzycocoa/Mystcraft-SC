package myst.synthetic.ink;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class InkMixerEffects {

    public enum ConsumeAction {
        ADD_EFFECTS,
        RESET_TO_PLAIN_INK,
        NONE
    }

    private InkMixerEffects() {
    }

    public static ConsumeAction getConsumeAction(ItemStack stack) {
        if (stack.isEmpty()) {
            return ConsumeAction.NONE;
        }

        if (stack.is(Items.INK_SAC)) {
            return ConsumeAction.RESET_TO_PLAIN_INK;
        }

        if (!getItemEffects(stack).isEmpty()) {
            return ConsumeAction.ADD_EFFECTS;
        }

        return ConsumeAction.NONE;
    }

    public static Map<String, Float> getItemEffects(ItemStack stack) {
        Map<String, Float> result = new HashMap<>();

        if (stack.isEmpty()) {
            return result;
        }

        if (stack.is(Items.MUSHROOM_STEW)) {
            result.put("mystcraft-sc:link_disarm", 0.05F);
        }

        if (stack.is(Items.EXPERIENCE_BOTTLE)) {
            result.put("mystcraft-sc:link_intra", 0.15F);
        }

        if (stack.is(Items.ENDER_PEARL)) {
            result.put("mystcraft-sc:link_intra", 0.15F);
            result.put("mystcraft-sc:link_disarm", 0.15F);
        }

        if (stack.is(Items.FEATHER)) {
            result.put("mystcraft-sc:link_momentum", 0.15F);
        }

        if (stack.is(Items.GUNPOWDER)) {
            result.put("mystcraft-sc:link_disarm", 0.20F);
        }

        if (stack.is(Items.CLAY_BALL)) {
            result.put("mystcraft-sc:link_platform", 0.25F);
        }

        if (stack.is(Items.FIRE_CHARGE)) {
            result.put("mystcraft-sc:link_disarm", 0.25F);
        }

        return result;
    }

    public static boolean isPropertyAllowed(String propertyId) {
        return propertyId != null && !propertyId.isBlank();
    }

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
        return getConsumeAction(stack) != ConsumeAction.NONE;
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