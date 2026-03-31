package myst.synthetic.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class PageRenderPipelines {

    private static final Map<Identifier, RenderType> WORLD_RENDER_TYPES = new HashMap<>();
    private static final Map<Identifier, RenderType> GUI_RENDER_TYPES = new HashMap<>();

    private PageRenderPipelines() {
    }

    private static RenderType getWorldRenderType(Identifier texture) {
        return WORLD_RENDER_TYPES.computeIfAbsent(texture, PageRenderPipelines::createWorldRenderType);
    }

    private static RenderType getGuiRenderType(Identifier texture) {
        return GUI_RENDER_TYPES.computeIfAbsent(texture, PageRenderPipelines::createGuiRenderType);
    }

    private static RenderType createWorldRenderType(Identifier texture) {
        return RenderType.create(
                "page_world_" + texture.toString().replace(':', '_').replace('/', '_'),
                RenderSetup.builder(RenderPipelines.CUTOUT_BLOCK)
                        .withTexture("Sampler0", texture)
                        .useLightmap()
                        .createRenderSetup()
        );
    }

    private static RenderType createGuiRenderType(Identifier texture) {
        return RenderType.create(
                "page_gui_" + texture.toString().replace(':', '_').replace('/', '_'),
                RenderSetup.builder(RenderPipelines.CUTOUT_BLOCK)
                        .withTexture("Sampler0", texture)
                        .createRenderSetup()
        );
    }

    public static void submitWorld(
            SubmitNodeCollector queue,
            PoseStack poseStack,
            Identifier texture,
            int light,
            int overlay
    ) {
        queue.submitCustomGeometry(
                poseStack,
                getWorldRenderType(texture),
                (pose, consumer) -> emitPageQuad(pose, consumer, light, overlay)
        );
    }

    public static void submitGui(
            SubmitNodeCollector queue,
            PoseStack poseStack,
            Identifier texture,
            int light,
            int overlay
    ) {
        queue.submitCustomGeometry(
                poseStack,
                getGuiRenderType(texture),
                (pose, consumer) -> emitPageQuad(pose, consumer, light, overlay)
        );
    }

    private static void emitPageQuad(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light,
            int overlay
    ) {
        emitFront(pose, consumer, light, overlay);
        emitBack(pose, consumer, light, overlay);
    }

    private static void emitFront(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light,
            int overlay
    ) {
        consumer.addVertex(pose, 0.0F, 1.0F, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(0.0F, 0.0F)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);

        consumer.addVertex(pose, 1.0F, 1.0F, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(1.0F, 0.0F)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);

        consumer.addVertex(pose, 1.0F, 0.0F, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(1.0F, 1.0F)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);

        consumer.addVertex(pose, 0.0F, 0.0F, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(0.0F, 1.0F)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    private static void emitBack(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light,
            int overlay
    ) {
        float z = -0.001F;

        consumer.addVertex(pose, 0.0F, 0.0F, z)
                .setColor(255, 255, 255, 255)
                .setUv(0.0F, 1.0F)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, -1.0F);

        consumer.addVertex(pose, 1.0F, 0.0F, z)
                .setColor(255, 255, 255, 255)
                .setUv(1.0F, 1.0F)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, -1.0F);

        consumer.addVertex(pose, 1.0F, 1.0F, z)
                .setColor(255, 255, 255, 255)
                .setUv(1.0F, 0.0F)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, -1.0F);

        consumer.addVertex(pose, 0.0F, 1.0F, z)
                .setColor(255, 255, 255, 255)
                .setUv(0.0F, 0.0F)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, -1.0F);
    }
}