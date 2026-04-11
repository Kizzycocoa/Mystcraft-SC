package myst.synthetic.block.entity;

import myst.synthetic.MystcraftItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class DisplayItemRules {

	private DisplayItemRules() {
	}

	public static DisplayContentType classify(ItemStack stack) {
		if (stack.isEmpty()) {
			return DisplayContentType.EMPTY;
		}

		if (stack.is(MystcraftItems.PAGE)) {
			return DisplayContentType.PAGE;
		}

		if (stack.is(Items.PAPER)) {
			return DisplayContentType.PAPER;
		}

		if (stack.is(Items.MAP) || stack.is(Items.FILLED_MAP)) {
			return DisplayContentType.MAP;
		}

		if (stack.is(Items.WRITABLE_BOOK)) {
			return DisplayContentType.WRITABLE_BOOK;
		}

		if (stack.is(Items.WRITTEN_BOOK)) {
			return DisplayContentType.WRITTEN_BOOK;
		}

		if (stack.is(MystcraftItems.LINKBOOK)) {
			return DisplayContentType.LINKING_BOOK;
		}

		if (stack.is(MystcraftItems.AGEBOOK)) {
			return DisplayContentType.DESCRIPTIVE_BOOK;
		}

		return DisplayContentType.EMPTY;
	}
	public static boolean canGoInBookReceptacle(ItemStack stack) {
		DisplayContentType type = classify(stack);
		return type == DisplayContentType.LINKING_BOOK
				|| type == DisplayContentType.DESCRIPTIVE_BOOK;
	}

	public static boolean canGoInBookstand(ItemStack stack) {
		return classify(stack).isBookLike();
	}

	public static boolean canGoInSlantBoard(ItemStack stack) {
		DisplayContentType type = classify(stack);

		return type.isBookLike()
				|| type == DisplayContentType.PAGE
				|| type == DisplayContentType.PAPER
				|| type == DisplayContentType.MAP;
	}
}