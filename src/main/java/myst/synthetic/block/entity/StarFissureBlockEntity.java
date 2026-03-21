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
		if (level.isClientSide()) {
			return;
		}

		AABB area = TELEPORT_BOX.move(pos);
		List<Entity> entities = level.getEntities((Entity) null, area);

		for (Entity entity : entities) {
			if (entity == null) {
				continue;
			}

			if (!entity.canUsePortal(false)) {
				continue;
			}

			ILinkInfo info = createStarFissureLink(level, entity);
			if (info == null) {
				continue;
			}
			info.setSpawnYaw(entity.getYRot());

			// TODO: Port legacy StarFissureLinkEvent hook.
			// Legacy Mystcraft fired an event here before travel.
			LinkController.travelEntity(level, entity, info);
		}
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

		// Debug rule: do not teleport if already in the overworld.
		if ("minecraft:overworld".equals(currentDimension)) {
			return null;
		}

		int baseX = entity.blockPosition().getX();
		int baseZ = entity.blockPosition().getZ();

		// Nether and future compressed dimensions can scale outward.
		if ("minecraft:the_nether".equals(currentDimension)) {
			baseX *= 8;
			baseZ *= 8;
		}

		int offsetX = level.random.nextInt(60001) - 30000;
		int offsetZ = level.random.nextInt(60001) - 30000;

		int targetX = baseX + offsetX;
		int targetZ = baseZ + offsetZ;

		LinkOptions info = new LinkOptions(null);
		info.setDimensionUID("minecraft:overworld");
		info.setSpawn(new BlockPos(targetX, entity.blockPosition().getY(), targetZ));
		info.setFlag(LinkPropertyAPI.FLAG_NATURAL, true);
		info.setFlag(LinkPropertyAPI.FLAG_EXTERNAL, true);
		info.setProperty(LinkPropertyAPI.PROP_SOUND, "mystcraft-sc:linking.link-fissure");

		return info;
	}
}