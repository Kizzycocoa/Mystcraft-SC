package myst.synthetic.util;

import java.util.ArrayDeque;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class PortalUtils {

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
            onPulse(level, basePos, requiredColor);
            pathTo(level, receptaclePos, requiredColor);
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

    private static void onPulse(Level level, BlockPos basePos, CrystalColor requiredColor) {
        ArrayDeque<BlockPos> frontier = new ArrayDeque<>();
        ArrayDeque<BlockPos> created = new ArrayDeque<>();
        Set<BlockPos> seen = new HashSet<>();

        addSurrounding(frontier, basePos);

        while (!frontier.isEmpty()) {
            BlockPos pos = frontier.removeFirst();
            if (!seen.add(pos)) {
                continue;
            }

            expandPortal(level, pos, requiredColor, frontier, created);
        }

        while (!created.isEmpty()) {
            BlockPos pos = created.removeLast();
            if (!checkPortalTension(level, pos, requiredColor)) {
                level.removeBlock(pos, false);
            }
        }
    }

    private static void expandPortal(
            Level level,
            BlockPos pos,
            CrystalColor requiredColor,
            ArrayDeque<BlockPos> frontier,
            ArrayDeque<BlockPos> created
    ) {
        BlockState state = level.getBlockState(pos);
        if (!state.isAir()) {
            return;
        }

        int score = validLinkPortalScore(level.getBlockState(pos.east()), requiredColor)
                + validLinkPortalScore(level.getBlockState(pos.west()), requiredColor)
                + validLinkPortalScore(level.getBlockState(pos.above()), requiredColor)
                + validLinkPortalScore(level.getBlockState(pos.below()), requiredColor)
                + validLinkPortalScore(level.getBlockState(pos.south()), requiredColor)
                + validLinkPortalScore(level.getBlockState(pos.north()), requiredColor);

        if (score > 1) {
            level.setBlock(
                    pos,
                    MystcraftBlocks.LINK_PORTAL.defaultBlockState()
                            .setValue(BlockLinkPortal.COLOR, requiredColor)
                            .setValue(BlockLinkPortal.ACTIVE, false)
                            .setValue(BlockLinkPortal.SOURCE_DIRECTION, Direction.DOWN)
                            .setValue(BlockLinkPortal.HAS_ROTATION, false)
                            .setValue(BlockLinkPortal.RENDER_ROTATION, Direction.Axis.X),
                    Block.UPDATE_ALL
            );

            created.add(pos);
            addSurrounding(frontier, pos);
        }
    }

    private static void pathTo(Level level, BlockPos receptaclePos, CrystalColor requiredColor) {
        ArrayDeque<BlockPos> crystals = new ArrayDeque<>();
        ArrayDeque<BlockPos> portals = new ArrayDeque<>();
        ArrayDeque<BlockPos> repath = new ArrayDeque<>();
        Set<BlockPos> redraw = new HashSet<>();

        crystals.add(receptaclePos);

        while (!portals.isEmpty() || !crystals.isEmpty()) {
            while (!crystals.isEmpty()) {
                BlockPos pos = crystals.removeFirst();

                directPortal(level, pos.east(), Direction.WEST, requiredColor, crystals, portals);
                directPortal(level, pos.above(), Direction.DOWN, requiredColor, crystals, portals);
                directPortal(level, pos.south(), Direction.NORTH, requiredColor, crystals, portals);
                directPortal(level, pos.west(), Direction.EAST, requiredColor, crystals, portals);
                directPortal(level, pos.below(), Direction.UP, requiredColor, crystals, portals);
                directPortal(level, pos.north(), Direction.SOUTH, requiredColor, crystals, portals);

                redraw.add(pos);
            }

            if (!portals.isEmpty()) {
                BlockPos pos = portals.removeFirst();

                directPortal(level, pos.east(), Direction.WEST, requiredColor, crystals, portals);
                directPortal(level, pos.above(), Direction.DOWN, requiredColor, crystals, portals);
                directPortal(level, pos.south(), Direction.NORTH, requiredColor, crystals, portals);
                directPortal(level, pos.west(), Direction.EAST, requiredColor, crystals, portals);
                directPortal(level, pos.below(), Direction.UP, requiredColor, crystals, portals);
                directPortal(level, pos.north(), Direction.SOUTH, requiredColor, crystals, portals);

                if (level.getBlockState(pos).is(MystcraftBlocks.LINK_PORTAL)) {
                    repath.add(pos);
                }
            }
        }

        while (!repath.isEmpty()) {
            BlockPos pos = repath.removeFirst();
            BlockState state = level.getBlockState(pos);
            if (!state.is(MystcraftBlocks.LINK_PORTAL)) {
                continue;
            }

            if (!isPortalBlockStable(level, pos, requiredColor)) {
                repathNeighbors(level, pos, requiredColor);
                level.removeBlock(pos, false);
                addSurrounding(repath, pos);
            } else {
                BlockState fixed = state
                        .setValue(BlockLinkPortal.HAS_ROTATION, true)
                        .setValue(BlockLinkPortal.RENDER_ROTATION, resolvePortalAxis(level, pos, requiredColor));
                level.setBlock(pos, fixed, Block.UPDATE_ALL);
                redraw.add(pos);
            }
        }

        for (BlockPos pos : redraw) {
            BlockState state = level.getBlockState(pos);
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        }
    }

    private static void directPortal(
            Level level,
            BlockPos pos,
            Direction sourceDirection,
            CrystalColor requiredColor,
            ArrayDeque<BlockPos> crystals,
            ArrayDeque<BlockPos> portals
    ) {
        BlockState state = level.getBlockState(pos);
        if (validLinkPortalScore(state, requiredColor) == 0) {
            return;
        }

        if (isBlockActive(state)) {
            return;
        }

        if (state.is(MystcraftBlocks.CRYSTAL)) {
            level.setBlock(
                    pos,
                    BlockCrystal.getDirectedState(state, sourceDirection),
                    Block.UPDATE_ALL
            );
            crystals.add(pos);
            return;
        }

        if (state.is(MystcraftBlocks.LINK_PORTAL)) {
            level.setBlock(
                    pos,
                    MystcraftBlocks.LINK_PORTAL.defaultBlockState()
                            .setValue(BlockLinkPortal.COLOR, requiredColor)
                            .setValue(BlockLinkPortal.ACTIVE, true)
                            .setValue(BlockLinkPortal.SOURCE_DIRECTION, sourceDirection)
                            .setValue(BlockLinkPortal.HAS_ROTATION, true)
                            .setValue(BlockLinkPortal.RENDER_ROTATION, resolvePortalAxis(level, pos, requiredColor)),
                    Block.UPDATE_ALL
            );
            portals.add(pos);
        }
    }

    private static void repathNeighbors(Level level, BlockPos pos, CrystalColor requiredColor) {
        BlockEntityBookReceptacle owner = getOwningReceptacle(level, pos);
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();

        BlockState state = level.getBlockState(pos);
        if (state.is(MystcraftBlocks.LINK_PORTAL)) {
            level.setBlock(pos, BlockLinkPortal.getDisabledState(state), Block.UPDATE_ALL);
        } else if (state.is(MystcraftBlocks.CRYSTAL)) {
            level.setBlock(pos, BlockCrystal.getDisabledState(state), Block.UPDATE_ALL);
        }

        queue.add(pos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();

            redirectPortal(level, owner, current.east(), Direction.WEST, requiredColor, queue);
            redirectPortal(level, owner, current.above(), Direction.DOWN, requiredColor, queue);
            redirectPortal(level, owner, current.south(), Direction.NORTH, requiredColor, queue);
            redirectPortal(level, owner, current.west(), Direction.EAST, requiredColor, queue);
            redirectPortal(level, owner, current.below(), Direction.UP, requiredColor, queue);
            redirectPortal(level, owner, current.north(), Direction.SOUTH, requiredColor, queue);
        }
    }

    private static void redirectPortal(
            Level level,
            BlockEntityBookReceptacle owner,
            BlockPos pos,
            Direction newSourceDirection,
            CrystalColor requiredColor,
            ArrayDeque<BlockPos> queue
    ) {
        BlockState state = level.getBlockState(pos);
        if (validLinkPortalScore(state, requiredColor) == 0) {
            return;
        }

        if (!isBlockActive(state)) {
            return;
        }

        BlockState redirected;
        if (state.is(MystcraftBlocks.CRYSTAL)) {
            redirected = BlockCrystal.getDirectedState(state, newSourceDirection);
        } else if (state.is(MystcraftBlocks.LINK_PORTAL)) {
            redirected = state
                    .setValue(BlockLinkPortal.ACTIVE, true)
                    .setValue(BlockLinkPortal.SOURCE_DIRECTION, newSourceDirection)
                    .setValue(BlockLinkPortal.HAS_ROTATION, true)
                    .setValue(BlockLinkPortal.RENDER_ROTATION, resolvePortalAxis(level, pos, requiredColor));
        } else {
            return;
        }

        level.setBlock(pos, redirected, Block.UPDATE_ALL);

        BlockEntityBookReceptacle local = getOwningReceptacle(level, pos);
        if (local == owner || (local != null && owner == null)) {
            return;
        }

        if (state.is(MystcraftBlocks.LINK_PORTAL)) {
            level.removeBlock(pos, false);
        } else {
            level.setBlock(pos, BlockCrystal.getDisabledState(state), Block.UPDATE_ALL);
        }

        queue.add(pos);
    }

    private static boolean isPortalBlockStable(Level level, BlockPos pos, CrystalColor requiredColor) {
        if (!checkPortalTension(level, pos, requiredColor)) {
            return false;
        }
        return getOwningReceptacle(level, pos) != null;
    }

    private static boolean checkPortalTension(Level level, BlockPos pos, CrystalColor requiredColor) {
        int score = 0;

        if (validLinkPortalScore(level.getBlockState(pos.east()), requiredColor) > 0
                && validLinkPortalScore(level.getBlockState(pos.west()), requiredColor) > 0) {
            ++score;
        }
        if (validLinkPortalScore(level.getBlockState(pos.above()), requiredColor) > 0
                && validLinkPortalScore(level.getBlockState(pos.below()), requiredColor) > 0) {
            ++score;
        }
        if (validLinkPortalScore(level.getBlockState(pos.south()), requiredColor) > 0
                && validLinkPortalScore(level.getBlockState(pos.north()), requiredColor) > 0) {
            ++score;
        }

        return score > 1;
    }

    private static int validLinkPortalScore(BlockState state, CrystalColor requiredColor) {
        if (state.is(MystcraftBlocks.CRYSTAL) && state.getValue(BlockCrystal.COLOR) == requiredColor) {
            return 1;
        }
        if (state.is(MystcraftBlocks.LINK_PORTAL) && state.getValue(BlockLinkPortal.COLOR) == requiredColor) {
            return 1;
        }
        return 0;
    }

    private static boolean isBlockActive(BlockState state) {
        if (state.is(MystcraftBlocks.CRYSTAL)) {
            return state.getValue(BlockCrystal.ACTIVE);
        }
        if (state.is(MystcraftBlocks.LINK_PORTAL)) {
            return state.getValue(BlockLinkPortal.ACTIVE);
        }
        return false;
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

    public static void validatePortal(Level level, BlockPos start) {
        if (isMutating(level)) {
            return;
        }

        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            BlockPos pos = queue.removeFirst();
            if (!visited.add(pos)) {
                continue;
            }

            BlockState state = level.getBlockState(pos);
            if (!state.is(MystcraftBlocks.LINK_PORTAL)) {
                continue;
            }

            CrystalColor requiredColor = state.getValue(BlockLinkPortal.COLOR);

            if (!isPortalBlockStable(level, pos, requiredColor)) {
                level.removeBlock(pos, false);
                addSurrounding(queue, pos);
                refreshNearbyReceptacles(level, pos, requiredColor);
            }
        }
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

    private static void addSurrounding(ArrayDeque<BlockPos> set, BlockPos pos) {
        set.add(pos.east());
        set.add(pos.west());
        set.add(pos.above());
        set.add(pos.below());
        set.add(pos.south());
        set.add(pos.north());

        set.add(pos.east().above());
        set.add(pos.west().above());
        set.add(pos.east().below());
        set.add(pos.west().below());
        set.add(pos.south().above());
        set.add(pos.north().above());
        set.add(pos.south().below());
        set.add(pos.north().below());
        set.add(pos.east().south());
        set.add(pos.west().south());
        set.add(pos.east().north());
        set.add(pos.west().north());
    }

    private static Direction.Axis resolvePortalAxis(Level level, BlockPos pos, CrystalColor requiredColor) {
        boolean xPair = validLinkPortalScore(level.getBlockState(pos.east()), requiredColor) > 0
                && validLinkPortalScore(level.getBlockState(pos.west()), requiredColor) > 0;
        boolean yPair = validLinkPortalScore(level.getBlockState(pos.above()), requiredColor) > 0
                && validLinkPortalScore(level.getBlockState(pos.below()), requiredColor) > 0;
        boolean zPair = validLinkPortalScore(level.getBlockState(pos.south()), requiredColor) > 0
                && validLinkPortalScore(level.getBlockState(pos.north()), requiredColor) > 0;

        int score = (xPair ? 1 : 0) + (yPair ? 1 : 0) + (zPair ? 1 : 0);

        if (score != 1) {
            return Direction.Axis.X;
        }
        if (xPair) {
            return Direction.Axis.X;
        }
        if (yPair) {
            return Direction.Axis.Y;
        }
        return Direction.Axis.Z;
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
