package myst.synthetic;

import net.minecraft.world.item.ItemStack;

public final class LinkBookClientBridge {

    public interface Opener {
        void open(ItemStack stack);
    }

    public static Opener OPENER = stack -> {
    };

    private LinkBookClientBridge() {
    }

    public static void open(ItemStack stack) {
        OPENER.open(stack);
    }
}