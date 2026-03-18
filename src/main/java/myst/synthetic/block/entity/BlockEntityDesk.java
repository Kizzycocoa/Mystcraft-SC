package myst.synthetic.block.entity;

import myst.synthetic.MystcraftBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityDesk extends BlockEntity {

	public BlockEntityDesk(BlockPos pos, BlockState state) {
		super(MystcraftBlockEntities.WRITING_DESK, pos, state);
	}
}