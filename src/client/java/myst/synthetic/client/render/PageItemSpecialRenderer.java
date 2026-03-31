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

        switch (itemDisplayContext) {
            case GUI -> {
                poseStack.translate(-0.5F, -0.5F, 0.0F);
            }
            case GROUND -> {
                poseStack.translate(-0.25F, -0.25F, 0.0F);
                poseStack.scale(0.5F, 0.5F, 0.5F);
            }
            case FIXED -> {
                poseStack.translate(-0.5F, -0.5F, 0.0F);
                poseStack.scale(0.9F, 0.9F, 0.9F);
            }
            case THIRD_PERSON_RIGHT_HAND -> {
                poseStack.translate(-0.5F, -0.35F, 0.02F);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
                poseStack.scale(0.75F, 0.75F, 0.75F);
            }
            case THIRD_PERSON_LEFT_HAND -> {
                poseStack.translate(-0.5F, -0.35F, 0.02F);
                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
                poseStack.scale(0.75F, 0.75F, 0.75F);
            }
            case FIRST_PERSON_RIGHT_HAND -> {
                poseStack.translate(-0.25F, -0.35F, 0.15F);
                poseStack.mulPose(Axis.YP.rotationDegrees(-35.0F));
                poseStack.scale(0.85F, 0.85F, 0.85F);
            }
            case FIRST_PERSON_LEFT_HAND -> {
                poseStack.translate(-0.75F, -0.35F, 0.15F);
                poseStack.mulPose(Axis.YP.rotationDegrees(35.0F));
                poseStack.scale(0.85F, 0.85F, 0.85F);
            }
            default -> {
                poseStack.translate(-0.5F, -0.5F, 0.0F);
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