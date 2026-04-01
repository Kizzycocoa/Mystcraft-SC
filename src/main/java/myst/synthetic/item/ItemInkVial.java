package myst.synthetic.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class ItemInkVial extends Item {

	public ItemInkVial(Properties properties) {
		super(properties.craftRemainder(Items.GLASS_BOTTLE));
	}
}