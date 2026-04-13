package myst.synthetic.recipe;

import myst.synthetic.MystcraftItems;
import myst.synthetic.item.BookmarkColorUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class BookmarkCloneRecipe extends CustomRecipe {

    public BookmarkCloneRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int dyedCount = 0;
        int blankCount = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (!stack.is(MystcraftItems.BOOKMARK)) {
                return false;
            }

            if (BookmarkColorUtil.isDyed(stack)) {
                dyedCount++;
            } else {
                blankCount++;
            }
        }

        return dyedCount == 1 && blankCount >= 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) {
        ItemStack dyedBookmark = ItemStack.EMPTY;
        int totalBookmarks = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (!stack.is(MystcraftItems.BOOKMARK)) {
                return ItemStack.EMPTY;
            }

            totalBookmarks++;

            if (BookmarkColorUtil.isDyed(stack)) {
                if (!dyedBookmark.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                dyedBookmark = stack.copyWithCount(1);
            }
        }

        if (dyedBookmark.isEmpty() || totalBookmarks < 2) {
            return ItemStack.EMPTY;
        }

        return dyedBookmark.copyWithCount(totalBookmarks);
    }

    @Override
    public RecipeSerializer<BookmarkCloneRecipe> getSerializer() {
        return MystcraftRecipeSerializers.BOOKMARK_CLONE;
    }
}