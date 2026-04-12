package myst.synthetic.util;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import myst.synthetic.MystcraftBlocks;
import myst.synthetic.block.BlockBookReceptacle;
import myst.synthetic.block.BlockCrystal;
import myst.synthetic.block.BlockLinkPortal;
import myst.synthetic.block.entity.BlockEntityBookReceptacle;
import myst.synthetic.block.property.CrystalColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.List;

public final class PortalUtils {

    private static final int MAX_FRAME_SCAN = 21;

    /**
     * Prevent recursive neighbor-update rebuild storms.
     */
    private static final Map<Level, Integer> MUTATION_DEPTH = new IdentityHashMap<>();
    private static final Map<Level, Set<QueuedRefresh>> PENDING_REFRESHES = new IdentityHashMap<>();

    private PortalUtils() {
    }

    public static boolean isMutating(Level level) {
        return MUTATION_DEPTH.getOrDefault(level, 0) > 0;
    }

    private static void beginMutation(Level level) {
        MUTATION_DEPTH.put(level, MUTATION_DEPTH.getOrDefault(level, 0) + 1);
    }

    private static void endMutation(Level level) {
        int depth = MUTATION_DEPTH.getOrDefault(level, 0);
        if (depth <= 1) {
            MUTATION_DEPTH.remove(level);
            flushQueuedRefreshes(level);
        } else {
            MUTATION_DEPTH.put(level, depth - 1);
        }
    }

    private record PortalRegion(List<BlockPos> positions) {
        private Iterable<BlockPos> placedSoFar(BlockPos exclusiveEnd) {
            List<BlockPos> placed = new ArrayList<>();
            for (BlockPos pos : positions) {
                if (pos.equals(exclusiveEnd)) {
                    break;
                }
                placed.add(pos);
            }
            return placed;
        }
    }

    private static void queueRefresh(Level level, BlockPos origin, CrystalColor color) {
        PENDING_REFRESHES
                .computeIfAbsent(level, ignored -> new HashSet<>())
                .add(new QueuedRefresh(origin.immutable(), color));
    }

    private static void flushQueuedRefreshes(Level level) {
        Set<QueuedRefresh> queued = PENDING_REFRESHES.remove(level);
        if (queued == null || queued.isEmpty()) {
            return;
        }

        beginMutation(level);
        try {
            Set<BlockPos> receptacles = new HashSet<>();

            for (QueuedRefresh refresh : queued) {
                for (Direction direction : Direction.values()) {
                    BlockPos neighborPos = refresh.origin.relative(direction);
                    collectOwningReceptaclesFrom(level, neighborPos, refresh.color, receptacles);
                }
            }

            for (BlockPos receptaclePos : receptacles) {
                BlockEntity be = level.getBlockEntity(receptaclePos);
                if (be instanceof BlockEntityBookReceptacle receptacle
                        && receptacle.hasValidPortalBook()
                        && receptacle.hasValidSupportCrystal()) {
                    CrystalColor color = receptacle.getBlockState().getValue(BlockBookReceptacle.COLOR);
                    firePortal(level, receptaclePos, color);
                }
            }
        } finally {
            int depth = MUTATION_DEPTH.getOrDefault(level, 0);
            if (depth <= 1) {
                MUTATION_DEPTH.remove(level);
            } else {
                MUTATION_DEPTH.put(level, depth - 1);
            }
        }
    }

    public static void firePortal(Level level, BlockPos receptaclePos, CrystalColor requiredColor) {
        beginMutation(level);
        try {
            BlockEntity be = level.getBlockEntity(receptaclePos);
            if (!(be instanceof BlockEntityBookReceptacle receptacle)) {
                return;
            }

            if (!receptacle.hasValidPortalBook()) {
                return;
            }

            if (!receptacle.hasValidSupportCrystal()) {
                return;
            }

            BlockPos basePos = receptacle.getSupportCrystalPos();
            BlockState baseState = level.getBlockState(basePos);

            if (!baseState.is(MystcraftBlocks.CRYSTAL)) {
                return;
            }

            if (baseState.getValue(BlockCrystal.COLOR) != requiredColor) {
                return;
            }

            depolarizeFrom(level, basePos, requiredColor);

            Set<BlockPos> activeCrystalNetwork = pathCrystalNetwork(level, receptaclePos, basePos, requiredColor);
            fillPortalFrames(level, activeCrystalNetwork, requiredColor);
        } finally {
            endMutation(level);
        }
    }

