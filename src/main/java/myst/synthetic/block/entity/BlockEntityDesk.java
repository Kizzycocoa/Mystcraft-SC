package myst.synthetic.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import myst.synthetic.init.ModBlockEntities;

public class BlockEntityDesk extends BlockEntity {

	public BlockEntityDesk(BlockPos pos, BlockState state) {
		super(ModBlockEntities.WRITING_DESK, pos, state);
	}
}