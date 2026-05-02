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

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class PendingAgeTeleportManager {

    private static final Queue<PendingTeleport> QUEUE = new ArrayDeque<>();
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

        QUEUE.add(new PendingTeleport(
                player.getUUID(),
                agebook.copy(),
                Phase.CREATE_AGE,
                1,
                null,
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
                continue;
            }

            switch (pending.phase) {
                case CREATE_AGE -> createAge(server, player, pending.agebook);
                case ADD_TICKET -> addTicket(server, player, pending);
                case WAIT_FOR_CHUNK -> waitForChunk(server, player, pending);
                case TELEPORT -> teleport(player, pending.agebook);
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
            return;
        }

        ServerLevel ageLevel = new AgeDimensionManager().getOrCreateAgeLevel(server, liveAgebook);
        if (ageLevel == null) {
            player.displayClientMessage(Component.literal("The descriptive book failed to form an Age."), true);
            MystcraftSyntheticCodex.LOGGER.warn("[MystAgeQueue] AgeDimensionManager returned null.");
            return;
        }

        MystcraftSyntheticCodex.LOGGER.info(
                "[MystAgeQueue] Age resolved as '{}'. Generator={}. Queuing ticket warmup.",
                ageLevel.dimension().identifier(),
                ageLevel.getChunkSource().getGenerator().getClass().getName()
        );

        QUEUE.add(new PendingTeleport(
                player.getUUID(),
                liveAgebook.copy(),
                Phase.ADD_TICKET,
                1,
                ageLevel.dimension().identifier().toString(),
                0
        ));
    }

    private static void addTicket(MinecraftServer server, ServerPlayer player, PendingTeleport pending) {
        ServerLevel ageLevel = getQueuedAgeLevel(server, pending);
        if (ageLevel == null) {
            player.displayClientMessage(Component.literal("The Age exists in the book, but the server level could not be found."), true);
            MystcraftSyntheticCodex.LOGGER.warn("[MystAgeQueue] ADD_TICKET failed: age level was null for '{}'.", pending.levelId);
            return;
        }

        ChunkPos spawnChunk = ChunkPos.ZERO;

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
                0
        ));
    }

    private static void waitForChunk(MinecraftServer server, ServerPlayer player, PendingTeleport pending) {
        ServerLevel ageLevel = getQueuedAgeLevel(server, pending);
        if (ageLevel == null) {
            player.displayClientMessage(Component.literal("The Age vanished before its spawn chunk loaded."), true);
            MystcraftSyntheticCodex.LOGGER.warn("[MystAgeQueue] WAIT_FOR_CHUNK failed: age level was null for '{}'.", pending.levelId);
            return;
        }

        var chunk = ageLevel.getChunkSource().getChunkNow(0, 0);

        MystcraftSyntheticCodex.LOGGER.info(
                "[MystAgeQueue] Warmup tick {} for '{}': chunkNow={}, pendingTasks={}, loadedChunks={}, debug={}",
                pending.warmupTicks,
                ageLevel.dimension().identifier(),
                chunk == null ? "null" : chunk.getClass().getName(),
                ageLevel.getChunkSource().getPendingTasksCount(),
                ageLevel.getChunkSource().getLoadedChunksCount(),
                ageLevel.getChunkSource().getChunkDebugData(ChunkPos.ZERO)
        );

        if (chunk != null) {
            MystcraftSyntheticCodex.LOGGER.info(
                    "[MystAgeQueue] Spawn chunk is loaded for '{}'. Queuing teleport.",
                    ageLevel.dimension().identifier()
            );

            QUEUE.add(new PendingTeleport(
                    pending.playerId,
                    pending.agebook,
                    Phase.TELEPORT,
                    1,
                    pending.levelId,
                    pending.warmupTicks
            ));
            return;
        }

        if (pending.warmupTicks >= 200) {
            player.displayClientMessage(Component.literal("The Age formed, but its spawn chunk never finished loading."), true);
            MystcraftSyntheticCodex.LOGGER.error(
                    "[MystAgeQueue] Spawn chunk warmup timed out for '{}'. Generator={}, pendingTasks={}, loadedChunks={}, debug={}",
                    ageLevel.dimension().identifier(),
                    ageLevel.getChunkSource().getGenerator().getClass().getName(),
                    ageLevel.getChunkSource().getPendingTasksCount(),
                    ageLevel.getChunkSource().getLoadedChunksCount(),
                    ageLevel.getChunkSource().getChunkDebugData(ChunkPos.ZERO)
            );
            return;
        }

        QUEUE.add(new PendingTeleport(
                pending.playerId,
                pending.agebook,
                Phase.WAIT_FOR_CHUNK,
                1,
                pending.levelId,
                pending.warmupTicks + 1
        ));
    }

    private static void teleport(ServerPlayer player, ItemStack queuedAgebook) {
        MystcraftSyntheticCodex.LOGGER.info(
                "[MystAgeQueue] Processing queued teleport for '{}'.",
                player.getScoreboardName()
        );

        ItemStack liveAgebook = findLiveAgebook(player, queuedAgebook);
        if (liveAgebook.isEmpty()) {
            player.displayClientMessage(Component.literal("The descriptive book was lost before linking."), true);
            MystcraftSyntheticCodex.LOGGER.warn("[MystAgeQueue] Live Agebook was not found for teleport.");
            return;
        }

        CustomData customData = liveAgebook.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            player.displayClientMessage(Component.literal("The descriptive book has no link target."), true);
            MystcraftSyntheticCodex.LOGGER.warn("[MystAgeQueue] Agebook has no custom data.");
            return;
        }

        CompoundTag tag = customData.copyTag();
        LinkOptions info = new LinkOptions(tag);

        String targetDimension = info.getDimensionUID();
        if (targetDimension == null || targetDimension.isBlank()) {
            player.displayClientMessage(Component.literal("The descriptive book has no dimension target."), true);
            MystcraftSyntheticCodex.LOGGER.warn("[MystAgeQueue] Agebook has no dimension UID.");
            return;
        }

        MystcraftSyntheticCodex.LOGGER.info(
                "[MystAgeQueue] Calling LinkController.travelEntity to '{}'.",
                targetDimension
        );

        LinkController.travelEntity(player.level(), player, info);
        AgeRenderDataSynchronizer.sendForCurrentLevel(player);
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
        TELEPORT
    }

    private record PendingTeleport(
            UUID playerId,
            ItemStack agebook,
            Phase phase,
            int delayTicks,
            String levelId,
            int warmupTicks
    ) {
        private PendingTeleport withDelay(int delayTicks) {
            return new PendingTeleport(
                    this.playerId,
                    this.agebook,
                    this.phase,
                    delayTicks,
                    this.levelId,
                    this.warmupTicks
            );
        }
    }
}