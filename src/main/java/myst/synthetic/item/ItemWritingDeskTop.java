package myst.synthetic.item;

import myst.synthetic.MystcraftBlocks;
import myst.synthetic.MystcraftSyntheticCodex;
import myst.synthetic.block.BlockWritingDesk;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ItemWritingDeskTop extends Item {

	public ItemWritingDeskTop(Item.Properties properties) {
		super(properties);
	}

	private static InteractionResult fail(Player player, String msg) {
		player.displayClientMessage(Component.literal(msg), true);
		MystcraftSyntheticCodex.LOGGER.info("[WritingDeskTop] {}", msg);
		return InteractionResult.FAIL;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
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
			return fail(player, "clicked block is not a writing desk");
		}

		if (BlockWritingDesk.isTop(clickedState)) {
			return fail(player, "clicked top half instead of bottom half");
		}

		BlockPos anchor = BlockWritingDesk.getAnchorPos(clickedState, clickedPos);
		BlockState anchorState = level.getBlockState(anchor);

		if (!(anchorState.getBlock() instanceof BlockWritingDesk)) {
			return fail(player, "anchor is not a writing desk");
		}

		Direction facing = BlockWritingDesk.getDeskFacing(anchorState);
		BlockPos footOffset = BlockWritingDesk.getFootOffset(facing);

		BlockPos topHead = anchor.above();
		BlockPos topFoot = topHead.offset(footOffset);

		BlockState topHeadState = level.getBlockState(topHead);
		BlockState topFootState = level.getBlockState(topFoot);

		if (!player.mayUseItemAt(topHead, context.getClickedFace(), context.getItemInHand())) {
			return fail(player, "no permission at top head");
		}
		if (!player.mayUseItemAt(topFoot, context.getClickedFace(), context.getItemInHand())) {
			return fail(player, "no permission at top foot");
		}

		if (!topHeadState.isAir()) {
			return fail(player, "top head is not air: " + topHeadState.getBlock().toString());
		}
		if (!topFootState.isAir()) {
			return fail(player, "top foot is not air: " + topFootState.getBlock().toString());
		}

		BlockState topState = MystcraftBlocks.WRITING_DESK_BLOCK.defaultBlockState()
				.setValue(BlockWritingDesk.FACING, facing)
				.setValue(BlockWritingDesk.IS_TOP, true)
				.setValue(BlockWritingDesk.IS_FOOT, false);

		boolean placedHead = level.setBlock(topHead, topState, 3);
		boolean placedFoot = level.setBlock(topFoot, topState.setValue(BlockWritingDesk.IS_FOOT, true), 3);

		if (!placedHead) {
			return fail(player, "setBlock failed for top head");
		}
		if (!placedFoot) {
			return fail(player, "setBlock failed for top foot");
		}

		if (!player.getAbilities().instabuild) {
			context.getItemInHand().shrink(1);
		}

		player.displayClientMessage(Component.literal("placed desk top"), true);
		MystcraftSyntheticCodex.LOGGER.info("[WritingDeskTop] placed desk top at {} and {}", topHead, topFoot);

		return InteractionResult.CONSUME;
	}
}