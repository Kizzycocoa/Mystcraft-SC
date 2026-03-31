
package myst.synthetic.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.Consumer;
import myst.synthetic.client.page.PageRenderCache;
import myst.synthetic.page.Page;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public final class PageItemSpecialRenderer implements SpecialModelRenderer<PageRenderKey> {

    @Override
    public void submit(
            @Nullable PageRenderKey key,
            ItemDisplayContext itemDisplayContext,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int light,
            int overlay,
            boolean hasFoil,
            int seed
    ) {
        PageRenderKey resolvedKey = key != null ? key : new PageRenderKey(PageRenderKey.Kind.BLANK, null);
        Identifier texture = PageRenderCache.getTexture(resolvedKey);

        poseStack.pushPose();

        // Establish the page in item-local space first.
        // The quad itself is centred around the origin (-0.5 .. 0.5),
        // so we move that origin into normal item model space here.
        poseStack.translate(0.5F, 0.5F, 0.0F);

        switch (itemDisplayContext) {
            case GUI -> {
                poseStack.scale(0.90F, 0.90F, 0.90F);
            }
            case GROUND -> {
                poseStack.scale(0.55F, 0.55F, 0.55F);
            }
            case FIXED -> {
                poseStack.scale(0.90F, 0.90F, 0.90F);
            }
            case THIRD_PERSON_RIGHT_HAND -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
                poseStack.scale(0.78F, 0.78F, 0.78F);
            }
            case THIRD_PERSON_LEFT_HAND -> {
                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
                poseStack.scale(0.78F, 0.78F, 0.78F);
            }
            case FIRST_PERSON_RIGHT_HAND -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(-35.0F));
                poseStack.scale(0.88F, 0.88F, 0.88F);
            }
            case FIRST_PERSON_LEFT_HAND -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(35.0F));
                poseStack.scale(0.88F, 0.88F, 0.88F);
            }
            default -> {
                poseStack.scale(0.90F, 0.90F, 0.90F);
            }
        }

        if (itemDisplayContext == ItemDisplayContext.GUI) {
            PageRenderPipelines.submitGui(
                    submitNodeCollector,
                    poseStack,
                    texture,
                    LightTexture.FULL_BRIGHT,
                    overlay
            );
        } else {
            PageRenderPipelines.submitWorld(
                    submitNodeCollector,
                    poseStack,
                    texture,
                    light,
                    overlay
            );
        }

        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        // Extents should match the item-local space after centering.
        consumer.accept(new Vector3f(0.0F, 0.0F, -0.01F));
        consumer.accept(new Vector3f(1.0F, 1.0F, 0.01F));
    }

    @Override
    public @Nullable PageRenderKey extractArgument(ItemStack itemStack) {
        if (Page.isLinkPanel(itemStack)) {
            return new PageRenderKey(PageRenderKey.Kind.LINK_PANEL, null);
        }

        Identifier symbol = Page.getSymbol(itemStack);
        if (symbol != null) {
            return new PageRenderKey(PageRenderKey.Kind.SYMBOL, symbol);
        }

        return new PageRenderKey(PageRenderKey.Kind.BLANK, null);
    }
}