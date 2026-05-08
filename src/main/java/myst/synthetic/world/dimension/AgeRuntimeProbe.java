package myst.synthetic.world.dimension;

import myst.synthetic.MystcraftSyntheticCodex;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

public final class AgeRuntimeProbe {

    private AgeRuntimeProbe() {
    }

    public static void logServerLevelSet(MinecraftServer server, ServerLevel level, String reason) {
        if (server == null || level == null) {
            MystcraftSyntheticCodex.LOGGER.warn(
                    "[MystProbe] Level-set probe skipped for reason='{}': server={}, level={}",
                    reason,
                    server,
                    level
            );
            return;
        }

        ResourceKey<Level> key = level.dimension();
        ServerLevel byKey = server.getLevel(key);

        int count = 0;
        boolean foundSameInstance = false;
        boolean foundSameKey = false;

        for (ServerLevel candidate : server.getAllLevels()) {
            count++;

            if (candidate == level) {
                foundSameInstance = true;
            }

            if (candidate.dimension().equals(key)) {
                foundSameKey = true;
            }
        }

        MystcraftSyntheticCodex.LOGGER.info(
                "[MystProbe] Level-set probe reason='{}': key={}, byKeySameInstance={}, getAllLevelsCount={}, getAllLevelsHasSameInstance={}, getAllLevelsHasSameKey={}, chunkSource={}, generator={}",
                reason,
                key.identifier(),
                byKey == level,
                count,
                foundSameInstance,
                foundSameKey,
                level.getChunkSource().getClass().getName(),
                level.getChunkSource().getGenerator().getClass().getName()
        );
    }

    /**
     * Kept under the old name so existing debug calls still compile, but this is
     * deliberately NON-BLOCKING now. Do not call ServerChunkCache#getChunk(..., true)
     * from these probes; it can hang startup/login for runtime-created dimensions.
     */
    public static boolean forceSpawnChunk(ServerLevel level) {
        return forceAndProbeSpawnChunk(level, "generic");
    }

    /**
     * Non-blocking despite the historical method name.
     */
    public static boolean forceAndProbeSpawnChunk(ServerLevel level, String reason) {
        if (level == null) {
            MystcraftSyntheticCodex.LOGGER.warn("[MystProbe] Spawn chunk probe skipped for reason='{}': level was null.", reason);
            return false;
        }

        MystcraftSyntheticCodex.LOGGER.info(
                "[MystProbe] Non-blocking spawn chunk probe reason='{}' level='{}': pendingTasks={}, loadedChunks={}, chunkNow={}, debug={}",
                reason,
                level.dimension().identifier(),
                level.getChunkSource().getPendingTasksCount(),
                level.getChunkSource().getLoadedChunksCount(),
                className(level.getChunkSource().getChunkNow(0, 0)),
                level.getChunkSource().getChunkDebugData(ChunkPos.ZERO)
        );

        return logSpawnColumnIfLoaded(level, new BlockPos(0, 65, 0), reason + " / non-blocking");
    }

    public static boolean logSpawnColumnIfLoaded(ServerLevel level, BlockPos target, String reason) {
        if (level == null || target == null) {
            MystcraftSyntheticCodex.LOGGER.warn(
                    "[MystProbe] Spawn column probe skipped for reason='{}': level={}, target={}",
                    reason,
                    level,
                    target
            );
            return false;
        }

        ChunkPos chunkPos = new ChunkPos(target);
        LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);

        MystcraftSyntheticCodex.LOGGER.info(
                "[MystProbe] Spawn column preflight reason='{}': level={}, target={}, chunk={}, pendingTasks={}, loadedChunks={}, chunkNow={}, debug={}",
                reason,
                level.dimension().identifier(),
                target,
                chunkPos,
                level.getChunkSource().getPendingTasksCount(),
                level.getChunkSource().getLoadedChunksCount(),
                className(chunk),
                level.getChunkSource().getChunkDebugData(chunkPos)
        );

        if (chunk == null) {
            MystcraftSyntheticCodex.LOGGER.info(
                    "[MystProbe] Spawn column skipped reason='{}': chunk {} is not loaded yet.",
                    reason,
                    chunkPos
            );
            return false;
        }

        logSpawnColumnLoaded(level, target, reason);
        return true;
    }

    public static void logSpawnColumn(ServerLevel level, BlockPos target, String reason) {
        logSpawnColumnIfLoaded(level, target, reason);
    }

    private static void logSpawnColumnLoaded(ServerLevel level, BlockPos target, String reason) {
        int x = target.getX();
        int z = target.getZ();
        ChunkPos chunkPos = new ChunkPos(target);

        int height = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

        MystcraftSyntheticCodex.LOGGER.info(
                "[MystProbe] Spawn column reason='{}': level={}, target={}, chunk={}, height={}, seaLevel={}, minY={}, maxY={}, pendingTasks={}, loadedChunks={}, chunkNow={}, debug={}",
                reason,
                level.dimension().identifier(),
                target,
                chunkPos,
                height,
                level.getSeaLevel(),
                level.getMinY(),
                level.getMaxY(),
                level.getChunkSource().getPendingTasksCount(),
                level.getChunkSource().getLoadedChunksCount(),
                className(level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z)),
                level.getChunkSource().getChunkDebugData(chunkPos)
        );

        int[] ys = {
                level.getMinY(),
                0,
                1,
                2,
                3,
                4,
                32,
                60,
                63,
                64,
                65,
                66,
                height - 2,
                height - 1,
                height,
                height + 1
        };

        for (int y : ys) {
            if (y < level.getMinY() || y > level.getMaxY()) {
                continue;
            }

            BlockPos pos = new BlockPos(x, y, z);
            MystcraftSyntheticCodex.LOGGER.info(
                    "[MystProbe] Spawn column block reason='{}' {} = {}",
                    reason,
                    pos,
                    level.getBlockState(pos)
            );
        }
    }

    public static void logPlayer(ServerPlayer player, String reason) {
        if (player == null) {
            MystcraftSyntheticCodex.LOGGER.warn("[MystProbe] Player probe skipped for reason='{}': player was null.", reason);
            return;
        }

        Level rawLevel = player.level();

        MystcraftSyntheticCodex.LOGGER.info(
                "[MystProbe] Player probe reason='{}': player='{}', level={}, pos={}, blockPos={}, removed={}, alive={}, vehicle={}, passengerCount={}",
                reason,
                player.getScoreboardName(),
                rawLevel.dimension().identifier(),
                player.position(),
                player.blockPosition(),
                player.isRemoved(),
                player.isAlive(),
                player.getVehicle(),
                player.getPassengers().size()
        );

        if (rawLevel instanceof ServerLevel serverLevel) {
            logServerLevelSet(serverLevel.getServer(), serverLevel, reason + " / player level");
            logSpawnColumnIfLoaded(serverLevel, player.blockPosition(), reason + " / player column");
        }
    }

    private static String className(Object object) {
        return object == null ? "null" : object.getClass().getName();
    }
}