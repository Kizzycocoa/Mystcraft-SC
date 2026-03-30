package myst.synthetic.page.emblem;

import net.minecraft.resources.Identifier;

import java.util.List;

public record ResolvedPageEmblem(
        Identifier symbolId,
        List<ResolvedPageWord> words
) {

    public ResolvedPageEmblem {
        words = List.copyOf(words);
    }

    public boolean isEmpty() {
        return words.isEmpty();
    }
}