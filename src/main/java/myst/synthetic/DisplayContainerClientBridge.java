package myst.synthetic;

import myst.synthetic.block.entity.DisplayContentType;
import net.minecraft.world.item.ItemStack;

public final class DisplayContainerClientBridge {

    public interface Opener {
        void open(ItemStack stack, DisplayContentType type);
    }

    public static Opener OPENER = (stack, type) -> {
    };

    private DisplayContainerClientBridge() {
    }

    public static void open(ItemStack stack, DisplayContentType type) {
        OPENER.open(stack, type);
    }
}