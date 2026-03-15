package myst.synthetic.recipe;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class MystcraftRecipeSerializers {

    private MystcraftRecipeSerializers() {
    }

    public static final RecipeSerializer<InkVialRecipe> INK_VIAL = Registry.register(
            BuiltInRegistries.RECIPE_SERIALIZER,
            Identifier.fromNamespaceAndPath("mystcraft-sc", "ink_vial"),
            new CustomRecipe.Serializer<>(InkVialRecipe::new)
    );

    public static void initialize() {
    }
}