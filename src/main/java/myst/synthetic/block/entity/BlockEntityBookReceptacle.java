package myst.synthetic.block.entity;

import myst.synthetic.MystcraftBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;

public class BlockEntityBookReceptacle extends BlockEntityDisplayContainer {

	public BlockEntityBookReceptacle(BlockPos pos, BlockState state) {
		super(MystcraftBlockEntities.BOOK_RECEPTACLE, pos, state);
	}

	@Override
	public boolean canAcceptDisplayItem(ItemStack stack) {
		return DisplayItemRules.canGoInBookReceptacle(stack);
	}

	@Override
	protected void setChangedAndSync() {
		super.setChangedAndSync();

		if (this.level == null || this.level.isClientSide()) {
			return;
		}

		BlockState state = this.getBlockState();
		boolean hasBook = this.hasStoredItem();

		if (state.hasProperty(myst.synthetic.block.BlockBookReceptacle.HAS_BOOK)
				&& state.getValue(myst.synthetic.block.BlockBookReceptacle.HAS_BOOK) != hasBook) {
			this.level.setBlock(
					this.worldPosition,
					state.setValue(myst.synthetic.block.BlockBookReceptacle.HAS_BOOK, hasBook),
					Block.UPDATE_ALL
			);
		}
	}

	public ItemStack getBook() {
		return this.getStoredItem();
	}

	public boolean hasBook() {
		return this.hasStoredItem();
	}

	public void clearBook() {
		this.clearContent();
	}
}