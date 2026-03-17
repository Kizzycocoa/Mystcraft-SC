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

    public static final int FACE_TOP = 0;
    public static final int FACE_BOTTOM = 1;

    public static final int SKY_PASS = 0;
    public static final int FIRST_PORTAL_PASS = 1;
    public static final int LAST_PORTAL_PASS = 7;
    public static final int TOTAL_PASSES = 8;

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

    public static final RenderPipeline SKY_TOP_PIPELINE = createPipeline("star_fissure_sky_top", FACE_TOP, SKY_PASS, true);
    public static final RenderPipeline SKY_BOTTOM_PIPELINE = createPipeline("star_fissure_sky_bottom", FACE_BOTTOM, SKY_PASS, true);

    public static final RenderPipeline[] PORTAL_TOP_PIPELINES = new RenderPipeline[LAST_PORTAL_PASS + 1];
    public static final RenderPipeline[] PORTAL_BOTTOM_PIPELINES = new RenderPipeline[LAST_PORTAL_PASS + 1];

    private static final RenderType SKY_TOP = createRenderType("star_fissure_sky_top", SKY_TOP_PIPELINE);
    private static final RenderType SKY_BOTTOM = createRenderType("star_fissure_sky_bottom", SKY_BOTTOM_PIPELINE);

    private static final RenderType[] PORTAL_TOP = new RenderType[LAST_PORTAL_PASS + 1];
    private static final RenderType[] PORTAL_BOTTOM = new RenderType[LAST_PORTAL_PASS + 1];

    static {
        for (int pass = FIRST_PORTAL_PASS; pass <= LAST_PORTAL_PASS; pass++) {
            PORTAL_TOP_PIPELINES[pass] = createPipeline("star_fissure_portal_top_" + pass, FACE_TOP, pass, false);
            PORTAL_BOTTOM_PIPELINES[pass] = createPipeline("star_fissure_portal_bottom_" + pass, FACE_BOTTOM, pass, false);

            PORTAL_TOP[pass] = createRenderType("star_fissure_portal_top_" + pass, PORTAL_TOP_PIPELINES[pass]);
            PORTAL_BOTTOM[pass] = createRenderType("star_fissure_portal_bottom_" + pass, PORTAL_BOTTOM_PIPELINES[pass]);
        }
    }

    private StarFissureRenderPipelines() {
    }

    @FunctionalInterface
    public interface QuadEmitter {
        void emit(PoseStack.Pose pose, VertexConsumer consumer);
    }

    private static RenderPipeline createPipeline(String name, int faceIndex, int passIndex, boolean isSkyPass) {
        return RenderPipelines.register(
                RenderPipeline.builder(STAR_FISSURE_SNIPPET)
                        .withLocation("pipeline/" + name)
                        .withShaderDefine("STAR_FISSURE_FACE_INDEX", faceIndex)
                        .withShaderDefine("STAR_FISSURE_PASS_INDEX", passIndex)
                        .withShaderDefine("STAR_FISSURE_IS_SKY_PASS", isSkyPass ? 1 : 0)
                        .withShaderDefine("STAR_FISSURE_TOTAL_PASSES", TOTAL_PASSES)
                        .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                        .build()
        );
    }

    private static RenderType createRenderType(String name, RenderPipeline pipeline) {
        return RenderType.create(
                name,
                RenderSetup.builder(pipeline)
                        .withTexture("Sampler0", AbstractEndPortalRenderer.END_SKY_LOCATION)
                        .withTexture("Sampler1", AbstractEndPortalRenderer.END_PORTAL_LOCATION)
                        .createRenderSetup()
        );
    }

    public static void submitSkyTop(
            SubmitNodeCollector queue,
            PoseStack poseStack,
            QuadEmitter emitter
    ) {
        queue.submitCustomGeometry(
                poseStack,
                SKY_TOP,
                emitter::emit
        );
    }

    public static void submitSkyBottom(
            SubmitNodeCollector queue,
            PoseStack poseStack,
            QuadEmitter emitter
    ) {
        queue.submitCustomGeometry(
                poseStack,
                SKY_BOTTOM,
                emitter::emit
        );
    }

    public static void submitPortalTop(
            SubmitNodeCollector queue,
            PoseStack poseStack,
            int passIndex,
            QuadEmitter emitter
    ) {
        validatePortalPass(passIndex);
        queue.submitCustomGeometry(
                poseStack,
                PORTAL_TOP[passIndex],
                emitter::emit
        );
    }

    public static void submitPortalBottom(
            SubmitNodeCollector queue,
            PoseStack poseStack,
            int passIndex,
            QuadEmitter emitter
    ) {
        validatePortalPass(passIndex);
        queue.submitCustomGeometry(
                poseStack,
                PORTAL_BOTTOM[passIndex],
                emitter::emit
        );
    }

    /**
     * Temporary compatibility wrapper.
     * Keeps the old call site compiling while the renderer is migrated
     * to explicit sky + portal pass submission.
     */
    public static void submitStarFissure(
            SubmitNodeCollector queue,
            PoseStack poseStack,
            QuadEmitter emitter
    ) {
        submitSkyTop(queue, poseStack, emitter);
    }

    private static void validatePortalPass(int passIndex) {
        if (passIndex < FIRST_PORTAL_PASS || passIndex > LAST_PORTAL_PASS) {
            throw new IllegalArgumentException("Portal pass index must be between "
                    + FIRST_PORTAL_PASS + " and " + LAST_PORTAL_PASS + ", got " + passIndex);
        }
    }
}