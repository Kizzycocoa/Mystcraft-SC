package myst.synthetic.item;

import myst.synthetic.block.BlockWritingDesk;
import myst.synthetic.init.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ItemWritingDesk extends BlockItem {

	public ItemWritingDesk() {
		super(ModBlocks.WRITING_DESK_BLOCK, new Item.Properties());
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		}

		Player player = context.getPlayer();
		if (player == null) {
			return InteractionResult.FAIL;
		}

		BlockPos clickedPos = context.getClickedPos();
		Direction clickedFace = context.getClickedFace();

		BlockPos basePos = clickedPos;
		if (level.getBlockState(clickedPos).canBeReplaced(context)) {
			basePos = clickedPos.below();
			clickedFace = Direction.UP;
		}

		if (clickedFace != Direction.UP) {
			return InteractionResult.FAIL;
		}

		Direction facing = player.getDirection().getClockWise();
		BlockPos footOffset = BlockWritingDesk.getFootOffset(facing);

		BlockPos bottomHead = basePos.above();
		BlockPos bottomFoot = bottomHead.offset(footOffset);

		if (!player.mayUseItemAt(bottomHead, clickedFace, context.getItemInHand())) {
			return InteractionResult.FAIL;
		}
		if (!player.mayUseItemAt(bottomFoot, clickedFace, context.getItemInHand())) {
			return InteractionResult.FAIL;
		}

		if (!level.getBlockState(bottomHead).canBeReplaced(context)) {
			return InteractionResult.FAIL;
		}
		if (!level.getBlockState(bottomFoot).canBeReplaced(context)) {
			return InteractionResult.FAIL;
		}

		BlockState baseState = ModBlocks.WRITING_DESK_BLOCK.defaultBlockState()
				.setValue(BlockWritingDesk.FACING, facing)
				.setValue(BlockWritingDesk.IS_TOP, false)
				.setValue(BlockWritingDesk.IS_FOOT, false);

		level.setBlock(bottomHead, baseState, 3);
		level.setBlock(bottomFoot, baseState.setValue(BlockWritingDesk.IS_FOOT, true), 3);

		if (!player.getAbilities().instabuild) {
			context.getItemInHand().shrink(1);
		}

		return InteractionResult.CONSUME;
	}
}