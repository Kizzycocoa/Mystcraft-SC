package myst.synthetic.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import myst.synthetic.block.BlockSlantBoard;
import myst.synthetic.block.entity.BlockEntitySlantBoard;
import myst.synthetic.client.render.model.ObjMesh;
import myst.synthetic.client.render.model.ObjMeshLoader;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SlantBoardBlockEntityRenderer
        implements BlockEntityRenderer<BlockEntitySlantBoard, SlantBoardRenderState> {

    private static final ObjMesh MODEL = ObjMeshLoader.load(
            Identifier.fromNamespaceAndPath("mystcraft-sc", "models/block/slant_board.obj")
    );

    public SlantBoardBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public SlantBoardRenderState createRenderState() {
        return new SlantBoardRenderState();
    }

    @Override
    public void extractRenderState(
            BlockEntitySlantBoard blockEntity,
            SlantBoardRenderState state,
            float tickProgress,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);

        state.facing = blockEntity.getBlockState().getValue(BlockSlantBoard.FACING);
        state.wood = blockEntity.getBlockState().getValue(BlockSlantBoard.WOOD);

        if (blockEntity.getLevel() != null) {
            state.packedLight = LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos());
        } else {
            state.packedLight = 0;
        }
    }

    @Override
    public void submit(
            SlantBoardRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector queue,
            CameraRenderState cameraState
    ) {
        poseStack.pushPose();

        // Move render origin to the center of the block.
        poseStack.translate(0.5F, 0.0F, 0.5F);

        // Rotate around the block center.
        rotateFromFacing(poseStack, state.facing);

        // Scale the raw OBJ up to a 1x1x7/16 block footprint.
        poseStack.scale(1.1428572F, 1.4071103F, 1.0666667F);

        // Re-center and floor-align the raw OBJ mesh in its own local space.
        poseStack.translate(0.0F, -0.0063086664F, -0.03125F);

        SlantBoardRenderPipelines.submit(queue, poseStack, state.wood, state.packedLight, MODEL);

        poseStack.popPose();
    }

    private static void rotateFromFacing(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
        }
    }
}