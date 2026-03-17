package myst.synthetic.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import myst.synthetic.block.entity.StarFissureBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class StarFissureBlockEntityRenderer implements BlockEntityRenderer<StarFissureBlockEntity, StarFissureRenderState> {

    private static final float TOP_Y = 0.1F;
    private static final float BOTTOM_Y = 0.0F;

    public StarFissureBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public StarFissureRenderState createRenderState() {
        return new StarFissureRenderState();
    }

    @Override
    public void extractRenderState(
            StarFissureBlockEntity blockEntity,
            StarFissureRenderState state,
            float tickProgress,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);

        if (blockEntity.getLevel() != null) {
            state.animationTime = blockEntity.getLevel().getGameTime() + tickProgress;
        } else {
            state.animationTime = tickProgress;
        }

        long seed = blockEntity.getBlockPos().asLong();
        state.seedOffset = (seed & 1023L) / 1023.0F;

        long millis = System.currentTimeMillis() % 700000L;
        state.time = millis / 200000.0F;

        state.topFaceIndex = StarFissureRenderPipelines.FACE_TOP;
        state.bottomFaceIndex = StarFissureRenderPipelines.FACE_BOTTOM;

        state.cameraPosX = (float) cameraPos.x;
        state.cameraPosY = (float) cameraPos.y;
        state.cameraPosZ = (float) cameraPos.z;
    }

    @Override
    public void submit(
            StarFissureRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector queue,
            CameraRenderState cameraState
    ) {
        submitTopFace(state, poseStack, queue);
        submitBottomFace(state, poseStack, queue);
    }

    private void submitTopFace(
            StarFissureRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector queue
    ) {
        poseStack.pushPose();
        poseStack.translate(0.0F, TOP_Y, 0.0F);

        // Sky pass
        StarFissureRenderPipelines.submitSkyTop(queue, poseStack, this::renderTopFace);

        // Portal passes 1..7
        for (int pass = StarFissureRenderPipelines.FIRST_PORTAL_PASS;
             pass <= StarFissureRenderPipelines.LAST_PORTAL_PASS;
             pass++) {
            StarFissureRenderPipelines.submitPortalTop(queue, poseStack, pass, this::renderTopFace);
        }

        poseStack.popPose();
    }

    private void submitBottomFace(
            StarFissureRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector queue
    ) {
        poseStack.pushPose();
        poseStack.translate(0.0F, BOTTOM_Y, 0.0F);

        // Sky pass
        StarFissureRenderPipelines.submitSkyBottom(queue, poseStack, this::renderBottomFace);

        // Portal passes 1..7
        for (int pass = StarFissureRenderPipelines.FIRST_PORTAL_PASS;
             pass <= StarFissureRenderPipelines.LAST_PORTAL_PASS;
             pass++) {
            StarFissureRenderPipelines.submitPortalBottom(queue, poseStack, pass, this::renderBottomFace);
        }

        poseStack.popPose();
    }

    private void renderTopFace(PoseStack.Pose pose, VertexConsumer consumer) {
        consumer.addVertex(pose, 0.0F, 0.0F, 0.0F);
        consumer.addVertex(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, 1.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, 1.0F, 0.0F, 0.0F);
    }

    private void renderBottomFace(PoseStack.Pose pose, VertexConsumer consumer) {
        consumer.addVertex(pose, 1.0F, 0.0F, 0.0F);
        consumer.addVertex(pose, 1.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, 0.0F, 0.0F, 0.0F);
    }
}