package myst.synthetic.item;

import myst.synthetic.block.property.WoodType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class WoodVariantItemNaming {

	private WoodVariantItemNaming() {
	}

	public static WoodType getWoodTypeFromStack(ItemStack stack) {
		var customData = stack.get(DataComponents.CUSTOM_DATA);

		if (customData == null) {
			return WoodType.OAK;
		}

		String name = customData.copyTag().getString("wood").orElse("oak");

		for (WoodType type : WoodType.values()) {
			if (type.getSerializedName().equals(name)) {
				return type;
			}
		}

		return WoodType.OAK;
	}

	public static Component getVariantName(ItemStack stack, String baseKey) {
		WoodType wood = getWoodTypeFromStack(stack);
		return Component.translatable("item.mystcraft-sc." + baseKey + "." + wood.getSerializedName());
	}
}