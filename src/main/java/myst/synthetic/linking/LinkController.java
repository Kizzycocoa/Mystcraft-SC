package myst.synthetic.linking;

import myst.synthetic.MystcraftSyntheticCodex;
import myst.synthetic.api.hook.LinkPropertyAPI;
import myst.synthetic.api.linking.ILinkInfo;
import myst.synthetic.config.MystcraftConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class LinkController {

    private LinkController() {
    }

    public static boolean travelEntity(Level world, Entity entity, ILinkInfo info) {
        if (world.isClientSide()) {
            return false;
        }

        if (!(world instanceof ServerLevel origin)) {
            MystcraftSyntheticCodex.LOGGER.warn("Mystcraft linking failed: origin was not a ServerLevel.");
            return false;
        }

        if (entity == null || info == null) {
            MystcraftSyntheticCodex.LOGGER.warn("Mystcraft linking failed: entity or link info was null.");
            return false;
        }

        String requestedDimension = info.getDimensionUID();

        MystcraftSyntheticCodex.LOGGER.info(
                "Mystcraft link requested from '{}' to '{}'.",
                origin.dimension().identifier(),
                requestedDimension
        );

        ServerLevel destination = getDestinationWorld(origin.getServer(), info);
        if (destination == null) {
            MystcraftSyntheticCodex.LOGGER.warn("Mystcraft linking failed: destination world was null.");
            return false;
        }

        BlockPos targetPos = resolveTargetPos(destination, info, entity);
        Vec3 targetVec = Vec3.atBottomCenterOf(targetPos);

        float yaw = info.getSpawnYaw();
        float pitch = entity.getXRot();

        MystcraftSyntheticCodex.LOGGER.info(
                "Mystcraft linking entity '{}' to '{}' at {}.",
                entity.getScoreboardName(),
                destination.dimension().identifier(),
                targetPos
        );

        TeleportTransition transition = new TeleportTransition(
                destination,
                targetVec,
                Vec3.ZERO,
                yaw,
                pitch,
                TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET)
        );

        Entity result = entity.teleport(transition);
        return result != null;
    }

    public static @Nullable ServerLevel getDestinationWorld(MinecraftServer server, ILinkInfo info) {
        String uid = info.getDimensionUID();

        if (uid == null || uid.isBlank()) {
            uid = MystcraftConfig.getString(
                    MystcraftConfig.CATEGORY_GENERAL,
                    "teleportation.homedim",
                    "minecraft:overworld"
            );
        }

        Identifier id = parseIdentifier(uid);
        if (id == null) {
            MystcraftSyntheticCodex.LOGGER.warn("Mystcraft linking failed: invalid dimension id '{}'", uid);
            return server.overworld();
        }

        ResourceKey<Level> levelKey = ResourceKey.create(Registries.DIMENSION, id);
        ServerLevel level = server.getLevel(levelKey);

        if (level == null) {
            MystcraftSyntheticCodex.LOGGER.warn("Mystcraft linking failed: unknown dimension '{}'", uid);
            return server.overworld();
        }

        return level;
    }

    public static BlockPos resolveTargetPos(ServerLevel destination, ILinkInfo info, Entity entity) {
        BlockPos configuredSpawn = info.getSpawn();

        if (configuredSpawn != null) {
            if (!info.getFlag(LinkPropertyAPI.FLAG_NATURAL)) {
                return configuredSpawn;
            }

            return findSafeLanding(destination, configuredSpawn);
        }

        BlockPos entityPos = entity.blockPosition();
        return findSafeLanding(destination, entityPos);
    }

    public static BlockPos findSafeLanding(ServerLevel level, BlockPos requested) {
        int x = requested.getX();
        int z = requested.getZ();

        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

        BlockPos top = new BlockPos(x, y, z);

        if (isSafeStandingPos(level, top)) {
            return top;
        }

        BlockPos below = top.below();
        if (isSafeStandingPos(level, below)) {
            return below;
        }

        LevelData.RespawnData respawnData = level.getServer().getRespawnData();

        if (respawnData != null && respawnData.dimension().equals(level.dimension())) {
            return respawnData.pos();
        }

        return new BlockPos(0, Math.max(level.getSeaLevel() + 1, 65), 0);
    }

    private static boolean isSafeStandingPos(ServerLevel level, BlockPos pos) {
        BlockPos feet = pos;
        BlockPos head = pos.above();
        BlockPos ground = pos.below();

        return level.getBlockState(feet).canBeReplaced()
                && level.getBlockState(head).canBeReplaced()
                && !level.getBlockState(ground).isAir();
    }

    private static @Nullable Identifier parseIdentifier(@Nullable String raw) {
        if (raw == null) {
            return null;
        }

        String value = raw.trim();
        if (value.isEmpty()) {
            return null;
        }

        try {
            String namespace = "minecraft";
            String path = value;

            int colon = value.indexOf(':');
            if (colon >= 0) {
                namespace = value.substring(0, colon);
                path = value.substring(colon + 1);
            }

            if (namespace.isBlank() || path.isBlank()) {
                return null;
            }

            return Identifier.fromNamespaceAndPath(namespace, path);
        } catch (Exception e) {
            return null;
        }
    }
}