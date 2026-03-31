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
        SYMBOL
    }

    public String cacheKey() {
        return switch (kind) {
            case BLANK -> "blank";
            case LINK_PANEL -> "link_panel";
            case SYMBOL -> "symbol_" + (symbolId == null ? "missing" : symbolId.toString().replace(':', '_').replace('/', '_'));
        };
    }
}