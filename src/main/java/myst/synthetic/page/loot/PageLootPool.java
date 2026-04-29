package myst.synthetic.page.loot;

import myst.synthetic.page.symbol.PageSymbol;
import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.Optional;

public record PageLootPool(
        String id,
        int minRank,
        int maxRank,
        List<String> requiredTags,
        List<String> anyTags,
        List<String> categoryRoots
) {
    public boolean matches(PageSymbol symbol) {
        if (symbol.cardRank() < minRank || symbol.cardRank() > maxRank) {
            return false;
        }

        for (String tag : requiredTags) {
            if (!symbol.lootTags().contains(tag)) {
                return false;
            }
        }

        if (!anyTags.isEmpty()) {
            boolean matched = false;
            for (String tag : anyTags) {
                if (symbol.lootTags().contains(tag)) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }

        if (!categoryRoots.isEmpty() && !categoryRoots.contains(symbol.rootCategory())) {
            return false;
        }

        return true;
    }

    public Optional<PageSymbol> pick(List<PageSymbol> symbols, RandomSource random) {
        List<PageSymbol> matches = symbols.stream()
                .filter(this::matches)
                .toList();

        if (matches.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(matches.get(random.nextInt(matches.size())));
    }
}