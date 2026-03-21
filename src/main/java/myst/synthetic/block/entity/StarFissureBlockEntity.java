package myst.synthetic.block.entity;

import myst.synthetic.MystcraftBlockEntities;
import myst.synthetic.api.hook.LinkPropertyAPI;
import myst.synthetic.api.linking.ILinkInfo;
import myst.synthetic.config.MystcraftConfig;
import myst.synthetic.linking.LinkController;
import myst.synthetic.linking.LinkOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class StarFissureBlockEntity extends BlockEntity {

	private static final AABB TELEPORT_BOX = new AABB(
			0.0D, 0.0D, 0.0D,
			1.0D, 0.2D, 1.0D
	);

	public StarFissureBlockEntity(BlockPos pos, BlockState state) {
		super(MystcraftBlockEntities.STAR_FISSURE, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, StarFissureBlockEntity blockEntity) {
		// Intentionally unused for now.
		// Star fissure teleport is triggered immediately in BlockStarFissure.entityInside(...).
	}
	public void teleportEntity(Entity entity) {
		if (this.level == null || this.level.isClientSide()) {
			return;
		}

		if (entity == null) {
			return;
		}

		ILinkInfo info = createStarFissureLink(this.level, entity);
		if (info == null) {
			return;
		}

		info.setSpawnYaw(entity.getYRot());
		LinkController.travelEntity(this.level, entity, info);
	}

	private static String extractDimensionId(String raw) {
		int slash = raw.lastIndexOf('/');
		int end = raw.lastIndexOf(']');

		if (slash >= 0 && end > slash) {
			return raw.substring(slash + 1, end).trim();
		}

		return raw;
	}

	private static ILinkInfo createStarFissureLink(Level level, Entity entity) {
		String currentDimension = extractDimensionId(level.dimension().toString());

		// Debug rule: do nothing if already in the overworld.
		if ("minecraft:overworld".equals(currentDimension)) {
			return null;
		}

		if (!(level instanceof net.minecraft.server.level.ServerLevel origin)) {
			return null;
		}

		net.minecraft.server.level.ServerLevel overworld = origin.getServer().overworld();
		if (overworld == null) {
			return null;
		}

		int baseX = entity.blockPosition().getX();
		int baseZ = entity.blockPosition().getZ();

		// Nether and future compressed dimensions scale outward first.
		if ("minecraft:the_nether".equals(currentDimension)) {
			baseX *= 8;
			baseZ *= 8;
		}

		int offsetX = level.random.nextInt(60001) - 30000;
		int offsetZ = level.random.nextInt(60001) - 30000;

		int targetX = baseX + offsetX;
		int targetZ = baseZ + offsetZ;

		// Force/generate the target chunk first so height is real, not fallback junk.
		overworld.getChunk(targetX >> 4, targetZ >> 4);

		int surfaceY = overworld.getHeight(
				net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				targetX,
				targetZ
		);

		LinkOptions info = new LinkOptions(new net.minecraft.nbt.CompoundTag());
		info.setDimensionUID("minecraft:overworld");

		// We already computed a safe target, so use it exactly.
		info.setSpawn(new BlockPos(targetX, surfaceY + 1, targetZ));
		info.setFlag(LinkPropertyAPI.FLAG_NATURAL, false);
		info.setFlag(LinkPropertyAPI.FLAG_EXTERNAL, true);
		info.setProperty(LinkPropertyAPI.PROP_SOUND, "mystcraft-sc:linking.link-fissure");

		return info;
	}
}