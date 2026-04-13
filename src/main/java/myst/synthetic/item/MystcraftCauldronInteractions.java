package myst.synthetic.item;

import myst.synthetic.MystcraftItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class MystcraftCauldronInteractions {

	private MystcraftCauldronInteractions() {
	}

	public static void initialize() {
		CauldronInteraction.WATER.map().put(MystcraftItems.BOOKMARK, MystcraftCauldronInteractions::washBookmark);
	}

	private static InteractionResult washBookmark(
			BlockState state,
			Level level,
			BlockPos pos,
			Player player,
			InteractionHand hand,
			ItemStack stack
	) {
		if (!BookmarkColorUtil.isDyed(stack)) {
			return InteractionResult.PASS;
		}

		if (!level.isClientSide()) {
			ItemStack cleaned = stack.copyWithCount(1);
			BookmarkColorUtil.clearColor(cleaned);

			if (stack.getCount() == 1) {
				player.setItemInHand(hand, cleaned);
			} else {
				stack.shrink(1);

				if (!player.getInventory().add(cleaned)) {
					player.drop(cleaned, false);
				}
			}

			player.awardStat(Stats.CLEAN_ARMOR);
			LayeredCauldronBlock.lowerFillLevel(state, level, pos);
		}

		return InteractionResult.SUCCESS;
	}
}