package myst.synthetic.item;

import myst.synthetic.MystcraftBlocks;
import myst.synthetic.block.BlockWritingDesk;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ItemWritingDeskTop extends Item {

	public ItemWritingDeskTop(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		throw new RuntimeException("ItemWritingDeskTop.useOn reached");
		Level level = context.getLevel();

		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		Player player = context.getPlayer();
		if (player == null) {
			return InteractionResult.FAIL;
		}

		BlockPos clickedPos = context.getClickedPos();
		BlockState clickedState = level.getBlockState(clickedPos);

		if (!(clickedState.getBlock() instanceof BlockWritingDesk)) {
			return InteractionResult.FAIL;
		}

		// Only allow adding the top onto the bottom half of the desk.
		if (BlockWritingDesk.isTop(clickedState)) {
			return InteractionResult.FAIL;
		}

		BlockPos anchor = BlockWritingDesk.getAnchorPos(clickedState, clickedPos);
		BlockState anchorState = level.getBlockState(anchor);

		if (!(anchorState.getBlock() instanceof BlockWritingDesk)) {
			return InteractionResult.FAIL;
		}

		Direction facing = BlockWritingDesk.getDeskFacing(anchorState);
		BlockPos footOffset = BlockWritingDesk.getFootOffset(facing);

		BlockPos topHead = anchor.above();
		BlockPos topFoot = topHead.offset(footOffset);

		// Do not place if the top already exists.
		BlockState existingTopHead = level.getBlockState(topHead);
		BlockState existingTopFoot = level.getBlockState(topFoot);
		if (existingTopHead.getBlock() instanceof BlockWritingDesk || existingTopFoot.getBlock() instanceof BlockWritingDesk) {
			return InteractionResult.FAIL;
		}

		BlockPlaceContext placeContext = new BlockPlaceContext(context);

		System.out.println("clicked=" + clickedState.getBlock());
		System.out.println("anchor=" + anchor);
		System.out.println("topHead=" + topHead + " replace=" + level.getBlockState(topHead).canBeReplaced(placeContext));
		System.out.println("topFoot=" + topFoot + " replace=" + level.getBlockState(topFoot).canBeReplaced(placeContext));

		if (!player.mayUseItemAt(topHead, context.getClickedFace(), context.getItemInHand())) {
			return InteractionResult.FAIL;
		}
		if (!player.mayUseItemAt(topFoot, context.getClickedFace(), context.getItemInHand())) {
			return InteractionResult.FAIL;
		}

		if (!level.getBlockState(topHead).canBeReplaced(placeContext)) {
			return InteractionResult.FAIL;
		}
		if (!level.getBlockState(topFoot).canBeReplaced(placeContext)) {
			return InteractionResult.FAIL;
		}

		BlockState topState = MystcraftBlocks.WRITING_DESK_BLOCK.defaultBlockState()
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