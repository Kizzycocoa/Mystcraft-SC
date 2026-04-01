package myst.synthetic.ink;

import myst.synthetic.MystcraftFluids;
import myst.synthetic.MystcraftItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class InkMixerInkSource {

    private InkMixerInkSource() {
    }

    public static boolean isValidInkSource(ItemStack stack) {
        return stack.is(MystcraftItems.VIAL) || stack.is(MystcraftFluids.BLACK_INK_BUCKET);
    }

    public static ItemStack getRemainingContainer(ItemStack stack) {
        if (stack.is(MystcraftItems.VIAL)) {
            return new ItemStack(Items.GLASS_BOTTLE);
        }

        if (stack.is(MystcraftFluids.BLACK_INK_BUCKET)) {
            return new ItemStack(Items.BUCKET);
        }

        return ItemStack.EMPTY;
    }
}