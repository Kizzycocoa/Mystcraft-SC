package myst.synthetic;

import myst.synthetic.block.entity.DisplayContentType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class DisplayContainerClientBridge {

    public interface Opener {
        void open(ItemStack stack, DisplayContentType type, @Nullable BlockPos blockPos);
    }

    public static Opener OPENER = (stack, type, blockPos) -> {
    };

    private DisplayContainerClientBridge() {
    }

    public static void open(ItemStack stack, DisplayContentType type, @Nullable BlockPos blockPos) {
        OPENER.open(stack, type, blockPos);
    }
}