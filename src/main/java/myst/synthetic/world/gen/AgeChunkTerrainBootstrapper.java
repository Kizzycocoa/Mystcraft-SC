package myst.synthetic.world.gen;

import myst.synthetic.MystcraftSyntheticCodex;
import myst.synthetic.world.dimension.AgeDimensionKeys;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public final class AgeChunkTerrainBootstrapper {

    private static boolean initialized = false;

    private AgeChunkTerrainBootstrapper() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;

        ServerChunkEvents.CHUNK_LOAD.register(AgeChunkTerrainBootstrapper::onChunkLoad);
        MystcraftSyntheticCodex.LOGGER.info("Mystcraft Age chunk terrain bootstrapper ready.");
    }

    private static void onChunkLoad(ServerLevel level, LevelChunk chunk) {
        Identifier dimensionId = level.dimension().identifier();

        if (!AgeDimensionKeys.DIMENSION_NAMESPACE.equals(dimensionId.getNamespace())) {
            return;
        }

        fillFlatAgeChunk(level, chunk);
    }

    private static void fillFlatAgeChunk(ServerLevel level, LevelChunk chunk) {
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        int groundY = guessGroundLevel(level);
        int seaY = Math.max(groundY, 63);

        ColumnMaterials materials = guessMaterials(level);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int localX = 0; localX < 16; localX++) {
            int x = minX + localX;

            for (int localZ = 0; localZ < 16; localZ++) {
                int z = minZ + localZ;

                /*
                 * If this chunk already has terrain at the expected surface,
                 * do not overwrite it. This lets future generators replace this
                 * bootstrap without fighting saved chunks.
                 */
                pos.set(x, groundY, z);
                if (!level.getBlockState(pos).isAir()) {
                    continue;
                }

                for (int y = 0; y <= seaY; y++) {
                    BlockState state = stateForY(level.getSeed(), x, y, z, groundY, seaY, materials);

                    if (state.isAir()) {
                        continue;
                    }

                    pos.set(x, y, z);
                    level.setBlock(pos, state, 2);
                }
            }
        }
    }

    private static BlockState stateForY(
            long seed,
            int x,
            int y,
            int z,
            int groundY,
            int seaY,
            ColumnMaterials materials
    ) {
        if (isVanillaFloorBedrock(seed, x, y, z)) {
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

    private static boolean isVanillaFloorBedrock(long seed, int x, int y, int z) {
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

    private static int guessGroundLevel(ServerLevel level) {
        /*
         * First-pass bootstrap:
         * Keep this deliberately simple. The proper height should come from
         * MystChunkGeneratorSettings once the runtime generator path is fully
         * participating in chunk generation.
         */
        return 64;
    }

    private static ColumnMaterials guessMaterials(ServerLevel level) {
        /*
         * First-pass bootstrap:
         * Grass/dirt/stone is enough to prove the runtime Age is no longer void.
         * Biome-specific surfaces can come next once chunk storage is confirmed.
         */
        return new ColumnMaterials(
                Blocks.GRASS_BLOCK.defaultBlockState(),
                Blocks.DIRT.defaultBlockState(),
                Blocks.STONE.defaultBlockState(),
                Blocks.WATER.defaultBlockState(),
                4
        );
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