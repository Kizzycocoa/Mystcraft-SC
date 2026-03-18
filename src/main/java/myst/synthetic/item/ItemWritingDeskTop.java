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
	}
}