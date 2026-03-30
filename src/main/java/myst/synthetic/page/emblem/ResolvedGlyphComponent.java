package myst.synthetic.page.emblem;

import myst.synthetic.page.glyph.PageGlyphComponent;

public record ResolvedGlyphComponent(
        PageGlyphSlot slot,
        PageGlyphComponent component,
        int componentOrder
) {
}