package myst.synthetic.world.dimension;

import myst.synthetic.MystcraftItems;
import myst.synthetic.MystcraftSyntheticCodex;
import myst.synthetic.item.ItemAgebook;
import myst.synthetic.linking.LinkController;
import myst.synthetic.linking.LinkOptions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;

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
        MystcraftSyntheticCodex.LOGGER.info("Mystcraft pending age teleport manager ready.");
    }

    public static void queue(ServerPlayer player, ItemStack agebook) {
        if (player == null || agebook == null || !agebook.is(MystcraftItems.AGEBOOK)) {
            return;
        }

        QUEUE.add(new PendingTeleport(player.getUUID(), agebook.copy(), Phase.CREATE_AGE, 1));
        player.displayClientMessage(Component.literal("The Age is forming..."), true);

        MystcraftSyntheticCodex.LOGGER.info(
                "Queued Mystcraft age teleport for player '{}'.",
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
                continue;
            }

            if (pending.phase == Phase.CREATE_AGE) {
                createAge(server, player, pending.agebook);
                continue;
            }

            if (pending.phase == Phase.TELEPORT) {
                teleport(player, pending.agebook);
            }
        }
    }

    private static void createAge(MinecraftServer server, ServerPlayer player, ItemStack queuedAgebook) {
        MystcraftSyntheticCodex.LOGGER.info(
                "Processing Mystcraft age creation for player '{}'.",
                player.getScoreboardName()
        );

        ItemStack liveAgebook = findLiveAgebook(player, queuedAgebook);
        if (liveAgebook.isEmpty()) {
            player.displayClientMessage(Component.literal("The descriptive book was lost before the Age could form."), true);
            MystcraftSyntheticCodex.LOGGER.warn("Queued Mystcraft age creation failed: live agebook was not found.");
            return;
        }

        ServerLevel ageLevel = new AgeDimensionManager().getOrCreateAgeLevel(server, liveAgebook);
        if (ageLevel == null) {
            player.displayClientMessage(Component.literal("The descriptive book failed to form an Age."), true);
            MystcraftSyntheticCodex.LOGGER.warn("Queued Mystcraft age creation failed: AgeDimensionManager returned null.");
            return;
        }

        MystcraftSyntheticCodex.LOGGER.info(
                "Mystcraft age '{}' is ready; teleport queued.",
                ageLevel.dimension().identifier()
        );

        QUEUE.add(new PendingTeleport(player.getUUID(), liveAgebook.copy(), Phase.TELEPORT, 5));
    }

    private static void teleport(ServerPlayer player, ItemStack queuedAgebook) {
        MystcraftSyntheticCodex.LOGGER.info(
                "Processing Mystcraft queued age teleport for player '{}'.",
                player.getScoreboardName()
        );

        ItemStack liveAgebook = findLiveAgebook(player, queuedAgebook);
        if (liveAgebook.isEmpty()) {
            player.displayClientMessage(Component.literal("The descriptive book was lost before linking."), true);
            MystcraftSyntheticCodex.LOGGER.warn("Queued Mystcraft teleport failed: live agebook was not found.");
            return;
        }

        CustomData customData = liveAgebook.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            player.displayClientMessage(Component.literal("The descriptive book has no link target."), true);
            MystcraftSyntheticCodex.LOGGER.warn("Queued Mystcraft teleport failed: agebook had no custom data.");
            return;
        }

        CompoundTag tag = customData.copyTag();
        LinkOptions info = new LinkOptions(tag);

        String targetDimension = info.getDimensionUID();
        if (targetDimension == null || targetDimension.isBlank()) {
            player.displayClientMessage(Component.literal("The descriptive book has no dimension target."), true);
            MystcraftSyntheticCodex.LOGGER.warn("Queued Mystcraft teleport failed: agebook had no dimension UID.");
            return;
        }

        String currentDimension = extractDimensionId(player.level().dimension().toString());
        if (currentDimension.equals(targetDimension)) {
            MystcraftSyntheticCodex.LOGGER.info("Queued Mystcraft teleport skipped: player is already in '{}'.", targetDimension);
            return;
        }

        MystcraftSyntheticCodex.LOGGER.info(
                "Teleporting player '{}' to Mystcraft age '{}'.",
                player.getScoreboardName(),
                targetDimension
        );

        LinkController.travelEntity(player.level(), player, info);
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
         * Important fallback:
         * after AgeDimensionManager binds the live book, its components change.
         * A queued copy from before binding will no longer compare equal.
         * If the exact stack match fails, use the selected Agebook.
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

    private static String extractDimensionId(String raw) {
        int start = raw.indexOf('[');
        int end = raw.indexOf(']');

        if (start >= 0 && end > start) {
            return raw.substring(start + 1, end);
        }

        return raw;
    }

    private enum Phase {
        CREATE_AGE,
        TELEPORT
    }

    private record PendingTeleport(
            UUID playerId,
            ItemStack agebook,
            Phase phase,
            int delayTicks
    ) {
        private PendingTeleport withDelay(int delayTicks) {
            return new PendingTeleport(this.playerId, this.agebook, this.phase, delayTicks);
        }
    }
}