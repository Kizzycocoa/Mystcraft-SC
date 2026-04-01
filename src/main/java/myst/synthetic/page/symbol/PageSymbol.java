package myst.synthetic.page.symbol;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record PageSymbol(
        Identifier id,
        @Nullable String translationKey,
        int cardRank,
        List<String> poemWords
) {

    public PageSymbol {
        poemWords = List.copyOf(poemWords);
    }

    public boolean hasPoem() {
        return poemWords.size() == 4;
    }

    public Component displayName() {
        if (translationKey != null && !translationKey.isBlank()) {
            return Component.translatable(translationKey);
        }

        return Component.literal(prettyNameFromId(id));
    }

    private static String prettyNameFromId(Identifier id) {
        String path = id.getPath();
        int slash = path.lastIndexOf('/');
        if (slash >= 0 && slash + 1 < path.length()) {
            path = path.substring(slash + 1);
        }

        String[] parts = path.split("[_\\-]+");
        StringBuilder builder = new StringBuilder();

        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }

            if (!builder.isEmpty()) {
                builder.append(' ');
            }

            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }

        if (builder.isEmpty()) {
            return id.toString();
        }

        return builder.toString();
    }
}