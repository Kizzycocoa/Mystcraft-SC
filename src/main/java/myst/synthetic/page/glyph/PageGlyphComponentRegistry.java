package myst.synthetic.page.glyph;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PageGlyphComponentRegistry {

    private static final Map<Integer, PageGlyphComponent> COMPONENTS = new LinkedHashMap<>();

    private PageGlyphComponentRegistry() {
    }

    public static PageGlyphComponent register(PageGlyphComponent component) {
        COMPONENTS.put(component.index(), component);
        return component;
    }

    public static PageGlyphComponent get(int index) {
        return COMPONENTS.get(index);
    }

    public static Collection<PageGlyphComponent> values() {
        return COMPONENTS.values();
    }

    public static int size() {
        return COMPONENTS.size();
    }
}