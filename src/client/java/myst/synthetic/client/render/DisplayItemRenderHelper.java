package myst.synthetic.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import myst.synthetic.MystcraftItems;
import myst.synthetic.block.entity.DisplayContentType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class DisplayItemRenderHelper {

    private DisplayItemRenderHelper() {
    }

    public static ItemStack canonicalizeForDisplay(ItemStack stack, DisplayContentType type) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (type == DisplayContentType.PAPER) {
            return new ItemStack(MystcraftItems.PAGE);
        }

        return stack.copyWithCount(1);
    }

    public static void prepareTopItem(
            ItemModelResolver itemModelResolver,
            ItemStackRenderState renderState,
            ItemStack stack,
            Level level
    ) {
        renderState.clear();

        if (stack.isEmpty()) {
            return;
        }

        itemModelResolver.updateForTopItem(
                renderState,
                stack,
                ItemDisplayContext.FIXED,
                level,
                null,
                0
        );
    }

    public static void submitPreparedItem(
            ItemStackRenderState renderState,
            PoseStack poseStack,
            SubmitNodeCollector queue,
            int light
    ) {
        if (renderState.isEmpty()) {
            return;
        }

        renderState.submit(
                poseStack,
                queue,
                light,
                OverlayTexture.NO_OVERLAY,
                0
        );
    }

    public static boolean isBookLike(DisplayContentType type) {
        return type == DisplayContentType.WRITABLE_BOOK
                || type == DisplayContentType.WRITTEN_BOOK
                || type == DisplayContentType.LINKING_BOOK
                || type == DisplayContentType.DESCRIPTIVE_BOOK;
    }

    public static boolean isFlatItem(DisplayContentType type) {
        return type == DisplayContentType.PAGE
                || type == DisplayContentType.PAPER
                || type == DisplayContentType.MAP;
    }
}