package myst.synthetic.item;

import myst.synthetic.block.BlockDecay;
import myst.synthetic.block.DecayType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.Block;

public class DecayBlockItem extends BlockItem {

	public DecayBlockItem(Block block, Item.Properties properties) {
		super(block, properties);
	}

	@Override
	public Component getName(ItemStack stack) {
		BlockItemStateProperties stateProperties = stack.get(DataComponents.BLOCK_STATE);

		if (stateProperties != null) {
			DecayType decayType = stateProperties.get(BlockDecay.DECAY);

			if (decayType != null) {
				return Component.translatable("block.mystcraft-sc.blockdecay." + decayType.getSerializedName());
			}
		}

		return Component.translatable("block.mystcraft-sc.blockdecay.black");
	}
}