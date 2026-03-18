package myst.synthetic.item;

import myst.synthetic.block.BlockWritingDesk;
import myst.synthetic.init.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ItemWritingDeskTop extends BlockItem {

	public ItemWritingDeskTop() {
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

		BlockPos pos = context.getClickedPos();
		BlockState clicked = level.getBlockState(pos);

		if (!(clicked.getBlock() instanceof BlockWritingDesk)) {
			return InteractionResult.FAIL;
		}
		if (BlockWritingDesk.isTop(clicked)) {
			return InteractionResult.FAIL;
		}

		BlockPos anchor = BlockWritingDesk.getAnchorPos(clicked, pos);
		BlockState anchorState = level.getBlockState(anchor);
		Direction facing = BlockWritingDesk.getDeskFacing(anchorState);
		BlockPos footOffset = BlockWritingDesk.getFootOffset(facing);

		BlockPos topHead = anchor.above();
		BlockPos topFoot = topHead.offset(footOffset);

		if (!player.mayUseItemAt(topHead, context.getClickedFace(), context.getItemInHand())) {
			return InteractionResult.FAIL;
		}
		if (!player.mayUseItemAt(topFoot, context.getClickedFace(), context.getItemInHand())) {
			return InteractionResult.FAIL;
		}

		if (!level.getBlockState(topHead).canBeReplaced(context)) {
			return InteractionResult.FAIL;
		}
		if (!level.getBlockState(topFoot).canBeReplaced(context)) {
			return InteractionResult.FAIL;
		}

		BlockState topState = ModBlocks.WRITING_DESK_BLOCK.defaultBlockState()
				.setValue(BlockWritingDesk.FACING, facing)
				.setValue(BlockWritingDesk.IS_TOP, true)
				.setValue(BlockWritingDesk.IS_FOOT, false);

		level.setBlock(topHead, topState, 3);
		level.setBlock(topFoot, topState.setValue(BlockWritingDesk.IS_FOOT, true), 3);

		if (!player.getAbilities().instabuild) {
			context.getItemInHand().shrink(1);
		}

		return InteractionResult.CONSUME;
	}
}