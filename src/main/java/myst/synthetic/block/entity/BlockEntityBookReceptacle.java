package myst.synthetic.block.entity;

import myst.synthetic.MystcraftBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityBookReceptacle extends BlockEntity {

	public BlockEntityBookReceptacle(BlockPos pos, BlockState state) {
		super(MystcraftBlockEntities.BOOK_RECEPTACLE, pos, state);
	}
}