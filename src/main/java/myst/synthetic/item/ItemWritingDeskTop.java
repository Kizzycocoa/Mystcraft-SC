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
		Player player = context.getPlayer();
		if (player != null) {
			player.displayClientMessage(Component.literal("USEON TRIGGERED"), true);
		}
		return InteractionResult.SUCCESS;
	}
}