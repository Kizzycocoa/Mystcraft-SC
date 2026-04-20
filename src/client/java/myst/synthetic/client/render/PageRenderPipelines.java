package myst.synthetic.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private static final Map<Identifier, PageMesh> MESH_CACHE = new HashMap<>();

    private static final float HALF_THICKNESS = 0.015F;

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

    private static PageMesh getMesh(PageRenderAsset asset) {
        return MESH_CACHE.computeIfAbsent(asset.textureId(), ignored -> PageMesh.fromImage(asset.image()));
    }

    public static void submitWorld(
            SubmitNodeCollector queue,
            PoseStack poseStack,
            PageRenderAsset asset,
            int light,
            int overlay,
            int tintArgb
    ) {
        PageMesh mesh = getMesh(asset);
        queue.submitCustomGeometry(
                poseStack,
                getWorldRenderType(asset.textureId()),
                (pose, consumer) -> emitPageGeometry(pose, consumer, light, overlay, mesh, tintArgb)
        );
    }

    public static void submitGui(
            SubmitNodeCollector queue,
            PoseStack poseStack,
            PageRenderAsset asset,
            int light,
            int overlay,
            int tintArgb
    ) {
        PageMesh mesh = getMesh(asset);
        queue.submitCustomGeometry(
                poseStack,
                getGuiRenderType(asset.textureId()),
                (pose, consumer) -> emitPageGeometry(pose, consumer, light, overlay, mesh, tintArgb)
        );
    }

    private static void emitPageGeometry(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light,
            int overlay,
            PageMesh mesh,
            int tintArgb
    ) {
        emitFrontFace(pose, consumer, light, overlay, tintArgb);
        emitBackFace(pose, consumer, light, overlay, tintArgb);
        emitTopEdges(pose, consumer, light, overlay, mesh, tintArgb);
        emitBottomEdges(pose, consumer, light, overlay, mesh, tintArgb);
        emitLeftEdges(pose, consumer, light, overlay, mesh, tintArgb);
        emitRightEdges(pose, consumer, light, overlay, mesh, tintArgb);
    }

    private static void emitFrontFace(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light,
            int overlay,
            int tintArgb
    ) {
        emitQuad(
                pose, consumer, light, overlay, tintArgb,
                -0.5F, -0.5F, HALF_THICKNESS,
                0.5F, -0.5F, HALF_THICKNESS,
                0.5F,  0.5F, HALF_THICKNESS,
                -0.5F,  0.5F, HALF_THICKNESS,
                0.0F, 0.0F, -1.0F,
                0.0F, 1.0F,
                1.0F, 1.0F,
                1.0F, 0.0F,
                0.0F, 0.0F
        );
    }

    private static void emitBackFace(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light,
            int overlay,
            int tintArgb
    ) {
        emitQuad(
                pose, consumer, light, overlay, tintArgb,
                -0.5F,  0.5F, -HALF_THICKNESS,
                0.5F,  0.5F, -HALF_THICKNESS,
                0.5F, -0.5F, -HALF_THICKNESS,
                -0.5F, -0.5F, -HALF_THICKNESS,
                0.0F, 0.0F, 1.0F,
                0.0F, 0.0F,
                1.0F, 0.0F,
                1.0F, 1.0F,
                0.0F, 1.0F
        );
    }

    private static void emitTopEdges(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light,
            int overlay,
            PageMesh mesh,
            int tintArgb
    ) {
        for (HorizontalSpan span : mesh.topSpans) {
            float x0 = pixelX(span.start, mesh.width);
            float x1 = pixelX(span.endExclusive, mesh.width);
            float y = pixelY(span.row, mesh.height);

            float u0 = (float) span.start / (float) mesh.width;
            float u1 = (float) span.endExclusive / (float) mesh.width;
            float v0 = (float) span.row / (float) mesh.height;
            float v1 = (float) (span.row + 1) / (float) mesh.height;

            emitQuad(
                    pose, consumer, light, overlay, tintArgb,
                    x0, y,  HALF_THICKNESS,
                    x1, y,  HALF_THICKNESS,
                    x1, y, -HALF_THICKNESS,
                    x0, y, -HALF_THICKNESS,
                    0.0F, 1.0F, 0.0F,
                    u0, v0,
                    u1, v0,
                    u1, v1,
                    u0, v1
            );
        }
    }

    private static void emitBottomEdges(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light,
            int overlay,
            PageMesh mesh,
            int tintArgb
    ) {
        for (HorizontalSpan span : mesh.bottomSpans) {
            float x0 = pixelX(span.start, mesh.width);
            float x1 = pixelX(span.endExclusive, mesh.width);
            float y = pixelY(span.row + 1, mesh.height);

            float u0 = (float) span.start / (float) mesh.width;
            float u1 = (float) span.endExclusive / (float) mesh.width;
            float v0 = (float) span.row / (float) mesh.height;
            float v1 = (float) (span.row + 1) / (float) mesh.height;

            emitQuad(
                    pose, consumer, light, overlay, tintArgb,
                    x0, y, -HALF_THICKNESS,
                    x1, y, -HALF_THICKNESS,
                    x1, y,  HALF_THICKNESS,
                    x0, y,  HALF_THICKNESS,
                    0.0F, -1.0F, 0.0F,
                    u0, v0,
                    u1, v0,
                    u1, v1,
                    u0, v1
            );
        }
    }

    private static void emitLeftEdges(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light,
            int overlay,
            PageMesh mesh,
            int tintArgb
    ) {
        for (VerticalSpan span : mesh.leftSpans) {
            float x = pixelX(span.column, mesh.width);
            float y0 = pixelY(span.start, mesh.height);
            float y1 = pixelY(span.endExclusive, mesh.height);

            float u0 = (float) span.column / (float) mesh.width;
            float u1 = (float) (span.column + 1) / (float) mesh.width;
            float v0 = (float) span.start / (float) mesh.height;
            float v1 = (float) span.endExclusive / (float) mesh.height;

            emitQuad(
                    pose, consumer, light, overlay, tintArgb,
                    x, y1,  HALF_THICKNESS,
                    x, y0,  HALF_THICKNESS,
                    x, y0, -HALF_THICKNESS,
                    x, y1, -HALF_THICKNESS,
                    -1.0F, 0.0F, 0.0F,
                    u0, v1,
                    u0, v0,
                    u1, v0,
                    u1, v1
            );
        }
    }

    private static void emitRightEdges(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light,
            int overlay,
            PageMesh mesh,
            int tintArgb
    ) {
        for (VerticalSpan span : mesh.rightSpans) {
            float x = pixelX(span.column + 1, mesh.width);
            float y0 = pixelY(span.start, mesh.height);
            float y1 = pixelY(span.endExclusive, mesh.height);

            float u0 = (float) span.column / (float) mesh.width;
            float u1 = (float) (span.column + 1) / (float) mesh.width;
            float v0 = (float) span.start / (float) mesh.height;
            float v1 = (float) span.endExclusive / (float) mesh.height;

            emitQuad(
                    pose, consumer, light, overlay, tintArgb,
                    x, y1, -HALF_THICKNESS,
                    x, y0, -HALF_THICKNESS,
                    x, y0,  HALF_THICKNESS,
                    x, y1,  HALF_THICKNESS,
                    1.0F, 0.0F, 0.0F,
                    u0, v1,
                    u0, v0,
                    u1, v0,
                    u1, v1
            );
        }
    }

    private static float pixelX(int pixel, int width) {
        return ((float) pixel / (float) width) - 0.5F;
    }

    private static float pixelY(int pixel, int height) {
        return 0.5F - ((float) pixel / (float) height);
    }

    private static boolean isOpaque(BufferedImage image, int x, int y) {
        if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) {
            return false;
        }

        int argb = image.getRGB(x, y);
        int alpha = (argb >>> 24) & 0xFF;
        return alpha > 0;
    }

    private static void emitQuad(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light,
            int overlay,
            int tintArgb,
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
        int a = (tintArgb >>> 24) & 0xFF;
        int r = (tintArgb >>> 16) & 0xFF;
        int g = (tintArgb >>> 8) & 0xFF;
        int b = tintArgb & 0xFF;

        consumer.addVertex(pose, x0, y0, z0)
                .setColor(r, g, b, a)
                .setUv(u0, v0)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);

        consumer.addVertex(pose, x1, y1, z1)
                .setColor(r, g, b, a)
                .setUv(u1, v1)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);

        consumer.addVertex(pose, x2, y2, z2)
                .setColor(r, g, b, a)
                .setUv(u2, v2)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);

        consumer.addVertex(pose, x3, y3, z3)
                .setColor(r, g, b, a)
                .setUv(u3, v3)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    private record PageMesh(
            int width,
            int height,
            List<HorizontalSpan> topSpans,
            List<HorizontalSpan> bottomSpans,
            List<VerticalSpan> leftSpans,
            List<VerticalSpan> rightSpans
    ) {
        private static PageMesh fromImage(BufferedImage image) {
            int width = image.getWidth();
            int height = image.getHeight();

            boolean[][] opaque = new boolean[height][width];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    opaque[y][x] = isOpaque(image, x, y);
                }
            }

            return new PageMesh(
                    width,
                    height,
                    buildTopSpans(opaque, width, height),
                    buildBottomSpans(opaque, width, height),
                    buildLeftSpans(opaque, width, height),
                    buildRightSpans(opaque, width, height)
            );
        }

        private static List<HorizontalSpan> buildTopSpans(boolean[][] opaque, int width, int height) {
            List<HorizontalSpan> spans = new ArrayList<>();

            for (int y = 0; y < height; y++) {
                int x = 0;
                while (x < width) {
                    if (!hasTopEdge(opaque, width, height, x, y)) {
                        x++;
                        continue;
                    }

                    int start = x;
                    x++;
                    while (x < width && hasTopEdge(opaque, width, height, x, y)) {
                        x++;
                    }

                    spans.add(new HorizontalSpan(y, start, x));
                }
            }

            return spans;
        }

        private static List<HorizontalSpan> buildBottomSpans(boolean[][] opaque, int width, int height) {
            List<HorizontalSpan> spans = new ArrayList<>();

            for (int y = 0; y < height; y++) {
                int x = 0;
                while (x < width) {
                    if (!hasBottomEdge(opaque, width, height, x, y)) {
                        x++;
                        continue;
                    }

                    int start = x;
                    x++;
                    while (x < width && hasBottomEdge(opaque, width, height, x, y)) {
                        x++;
                    }

                    spans.add(new HorizontalSpan(y, start, x));
                }
            }

            return spans;
        }

        private static List<VerticalSpan> buildLeftSpans(boolean[][] opaque, int width, int height) {
            List<VerticalSpan> spans = new ArrayList<>();

            for (int x = 0; x < width; x++) {
                int y = 0;
                while (y < height) {
                    if (!hasLeftEdge(opaque, width, height, x, y)) {
                        y++;
                        continue;
                    }

                    int start = y;
                    y++;
                    while (y < height && hasLeftEdge(opaque, width, height, x, y)) {
                        y++;
                    }

                    spans.add(new VerticalSpan(x, start, y));
                }
            }

            return spans;
        }

        private static List<VerticalSpan> buildRightSpans(boolean[][] opaque, int width, int height) {
            List<VerticalSpan> spans = new ArrayList<>();

            for (int x = 0; x < width; x++) {
                int y = 0;
                while (y < height) {
                    if (!hasRightEdge(opaque, width, height, x, y)) {
                        y++;
                        continue;
                    }

                    int start = y;
                    y++;
                    while (y < height && hasRightEdge(opaque, width, height, x, y)) {
                        y++;
                    }

                    spans.add(new VerticalSpan(x, start, y));
                }
            }

            return spans;
        }

        private static boolean hasTopEdge(boolean[][] opaque, int width, int height, int x, int y) {
            if (!opaque[y][x]) {
                return false;
            }
            return y == 0 || !opaque[y - 1][x];
        }

        private static boolean hasBottomEdge(boolean[][] opaque, int width, int height, int x, int y) {
            if (!opaque[y][x]) {
                return false;
            }
            return y == height - 1 || !opaque[y + 1][x];
        }

        private static boolean hasLeftEdge(boolean[][] opaque, int width, int height, int x, int y) {
            if (!opaque[y][x]) {
                return false;
            }
            return x == 0 || !opaque[y][x - 1];
        }

        private static boolean hasRightEdge(boolean[][] opaque, int width, int height, int x, int y) {
            if (!opaque[y][x]) {
                return false;
            }
            return x == width - 1 || !opaque[y][x + 1];
        }
    }

    private record HorizontalSpan(int row, int start, int endExclusive) {
    }

    private record VerticalSpan(int column, int start, int endExclusive) {
    }
}