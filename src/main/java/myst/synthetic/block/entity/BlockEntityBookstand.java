package myst.synthetic.block.entity;

import myst.synthetic.MystcraftBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityBookstand extends BlockEntityDisplayContainer {

	public BlockEntityBookstand(BlockPos pos, BlockState state) {
		super(MystcraftBlockEntities.BOOKSTAND, pos, state);
	}

	@Override
	public boolean canAcceptDisplayItem(ItemStack stack) {
		return DisplayItemRules.canGoInBookstand(stack);
	}
}