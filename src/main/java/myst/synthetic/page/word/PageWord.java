package myst.synthetic.page.word;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public record PageWord(
        String key,
        @Nullable String translationKey,
        List<Integer> componentIndices,
        List<Integer> colors
) {

    public PageWord {
        key = normalizeKey(key);
        componentIndices = copyComponentIndices(componentIndices);
        colors = copyColors(colors);
    }

    public boolean isFallback() {
        return translationKey == null || translationKey.isBlank();
    }

    public static PageWord of(String key, String translationKey, Integer... components) {
        List<Integer> componentList = new ArrayList<>();
        for (Integer component : components) {
            if (component != null) {
                componentList.add(component);
            }
        }
        return new PageWord(key, translationKey, componentList, List.of());
    }

    public static PageWord fallback(String rawKey) {
        String normalized = normalizeKey(rawKey);

        java.util.Random rand = new java.util.Random(normalized.hashCode());
        int maxComponentIndex = 20;
        int count = rand.nextInt(10) + 3;

        if (normalized.startsWith("easter")) {
            count = 4;
            maxComponentIndex = 8;
        }

        List<Integer> generated = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            generated.add(rand.nextInt(maxComponentIndex) + 4);
        }

        return new PageWord(normalized, null, generated, List.of());
    }

    public int getColorForIndex(int index) {
        if (colors.isEmpty()) {
            return 0x000000;
        }
        if (index < colors.size()) {
            return colors.get(index);
        }
        return colors.get(0);
    }

    private static String normalizeKey(String key) {
        if (key == null) {
            return "";
        }

        return key.trim().toLowerCase(Locale.ROOT);
    }

    private static List<Integer> copyComponentIndices(List<Integer> componentIndices) {
        if (componentIndices == null || componentIndices.isEmpty()) {
            return List.of();
        }

        List<Integer> copied = new ArrayList<>();
        for (Integer value : componentIndices) {
            if (value != null && value >= 0) {
                copied.add(value);
            }
        }

        return List.copyOf(copied);
    }

    private static List<Integer> copyColors(List<Integer> colors) {
        if (colors == null || colors.isEmpty()) {
            return List.of();
        }

        List<Integer> copied = new ArrayList<>();
        for (Integer color : colors) {
            if (color != null) {
                copied.add(color);
            }
        }

        return List.copyOf(copied);
    }
}