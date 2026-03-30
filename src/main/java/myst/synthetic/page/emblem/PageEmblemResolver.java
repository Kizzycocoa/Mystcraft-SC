package myst.synthetic.page.emblem;

import myst.synthetic.page.symbol.PageSymbol;
import myst.synthetic.page.symbol.PageSymbolRegistry;
import myst.synthetic.page.word.PageWord;
import myst.synthetic.page.word.PageWordRegistry;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class PageEmblemResolver {

    private PageEmblemResolver() {
    }

    public static ResolvedPageEmblem resolve(PageSymbol symbol) {
        List<ResolvedPageWord> words = new ArrayList<>();

        List<String> poemWords = symbol.poemWords();
        for (int i = 0; i < poemWords.size() && i < 4; i++) {
            String rawWord = poemWords.get(i);
            PageWord resolvedWord = PageWordRegistry.resolve(rawWord);

            words.add(new ResolvedPageWord(
                    PageGlyphSlot.fromPoemIndex(i),
                    rawWord,
                    resolvedWord
            ));
        }

        return new ResolvedPageEmblem(symbol.id(), words);
    }

    @Nullable
    public static ResolvedPageEmblem resolve(Identifier symbolId) {
        PageSymbol symbol = PageSymbolRegistry.get(symbolId);
        if (symbol == null) {
            return null;
        }

        return resolve(symbol);
    }
}