package myst.synthetic.block.entity;

import myst.synthetic.MystcraftBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntitySlantBoard extends BlockEntity {

	private boolean needsInitialRefresh = true;

	public BlockEntitySlantBoard(BlockPos pos, BlockState state) {
		super(MystcraftBlockEntities.SLANT_BOARD, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, BlockEntitySlantBoard blockEntity) {
		if (!blockEntity.needsInitialRefresh) {
			return;
		}

		blockEntity.needsInitialRefresh = false;

		level.getLightEngine().checkBlock(pos);
		level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);

		blockEntity.setChanged();
	}
}