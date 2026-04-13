package myst.synthetic.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.jetbrains.annotations.Nullable;

public final class BookmarkColorUtil {

	public static final int DEFAULT_COLOR = 0xC9B28D;

	private BookmarkColorUtil() {
	}

	public static int getColor(ItemStack stack) {
		DyedItemColor dyed = stack.get(DataComponents.DYED_COLOR);
		return dyed != null ? dyed.rgb() : DEFAULT_COLOR;
	}

	public static boolean isDyed(ItemStack stack) {
		return stack.has(DataComponents.DYED_COLOR);
	}

	public static void setColor(ItemStack stack, int color) {
		stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
	}

	public static void clearColor(ItemStack stack) {
		stack.remove(DataComponents.DYED_COLOR);
	}

	public static @Nullable Integer getStoredColor(ItemStack stack) {
		DyedItemColor dyed = stack.get(DataComponents.DYED_COLOR);
		return dyed != null ? dyed.rgb() : null;
	}
}