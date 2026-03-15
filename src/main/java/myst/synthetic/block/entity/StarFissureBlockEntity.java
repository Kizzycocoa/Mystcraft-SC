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

			ILinkInfo info = createDefaultStarFissureLink();
			info.setSpawnYaw(entity.getYRot());

			// TODO: Port legacy StarFissureLinkEvent hook.
			// Legacy Mystcraft fired an event here before travel.
			LinkController.travelEntity(level, entity, info);
		}
	}

	private static ILinkInfo createDefaultStarFissureLink() {
		LinkOptions info = new LinkOptions(null);

		info.setDimensionUID(
				MystcraftConfig.getString(
						MystcraftConfig.CATEGORY_GENERAL,
						"teleportation.homedim",
						"minecraft:overworld"
				)
		);

		info.setFlag(LinkPropertyAPI.FLAG_NATURAL, true);
		info.setFlag(LinkPropertyAPI.FLAG_EXTERNAL, true);
		info.setProperty(LinkPropertyAPI.PROP_SOUND, "mystcraft-sc:linking.link-fissure");

		return info;
	}
}