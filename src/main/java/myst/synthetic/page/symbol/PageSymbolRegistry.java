package myst.synthetic.page.symbol;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PageSymbolRegistry {

    private static final Map<Identifier, PageSymbol> SYMBOLS = new LinkedHashMap<>();

    private PageSymbolRegistry() {
    }

    public static PageSymbol register(PageSymbol symbol) {
        PageSymbol existing = SYMBOLS.putIfAbsent(symbol.id(), symbol);
        if (existing != null) {
            throw new IllegalStateException("Duplicate page symbol registration: " + symbol.id());
        }
        return symbol;
    }

    public static PageSymbol registerOrReplace(PageSymbol symbol) {
        SYMBOLS.put(symbol.id(), symbol);
        return symbol;
    }

    public static void unregister(Identifier id) {
        SYMBOLS.remove(id);
    }

    @Nullable
    public static PageSymbol get(Identifier id) {
        return SYMBOLS.get(id);
    }

    public static boolean contains(Identifier id) {
        return SYMBOLS.containsKey(id);
    }

    public static Collection<PageSymbol> values() {
        return java.util.List.copyOf(SYMBOLS.values());
    }

    public static int size() {
        return SYMBOLS.size();
    }

    public static void clear() {
        SYMBOLS.clear();
    }
}