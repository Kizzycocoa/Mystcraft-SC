package myst.synthetic.client.render;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public record PageRenderKey(
        Kind kind,
        @Nullable Identifier symbolId
) {

    public enum Kind {
        BLANK,
        LINK_PANEL,
        SYMBOL,
        BLANK_CONTENT,
        LINK_PANEL_CONTENT,
        SYMBOL_CONTENT
    }

    public String cacheKey() {
        return switch (kind) {
            case BLANK -> "blank";
            case LINK_PANEL -> "link_panel";
            case SYMBOL -> "symbol_" + sanitize(symbolId);
            case BLANK_CONTENT -> "blank_content";
            case LINK_PANEL_CONTENT -> "link_panel_content";
            case SYMBOL_CONTENT -> "symbol_content_" + sanitize(symbolId);
        };
    }

    private static String sanitize(@Nullable Identifier id) {
        return id == null ? "missing" : id.toString().replace(':', '_').replace('/', '_');
    }
}