package myst.synthetic.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemWritingDeskTop extends Item {

	public ItemWritingDeskTop(Item.Properties properties) {
		super(properties);
	}

	@Override
	public Component getName(ItemStack stack) {
		return WoodVariantItemNaming.getVariantName(stack, "writingdesk_top");
	}
}