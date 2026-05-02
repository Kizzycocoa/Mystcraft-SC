package myst.synthetic.world.dimension;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import myst.synthetic.MystcraftItems;
import myst.synthetic.MystcraftSyntheticCodex;
import myst.synthetic.component.AgebookDataComponent;
import myst.synthetic.component.MystcraftDataComponents;
import myst.synthetic.item.ItemAgebook;
import myst.synthetic.linking.LinkOptions;
import myst.synthetic.world.age.AgeRegistryData;
import myst.synthetic.world.age.AgeSpec;
import myst.synthetic.world.age.AgeSpecCompiler;
import myst.synthetic.world.age.AgeStoragePaths;
import myst.synthetic.world.age.AgeDataFileCompiler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AgeDimensionManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final AgeDataFileCompiler ageDataCompiler = new AgeDataFileCompiler();
    private final AgeLevelStemFactory levelStemFactory = new AgeLevelStemFactory();

    public ServerLevel getOrCreateAgeLevel(MinecraftServer server, ItemStack agebook) {
        MystcraftSyntheticCodex.LOGGER.info("[MystAge] getOrCreateAgeLevel entered.");

        if (!agebook.is(MystcraftItems.AGEBOOK)) {
            MystcraftSyntheticCodex.LOGGER.warn("[MystAge] Refused non-agebook stack: {}", agebook);
            return null;
        }

        AgebookDataComponent data = ItemAgebook.getData(agebook);
        MystcraftSyntheticCodex.LOGGER.info(
                "[MystAge] Agebook data: pages={}, display='{}', dimensionUid='{}', targetUuid='{}', seed={}",
                data.pages().size(),
                data.displayName(),
                data.dimensionUid(),
                data.targetUuid(),
                data.seed()
        );

        if (data.pages().isEmpty()) {
            MystcraftSyntheticCodex.LOGGER.warn("[MystAge] Refused empty agebook.");
            return null;
        }

        AgeRegistryData registry = AgeRegistryData.get(server.overworld());

        long seed = data.seed() != null ? data.seed() : createSeed(server, agebook, data);
        String displayName = data.displayName() == null ? "" : data.displayName().trim();

        String dimensionUid = AgeDimensionKeys.toDimensionUid(data.dimensionUid());
        UUID targetUuid = parseUuid(data.targetUuid());

        AgeRegistryData.AgeEntry entry = null;
        if (dimensionUid != null) {
            entry = registry.getByDimensionUid(dimensionUid);
        }
        if (entry == null && targetUuid != null) {
            entry = registry.getByTargetUuid(targetUuid);
        }
        if (entry == null) {
            entry = registry.reserveOrGetExisting(targetUuid, seed, displayName);
            MystcraftSyntheticCodex.LOGGER.info("[MystAge] Reserved new age entry: {}", entry.dimensionUid());
        } else {
            MystcraftSyntheticCodex.LOGGER.info("[MystAge] Reusing age entry: {}", entry.dimensionUid());
        }

        AgeDataFileCompiler.Result compiledResult = this.ageDataCompiler.compile(
                data,
                server.registryAccess(),
                seed,
                entry.dimensionUid(),
                AgeDimensionKeys.levelIdString(entry.dimensionUid()),
                entry.targetUuid()
        );

        AgeSpec compiled = compiledResult.spec();

        MystcraftSyntheticCodex.LOGGER.info(
                "[MystAge] Compiled Age data: dim={}, biome={}, terrain={}, ground={}, bedrock={}",
                compiled.dimensionUid(),
                compiled.resolvedBiome(),
                compiled.terrain(),
                compiled.resolvedGroundLevel(),
                compiled.bedrockProfile()
        );

        writeAgeData(server, entry.dimensionUid(), compiledResult.document());

        ServerLevel existing = server.getLevel(AgeDimensionKeys.levelKey(entry.dimensionUid()));
        if (existing == null) {
            existing = createAndRegisterAgeLevel(server, entry.dimensionUid(), compiled);
        } else {
            MystcraftSyntheticCodex.LOGGER.info(
                    "[MystAge] Existing ServerLevel found for {}.",
                    existing.dimension().identifier()
            );
        }

        if (existing == null) {
            MystcraftSyntheticCodex.LOGGER.error("[MystAge] Failed to obtain ServerLevel for {}.", entry.dimensionUid());
            return null;
        }

        UUID resolvedTarget = entry.targetUuidAsUuid();
        if (resolvedTarget == null) {
            resolvedTarget = UUID.randomUUID();
            registry.bindTargetUuid(entry.dimensionUid(), resolvedTarget);
            MystcraftSyntheticCodex.LOGGER.info("[MystAge] Bound new target UUID {}.", resolvedTarget);
        }

        bindAgebook(agebook, existing, compiled, resolvedTarget);
        registry.updateDisplayName(entry.dimensionUid(), compiled.displayName());

        MystcraftSyntheticCodex.LOGGER.info(
                "[MystAge] Age ready: dimUid={}, level={}, targetUuid={}",
                entry.dimensionUid(),
                existing.dimension().identifier(),
                resolvedTarget
        );

        return existing;
    }

    private @Nullable ServerLevel createAndRegisterAgeLevel(
            MinecraftServer server,
            String dimensionUid,
            AgeSpec spec
    ) {
        try {
            var levelKey = AgeDimensionKeys.levelKey(dimensionUid);
            var existing = server.getLevel(levelKey);
            if (existing != null) {
                return existing;
            }

            MystcraftSyntheticCodex.LOGGER.info("[MystAge] Creating ServerLevel for {} / {}.", dimensionUid, levelKey.identifier());

            ServerLevel template = server.overworld();
            LevelStem stem = this.levelStemFactory.create(template, spec);

            MystcraftSyntheticCodex.LOGGER.info(
                    "[MystAge] LevelStem created. generator={}",
                    stem.generator().getClass().getName()
            );

            LevelStorageSource.LevelStorageAccess session = getStorageAccess(server);
            ServerLevelData levelData = new DerivedLevelData(server.getWorldData(), server.getWorldData().overworldData());

            ServerLevel created = new ServerLevel(
                    server,
                    server,
                    session,
                    levelData,
                    levelKey,
                    stem,
                    false,
                    spec.seed(),
                    List.of(),
                    true,
                    new RandomSequences()
            );

            getLevelMap(server).put(levelKey, created);

            MystcraftSyntheticCodex.LOGGER.info(
                    "[MystAge] Created Mystcraft age level '{}' as '{}'. mapContains={}",
                    dimensionUid,
                    levelKey.identifier(),
                    server.getLevel(levelKey) != null
            );

            return created;
        } catch (Exception e) {
            MystcraftSyntheticCodex.LOGGER.error("[MystAge] Failed to create Mystcraft age '{}'.", dimensionUid, e);
            return null;
        }
    }

    private void bindAgebook(ItemStack agebook, ServerLevel level, AgeSpec spec, UUID targetUuid) {
        String levelId = level.dimension().identifier().toString();

        MystcraftSyntheticCodex.LOGGER.info(
                "[MystAge] Binding agebook to levelId={}, targetUuid={}, spawnY={}.",
                levelId,
                targetUuid,
                spec.resolvedGroundLevel() + 1
        );

        ItemAgebook.bindToGeneratedAge(agebook, levelId, targetUuid, spec.seed(), spec.displayName());

        CompoundTag tag = agebook.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag = LinkOptions.setDimensionUID(tag, levelId);
        tag = LinkOptions.setUUID(tag, targetUuid);

        BlockPos spawn = new BlockPos(0, Math.max(1, spec.resolvedGroundLevel() + 1), 0);
        tag = LinkOptions.setSpawn(tag, spawn);
        tag = LinkOptions.setSpawnYaw(tag, 0.0F);

        agebook.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        AgebookDataComponent current = ItemAgebook.getData(agebook);
        agebook.set(
                MystcraftDataComponents.AGEBOOK_DATA,
                new AgebookDataComponent(
                        current.pagesCopy(),
                        current.authorsCopy(),
                        spec.displayName(),
                        levelId,
                        targetUuid.toString(),
                        spec.seed()
                )
        );

        if (!spec.displayName().isBlank()) {
            agebook.set(DataComponents.CUSTOM_NAME, Component.literal(spec.displayName()));
        }
    }

    private void writeAgeData(MinecraftServer server, String dimensionUid, JsonObject document) {
        try {
            Path worldRoot = server.getWorldPath(LevelResource.ROOT);
            Path folder = AgeStoragePaths.dimensionFolder(worldRoot, dimensionUid);
            Files.createDirectories(folder);

            Path file = AgeStoragePaths.ageDataFile(worldRoot, dimensionUid);
            Files.writeString(file, GSON.toJson(document));

            MystcraftSyntheticCodex.LOGGER.info("[MystAge] Wrote Age data file: {}", file);
        } catch (IOException e) {
            MystcraftSyntheticCodex.LOGGER.error("[MystAge] Failed to write Age data for {}.", dimensionUid, e);
        }
    }

    private long createSeed(MinecraftServer server, ItemStack agebook, AgebookDataComponent data) {
        long seed = server.overworld().getSeed();
        seed ^= (long) data.displayName().hashCode() << 32;
        seed ^= ItemStack.hashItemAndComponents(agebook);
        seed ^= System.nanoTime();
        return seed;
    }

    @SuppressWarnings("unchecked")
    private Map<net.minecraft.resources.ResourceKey<Level>, ServerLevel> getLevelMap(MinecraftServer server) throws Exception {
        Field field = MinecraftServer.class.getDeclaredField("levels");
        field.setAccessible(true);
        return (Map<net.minecraft.resources.ResourceKey<Level>, ServerLevel>) field.get(server);
    }

    private LevelStorageSource.LevelStorageAccess getStorageAccess(MinecraftServer server) throws Exception {
        Field field = MinecraftServer.class.getDeclaredField("storageSource");
        field.setAccessible(true);
        return (LevelStorageSource.LevelStorageAccess) field.get(server);
    }

    private static @Nullable UUID parseUuid(@Nullable String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}