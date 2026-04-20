package myst.synthetic.page.symbol;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import myst.synthetic.MystcraftSyntheticCodex;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DirectionPageLoader {

    private static final Gson GSON = new Gson();
    private static final String BUILTIN_RESOURCE = "data/mystcraft-sc/pages/pages-directions.json";
    private static final Identifier RELOAD_ID = Identifier.fromNamespaceAndPath("mystcraft-sc", "direction_pages");
    private static final Set<Identifier> LOADED_SYMBOLS = new LinkedHashSet<>();

    private DirectionPageLoader() {
    }

    public static void initialize() {
        bootstrapBuiltin();
        registerReloadListener();
    }

    private static void bootstrapBuiltin() {
        try (InputStream stream = DirectionPageLoader.class.getClassLoader().getResourceAsStream(BUILTIN_RESOURCE)) {
            if (stream == null) {
                MystcraftSyntheticCodex.LOGGER.warn("Missing built-in direction page file: {}", BUILTIN_RESOURCE);
                return;
            }

            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                List<DirectionPageDefinition> definitions = parseDefinitions(reader, "mystcraft-sc");
                applyDefinitions(definitions);
            }
        } catch (Exception e) {
            MystcraftSyntheticCodex.LOGGER.error("Failed to bootstrap direction pages from {}", BUILTIN_RESOURCE, e);
        }
    }

    private static void registerReloadListener() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return RELOAD_ID;
            }

            @Override
            public void onResourceManagerReload(ResourceManager manager) {
                reloadFrom(manager);
            }
        });
    }

    private static void reloadFrom(ResourceManager manager) {
        List<DirectionPageDefinition> definitions = new ArrayList<>();

        Map<Identifier, Resource> resources = manager.listResources(
                "pages",
                id -> id.getPath().endsWith("pages-directions.json")
        );

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier fileId = entry.getKey();
            Resource resource = entry.getValue();

            try (Reader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                definitions.addAll(parseDefinitions(reader, fileId.getNamespace()));
            } catch (Exception e) {
                MystcraftSyntheticCodex.LOGGER.error("Failed to load direction page file {}", fileId, e);
            }
        }

        applyDefinitions(definitions);
    }

    private static void applyDefinitions(List<DirectionPageDefinition> definitions) {
        for (Identifier id : LOADED_SYMBOLS) {
            PageSymbolRegistry.unregister(id);
        }
        LOADED_SYMBOLS.clear();

        for (DirectionPageDefinition definition : definitions) {
            PageSymbol symbol = new PageSymbol(
                    definition.id(),
                    definition.translationKey(),
                    definition.category(),
                    definition.cardRank(),
                    definition.poemWords(),
                    definition.tested(),
                    definition.value()
            );

            PageSymbolRegistry.registerOrReplace(symbol);
            LOADED_SYMBOLS.add(symbol.id());
        }

        MystcraftSyntheticCodex.LOGGER.info("Loaded {} data-driven direction page symbols.", LOADED_SYMBOLS.size());
    }

    private static List<DirectionPageDefinition> parseDefinitions(Reader reader, String defaultNamespace) {
        JsonElement rootElement = GSON.fromJson(reader, JsonElement.class);
        List<DirectionPageDefinition> definitions = new ArrayList<>();

        if (rootElement == null || rootElement.isJsonNull()) {
            return definitions;
        }

        JsonArray pagesArray;
        if (rootElement.isJsonArray()) {
            pagesArray = rootElement.getAsJsonArray();
        } else {
            JsonObject rootObject = rootElement.getAsJsonObject();
            JsonElement pagesElement = rootObject.get("pages");
            if (pagesElement == null || !pagesElement.isJsonArray()) {
                return definitions;
            }
            pagesArray = pagesElement.getAsJsonArray();
        }

        for (JsonElement element : pagesArray) {
            if (!element.isJsonObject()) {
                continue;
            }

            JsonObject object = element.getAsJsonObject();

            String rawId = getString(object, "id", null);
            if (rawId == null || rawId.isBlank()) {
                continue;
            }

            Identifier id = rawId.contains(":")
                    ? Identifier.tryParse(rawId)
                    : Identifier.fromNamespaceAndPath(defaultNamespace, rawId);

            if (id == null) {
                continue;
            }

            String translationKey = getString(object, "translation_key", null);
            String category = getString(object, "category", "modifier/direction");
            int cardRank = getInt(object, "card_rank", 1);
            int tested = getInt(object, "tested", 0);
            Float value = getFloat(object, "value", null);
            List<String> poemWords = getStringList(object, "poem_words");

            if (poemWords.size() != 4) {
                MystcraftSyntheticCodex.LOGGER.warn("Skipping direction page {} because it does not have exactly 4 poem words.", id);
                continue;
            }

            definitions.add(new DirectionPageDefinition(
                    id,
                    translationKey,
                    category,
                    cardRank,
                    poemWords,
                    tested,
                    value
            ));
        }

        return definitions;
    }

    private static String getString(JsonObject object, String key, String fallback) {
        JsonElement element = object.get(key);
        if (element == null || element.isJsonNull()) {
            return fallback;
        }
        return element.getAsString();
    }

    private static int getInt(JsonObject object, String key, int fallback) {
        JsonElement element = object.get(key);
        if (element == null || element.isJsonNull()) {
            return fallback;
        }
        return element.getAsInt();
    }

    private static Float getFloat(JsonObject object, String key, Float fallback) {
        JsonElement element = object.get(key);
        if (element == null || element.isJsonNull()) {
            return fallback;
        }

        float value = element.getAsFloat();
        return Float.isFinite(value) ? value : fallback;
    }

    private static List<String> getStringList(JsonObject object, String key) {
        JsonElement element = object.get(key);
        if (element == null || !element.isJsonArray()) {
            return List.of();
        }

        List<String> values = new ArrayList<>();
        for (JsonElement entry : element.getAsJsonArray()) {
            if (!entry.isJsonNull()) {
                values.add(entry.getAsString());
            }
        }
        return List.copyOf(values);
    }

    private record DirectionPageDefinition(
            Identifier id,
            String translationKey,
            String category,
            int cardRank,
            List<String> poemWords,
            int tested,
            Float value
    ) {
    }
}