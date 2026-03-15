package myst.synthetic.client.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractEndPortalRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class StarFissureRenderPipelines {

    public static final RenderPipeline.Snippet STAR_FISSURE_SNIPPET = RenderPipeline.builder(
                    RenderPipelines.MATRICES_PROJECTION_SNIPPET,
                    RenderPipelines.FOG_SNIPPET,
                    RenderPipelines.GLOBALS_SNIPPET
            )
            .withVertexShader("core/rendertype_star_fissure")
            .withFragmentShader("core/rendertype_star_fissure")
            .withSampler("Sampler0")
            .withSampler("Sampler1")
            .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
            .buildSnippet();

    public static final RenderPipeline STAR_FISSURE_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(STAR_FISSURE_SNIPPET)
                    .withLocation("pipeline/star_fissure")
                    .withShaderDefine("PORTAL_LAYERS", 8)
                    .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                    .build()
    );

    private static final RenderType STAR_FISSURE = RenderType.create(
            "star_fissure",
            RenderSetup.builder(STAR_FISSURE_PIPELINE)
                    .withTexture("Sampler0", AbstractEndPortalRenderer.END_SKY_LOCATION)
                    .withTexture("Sampler1", AbstractEndPortalRenderer.END_PORTAL_LOCATION)
                    .createRenderSetup()
    );

    private StarFissureRenderPipelines() {
    }

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