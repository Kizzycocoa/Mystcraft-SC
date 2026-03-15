package myst.synthetic.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MystcraftConfig {

    public static final String CATEGORY_GENERAL = "general";
    public static final String CATEGORY_TEXTURE = "texture";
    public static final String CATEGORY_ENTITY = "entity";
    public static final String CATEGORY_DEBUG = "debug";
    public static final String CATEGORY_RENDER = "render";
    public static final String CATEGORY_CLIENT = "client";
    public static final String CATEGORY_SERVER = "server";

    public static final String CATEGORY_SYMBOL = "symbol";

    public static final String CATEGORY_INSTABILITY = "instability";

    public static final String CATEGORY_BASELINING = "baselining";
    public static final String CATEGORY_FLUIDS = "fluids";

    private static final Logger LOGGER = LoggerFactory.getLogger("mystcraft-sc/config");
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static final Path CONFIG_DIR = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("mystcraft-sc");

    private static final Path CORE_PATH = CONFIG_DIR.resolve("core.cfg");
    private static final Path BALANCE_PATH = CONFIG_DIR.resolve("balance.cfg");
    private static final Path SYMBOLS_PATH = CONFIG_DIR.resolve("symbols.cfg");
    private static final Path INSTABILITIES_PATH = CONFIG_DIR.resolve("instabilities.cfg");

    private static JsonObject coreRoot = createCoreDefaults();
    private static JsonObject balanceRoot = createBalanceDefaults();
    private static JsonObject symbolsRoot = createSymbolsDefaults();
    private static JsonObject instabilitiesRoot = createInstabilitiesDefaults();

    private MystcraftConfig() {
    }

    public static void load() {
        coreRoot = loadFile(CORE_PATH, createCoreDefaults());
        balanceRoot = loadFile(BALANCE_PATH, createBalanceDefaults());
        symbolsRoot = loadFile(SYMBOLS_PATH, createSymbolsDefaults());
        instabilitiesRoot = loadFile(INSTABILITIES_PATH, createInstabilitiesDefaults());
    }

    public static void save() {
        saveFile(CORE_PATH, coreRoot);
        saveFile(BALANCE_PATH, balanceRoot);
        saveFile(SYMBOLS_PATH, symbolsRoot);
        saveFile(INSTABILITIES_PATH, instabilitiesRoot);
    }

    public static Path getConfigDir() {
        return CONFIG_DIR;
    }

    public static Path getCorePath() {
        return CORE_PATH;
    }

    public static Path getBalancePath() {
        return BALANCE_PATH;
    }

    public static Path getSymbolsPath() {
        return SYMBOLS_PATH;
    }

    public static Path getInstabilitiesPath() {
        return INSTABILITIES_PATH;
    }

    public static boolean getBoolean(String category, String key, boolean fallback) {
        JsonElement element = getElement(category, key);
        if (element == null || element.isJsonNull()) {
            return fallback;
        }

        try {
            if (element.isJsonPrimitive()) {
                if (element.getAsJsonPrimitive().isBoolean()) {
                    return element.getAsBoolean();
                }
                if (element.getAsJsonPrimitive().isString()) {
                    String value = element.getAsString();
                    if (value == null || value.isBlank()) {
                        return fallback;
                    }
                    return Boolean.parseBoolean(value);
                }
            }
        } catch (Exception ignored) {
        }

        return fallback;
    }

    public static int getInt(String category, String key, int fallback) {
        JsonElement element = getElement(category, key);
        if (element == null || element.isJsonNull()) {
            return fallback;
        }

        try {
            if (element.isJsonPrimitive()) {
                if (element.getAsJsonPrimitive().isNumber()) {
                    return element.getAsInt();
                }
                if (element.getAsJsonPrimitive().isString()) {
                    String value = element.getAsString();
                    if (value == null || value.isBlank()) {
                        return fallback;
                    }
                    return Integer.parseInt(value);
                }
            }
        } catch (Exception ignored) {
        }

        return fallback;
    }

    public static float getFloat(String category, String key, float fallback) {
        JsonElement element = getElement(category, key);
        if (element == null || element.isJsonNull()) {
            return fallback;
        }

        try {
            if (element.isJsonPrimitive()) {
                if (element.getAsJsonPrimitive().isNumber()) {
                    return element.getAsFloat();
                }
                if (element.getAsJsonPrimitive().isString()) {
                    String value = element.getAsString();
                    if (value == null || value.isBlank()) {
                        return fallback;
                    }
                    return Float.parseFloat(value);
                }
            }
        } catch (Exception ignored) {
        }

        return fallback;
    }

    public static String getString(String category, String key, String fallback) {
        JsonElement element = getElement(category, key);
        if (element == null || element.isJsonNull()) {
            return fallback;
        }

        try {
            if (element.isJsonPrimitive()) {
                String value = element.getAsString();
                if (value == null || value.isBlank()) {
                    return fallback;
                }
                return value;
            }
        } catch (Exception ignored) {
        }

        return fallback;
    }

    public static JsonObject getCategory(String category) {
        JsonObject root = getRootForCategory(category);
        JsonElement element = root.get(category);
        if (element != null && element.isJsonObject()) {
            return element.getAsJsonObject();
        }
        return new JsonObject();
    }

    private static JsonObject loadFile(Path path, JsonObject defaults) {
        JsonObject loaded = null;

        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                JsonElement parsed = JsonParser.parseReader(reader);
                if (parsed != null && parsed.isJsonObject()) {
                    loaded = parsed.getAsJsonObject();
                } else {
                    LOGGER.warn("Mystcraft config at {} was not a JSON object. Replacing with defaults.", path);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to read Mystcraft config at {}. Using defaults.", path, e);
            }
        }

        if (loaded == null) {
            JsonObject created = defaults.deepCopy();
            saveFile(path, created);
            LOGGER.info("Created default Mystcraft config at {}", path);
            return created;
        }

        boolean changed = mergeMissing(loaded, defaults);
        if (changed) {
            saveFile(path, loaded);
            LOGGER.info("Updated Mystcraft config with missing defaults at {}", path);
        } else {
            LOGGER.info("Loaded Mystcraft config from {}", path);
        }

        return loaded;
    }

    private static void saveFile(Path path, JsonObject root) {
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(root, writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save Mystcraft config at {}", path, e);
        }
    }

    private static JsonElement getElement(String category, String key) {
        JsonObject root = getRootForCategory(category);
        JsonElement current = root.get(category);
        if (current == null || !current.isJsonObject()) {
            return null;
        }

        JsonObject currentObject = current.getAsJsonObject();
        String[] parts = key.split("\\.");

        for (int i = 0; i < parts.length; i++) {
            JsonElement next = currentObject.get(parts[i]);
            if (next == null) {
                return null;
            }

            if (i == parts.length - 1) {
                return next;
            }

            if (!next.isJsonObject()) {
                return null;
            }

            currentObject = next.getAsJsonObject();
        }

        return null;
    }

    private static JsonObject getRootForCategory(String category) {
        return switch (category) {
            case CATEGORY_GENERAL, CATEGORY_TEXTURE, CATEGORY_ENTITY, CATEGORY_DEBUG,
                 CATEGORY_RENDER, CATEGORY_CLIENT, CATEGORY_SERVER -> coreRoot;

            case CATEGORY_BASELINING, CATEGORY_FLUIDS -> balanceRoot;

            case CATEGORY_SYMBOL -> symbolsRoot;

            case CATEGORY_INSTABILITY -> instabilitiesRoot;

            default -> throw new IllegalArgumentException("Unknown Mystcraft config category: " + category);
        };
    }

    private static boolean mergeMissing(JsonObject target, JsonObject defaults) {
        boolean changed = false;

        for (String key : defaults.keySet()) {
            JsonElement defaultValue = defaults.get(key);
            JsonElement existingValue = target.get(key);

            if (existingValue == null) {
                target.add(key, defaultValue.deepCopy());
                changed = true;
                continue;
            }

            if (defaultValue.isJsonObject() && existingValue.isJsonObject()) {
                if (mergeMissing(existingValue.getAsJsonObject(), defaultValue.getAsJsonObject())) {
                    changed = true;
                }
            }
        }

        return changed;
    }

    private static JsonObject createCoreDefaults() {
        JsonObject defaults = new JsonObject();

        defaults.add(CATEGORY_GENERAL, new JsonObject());
        defaults.add(CATEGORY_TEXTURE, new JsonObject());
        defaults.add(CATEGORY_ENTITY, new JsonObject());
        defaults.add(CATEGORY_DEBUG, new JsonObject());
        defaults.add(CATEGORY_RENDER, new JsonObject());
        defaults.add(CATEGORY_CLIENT, new JsonObject());
        defaults.add(CATEGORY_SERVER, new JsonObject());

        putBoolean(defaults, CATEGORY_GENERAL, "configs.generate_template.balance", false);
        putBoolean(defaults, CATEGORY_GENERAL, "commands.spawnmeteor.enabled", false);
        putBoolean(defaults, CATEGORY_GENERAL, "crafting.linkbook.enabled", true);
        ensureObject(defaults, CATEGORY_GENERAL, "crafting.linkeffects");
        putBoolean(defaults, CATEGORY_GENERAL, "generation.villageDeskGen", true);
        putBoolean(defaults, CATEGORY_GENERAL, "respawning.respawnInAges", true);
        putBoolean(defaults, CATEGORY_GENERAL, "teleportation.requireUUIDTest", false);
        putString(defaults, CATEGORY_GENERAL, "teleportation.homedim", "minecraft:overworld");
        putBoolean(defaults, CATEGORY_GENERAL, "ids.villager.archivist", true);
        putInt(defaults, CATEGORY_GENERAL, "ids.dim_provider", 1210950779);

        putBoolean(defaults, CATEGORY_RENDER, "renderlabels", false);
        putBoolean(defaults, CATEGORY_RENDER, "fast_rainbows", true);

        return defaults;
    }

    private static JsonObject createBalanceDefaults() {
        JsonObject defaults = new JsonObject();

        defaults.add(CATEGORY_BASELINING, new JsonObject());
        defaults.add(CATEGORY_FLUIDS, new JsonObject());

        putBoolean(defaults, CATEGORY_BASELINING, "client.persave", false);
        putBoolean(defaults, CATEGORY_BASELINING, "useconfigs", false);
        putBoolean(defaults, CATEGORY_BASELINING, "server.disconnectclients", false);
        putInt(defaults, CATEGORY_BASELINING, "tickrate.minimum", 40);

        return defaults;
    }

    private static JsonObject createSymbolsDefaults() {
        JsonObject defaults = new JsonObject();
        defaults.add(CATEGORY_SYMBOL, new JsonObject());
        return defaults;
    }

    private static JsonObject createInstabilitiesDefaults() {
        JsonObject defaults = new JsonObject();
        defaults.add(CATEGORY_INSTABILITY, new JsonObject());

        putInt(defaults, CATEGORY_INSTABILITY, "global.difficulty", 2);
        putBoolean(defaults, CATEGORY_INSTABILITY, "global.enabled", true);

        return defaults;
    }

    private static void putBoolean(JsonObject root, String category, String path, boolean value) {
        JsonObject object = ensureObject(root, category, path);
        object.addProperty(lastPathPart(path), value);
    }

    private static void putInt(JsonObject root, String category, String path, int value) {
        JsonObject object = ensureObject(root, category, path);
        object.addProperty(lastPathPart(path), value);
    }

    private static void putString(JsonObject root, String category, String path, String value) {
        JsonObject object = ensureObject(root, category, path);
        object.addProperty(lastPathPart(path), value);
    }

    private static JsonObject ensureObject(JsonObject root, String category, String path) {
        JsonObject categoryObject = root.getAsJsonObject(category);
        if (categoryObject == null) {
            categoryObject = new JsonObject();
            root.add(category, categoryObject);
        }

        String[] parts = path.split("\\.");
        JsonObject current = categoryObject;

        for (int i = 0; i < parts.length - 1; i++) {
            JsonElement next = current.get(parts[i]);
            if (next == null || !next.isJsonObject()) {
                JsonObject created = new JsonObject();
                current.add(parts[i], created);
                current = created;
            } else {
                current = next.getAsJsonObject();
            }
        }

        return current;
    }

    private static String lastPathPart(String path) {
        int index = path.lastIndexOf('.');
        return index >= 0 ? path.substring(index + 1) : path;
    }
}