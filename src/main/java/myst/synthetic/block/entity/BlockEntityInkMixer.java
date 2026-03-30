package myst.synthetic.block.entity;

import myst.synthetic.MystcraftBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityInkMixer extends BlockEntity {

	public BlockEntityInkMixer(BlockPos pos, BlockState state) {
		super(MystcraftBlockEntities.INK_MIXER, pos, state);
	}
}