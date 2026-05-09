package myst.synthetic.world.dimension;

import myst.synthetic.MystcraftSyntheticCodex;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public final class AgePlayerChunkTracker {

    private AgePlayerChunkTracker() {
    }

    public static void refreshAfterAgeTeleport(ServerPlayer player, ServerLevel destination, BlockPos target, String reason) {
        if (player == null || destination == null || target == null) {
            MystcraftSyntheticCodex.LOGGER.warn(
                    "[MystChunkTrack] Skipped refresh reason='{}': player={}, destination={}, target={}",
                    reason,
                    player,
                    destination == null ? "null" : destination.dimension().identifier(),
                    target
            );
            return;
        }

        ChunkPos chunkPos = new ChunkPos(target);
        SectionPos sectionPos = SectionPos.of(target);
        int viewDistance = Math.max(2, player.requestedViewDistance());

        MystcraftSyntheticCodex.LOGGER.info(
                "[MystChunkTrack] Before refresh reason='{}': player='{}', level={}, target={}, chunk={}, section={}, requestedViewDistance={}, lastSection={}, trackingView={}",
                reason,
                player.getScoreboardName(),
                player.level().dimension().identifier(),
                target,
                chunkPos,
                sectionPos,
                viewDistance,
                player.getLastSectionPos(),
                player.getChunkTrackingView()
        );

        player.setLastSectionPos(sectionPos);
        player.setChunkTrackingView(ChunkTrackingView.of(chunkPos, viewDistance));

        MystcraftSyntheticCodex.LOGGER.info(
                "[MystChunkTrack] After refresh reason='{}': player='{}', level={}, lastSection={}, trackingView={}",
                reason,
                player.getScoreboardName(),
                player.level().dimension().identifier(),
                player.getLastSectionPos(),
                player.getChunkTrackingView()
        );
    }
}