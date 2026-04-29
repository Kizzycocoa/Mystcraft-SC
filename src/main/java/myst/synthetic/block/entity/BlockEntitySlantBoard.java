package myst.synthetic.block.entity;

import myst.synthetic.MystcraftBlockEntities;
import myst.synthetic.page.loot.PageLootPools;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BlockEntitySlantBoard extends BlockEntityDisplayContainer {

	private static final String PAGE_POOL_KEY = "mystcraftPagePool";

	private boolean needsInitialRefresh = true;
	private String pendingPagePool = "";

	public BlockEntitySlantBoard(BlockPos pos, BlockState state) {
		super(MystcraftBlockEntities.SLANT_BOARD, pos, state);
	}

	@Override
	public boolean canAcceptDisplayItem(ItemStack stack) {
		return DisplayItemRules.canGoInSlantBoard(stack);
	}

	@Override
	public void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.pendingPagePool = input.getString(PAGE_POOL_KEY).orElse("");
	}

	@Override
	public void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);

		if (!this.pendingPagePool.isBlank()) {
			output.putString(PAGE_POOL_KEY, this.pendingPagePool);
		}
	}

	public static void tick(Level level, BlockPos pos, BlockState state, BlockEntitySlantBoard blockEntity) {
		if (!level.isClientSide()) {
			blockEntity.tryResolvePendingPagePool(level);
			return;
		}

		if (!blockEntity.needsInitialRefresh) {
			return;
		}

		blockEntity.needsInitialRefresh = false;

		level.getLightEngine().checkBlock(pos);
		level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
	}

	private void tryResolvePendingPagePool(Level level) {
		if (this.pendingPagePool.isBlank() || this.hasStoredItem()) {
			return;
		}

		ItemStack rolledPage = PageLootPools.pickPageStack(this.pendingPagePool, level.getRandom());
		this.pendingPagePool = "";

		if (!rolledPage.isEmpty()) {
			this.setStoredItem(rolledPage);
			return;
		}

		this.setChangedAndSync();
	}

	public ItemStack getPage() {
		return this.getStoredItem();
	}

	public void clearPage() {
		this.setStoredItem(ItemStack.EMPTY);
	}
}