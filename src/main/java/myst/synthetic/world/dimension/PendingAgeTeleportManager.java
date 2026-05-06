package myst.synthetic.world.dimension;

import myst.synthetic.MystcraftItems;
import myst.synthetic.MystcraftSyntheticCodex;
import myst.synthetic.linking.LinkController;
import myst.synthetic.linking.LinkOptions;
import myst.synthetic.world.age.AgeRenderDataSynchronizer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.BlockPos;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.HashSet;
import java.util.Set;

public final class PendingAgeTeleportManager {

    private static final Queue<PendingTeleport> QUEUE = new ArrayDeque<>();
    private static final Set<UUID> IN_FLIGHT = new HashSet<>();

    private static final int MAX_WARMUP_TICKS = 200;
    private static final int REQUIRED_STABLE_TICKS = 20;

    private static boolean initialized = false;

    private PendingAgeTeleportManager() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;
        ServerTickEvents.END_SERVER_TICK.register(PendingAgeTeleportManager::tick);
        MystcraftSyntheticCodex.LOGGER.info("[MystAgeQueue] Pending age teleport manager ready.");
    }

    public static void queue(ServerPlayer player, ItemStack agebook) {
        if (player == null || agebook == null || !agebook.is(MystcraftItems.AGEBOOK)) {
            MystcraftSyntheticCodex.LOGGER.warn("[MystAgeQueue] Refused queue request: invalid player or agebook.");
            return;
        }

        UUID playerId = player.getUUID();

        if (IN_FLIGHT.contains(playerId)) {
            MystcraftSyntheticCodex.LOGGER.warn(
                    "[MystAgeQueue] Ignored duplicate Age queue request for '{}'; teleport already in flight.",
                    player.getScoreboardName()
            );
            return;
        }

        IN_FLIGHT.add(playerId);

        QUEUE.add(new PendingTeleport(
                playerId,
                agebook.copy(),
                Phase.CREATE_AGE,
                1,
                null,
                0,
                0
        ));

        player.displayClientMessage(Component.literal("The Age is forming..."), true);

        MystcraftSyntheticCodex.LOGGER.info(
                "[MystAgeQueue] Queued Age creation for player '{}'.",
                player.getScoreboardName()
        );
    }

    private static void tick(MinecraftServer server) {
        if (QUEUE.isEmpty()) {
            return;
        }

        int size = QUEUE.size();

        for (int i = 0; i < size; i++) {
            PendingTeleport pending = QUEUE.poll();
            if (pending == null) {
                continue;
            }

            if (pending.delayTicks > 0) {
                QUEUE.add(pending.withDelay(pending.delayTicks - 1));
                continue;
            }

            ServerPlayer player = server.getPlayerList().getPlayer(pending.playerId);
            if (player == null) {
                MystcraftSyntheticCodex.LOGGER.warn("[MystAgeQueue] Player vanished before queued Age action.");
                finish(pending.playerId);
                continue;
            }

            switch (pending.phase) {
                case CREATE_AGE -> createAge(server, player, pending.agebook);
                case ADD_TICKET -> addTicket(server, player, pending);
                case WAIT_FOR_CHUNK -> waitForChunk(server, player, pending);
                case TELEPORT -> teleport(server, player, pending);
                case POST_TELEPORT_PROBE -> postTeleportProbe(server, player, pending);
            }
        }
    }

    private static void createAge(MinecraftServer server, ServerPlayer player, ItemStack queuedAgebook) {
        MystcraftSyntheticCodex.LOGGER.info(
                "[MystAgeQueue] Processing Age creation for '{}'.",
                player.getScoreboardName()
        );

        ItemStack liveAgebook = findLiveAgebook(player, queuedAgebook);
        if (liveAgebook.isEmpty()) {
            player.displayClientMessage(Component.literal("The descriptive book was lost before the Age could form."), true);
            MystcraftSyntheticCodex.LOGGER.warn("[MystAgeQueue] Live Agebook was not found.");
            finish(player.getUUID());
            return;
        }

        ServerLevel ageLevel = new AgeDimensionManager().getOrCreateAgeLevel(server, liveAgebook);
        if (ageLevel == null) {
            player.displayClientMessage(Component.literal("The descriptive book failed to form an Age."), true);
            MystcraftSyntheticCodex.LOGGER.warn("[MystAgeQueue] AgeDimensionManager returned null.");
            finish(player.getUUID());
            return;
        }

        MystcraftSyntheticCodex.LOGGER.info(
                "[MystAgeQueue] Age resolved as '{}'. Generator={}. Queuing ticket warmup.",
                ageLevel.dimension().identifier(),
                ageLevel.getChunkSource().getGenerator().getClass().getName()
        );

        AgeRuntimeProbe.logServerLevelSet(server, ageLevel, "Age queue after getOrCreateAgeLevel");
        AgeRuntimeProbe.forceAndProbeSpawnChunk(ageLevel, "Age queue after getOrCreateAgeLevel");

        QUEUE.add(new PendingTeleport(
                player.getUUID(),
                liveAgebook.copy(),
                Phase.ADD_TICKET,
                1,
                ageLevel.dimension().identifier().toString(),
                0,
                0
        ));
    }

    private static void addTicket(MinecraftServer server, ServerPlayer player, PendingTeleport pending) {
        ServerLevel ageLevel = getQueuedAgeLevel(server, pending);
        if (ageLevel == null) {
            player.displayClientMessage(Component.literal("The Age exists in the book, but the server level could not be found."), true);
            MystcraftSyntheticCodex.LOGGER.warn("[MystAgeQueue] ADD_TICKET failed: age level was null for '{}'.", pending.levelId);
            finish(pending.playerId);
            return;
        }

        ChunkPos spawnChunk = ChunkPos.ZERO;

        AgeRuntimeProbe.logServerLevelSet(server, ageLevel, "Age queue before portal ticket");
        AgeRuntimeProbe.logSpawnColumn(ageLevel, new BlockPos(0, 65, 0), "Age queue before portal ticket");

        MystcraftSyntheticCodex.LOGGER.info(
                "[MystAgeQueue] Adding non-blocking PORTAL loading ticket for '{}' chunk {}. pendingTasks={}, loadedChunks={}",
                ageLevel.dimension().identifier(),
                spawnChunk,
                ageLevel.getChunkSource().getPendingTasksCount(),
                ageLevel.getChunkSource().getLoadedChunksCount()
        );

        CompletableFuture<?> future = ageLevel.getChunkSource().addTicketAndLoadWithRadius(
                TicketType.PORTAL,
                spawnChunk,
                1
        );

        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                MystcraftSyntheticCodex.LOGGER.error("[MystAgeQueue] Spawn chunk ticket future failed.", throwable);
            } else {
                MystcraftSyntheticCodex.LOGGER.info("[MystAgeQueue] Spawn chunk ticket future completed.");
            }
        });

        QUEUE.add(new PendingTeleport(
                pending.playerId,
                pending.agebook,
                Phase.WAIT_FOR_CHUNK,
                1,
                pending.levelId,
                0,
                0
        ));
    }

    private static void waitForChunk(MinecraftServer server, ServerPlayer player, PendingTeleport pending) {
        ServerLevel ageLevel = getQueuedAgeLevel(server, pending);
        if (ageLevel == null) {
            player.displayClientMessage(Component.literal("The Age vanished before its spawn chunk loaded."), true);
            MystcraftSyntheticCodex.LOGGER.warn("[MystAgeQueue] WAIT_FOR_CHUNK failed: age level was null for '{}'.", pending.levelId);
            finish(pending.playerId);
            return;
        }

        var chunk = ageLevel.getChunkSource().getChunkNow(0, 0);
        int pendingTasks = ageLevel.getChunkSource().getPendingTasksCount();
        int loadedChunks = ageLevel.getChunkSource().getLoadedChunksCount();

        boolean chunkReady = chunk != null;
        boolean tasksIdle = pendingTasks == 0;
        boolean stableThisTick = chunkReady && tasksIdle;

        int nextStableTicks = stableThisTick ? pending.stableTicks + 1 : 0;

        MystcraftSyntheticCodex.LOGGER.info(
                "[MystAgeQueue] Warmup tick {} for '{}': chunkNow={}, pendingTasks={}, loadedChunks={}, stableTicks={}/{}, debug={}",
                pending.warmupTicks,
                ageLevel.dimension().identifier(),
                chunk == null ? "null" : chunk.getClass().getName(),
                pendingTasks,
                loadedChunks,
                nextStableTicks,
                REQUIRED_STABLE_TICKS,
                ageLevel.getChunkSource().getChunkDebugData(ChunkPos.ZERO)
        );

        if (nextStableTicks >= REQUIRED_STABLE_TICKS) {
            MystcraftSyntheticCodex.LOGGER.info(
                    "[MystAgeQueue] Spawn chunk is stable for '{}'. Queuing teleport.",
                    ageLevel.dimension().identifier()
            );

            AgeRuntimeProbe.logServerLevelSet(server, ageLevel, "Age queue stable before teleport");
            AgeRuntimeProbe.logSpawnColumn(ageLevel, new BlockPos(0, 65, 0), "Age queue stable before teleport");

            QUEUE.add(new PendingTeleport(
                    pending.playerId,
                    pending.agebook,
                    Phase.TELEPORT,
                    1,
                    pending.levelId,
                    pending.warmupTicks,
                    nextStableTicks
            ));
            return;
        }

        if (pending.warmupTicks >= MAX_WARMUP_TICKS) {
            player.displayClientMessage(Component.literal("The Age formed, but its spawn chunk never finished loading."), true);
            MystcraftSyntheticCodex.LOGGER.error(
                    "[MystAgeQueue] Spawn chunk warmup timed out for '{}'. Generator={}, pendingTasks={}, loadedChunks={}, stableTicks={}, debug={}",
                    ageLevel.dimension().identifier(),
                    ageLevel.getChunkSource().getGenerator().getClass().getName(),
                    pendingTasks,
                    loadedChunks,
                    nextStableTicks,
                    ageLevel.getChunkSource().getChunkDebugData(ChunkPos.ZERO)
            );
            finish(pending.playerId);
            return;
        }

        QUEUE.add(new PendingTeleport(
                pending.playerId,
                pending.agebook,
                Phase.WAIT_FOR_CHUNK,
                1,
                pending.levelId,
                pending.warmupTicks + 1,
                nextStableTicks
        ));
    }

    private static void teleport(MinecraftServer server, ServerPlayer player, PendingTeleport pending) {
        MystcraftSyntheticCodex.LOGGER.info(
                "[MystAgeQueue] Processing queued resolved teleport for '{}'. target='{}'",
                player.getScoreboardName(),
                pending.levelId
        );

        ServerLevel ageLevel = getQueuedAgeLevel(server, pending);
        if (ageLevel == null) {
            player.displayClientMessage(Component.literal("The Age exists in the book, but the server level could not be found."), true);
            MystcraftSyntheticCodex.LOGGER.warn(
                    "[MystAgeQueue] TELEPORT failed: queued destination level was null for '{}'.",
                    pending.levelId
            );
            finish(pending.playerId);
            return;
        }

        /*
         * Important:
         * The Age has already been resolved by createAge(...), and pending.levelId is
         * the authoritative target for this queued action. Do not re-read CustomData
         * from the book here. The live item may have changed components during binding,
         * and we do not need book parsing at the final teleport stage anyway.
         */
        BlockPos target = new BlockPos(0, 65, 0);

        AgeRuntimeProbe.logPlayer(player, "Age queue immediately before teleport / origin");
        AgeRuntimeProbe.logServerLevelSet(server, ageLevel, "Age queue immediately before teleport / destination");
        AgeRuntimeProbe.forceAndProbeSpawnChunk(ageLevel, "Age queue immediately before teleport");
        AgeRuntimeProbe.logSpawnColumn(ageLevel, target, "Age queue immediately before teleport");

        MystcraftSyntheticCodex.LOGGER.info(
                "[MystAgeQueue] Direct queued Age teleport: player='{}', from='{}', to='{}', target={}, loadedChunks={}, pendingTasks={}, debug={}",
                player.getScoreboardName(),
                player.level().dimension().identifier(),
                ageLevel.dimension().identifier(),
                target,
                ageLevel.getChunkSource().getLoadedChunksCount(),
                ageLevel.getChunkSource().getPendingTasksCount(),
                ageLevel.getChunkSource().getChunkDebugData(ChunkPos.ZERO)
        );

        boolean linked = LinkController.travelEntityToLevel(player, ageLevel, target, 0.0F);

        MystcraftSyntheticCodex.LOGGER.info(
                "[MystAgeQueue] Direct queued Age teleport result={} for '{}'. nowIn='{}' pos={}",
                linked,
                player.getScoreboardName(),
                player.level().dimension().identifier(),
                player.blockPosition()
        );

        AgeRuntimeProbe.logPlayer(player, "Age queue immediately after teleport");

        if (!linked) {
            player.displayClientMessage(Component.literal("The Age refused the link."), true);
            finish(pending.playerId);
            return;
        }

        AgeRenderDataSynchronizer.sendForCurrentLevel(player);

        QUEUE.add(new PendingTeleport(
                pending.playerId,
                pending.agebook,
                Phase.POST_TELEPORT_PROBE,
                20,
                pending.levelId,
                1,
                0
        ));

        AgeRenderDataSynchronizer.sendForCurrentLevel(player);
        finish(pending.playerId);
    }

    private static void postTeleportProbe(MinecraftServer server, ServerPlayer player, PendingTeleport pending) {
        MystcraftSyntheticCodex.LOGGER.info(
                "[MystAgeQueue] Running post-teleport probe {} for '{}'. Expected target='{}'",
                pending.warmupTicks,
                player.getScoreboardName(),
                pending.levelId
        );

        AgeRuntimeProbe.logPlayer(player, "Age queue post-teleport probe " + pending.warmupTicks);

        ServerLevel queuedLevel = getQueuedAgeLevel(server, pending);
        if (queuedLevel != null) {
            AgeRuntimeProbe.logServerLevelSet(server, queuedLevel, "Age queue post-teleport expected destination " + pending.warmupTicks);
            AgeRuntimeProbe.logSpawnColumn(queuedLevel, new BlockPos(0, 65, 0), "Age queue post-teleport expected destination " + pending.warmupTicks);
        } else {
            MystcraftSyntheticCodex.LOGGER.warn(
                    "[MystAgeQueue] Post-teleport probe {} could not find queued destination '{}'.",
                    pending.warmupTicks,
                    pending.levelId
            );
        }

        if (pending.warmupTicks >= 4) {
            finish(pending.playerId);
            return;
        }

        int nextDelay = switch (pending.warmupTicks) {
            case 1 -> 20;
            case 2 -> 60;
            default -> 100;
        };

        QUEUE.add(new PendingTeleport(
                pending.playerId,
                pending.agebook,
                Phase.POST_TELEPORT_PROBE,
                nextDelay,
                pending.levelId,
                pending.warmupTicks + 1,
                pending.stableTicks
        ));
    }

    private static ServerLevel getQueuedAgeLevel(MinecraftServer server, PendingTeleport pending) {
        if (pending.levelId == null || pending.levelId.isBlank()) {
            return null;
        }

        net.minecraft.resources.Identifier id = net.minecraft.resources.Identifier.tryParse(pending.levelId);
        if (id == null) {
            return null;
        }

        net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> key =
                net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, id);

        return server.getLevel(key);
    }

    private static ItemStack findLiveAgebook(ServerPlayer player, ItemStack queuedAgebook) {
        ItemStack main = player.getMainHandItem();
        if (ItemStack.isSameItemSameComponents(main, queuedAgebook)) {
            return main;
        }

        ItemStack off = player.getOffhandItem();
        if (ItemStack.isSameItemSameComponents(off, queuedAgebook)) {
            return off;
        }

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, queuedAgebook)) {
                return stack;
            }
        }

        /*
         * After AgeDimensionManager binds the live book, its components change.
         * A queued copy from before binding will no longer compare equal.
         */
        if (main.is(MystcraftItems.AGEBOOK)) {
            return main;
        }

        if (off.is(MystcraftItems.AGEBOOK)) {
            return off;
        }

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(MystcraftItems.AGEBOOK)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    private enum Phase {
        CREATE_AGE,
        ADD_TICKET,
        WAIT_FOR_CHUNK,
        TELEPORT,
        POST_TELEPORT_PROBE
    }

    private record PendingTeleport(
            UUID playerId,
            ItemStack agebook,
            Phase phase,
            int delayTicks,
            String levelId,
            int warmupTicks,
            int stableTicks
    ) {
        private PendingTeleport withDelay(int delayTicks) {
            return new PendingTeleport(
                    this.playerId,
                    this.agebook,
                    this.phase,
                    delayTicks,
                    this.levelId,
                    this.warmupTicks,
                    this.stableTicks
            );
        }
    }
    private static void finish(UUID playerId) {
        IN_FLIGHT.remove(playerId);
    }
}