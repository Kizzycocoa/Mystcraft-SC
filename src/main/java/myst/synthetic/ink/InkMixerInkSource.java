package myst.synthetic.ink;

import myst.synthetic.MystcraftItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class InkMixerInkSource {

    private InkMixerInkSource() {
    }

    public static boolean isValidInkSource(ItemStack stack) {
        return stack.is(MystcraftItems.VIAL);
    }

    public static ItemStack getRemainingContainer(ItemStack stack) {
        if (stack.is(MystcraftItems.VIAL)) {
            return new ItemStack(Items.GLASS_BOTTLE);
        }
        return ItemStack.EMPTY;
    }
}