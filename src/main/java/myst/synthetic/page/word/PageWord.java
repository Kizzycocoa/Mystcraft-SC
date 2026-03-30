package myst.synthetic.page.word;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public record PageWord(
        String key,
        @Nullable String translationKey,
        List<Integer> componentIndices
) {

    public PageWord {
        key = normalizeKey(key);
        componentIndices = sanitizeComponentIndices(componentIndices);
    }

    public boolean isFallback() {
        return translationKey == null || translationKey.isBlank();
    }

    public String getDebugName() {
        if (key.isBlank()) {
            return "";
        }

        return Character.toUpperCase(key.charAt(0)) + key.substring(1);
    }

    public static PageWord fallback(String rawKey) {
        String normalized = normalizeKey(rawKey);
        int hash = normalized.hashCode();

        List<Integer> generated = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            generated.add(Math.floorMod(hash + (i * 37), 64));
        }

        return new PageWord(normalized, null, generated);
    }

    private static String normalizeKey(String key) {
        if (key == null) {
            return "";
        }

        return key.trim().toLowerCase(Locale.ROOT);
    }

    private static List<Integer> sanitizeComponentIndices(List<Integer> componentIndices) {
        if (componentIndices == null || componentIndices.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<Integer> cleaned = new LinkedHashSet<>();
        for (Integer value : componentIndices) {
            if (value != null && value >= 0) {
                cleaned.add(value);
            }
        }

        return List.copyOf(cleaned);
    }
}