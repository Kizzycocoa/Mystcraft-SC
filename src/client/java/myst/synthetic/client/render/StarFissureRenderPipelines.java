package myst.synthetic.client.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractEndPortalRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class StarFissureRenderPipelines {

    public static final RenderPipeline STAR_FISSURE_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation("pipeline/star_fissure_debug")
                    .withVertexShader("core/position_color")
                    .withFragmentShader("core/rendertype_star_fissure")
                    .withSampler("Sampler0")
                    .withSampler("Sampler1")
                    .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                    .build()
    );

    private static final RenderType STAR_FISSURE = RenderType.create(
            "star_fissure_debug",
            RenderSetup.builder(STAR_FISSURE_PIPELINE)
                    .withTexture("Sampler0", AbstractEndPortalRenderer.END_SKY_LOCATION)
                    .withTexture("Sampler1", AbstractEndPortalRenderer.END_PORTAL_LOCATION)
                    .createRenderSetup()
    );

    private StarFissureRenderPipelines() {}

    @FunctionalInterface
    public interface QuadEmitter {
        void emit(PoseStack.Pose pose, VertexConsumer consumer);
    }

    public static void submitStarFissure(
            SubmitNodeCollector queue,
            PoseStack poseStack,
            QuadEmitter emitter
    ) {
        queue.submitCustomGeometry(
                poseStack,
                STAR_FISSURE,
                emitter::emit
        );
    }
}