package myst.synthetic.block.entity;

import myst.synthetic.MystcraftBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityLinkModifier extends BlockEntity {

	public BlockEntityLinkModifier(BlockPos pos, BlockState state) {
		super(MystcraftBlockEntities.LINK_MODIFIER, pos, state);
	}
}