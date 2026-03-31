package myst.synthetic.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import myst.synthetic.client.page.PageRenderAsset;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public final class PageRenderPipelines {

    private static final Map<Identifier, RenderType> WORLD_RENDER_TYPES = new HashMap<>();
    private static final Map<Identifier, RenderType> GUI_RENDER_TYPES = new HashMap<>();

    private static final float HALF_WIDTH = 0.5F;
    private static final float HALF_HEIGHT = 0.5F;
    private static final float HALF_THICKNESS = 0.015F;
    private static final float FRONT_FACE_OFFSET = 0.0625F;

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
            PageRenderAsset asset,
            int light,
            int overlay
    ) {
        queue.submitCustomGeometry(
                poseStack,
                getWorldRenderType(asset.textureId()),
                (pose, consumer) -> emitPageGeometry(pose, consumer, light, overlay, asset.image())
        );
    }

    public static void submitGui(
            SubmitNodeCollector queue,
            PoseStack poseStack,
            PageRenderAsset asset,
            int light,
            int overlay
    ) {
        queue.submitCustomGeometry(
                poseStack,
                getGuiRenderType(asset.textureId()),
                (pose, consumer) -> emitPageGeometry(pose, consumer, light, overlay, asset.image())
        );
    }

    private static void emitPageGeometry(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light,
            int overlay,
            BufferedImage image
    ) {
        emitFront(pose, consumer, light, overlay);
        emitBack(pose, consumer, light, overlay);
        emitExtrudedEdges(pose, consumer, light, overlay, image);
    }

    private static void emitFront(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light,
            int overlay
    ) {
        float z = HALF_THICKNESS + FRONT_FACE_OFFSET;

        emitQuad(
                pose, consumer, light, overlay,
                -HALF_WIDTH,  HALF_HEIGHT,  z,
                HALF_WIDTH,  HALF_HEIGHT,  z,
                HALF_WIDTH, -HALF_HEIGHT,  z,
                -HALF_WIDTH, -HALF_HEIGHT,  z,
                0.0F, 0.0F, 1.0F,
                0.0F, 0.0F,
                1.0F, 0.0F,
                1.0F, 1.0F,
                0.0F, 1.0F
        );
    }

    private static void emitBack(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light,
            int overlay
    ) {
        emitQuad(
                pose, consumer, light, overlay,
                -HALF_WIDTH, -HALF_HEIGHT, -HALF_THICKNESS,
                HALF_WIDTH, -HALF_HEIGHT, -HALF_THICKNESS,
                HALF_WIDTH,  HALF_HEIGHT, -HALF_THICKNESS,
                -HALF_WIDTH,  HALF_HEIGHT, -HALF_THICKNESS,
                0.0F, 0.0F, -1.0F,
                0.0F, 1.0F,
                1.0F, 1.0F,
                1.0F, 0.0F,
                0.0F, 0.0F
        );
    }

    private static void emitExtrudedEdges(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light,
            int overlay,
            BufferedImage image
    ) {
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!isOpaque(image, x, y)) {
                    continue;
                }

                float x0 = pixelX(x, width);
                float x1 = pixelX(x + 1, width);
                float y0 = pixelY(y, height);
                float y1 = pixelY(y + 1, height);

                float u0 = (float) x / (float) width;
                float u1 = (float) (x + 1) / (float) width;
                float v0 = (float) y / (float) height;
                float v1 = (float) (y + 1) / (float) height;

                if (!isOpaque(image, x, y - 1)) {
                    emitQuad(
                            pose, consumer, light, overlay,
                            x0, y0, -HALF_THICKNESS,
                            x1, y0, -HALF_THICKNESS,
                            x1, y0,  HALF_THICKNESS,
                            x0, y0,  HALF_THICKNESS,
                            0.0F, 1.0F, 0.0F,
                            u0, v0,
                            u1, v0,
                            u1, v1,
                            u0, v1
                    );
                }

                if (!isOpaque(image, x, y + 1)) {
                    emitQuad(
                            pose, consumer, light, overlay,
                            x0, y1,  HALF_THICKNESS,
                            x1, y1,  HALF_THICKNESS,
                            x1, y1, -HALF_THICKNESS,
                            x0, y1, -HALF_THICKNESS,
                            0.0F, -1.0F, 0.0F,
                            u0, v0,
                            u1, v0,
                            u1, v1,
                            u0, v1
                    );
                }

                if (!isOpaque(image, x - 1, y)) {
                    emitQuad(
                            pose, consumer, light, overlay,
                            x0, y1, -HALF_THICKNESS,
                            x0, y0, -HALF_THICKNESS,
                            x0, y0,  HALF_THICKNESS,
                            x0, y1,  HALF_THICKNESS,
                            -1.0F, 0.0F, 0.0F,
                            u0, v1,
                            u0, v0,
                            u1, v0,
                            u1, v1
                    );
                }

                if (!isOpaque(image, x + 1, y)) {
                    emitQuad(
                            pose, consumer, light, overlay,
                            x1, y1,  HALF_THICKNESS,
                            x1, y0,  HALF_THICKNESS,
                            x1, y0, -HALF_THICKNESS,
                            x1, y1, -HALF_THICKNESS,
                            1.0F, 0.0F, 0.0F,
                            u0, v1,
                            u0, v0,
                            u1, v0,
                            u1, v1
                    );
                }
            }
        }
    }

    private static boolean isOpaque(BufferedImage image, int x, int y) {
        if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) {
            return false;
        }

        int argb = image.getRGB(x, y);
        int alpha = (argb >>> 24) & 0xFF;
        return alpha > 0;
    }

    private static float pixelX(int pixel, int width) {
        return ((float) pixel / (float) width) - 0.5F;
    }

    private static float pixelY(int pixel, int height) {
        return 0.5F - ((float) pixel / (float) height);
    }

    private static void emitQuad(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light,
            int overlay,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float nx, float ny, float nz,
            float u0, float v0,
            float u1, float v1,
            float u2, float v2,
            float u3, float v3
    ) {
        consumer.addVertex(pose, x0, y0, z0)
                .setColor(255, 255, 255, 255)
                .setUv(u0, v0)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);

        consumer.addVertex(pose, x1, y1, z1)
                .setColor(255, 255, 255, 255)
                .setUv(u1, v1)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);

        consumer.addVertex(pose, x2, y2, z2)
                .setColor(255, 255, 255, 255)
                .setUv(u2, v2)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);

        consumer.addVertex(pose, x3, y3, z3)
                .setColor(255, 255, 255, 255)
                .setUv(u3, v3)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }
}