package myst.synthetic.block.entity;

import myst.synthetic.MystcraftBlockEntities;
import myst.synthetic.MystcraftBlocks;
import myst.synthetic.block.BlockBookReceptacle;
import myst.synthetic.block.BlockCrystal;
import myst.synthetic.linking.LinkColorUtil;
import myst.synthetic.util.PortalUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityBookReceptacle extends BlockEntityDisplayContainer {

	public BlockEntityBookReceptacle(BlockPos pos, BlockState state) {
		super(MystcraftBlockEntities.BOOK_RECEPTACLE, pos, state);
	}

	@Override
	public boolean canAcceptDisplayItem(ItemStack stack) {
		return DisplayItemRules.canGoInBookReceptacle(stack);
	}

	public ItemStack getBook() {
		return this.getStoredItem();
	}

	public boolean hasBook() {
		return this.hasStoredItem();
	}

	public int getPortalColor() {
		return LinkColorUtil.getPortalColor(this.getBook());
	}

	public void clearBook() {
		this.clearContent();
	}

	public void setStoredItem(ItemStack stack) {
		this.setItem(0, stack);
	}

	public boolean hasValidPortalBook() {
		ItemStack stack = this.getBook();
		return !stack.isEmpty() && DisplayItemRules.canGoInBookReceptacle(stack);
	}

	public BlockPos getSupportCrystalPos() {
		BlockState state = this.getBlockState();
		return BlockBookReceptacle.getSupportPos(
				this.getBlockPos(),
				state.getValue(BlockBookReceptacle.FACE),
				state.getValue(BlockBookReceptacle.FACING)
		);
	}

	public BlockState getSupportCrystalState() {
		if (this.level == null) {
			return null;
		}
		return this.level.getBlockState(getSupportCrystalPos());
	}

	public boolean hasValidSupportCrystal() {
		if (this.level == null) {
			return false;
		}

		BlockState support = getSupportCrystalState();
		return support != null
				&& support.is(MystcraftBlocks.CRYSTAL)
				&& support.getValue(BlockCrystal.COLOR) == this.getBlockState().getValue(BlockBookReceptacle.COLOR);
	}

	@Override
	protected void setChangedAndSync() {
		super.setChangedAndSync();

		if (this.level == null || this.level.isClientSide()) {
			return;
		}

		BlockState state = this.getBlockState();
		boolean hasBook = this.hasStoredItem();

		if (state.hasProperty(BlockBookReceptacle.HAS_BOOK)
				&& state.getValue(BlockBookReceptacle.HAS_BOOK) != hasBook) {
			this.level.setBlock(
					this.worldPosition,
					state.setValue(BlockBookReceptacle.HAS_BOOK, hasBook),
					Block.UPDATE_ALL
			);
		}

		handleItemChange();
	}

	public void handleItemChange() {
		if (this.level == null || this.level.isClientSide()) {
			return;
		}

		PortalUtils.shutdownPortal(this.level, this.worldPosition);

		if (!hasValidPortalBook()) {
			return;
		}

		if (!hasValidSupportCrystal()) {
			return;
		}

		PortalUtils.firePortal(
				this.level,
				this.worldPosition,
				this.getBlockState().getValue(BlockBookReceptacle.COLOR)
		);
	}
}