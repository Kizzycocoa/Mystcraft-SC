package myst.synthetic.page.symbol;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import myst.synthetic.MystcraftSyntheticCodex;
import myst.synthetic.page.PageValue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DatapackPageLoader {

    private static final Gson GSON = new Gson();
    private static final Identifier RELOAD_ID = Identifier.fromNamespaceAndPath("mystcraft-sc", "value_pages");

    private static final List<PageFileSpec> FILES = List.of(
            new PageFileSpec("pages-directions.json"),
            new PageFileSpec("pages-lengths.json"),
            new PageFileSpec("pages-angles.json"),
            new PageFileSpec("pages-tilts.json"),
            new PageFileSpec("pages-sizes.json"),
            new PageFileSpec("pages-colors.json"),
            new PageFileSpec("pages-blocks-terrain.json"),
            new PageFileSpec("pages-blocks-topsoil.json"),
            new PageFileSpec("pages-blocks-crystal.json"),
            new PageFileSpec("pages-blocks-ore.json"),
            new PageFileSpec("pages-blocks-fluid.json"),
            new PageFileSpec("pages-blocks-structure.json"),
            new PageFileSpec("pages-cloud-height.json"),
            new PageFileSpec("core-weather.json"),
            new PageFileSpec("core-weather-length.json"),
            new PageFileSpec("core-lighting.json"),
            new PageFileSpec("core-terrain.json"),
            new PageFileSpec("core-biome-controllers.json"),
            new PageFileSpec("core-color-modifiers.json"),
            new PageFileSpec("core-celestials.json"),
            new PageFileSpec("core-misc.json"),
            new PageFileSpec("core-effects.json"),
            new PageFileSpec("core-features-small.json"),
            new PageFileSpec("core-features-medium.json"),
            new PageFileSpec("core-features-large.json")
    );

    private static final Set<Identifier> LOADED_SYMBOLS = new LinkedHashSet<>();

    private DatapackPageLoader() {
    }

    public static void initialize() {
        bootstrapBuiltin();
        registerReloadListener();
    }

    private static void bootstrapBuiltin() {
        List<PageDefinition> definitions = new ArrayList<>();

        for (PageFileSpec file : FILES) {
            String builtinResource = "data/mystcraft-sc/pages/" + file.fileName();

            try (InputStream stream = DatapackPageLoader.class.getClassLoader().getResourceAsStream(builtinResource)) {
                if (stream == null) {
                    MystcraftSyntheticCodex.LOGGER.warn("Missing built-in value page file: {}", builtinResource);
                    continue;
                }

                try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    definitions.addAll(parseDefinitions(reader, "mystcraft-sc"));
                }
            } catch (Exception e) {
                MystcraftSyntheticCodex.LOGGER.error("Failed to bootstrap value pages from {}", builtinResource, e);
            }
        }

        applyDefinitions(definitions);
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
        List<PageDefinition> definitions = new ArrayList<>();

        for (PageFileSpec file : FILES) {
            Map<Identifier, Resource> resources = manager.listResources(
                    "pages",
                    id -> id.getPath().endsWith(file.fileName())
            );

            for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
                Identifier fileId = entry.getKey();
                Resource resource = entry.getValue();

                try (Reader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                    definitions.addAll(parseDefinitions(reader, fileId.getNamespace()));
                } catch (Exception e) {
                    MystcraftSyntheticCodex.LOGGER.error("Failed to load value page file {}", fileId, e);
                }
            }
        }

        applyDefinitions(definitions);
    }

    private static void applyDefinitions(List<PageDefinition> definitions) {
        for (Identifier id : LOADED_SYMBOLS) {
            PageSymbolRegistry.unregister(id);
        }
        LOADED_SYMBOLS.clear();

        for (PageDefinition definition : definitions) {
            PageSymbol symbol = new PageSymbol(
                    definition.id(),
                    definition.translationKey(),
                    definition.origin(),
                    definition.category(),
                    definition.cardRank(),
                    definition.poemWords(),
                    definition.tested(),
                    definition.value()
            );

            PageSymbolRegistry.registerOrReplace(symbol);
            LOADED_SYMBOLS.add(symbol.id());
        }

        MystcraftSyntheticCodex.LOGGER.info("Loaded {} data-driven value page symbols.", LOADED_SYMBOLS.size());
    }

    private static List<PageDefinition> parseDefinitions(Reader reader, String defaultNamespace) {
        JsonElement rootElement = GSON.fromJson(reader, JsonElement.class);
        List<PageDefinition> definitions = new ArrayList<>();

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
            String origin = getString(object, "origin", "");
            String category = getString(object, "category", "");
            int cardRank = getInt(object, "card_rank", 1);
            int tested = getInt(object, "tested", 0);
            PageValue value = getPageValue(object.get("value"));
            List<String> poemWords = getStringList(object, "poem_words");

            if (poemWords.size() != 4) {
                MystcraftSyntheticCodex.LOGGER.warn("Skipping value page {} because it does not have exactly 4 poem words.", id);
                continue;
            }

            definitions.add(new PageDefinition(
                    id,
                    translationKey,
                    origin,
                    category,
                    cardRank,
                    poemWords,
                    tested,
                    value
            ));
        }

        return definitions;
    }

    private static PageValue getPageValue(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }

        if (element.isJsonPrimitive()) {
            if (element.getAsJsonPrimitive().isNumber()) {
                float scalar = element.getAsFloat();
                return Float.isFinite(scalar) ? PageValue.scalar(scalar) : null;
            }

            if (element.getAsJsonPrimitive().isString()) {
                String text = element.getAsString();
                return (text == null || text.isBlank()) ? null : PageValue.text(text);
            }
        }

        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            if (array.size() != 3) {
                return null;
            }

            float r = array.get(0).getAsFloat();
            float g = array.get(1).getAsFloat();
            float b = array.get(2).getAsFloat();

            if (!Float.isFinite(r) || !Float.isFinite(g) || !Float.isFinite(b)) {
                return null;
            }

            return PageValue.vector(r, g, b);
        }

        return null;
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

    private record PageFileSpec(String fileName) {
    }

    private record PageDefinition(
            Identifier id,
            String translationKey,
            String origin,
            String category,
            int cardRank,
            List<String> poemWords,
            int tested,
            PageValue value
    ) {
    }
}