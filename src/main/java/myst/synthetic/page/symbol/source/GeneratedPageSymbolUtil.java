package myst.synthetic.page.symbol.source;

import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class GeneratedPageSymbolUtil {

    private GeneratedPageSymbolUtil() {
    }

    public static List<String> biomeWords(Identifier id) {
        List<String> tokens = extractTokens(id, true, false);
        return List.of(
                "Terrain",
                pick(tokens, 0, "Nature"),
                pick(tokens, 1, "Biome"),
                "Nature"
        );
    }

    public static List<String> fluidWords(Identifier id) {
        List<String> tokens = extractTokens(id, true, true);
        return List.of(
                "Flow",
                pick(tokens, 0, "Fluid"),
                pick(tokens, 1, "Constraint"),
                "Constraint"
        );
    }

    public static String generatedTranslationKey(String category, Identifier id) {
        String safe = id.getNamespace() + "." + id.getPath().replace('/', '.').replace('-', '_');
        return "symbol.mystcraft-sc.generated." + category + "." + safe;
    }

    private static List<String> extractTokens(Identifier id, boolean includeNamespaceIfModded, boolean stripFluidWords) {
        LinkedHashSet<String> output = new LinkedHashSet<>();

        if (includeNamespaceIfModded && !"minecraft".equals(id.getNamespace())) {
            addSplitTokens(output, id.getNamespace(), stripFluidWords);
        }

        addSplitTokens(output, id.getPath(), stripFluidWords);

        return List.copyOf(output);
    }

    private static void addSplitTokens(LinkedHashSet<String> output, String raw, boolean stripFluidWords) {
        for (String token : raw.split("[/_\\-]+")) {
            if (token.isBlank()) {
                continue;
            }

            if (stripFluidWords) {
                if (token.equals("flowing") || token.equals("still") || token.equals("fluid")) {
                    continue;
                }
            }

            output.add(capitalize(token));
        }
    }

    private static String pick(List<String> values, int index, String fallback) {
        if (index >= 0 && index < values.size()) {
            return values.get(index);
        }
        return fallback;
    }

    private static String capitalize(String input) {
        if (input.isBlank()) {
            return input;
        }

        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }
}