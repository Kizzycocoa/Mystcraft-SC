package myst.synthetic.page.word;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class PageWordRegistry {

    private static final Map<String, PageWord> WORDS = new LinkedHashMap<>();

    private PageWordRegistry() {
    }

    public static PageWord register(PageWord word) {
        PageWord existing = WORDS.putIfAbsent(word.key(), word);
        if (existing != null) {
            throw new IllegalStateException("Duplicate page word registration: " + word.key());
        }
        return word;
    }

    @Nullable
    public static PageWord get(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }

        return WORDS.get(normalizeKey(key));
    }

    public static PageWord resolve(String key) {
        PageWord word = get(key);
        return word != null ? word : PageWord.fallback(key);
    }

    public static boolean contains(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }

        return WORDS.containsKey(normalizeKey(key));
    }

    public static Collection<PageWord> values() {
        return List.copyOf(WORDS.values());
    }

    public static int size() {
        return WORDS.size();
    }

    public static void clear() {
        WORDS.clear();
    }

    private static String normalizeKey(String key) {
        return key.trim().toLowerCase(Locale.ROOT);
    }
}