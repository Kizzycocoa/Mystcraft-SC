package myst.synthetic.page.glyph;

import myst.synthetic.MystcraftSyntheticCodex;

public final class MystcraftGlyphComponents {

    private MystcraftGlyphComponents() {}

    public static void initialize() {

        for (int i = 0; i < 64; i++) {
            PageGlyphComponentRegistry.register(
                    new PageGlyphComponent(
                            i,
                            (i * 37f) % 360f,
                            1.0f
                    )
            );
        }

        MystcraftSyntheticCodex.LOGGER.info(
                "Registered {} glyph components",
                PageGlyphComponentRegistry.size()
        );
    }
}