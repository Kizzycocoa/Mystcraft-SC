package myst.synthetic.page.symbol;

import net.minecraft.resources.Identifier;

import java.util.List;

public record PageSymbol(
        Identifier id,
        String translationKey,
        int cardRank,
        List<String> poemWords
) {

    public PageSymbol {
        poemWords = List.copyOf(poemWords);
    }

    public boolean hasPoem() {
        return poemWords.size() == 4;
    }
}