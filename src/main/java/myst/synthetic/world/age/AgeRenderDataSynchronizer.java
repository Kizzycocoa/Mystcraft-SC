package myst.synthetic.world.age;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import myst.synthetic.MystcraftSyntheticCodex;
import myst.synthetic.network.AgeRenderDataPayload;
import myst.synthetic.world.dimension.AgeDimensionKeys;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AgeRenderDataSynchronizer {

    private AgeRenderDataSynchronizer() {
    }

    public static void sendForCurrentLevel(ServerPlayer player) {
        if (player == null || player.level() == null) {
            return;
        }

        if (player.level() instanceof ServerLevel serverLevel) {
            sendForLevel(player, serverLevel);
        }
    }

    public static void sendForLevel(ServerPlayer player, ServerLevel level) {
        if (player == null || level == null) {
            return;
        }

        Identifier dimensionId = level.dimension().identifier();

        if (!AgeDimensionKeys.DIMENSION_NAMESPACE.equals(dimensionId.getNamespace())) {
            ServerPlayNetworking.send(player, new AgeRenderDataPayload(
                    dimensionId.toString(),
                    false,
                    0.0F
            ));

            MystcraftSyntheticCodex.LOGGER.info(
                    "[MystAgeRender] Sent vanilla render reset for '{}' in '{}'.",
                    player.getScoreboardName(),
                    dimensionId
            );
            return;
        }

        String dimensionUid = AgeDimensionKeys.toDimensionUid(dimensionId.toString());
        Float moonDirection = readFirstMoonDirection(level.getServer(), dimensionUid);

        ServerPlayNetworking.send(player, new AgeRenderDataPayload(
                dimensionId.toString(),
                moonDirection != null,
                moonDirection == null ? 0.0F : moonDirection
        ));

        MystcraftSyntheticCodex.LOGGER.info(
                "[MystAgeRender] Sent render data for '{}' in '{}': moonDirection={}",
                player.getScoreboardName(),
                dimensionId,
                moonDirection == null ? "<none>" : moonDirection
        );
    }

    @Nullable
    private static Float readFirstMoonDirection(MinecraftServer server, @Nullable String dimensionUid) {
        if (server == null || dimensionUid == null || dimensionUid.isBlank()) {
            return null;
        }

        Path worldRoot = server.getWorldPath(LevelResource.ROOT);
        Path ageDataFile = AgeStoragePaths.ageDataFile(worldRoot, dimensionUid);

        if (!Files.isRegularFile(ageDataFile)) {
            MystcraftSyntheticCodex.LOGGER.warn(
                    "[MystAgeRender] No age_data.json found for {} at {}.",
                    dimensionUid,
                    ageDataFile
            );
            return null;
        }

        try (Reader reader = Files.newBufferedReader(ageDataFile)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            JsonObject global = getObject(root, "global");
            JsonObject celestials = getObject(global, "celestials");
            JsonArray moons = getArray(celestials, "moons");

            if (moons == null || moons.isEmpty()) {
                return null;
            }

            JsonElement first = moons.get(0);
            if (!first.isJsonObject()) {
                return null;
            }

            JsonObject moon = first.getAsJsonObject();
            JsonElement direction = moon.get("direction");

            if (direction == null || !direction.isJsonPrimitive() || !direction.getAsJsonPrimitive().isNumber()) {
                return null;
            }

            return direction.getAsFloat();
        } catch (IOException | IllegalStateException | ClassCastException e) {
            MystcraftSyntheticCodex.LOGGER.error(
                    "[MystAgeRender] Failed reading moon render data for {}.",
                    dimensionUid,
                    e
            );
            return null;
        }
    }

    @Nullable
    private static JsonObject getObject(@Nullable JsonObject parent, String key) {
        if (parent == null) {
            return null;
        }

        JsonElement element = parent.get(key);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    @Nullable
    private static JsonArray getArray(@Nullable JsonObject parent, String key) {
        if (parent == null) {
            return null;
        }

        JsonElement element = parent.get(key);
        return element != null && element.isJsonArray() ? element.getAsJsonArray() : null;
    }
}