    public static void shutdownPortal(Level level, BlockPos receptaclePos) {
        beginMutation(level);
        try {
            BlockEntity be = level.getBlockEntity(receptaclePos);
            if (!(be instanceof BlockEntityBookReceptacle receptacle)) {
                return;
            }

            if (!receptacle.hasValidSupportCrystal()) {
                return;
            }

            BlockPos basePos = receptacle.getSupportCrystalPos();
            CrystalColor requiredColor = receptacle.getBlockState().getValue(BlockBookReceptacle.COLOR);

            depolarizeFrom(level, basePos, requiredColor);
        } finally {
            endMutation(level);
        }
    }

    private static Set<BlockPos> pathCrystalNetwork(
            Level level,
            BlockPos receptaclePos,
            BlockPos basePos,
            CrystalColor requiredColor
    ) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        Map<BlockPos, BlockPos> parentByPos = new HashMap<>();

        queue.add(basePos);
        parentByPos.put(basePos, receptaclePos);

        while (!queue.isEmpty()) {
            BlockPos pos = queue.removeFirst();
            if (!visited.add(pos)) {
                continue;
            }

            BlockState state = level.getBlockState(pos);
            if (!state.is(MystcraftBlocks.CRYSTAL)) {
                continue;
            }

            if (state.getValue(BlockCrystal.COLOR) != requiredColor) {
                continue;
            }

            BlockPos parentPos = parentByPos.get(pos);
            if (parentPos == null) {
                continue;
            }

            Direction sourceDirection = directionFromTo(pos, parentPos);

            level.setBlock(
                    pos,
                    BlockCrystal.getDirectedState(state, sourceDirection),
                    Block.UPDATE_ALL
            );

            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = pos.relative(direction);

                if (visited.contains(neighborPos) || parentByPos.containsKey(neighborPos)) {
                    continue;
                }

                BlockState neighborState = level.getBlockState(neighborPos);
                if (!neighborState.is(MystcraftBlocks.CRYSTAL)) {
                    continue;
                }

                if (neighborState.getValue(BlockCrystal.COLOR) != requiredColor) {
                    continue;
                }

                parentByPos.put(neighborPos, pos);
                queue.add(neighborPos);
            }
        }

        return visited;
    }

    /**
     * Build portals in three full passes instead of choosing the first valid axis
     * per block. This prevents intersecting frames from fighting each other.
     *
     * Axis order is shuffled from the current server game time, so every rebuild
     * uses one deterministic X/Y/Z priority for that tick.
     */
    private static void fillPortalFrames(Level level, Set<BlockPos> activeCrystalNetwork, CrystalColor requiredColor) {
        Set<BlockPos> seedCandidates = new HashSet<>();

        for (BlockPos crystalPos : activeCrystalNetwork) {
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = crystalPos.relative(direction);
                if (level.getBlockState(neighborPos).isAir()) {
                    seedCandidates.add(neighborPos);
                }
            }
        }

        Direction.Axis[] axisOrder = getAxisBuildOrder(level);

        for (Direction.Axis axis : axisOrder) {
            Set<BlockPos> consumed = new HashSet<>();

            for (BlockPos seed : seedCandidates) {
                if (consumed.contains(seed)) {
                    continue;
                }

                if (!level.getBlockState(seed).isAir()) {
                    continue;
                }

                if (!isFramedAlongAxis(level, seed, axis, requiredColor)) {
                    continue;
                }

                PortalRegion region = collectPortalRegion(level, seed, axis, requiredColor);
                if (region == null) {
                    continue;
                }

                consumed.addAll(region.positions());

                for (BlockPos pos : region.positions()) {
                    BlockPos sourcePos = findAdjacentActiveConductor(level, pos, requiredColor);
                    if (sourcePos == null) {
                        // Fail closed. If we cannot power the region cleanly, place none of it.
                        for (BlockPos placedPos : region.placedSoFar(pos)) {
                            if (level.getBlockState(placedPos).is(MystcraftBlocks.LINK_PORTAL)) {
                                level.removeBlock(placedPos, false);
                            }
                        }
                        break;
                    }

                    Direction sourceDirection = directionFromTo(pos, sourcePos);

                    BlockState portalState = BlockLinkPortal.getDirectedState(
                            MystcraftBlocks.LINK_PORTAL.defaultBlockState(),
                            requiredColor,
                            sourceDirection,
                            axis,
                            true
                    );

                    level.setBlock(pos, portalState, Block.UPDATE_ALL);
                }
            }
        }
    }

    private static Direction.Axis[] getAxisBuildOrder(Level level) {
        Direction.Axis[] axes = new Direction.Axis[]{
                Direction.Axis.X,
                Direction.Axis.Y,
                Direction.Axis.Z
        };

        RandomSource random = RandomSource.create(level.getGameTime());

        for (int i = axes.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Direction.Axis tmp = axes[i];
            axes[i] = axes[j];
            axes[j] = tmp;
        }

        return axes;
    }

    private static PortalRegion collectPortalRegion(
            Level level,
            BlockPos seed,
            Direction.Axis axis,
            CrystalColor requiredColor
    ) {
        if (!level.getBlockState(seed).isAir()) {
            return null;
        }

        if (!isFramedAlongAxis(level, seed, axis, requiredColor)) {
            return null;
        }

        Direction[] planeDirections = getPlaneDirections(axis);

        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        List<BlockPos> ordered = new ArrayList<>();

        int minA = getPlaneCoordA(seed, axis);
        int maxA = minA;
        int minB = getPlaneCoordB(seed, axis);
        int maxB = minB;

        queue.add(seed);

        while (!queue.isEmpty()) {
            BlockPos pos = queue.removeFirst();
            if (!visited.add(pos)) {
                continue;
            }

            BlockState state = level.getBlockState(pos);
            if (!state.isAir()) {
                return null;
            }

            if (!isFramedAlongAxis(level, pos, axis, requiredColor)) {
                return null;
            }

            if (hasConflictingAdjacentPortalAxis(level, pos, requiredColor, axis)) {
                return null;
            }

            ordered.add(pos);

            int a = getPlaneCoordA(pos, axis);
            int b = getPlaneCoordB(pos, axis);

            minA = Math.min(minA, a);
            maxA = Math.max(maxA, a);
            minB = Math.min(minB, b);
            maxB = Math.max(maxB, b);

            if ((maxA - minA) >= MAX_FRAME_SCAN || (maxB - minB) >= MAX_FRAME_SCAN) {
                return null;
            }

            for (Direction direction : planeDirections) {
                BlockPos neighborPos = pos.relative(direction);
                BlockState neighborState = level.getBlockState(neighborPos);

                if (neighborState.isAir()) {
                    if (!isFramedAlongAxis(level, neighborPos, axis, requiredColor)) {
                        // Leak or invalid continuation: fail the whole attempted portal.
                        return null;
                    }

                    if (!visited.contains(neighborPos)) {
                        queue.add(neighborPos);
                    }
                    continue;
                }

                if (neighborState.is(MystcraftBlocks.CRYSTAL)) {
                    if (!neighborState.getValue(BlockCrystal.ACTIVE)
                            || neighborState.getValue(BlockCrystal.COLOR) != requiredColor) {
                        return null;
                    }
                    continue;
                }

                if (neighborState.is(MystcraftBlocks.LINK_PORTAL)) {
                    if (!neighborState.getValue(BlockLinkPortal.ACTIVE)
                            || neighborState.getValue(BlockLinkPortal.COLOR) != requiredColor
                            || neighborState.getValue(BlockLinkPortal.RENDER_ROTATION) != axis) {
                        return null;
                    }
                    continue;
                }

                BlockEntity be = level.getBlockEntity(neighborPos);
                if (be instanceof BlockEntityBookReceptacle receptacle) {
                    if (!receptacle.hasValidPortalBook()
                            || !receptacle.hasValidSupportCrystal()
                            || receptacle.getBlockState().getValue(BlockBookReceptacle.COLOR) != requiredColor) {
                        return null;
                    }
                    continue;
                }

                // Any other solid/non-air thing touching the filled region means failure.
                return null;
            }
        }

        return new PortalRegion(ordered);
    }

    private static Direction[] getPlaneDirections(Direction.Axis axis) {
        return switch (axis) {
            case X -> new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH};
            case Y -> new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
            case Z -> new Direction[]{Direction.UP, Direction.DOWN, Direction.WEST, Direction.EAST};
        };
    }

    private static int getPlaneCoordA(BlockPos pos, Direction.Axis axis) {
        return switch (axis) {
            case X -> pos.getY();
            case Y -> pos.getX();
            case Z -> pos.getY();
        };
    }

    private static int getPlaneCoordB(BlockPos pos, Direction.Axis axis) {
        return switch (axis) {
            case X -> pos.getZ();
            case Y -> pos.getZ();
            case Z -> pos.getX();
        };
    }

    public static void depolarizeFrom(Level level, BlockPos startPos, CrystalColor requiredColor) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos pos = queue.removeFirst();
            if (!visited.add(pos)) {
                continue;
            }

            BlockState state = level.getBlockState(pos);

            if (state.is(MystcraftBlocks.CRYSTAL)) {
                if (state.getValue(BlockCrystal.COLOR) != requiredColor || !state.getValue(BlockCrystal.ACTIVE)) {
                    continue;
                }

                level.setBlock(pos, BlockCrystal.getDisabledState(state), Block.UPDATE_ALL);
                addSameColorNeighbors(level, pos, requiredColor, queue, visited);
                continue;
            }

            if (state.is(MystcraftBlocks.LINK_PORTAL)) {
                if (state.getValue(BlockLinkPortal.COLOR) != requiredColor || !state.getValue(BlockLinkPortal.ACTIVE)) {
                    continue;
                }

                level.removeBlock(pos, false);
                addSameColorNeighbors(level, pos, requiredColor, queue, visited);
            }
        }
    }

    public static void validatePortal(Level level, BlockPos portalPos) {
        BlockState state = level.getBlockState(portalPos);
        if (!state.is(MystcraftBlocks.LINK_PORTAL)) {
            return;
        }

        CrystalColor requiredColor = state.getValue(BlockLinkPortal.COLOR);

        Set<BlockPos> component = collectPortalComponent(level, portalPos, requiredColor);
        if (component.isEmpty()) {
            level.removeBlock(portalPos, false);
            refreshNearbyReceptacles(level, portalPos, requiredColor);
            return;
        }

        Direction.Axis requiredAxis = state.getValue(BlockLinkPortal.RENDER_ROTATION);

        for (BlockPos pos : component) {
            BlockState portalState = level.getBlockState(pos);
            if (!portalState.is(MystcraftBlocks.LINK_PORTAL)) {
                removePortalComponent(level, component);
                refreshNearbyReceptacles(level, portalPos, requiredColor);
                return;
            }

            if (!portalState.getValue(BlockLinkPortal.ACTIVE)
                    || portalState.getValue(BlockLinkPortal.COLOR) != requiredColor
                    || portalState.getValue(BlockLinkPortal.RENDER_ROTATION) != requiredAxis
                    || hasConflictingAdjacentPortalAxis(level, pos, requiredColor, requiredAxis)
                    || !portalBlockStillValid(level, pos, requiredColor, requiredAxis)) {
                removePortalComponent(level, component);
                refreshNearbyReceptacles(level, portalPos, requiredColor);
                return;
            }
        }
    }

    private static Set<BlockPos> collectPortalComponent(Level level, BlockPos startPos, CrystalColor requiredColor) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();

        queue.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos pos = queue.removeFirst();
            if (!visited.add(pos)) {
                continue;
            }

            BlockState state = level.getBlockState(pos);
            if (!state.is(MystcraftBlocks.LINK_PORTAL)
                    || !state.getValue(BlockLinkPortal.ACTIVE)
                    || state.getValue(BlockLinkPortal.COLOR) != requiredColor) {
                continue;
            }

            for (Direction direction : Direction.values()) {
                queue.add(pos.relative(direction));
            }
        }

        return visited;
    }

    private static void removePortalComponent(Level level, Set<BlockPos> component) {
        for (BlockPos pos : component) {
            BlockState state = level.getBlockState(pos);
            if (state.is(MystcraftBlocks.LINK_PORTAL)) {
                level.removeBlock(pos, false);
            }
        }
    }

    private static boolean portalBlockStillValid(
            Level level,
            BlockPos portalPos,
            CrystalColor requiredColor,
            Direction.Axis axis
    ) {
        BlockState state = level.getBlockState(portalPos);
        if (!state.is(MystcraftBlocks.LINK_PORTAL)) {
            return false;
        }

        Direction sourceDirection = state.getValue(BlockLinkPortal.SOURCE_DIRECTION);
        BlockPos sourcePos = portalPos.relative(sourceDirection);
        BlockState sourceState = level.getBlockState(sourcePos);

        boolean hasValidSource =
                (sourceState.is(MystcraftBlocks.CRYSTAL)
                        && sourceState.getValue(BlockCrystal.ACTIVE)
                        && sourceState.getValue(BlockCrystal.COLOR) == requiredColor)
                        || (sourceState.is(MystcraftBlocks.LINK_PORTAL)
                        && sourceState.getValue(BlockLinkPortal.ACTIVE)
                        && sourceState.getValue(BlockLinkPortal.COLOR) == requiredColor);

        if (!hasValidSource) {
            return false;
        }

        return isFramedAlongAxis(level, portalPos, axis, requiredColor);
    }

    public static void refreshNearbyReceptacles(Level level, BlockPos origin, CrystalColor requiredColor) {
        if (isMutating(level)) {
            queueRefresh(level, origin, requiredColor);
            return;
        }

        Set<BlockPos> receptacles = new HashSet<>();

        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = origin.relative(direction);
            collectOwningReceptaclesFrom(level, neighborPos, requiredColor, receptacles);
        }

        beginMutation(level);
        try {
            for (BlockPos receptaclePos : receptacles) {
                BlockEntity be = level.getBlockEntity(receptaclePos);
                if (be instanceof BlockEntityBookReceptacle receptacle
                        && receptacle.hasValidPortalBook()
                        && receptacle.hasValidSupportCrystal()
                        && receptacle.getBlockState().getValue(BlockBookReceptacle.COLOR) == requiredColor) {
                    firePortal(level, receptaclePos, requiredColor);
                }
            }
        } finally {
            endMutation(level);
        }
    }

    private static void collectOwningReceptaclesFrom(
            Level level,
            BlockPos startPos,
            CrystalColor requiredColor,
            Set<BlockPos> receptacles
    ) {
        BlockState startState = level.getBlockState(startPos);

        if (startState.is(MystcraftBlocks.CRYSTAL)) {
            if (!startState.getValue(BlockCrystal.ACTIVE)
                    || startState.getValue(BlockCrystal.COLOR) != requiredColor) {
                return;
            }
        } else if (startState.is(MystcraftBlocks.LINK_PORTAL)) {
            if (!startState.getValue(BlockLinkPortal.ACTIVE)
                    || startState.getValue(BlockLinkPortal.COLOR) != requiredColor) {
                return;
            }
        } else {
            return;
        }

        BlockEntityBookReceptacle receptacle = getOwningReceptacle(level, startPos);
        if (receptacle != null) {
            receptacles.add(receptacle.getBlockPos());
        }
    }

    public static boolean isCrystalStillValid(Level level, BlockPos pos, CrystalColor requiredColor) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(MystcraftBlocks.CRYSTAL) || !state.getValue(BlockCrystal.ACTIVE)) {
            return false;
        }

        if (state.getValue(BlockCrystal.COLOR) != requiredColor) {
            return false;
        }

        Direction sourceDirection = state.getValue(BlockCrystal.SOURCE_DIRECTION);
        BlockPos sourcePos = pos.relative(sourceDirection);
        BlockState sourceState = level.getBlockState(sourcePos);

        if (sourceState.is(MystcraftBlocks.CRYSTAL)) {
            return sourceState.getValue(BlockCrystal.ACTIVE)
                    && sourceState.getValue(BlockCrystal.COLOR) == requiredColor;
        }

        if (sourceState.is(MystcraftBlocks.LINK_PORTAL)) {
            return sourceState.getValue(BlockLinkPortal.ACTIVE)
                    && sourceState.getValue(BlockLinkPortal.COLOR) == requiredColor;
        }

        BlockEntity be = level.getBlockEntity(sourcePos);
        if (be instanceof BlockEntityBookReceptacle receptacle) {
            return receptacle.hasValidPortalBook()
                    && receptacle.hasValidSupportCrystal()
                    && receptacle.getBlockState().getValue(BlockBookReceptacle.COLOR) == requiredColor
                    && receptacle.getSupportCrystalPos().equals(pos);
        }

        return false;
    }

    public static void validateAround(Level level, BlockPos origin, CrystalColor requiredColor) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = origin.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);

            if (neighborState.is(MystcraftBlocks.LINK_PORTAL)
                    && neighborState.getValue(BlockLinkPortal.COLOR) == requiredColor) {
                validatePortal(level, neighborPos);
            } else if (neighborState.is(MystcraftBlocks.CRYSTAL)
                    && neighborState.getValue(BlockCrystal.COLOR) == requiredColor
                    && neighborState.getValue(BlockCrystal.ACTIVE)
                    && !isCrystalStillValid(level, neighborPos, requiredColor)) {
                level.setBlock(neighborPos, BlockCrystal.getDisabledState(neighborState), Block.UPDATE_ALL);
            }
        }
    }

    public static BlockEntityBookReceptacle getOwningReceptacle(Level level, BlockPos startPos) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos pos = queue.removeFirst();
            if (!visited.add(pos)) {
                continue;
            }

            BlockState state = level.getBlockState(pos);

            if (state.is(MystcraftBlocks.CRYSTAL) && state.getValue(BlockCrystal.ACTIVE)) {
                queue.add(pos.relative(state.getValue(BlockCrystal.SOURCE_DIRECTION)));
                continue;
            }

            if (state.is(MystcraftBlocks.LINK_PORTAL) && state.getValue(BlockLinkPortal.ACTIVE)) {
                queue.add(pos.relative(state.getValue(BlockLinkPortal.SOURCE_DIRECTION)));
                continue;
            }

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlockEntityBookReceptacle receptacle) {
                return receptacle;
            }
        }

        return null;
    }

    private static boolean isPortalFillSpace(BlockState state) {
        return state.isAir();
    }

    private static BlockPos findAdjacentActiveConductor(Level level, BlockPos pos, CrystalColor requiredColor) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);

            if (neighborState.is(MystcraftBlocks.CRYSTAL)
                    && neighborState.getValue(BlockCrystal.ACTIVE)
                    && neighborState.getValue(BlockCrystal.COLOR) == requiredColor) {
                return neighborPos;
            }

            if (neighborState.is(MystcraftBlocks.LINK_PORTAL)
                    && neighborState.getValue(BlockLinkPortal.ACTIVE)
                    && neighborState.getValue(BlockLinkPortal.COLOR) == requiredColor) {
                return neighborPos;
            }
        }

        return null;
    }

    private static boolean isFramedAlongAxis(Level level, BlockPos pos, Direction.Axis axis, CrystalColor requiredColor) {
        Direction[] planeDirections = switch (axis) {
            case X -> new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH};
            case Y -> new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
            case Z -> new Direction[]{Direction.UP, Direction.DOWN, Direction.WEST, Direction.EAST};
        };

        for (Direction direction : planeDirections) {
            if (!rayHitsFrameBoundary(level, pos, direction, requiredColor, axis)) {
                return false;
            }
        }

        return true;
    }

    private static boolean rayHitsFrameBoundary(
            Level level,
            BlockPos startPos,
            Direction direction,
            CrystalColor requiredColor,
            Direction.Axis portalAxis
    ) {
        BlockPos.MutableBlockPos cursor = startPos.mutable();

        for (int i = 0; i < MAX_FRAME_SCAN; i++) {
            cursor.move(direction);
            BlockState state = level.getBlockState(cursor);

            if (state.is(MystcraftBlocks.CRYSTAL)) {
                return state.getValue(BlockCrystal.ACTIVE)
                        && state.getValue(BlockCrystal.COLOR) == requiredColor;
            }

            if (state.is(MystcraftBlocks.LINK_PORTAL)) {
                if (state.getValue(BlockLinkPortal.ACTIVE)
                        && state.getValue(BlockLinkPortal.COLOR) == requiredColor
                        && state.getValue(BlockLinkPortal.RENDER_ROTATION) == portalAxis) {
                    continue;
                }
                return false;
            }

            if (!state.isAir()) {
                return false;
            }
        }

        return false;
    }

    private static boolean hasConflictingAdjacentPortalAxis(
            Level level,
            BlockPos pos,
            CrystalColor requiredColor,
            Direction.Axis axis
    ) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);

            if (!neighborState.is(MystcraftBlocks.LINK_PORTAL)) {
                continue;
            }

            if (!neighborState.getValue(BlockLinkPortal.ACTIVE)) {
                continue;
            }

            if (neighborState.getValue(BlockLinkPortal.COLOR) != requiredColor) {
                continue;
            }

            if (neighborState.getValue(BlockLinkPortal.RENDER_ROTATION) != axis) {
                return true;
            }
        }

        return false;
    }

    private static void addSameColorNeighbors(
            Level level,
            BlockPos pos,
            CrystalColor requiredColor,
            ArrayDeque<BlockPos> queue,
            Set<BlockPos> visited
    ) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            if (visited.contains(neighborPos)) {
                continue;
            }

            BlockState neighborState = level.getBlockState(neighborPos);

            if (neighborState.is(MystcraftBlocks.CRYSTAL)
                    && neighborState.getValue(BlockCrystal.COLOR) == requiredColor
                    && neighborState.getValue(BlockCrystal.ACTIVE)) {
                queue.add(neighborPos);
                continue;
            }

            if (neighborState.is(MystcraftBlocks.LINK_PORTAL)
                    && neighborState.getValue(BlockLinkPortal.COLOR) == requiredColor
                    && neighborState.getValue(BlockLinkPortal.ACTIVE)) {
                queue.add(neighborPos);
            }
        }
    }

    public static Direction directionFromTo(BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        int dz = to.getZ() - from.getZ();

        if (dx == 1 && dy == 0 && dz == 0) {
            return Direction.EAST;
        }
        if (dx == -1 && dy == 0 && dz == 0) {
            return Direction.WEST;
        }
        if (dx == 0 && dy == 1 && dz == 0) {
            return Direction.UP;
        }
        if (dx == 0 && dy == -1 && dz == 0) {
            return Direction.DOWN;
        }
        if (dx == 0 && dy == 0 && dz == 1) {
            return Direction.SOUTH;
        }
        if (dx == 0 && dy == 0 && dz == -1) {
            return Direction.NORTH;
        }

        throw new IllegalArgumentException("Positions are not adjacent: " + from + " -> " + to);
    }

    private record QueuedRefresh(BlockPos origin, CrystalColor color) {
    }
}