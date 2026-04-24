package myst.synthetic.world.dimension;

import myst.synthetic.MystcraftSyntheticCodex;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public final class AgeRuntimeProbe {

    private AgeRuntimeProbe() {
    }

    public static boolean forceSpawnChunk(ServerLevel level) {
        MystcraftSyntheticCodex.LOGGER.info("[MystProbe] === Spawn chunk probe started for {} ===", level.dimension().identifier());

        try {
            MystcraftSyntheticCodex.LOGGER.info(
                    "[MystProbe] ChunkSource class: {}",
                    level.getChunkSource().getClass().getName()
            );

            MystcraftSyntheticCodex.LOGGER.info(
                    "[MystProbe] ChunkSource generator: {}",
                    level.getChunkSource().getGenerator().getClass().getName()
            );

            MystcraftSyntheticCodex.LOGGER.info("[MystProbe] Requesting FULL chunk 0,0 now...");

            ChunkAccess chunk = level.getChunkSource().getChunk(0, 0, ChunkStatus.FULL, true);

            MystcraftSyntheticCodex.LOGGER.info(
                    "[MystProbe] FULL chunk request returned: {}",
                    chunk == null ? "null" : chunk.getClass().getName()
            );

            if (chunk == null) {
                MystcraftSyntheticCodex.LOGGER.error("[MystProbe] Spawn chunk is null after FULL request.");
                return false;
            }

            BlockPos surface = new BlockPos(0, 64, 0);
            BlockPos stone = new BlockPos(0, 32, 0);
            BlockPos bedrock = new BlockPos(0, 0, 0);

            MystcraftSyntheticCodex.LOGGER.info("[MystProbe] Block at {} = {}", surface, level.getBlockState(surface));
            MystcraftSyntheticCodex.LOGGER.info("[MystProbe] Block at {} = {}", stone, level.getBlockState(stone));
            MystcraftSyntheticCodex.LOGGER.info("[MystProbe] Block at {} = {}", bedrock, level.getBlockState(bedrock));

            MystcraftSyntheticCodex.LOGGER.info("[MystProbe] === Spawn chunk probe finished successfully ===");
            return true;
        } catch (Throwable throwable) {
            MystcraftSyntheticCodex.LOGGER.error("[MystProbe] Spawn chunk probe failed.", throwable);
            return false;
        }
    }
}