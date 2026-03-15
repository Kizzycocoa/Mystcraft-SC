package myst.synthetic.recipe;

import myst.synthetic.MystcraftItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class InkVialRecipe extends CustomRecipe {

    public InkVialRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.ingredientCount() != 3) {
            return false;
        }

        int inkSacCount = 0;
        int waterBottleCount = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.is(Items.INK_SAC)) {
                inkSacCount++;
            } else if (isWaterBottle(stack)) {
                waterBottleCount++;
            } else {
                return false;
            }
        }

        return inkSacCount == 2 && waterBottleCount == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) {
        return new ItemStack(MystcraftItems.VIAL);
    }

    @Override
    public RecipeSerializer<InkVialRecipe> getSerializer() {
        return MystcraftRecipeSerializers.INK_VIAL;
    }

    private static boolean isWaterBottle(ItemStack stack) {
        if (!stack.is(Items.POTION)) {
            return false;
        }

        PotionContents potionContents = stack.get(DataComponents.POTION_CONTENTS);
        return potionContents != null && potionContents.is(Potions.WATER);
    }
}