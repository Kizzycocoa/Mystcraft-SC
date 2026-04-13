package myst.synthetic.recipe;

import java.util.ArrayList;
import java.util.List;
import myst.synthetic.MystcraftItems;
import myst.synthetic.item.BookmarkColorUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class BookmarkDyeRecipe extends CustomRecipe {

    public BookmarkDyeRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int bookmarkCount = 0;
        int dyeCount = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.is(MystcraftItems.BOOKMARK)) {
                bookmarkCount++;
            } else if (stack.getItem() instanceof DyeItem) {
                dyeCount++;
            } else {
                return false;
            }
        }

        return bookmarkCount == 1 && dyeCount >= 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) {
        ItemStack bookmark = ItemStack.EMPTY;
        List<DyeItem> dyes = new ArrayList<>();

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.is(MystcraftItems.BOOKMARK)) {
                bookmark = stack.copyWithCount(1);
            } else if (stack.getItem() instanceof DyeItem dyeItem) {
                dyes.add(dyeItem);
            }
        }

        if (bookmark.isEmpty() || dyes.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int color = mixBookmarkColor(bookmark, dyes);
        bookmark.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
        return bookmark;
    }

    @Override
    public RecipeSerializer<BookmarkDyeRecipe> getSerializer() {
        return MystcraftRecipeSerializers.BOOKMARK_DYE;
    }

    private static int mixBookmarkColor(ItemStack bookmark, List<DyeItem> dyes) {
        Integer existingColor = BookmarkColorUtil.isDyed(bookmark)
                ? BookmarkColorUtil.getStoredColor(bookmark)
                : null;

        // Requirement: the first dye on an undyed bookmark should fully define the color.
        if (existingColor == null && dyes.size() == 1) {
            return dyes.getFirst().getDyeColor().getTextureDiffuseColor();
        }

        int totalR = 0;
        int totalG = 0;
        int totalB = 0;
        int samples = 0;

        if (existingColor != null) {
            totalR += (existingColor >> 16) & 0xFF;
            totalG += (existingColor >> 8) & 0xFF;
            totalB += existingColor & 0xFF;
            samples++;
        }

        for (DyeItem dye : dyes) {
            int dyeColor = dye.getDyeColor().getTextureDiffuseColor();
            totalR += (dyeColor >> 16) & 0xFF;
            totalG += (dyeColor >> 8) & 0xFF;
            totalB += dyeColor & 0xFF;
            samples++;
        }

        if (samples == 0) {
            return BookmarkColorUtil.DEFAULT_COLOR;
        }

        int r = totalR / samples;
        int g = totalG / samples;
        int b = totalB / samples;
        return (r << 16) | (g << 8) | b;
    }
}