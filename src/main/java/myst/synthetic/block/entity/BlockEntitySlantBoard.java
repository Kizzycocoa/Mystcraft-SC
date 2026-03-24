package myst.synthetic.block.entity;

import myst.synthetic.MystcraftBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntitySlantBoard extends BlockEntity {

	public BlockEntitySlantBoard (BlockPos pos, BlockState state) {
		super(MystcraftBlockEntities.SLANT_BOARD, pos, state);
	}
}