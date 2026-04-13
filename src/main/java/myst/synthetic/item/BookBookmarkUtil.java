package myst.synthetic.item;

import myst.synthetic.MystcraftItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class BookBookmarkUtil {

	private static final String BOOKMARK_TAG = "Bookmark";

	private BookBookmarkUtil() {
	}

	public static boolean hasBookmark(ItemStack bookStack) {
		return !getBookmark(bookStack).isEmpty();
	}

	public static ItemStack getBookmark(ItemStack bookStack) {
		CompoundTag tag = getBookTag(bookStack);
		if (tag == null || !tag.contains(BOOKMARK_TAG)) {
			return ItemStack.EMPTY;
		}

		CompoundTag bookmarkTag = tag.getCompound(BOOKMARK_TAG).orElse(new CompoundTag());
		ItemStack bookmark = new ItemStack(MystcraftItems.BOOKMARK);

		int color = bookmarkTag.getInt("Color").orElse(BookmarkColorUtil.DEFAULT_COLOR);
		BookmarkColorUtil.setColor(bookmark, color);

		return bookmark;
	}

	public static void setBookmark(ItemStack bookStack, ItemStack bookmarkStack) {
		if (bookStack.isEmpty() || !bookmarkStack.is(MystcraftItems.BOOKMARK)) {
			return;
		}

		CompoundTag tag = getOrCreateBookTag(bookStack);
		CompoundTag bookmarkTag = new CompoundTag();
		bookmarkTag.putInt("Color", BookmarkColorUtil.getColor(bookmarkStack));
		tag.put(BOOKMARK_TAG, bookmarkTag);

		bookStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static ItemStack removeBookmark(ItemStack bookStack) {
		ItemStack existing = getBookmark(bookStack);
		if (existing.isEmpty()) {
			return ItemStack.EMPTY;
		}

		CompoundTag tag = getOrCreateBookTag(bookStack);
		tag.remove(BOOKMARK_TAG);
		bookStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

		return existing;
	}

	public static boolean sameBookmark(ItemStack a, ItemStack b) {
		return a.is(MystcraftItems.BOOKMARK)
				&& b.is(MystcraftItems.BOOKMARK)
				&& BookmarkColorUtil.getColor(a) == BookmarkColorUtil.getColor(b);
	}

	private static CompoundTag getBookTag(ItemStack bookStack) {
		CustomData customData = bookStack.get(DataComponents.CUSTOM_DATA);
		return customData != null ? customData.copyTag() : null;
	}

	private static CompoundTag getOrCreateBookTag(ItemStack bookStack) {
		CompoundTag tag = getBookTag(bookStack);
		return tag != null ? tag : new CompoundTag();
	}
}