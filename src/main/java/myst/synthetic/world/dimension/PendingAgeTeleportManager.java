package myst.synthetic.world.dimension;

import myst.synthetic.MystcraftItems;
import myst.synthetic.MystcraftSyntheticCodex;
import myst.synthetic.component.AgebookDataComponent;
import myst.synthetic.item.ItemAgebook;
import myst.synthetic.linking.LinkController;
import myst.synthetic.linking.LinkOptions;
import myst.synthetic.world.terrain.BedrockProfile;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;

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
    }

    public static void queue(ServerPlayer player, ItemStack agebook) {
        if (player == null || agebook == null || !agebook.is(MystcraftItems.AGEBOOK)) {
            return;
        }

        QUEUE.add(new PendingTeleport(player.getUUID(), agebook.copy(), 1));
        player.displayClientMessage(Component.literal("The Age is forming..."), true);
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
                QUEUE.add(new PendingTeleport(pending.playerId, pending.agebook, pending.delayTicks - 1));
                continue;
            }

            ServerPlayer player = server.getPlayerList().getPlayer(pending.playerId);
            if (player == null) {
                continue;
            }

            process(server, player, pending.agebook);
        }
    }

    private static void process(MinecraftServer server, ServerPlayer player, ItemStack queuedAgebook) {
        ItemStack liveAgebook = findLiveAgebook(player, queuedAgebook);
        if (liveAgebook.isEmpty()) {
            player.displayClientMessage(Component.literal("The descriptive book was lost before the Age could form."), true);
            return;
        }

        ServerLevel ageLevel = new AgeDimensionManager().getOrCreateAgeLevel(server, liveAgebook);
        if (ageLevel == null) {
            player.displayClientMessage(Component.literal("The descriptive book failed to form an Age."), true);
            return;
        }

        AgebookDataComponent data = ItemAgebook.getData(liveAgebook);
        int groundY = 64;
        if (data.seed() != null) {
            groundY = guessGroundLevelFromAgebook(data);
        }

        prepareSpawnChunk(ageLevel, data, groundY);
        teleport(player, liveAgebook);
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

        return ItemStack.EMPTY;
    }

    private static void teleport(ServerPlayer player, ItemStack agebook) {
        CustomData customData = agebook.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            player.displayClientMessage(Component.literal("The descriptive book has no link target."), true);
            return;
        }

        CompoundTag tag = customData.copyTag();
        LinkOptions info = new LinkOptions(tag);

        String targetDimension = info.getDimensionUID();
        if (targetDimension == null || targetDimension.isBlank()) {
            player.displayClientMessage(Component.literal("The descriptive book has no dimension target."), true);
            return;
        }

        String currentDimension = extractDimensionId(player.level().dimension().toString());
        if (currentDimension.equals(targetDimension)) {
            return;
        }

        LinkController.travelEntity(player.level(), player, info);
    }

    private static void prepareSpawnChunk(ServerLevel level, AgebookDataComponent data, int groundY) {
        groundY = Math.max(8, groundY);
        int seaY = Math.max(groundY, 63);

        ColumnMaterials materials = resolveMaterials(data);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int chunkX = -1; chunkX <= 1; chunkX++) {
            for (int chunkZ = -1; chunkZ <= 1; chunkZ++) {
                level.getChunk(chunkX, chunkZ);

                int minX = chunkX << 4;
                int minZ = chunkZ << 4;

                for (int localX = 0; localX < 16; localX++) {
                    int x = minX + localX;

                    for (int localZ = 0; localZ < 16; localZ++) {
                        int z = minZ + localZ;

                        for (int y = 0; y <= seaY; y++) {
                            BlockState state = stateForY(y, x, z, groundY, seaY, materials, data.seed() == null ? 0L : data.seed());
                            if (state.isAir()) {
                                continue;
                            }

                            pos.set(x, y, z);
                            level.setBlock(pos, state, 2);
                        }
                    }
                }
            }
        }

        BlockPos spawn = new BlockPos(0, groundY + 1, 0);

        if (level.getLevelData() instanceof WritableLevelData writableLevelData) {
            writableLevelData.setSpawn(new LevelData.RespawnData(
                    GlobalPos.of(level.dimension(), spawn),
                    0.0F,
                    0.0F
            ));
        }
    }

    private static BlockState stateForY(
            int y,
            int x,
            int z,
            int groundY,
            int seaY,
            ColumnMaterials materials,
            long seed
    ) {
        if (isVanillaFloorBedrock(seed, x, y, z, BedrockProfile.VANILLA_FLOOR)) {
            return Blocks.BEDROCK.defaultBlockState();
        }

        if (y > groundY) {
            return y <= seaY ? materials.fluid : Blocks.AIR.defaultBlockState();
        }

        if (y == groundY) {
            return materials.top;
        }

        if (y >= groundY - materials.underDepth) {
            return materials.under;
        }

        return materials.base;
    }

    private static boolean isVanillaFloorBedrock(long seed, int x, int y, int z, BedrockProfile profile) {
        if (profile == BedrockProfile.NONE) {
            return false;
        }

        if (y == 0) {
            return true;
        }

        if (y < 0 || y > 4) {
            return false;
        }

        long mixed = seed
                ^ (((long) x) * 341873128712L)
                ^ (((long) z) * 132897987541L)
                ^ (((long) y) * 42317861L);

        int value = Math.floorMod((int) (mixed ^ (mixed >>> 32)), 5);
        return y <= value;
    }

    private static ColumnMaterials resolveMaterials(AgebookDataComponent data) {
        String biomePath = data.dimensionUid() == null ? "" : data.dimensionUid();

        for (ItemStack page : data.pages()) {
            String text = page.toString().toLowerCase();
            if (text.contains("desert") || text.contains("beach")) {
                return new ColumnMaterials(
                        Blocks.SAND.defaultBlockState(),
                        Blocks.SAND.defaultBlockState(),
                        Blocks.SANDSTONE.defaultBlockState(),
                        Blocks.WATER.defaultBlockState(),
                        4
                );
            }

            if (text.contains("badlands")) {
                return new ColumnMaterials(
                        Blocks.RED_SAND.defaultBlockState(),
                        Blocks.RED_SAND.defaultBlockState(),
                        Blocks.RED_SANDSTONE.defaultBlockState(),
                        Blocks.WATER.defaultBlockState(),
                        4
                );
            }

            if (text.contains("deep_dark")) {
                return new ColumnMaterials(
                        Blocks.DEEPSLATE.defaultBlockState(),
                        Blocks.DEEPSLATE.defaultBlockState(),
                        Blocks.DEEPSLATE.defaultBlockState(),
                        Blocks.WATER.defaultBlockState(),
                        2
                );
            }

            if (text.contains("ocean") || text.contains("river")) {
                return new ColumnMaterials(
                        Blocks.SAND.defaultBlockState(),
                        Blocks.DIRT.defaultBlockState(),
                        Blocks.STONE.defaultBlockState(),
                        Blocks.WATER.defaultBlockState(),
                        3
                );
            }
        }

        if (biomePath.contains("desert")) {
            return new ColumnMaterials(
                    Blocks.SAND.defaultBlockState(),
                    Blocks.SAND.defaultBlockState(),
                    Blocks.SANDSTONE.defaultBlockState(),
                    Blocks.WATER.defaultBlockState(),
                    4
            );
        }

        return new ColumnMaterials(
                Blocks.GRASS_BLOCK.defaultBlockState(),
                Blocks.DIRT.defaultBlockState(),
                Blocks.STONE.defaultBlockState(),
                Blocks.WATER.defaultBlockState(),
                4
        );
    }

    private static int guessGroundLevelFromAgebook(AgebookDataComponent data) {
        for (ItemStack page : data.pages()) {
            String text = page.toString().toLowerCase();

            if (text.contains("deep_dark")) {
                return 48;
            }

            if (text.contains("ocean")) {
                return 48;
            }

            if (text.contains("river")) {
                return 58;
            }

            if (text.contains("badlands")) {
                return 68;
            }

            if (text.contains("grove") || text.contains("pale_garden")) {
                return 72;
            }
        }

        return 64;
    }

    private static String extractDimensionId(String raw) {
        int start = raw.indexOf('[');
        int end = raw.indexOf(']');

        if (start >= 0 && end > start) {
            return raw.substring(start + 1, end);
        }

        return raw;
    }

    private record PendingTeleport(UUID playerId, ItemStack agebook, int delayTicks) {
    }

    private record ColumnMaterials(
            BlockState top,
            BlockState under,
            BlockState base,
            BlockState fluid,
            int underDepth
    ) {
    }
}