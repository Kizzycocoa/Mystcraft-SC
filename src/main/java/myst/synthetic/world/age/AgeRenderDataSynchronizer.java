package myst.synthetic.world.age;

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
        Float cloudHeight = readCloudHeight(level.getServer(), dimensionUid);

        ServerPlayNetworking.send(player, new AgeRenderDataPayload(
                dimensionId.toString(),
                cloudHeight != null,
                cloudHeight == null ? 0.0F : cloudHeight
        ));

        MystcraftSyntheticCodex.LOGGER.info(
                "[MystAgeRender] Sent render data for '{}' in '{}': cloudHeight={}",
                player.getScoreboardName(),
                dimensionId,
                cloudHeight == null ? "<none>" : cloudHeight
        );
    }

    @Nullable
    private static Float readCloudHeight(MinecraftServer server, @Nullable String dimensionUid) {
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

            if (global == null) {
                return null;
            }

            JsonElement cloudHeight = global.get("cloud_height");
            if (cloudHeight == null || !cloudHeight.isJsonPrimitive()) {
                return null;
            }

            if (cloudHeight.getAsJsonPrimitive().isNumber()) {
                return cloudHeight.getAsFloat();
            }

            /*
             * "none" exists in the age-data shape, but disabling cloud rendering
             * needs a different hook than height adjustment. Leave it as vanilla
             * for now rather than pretending a height can fully disable clouds.
             */
            return null;
        } catch (IOException | IllegalStateException | ClassCastException e) {
            MystcraftSyntheticCodex.LOGGER.error(
                    "[MystAgeRender] Failed reading cloud render data for {}.",
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
}