package myst.synthetic.page.emblem;

import myst.synthetic.page.word.PageWord;

public record ResolvedPageWord(
        PageGlyphSlot slot,
        String rawWord,
        PageWord resolvedWord
) {
}