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
            var level = blockEntity.getLevel();
            var pos = blockEntity.getBlockPos();

            state.selfLight = LevelRenderer.getLightColor(level, pos);
            state.northLight = LevelRenderer.getLightColor(level, pos.north());
            state.southLight = LevelRenderer.getLightColor(level, pos.south());
            state.eastLight = LevelRenderer.getLightColor(level, pos.east());
            state.westLight = LevelRenderer.getLightColor(level, pos.west());
            state.downLight = LevelRenderer.getLightColor(level, pos.below());
        } else {
            state.selfLight = 0;
            state.northLight = 0;
            state.southLight = 0;
            state.eastLight = 0;
            state.westLight = 0;
            state.downLight = 0;
        }
    }

    private static float[] averageNormal(ObjMesh.Face face) {
        float x = 0.0F;
        float y = 0.0F;
        float z = 0.0F;

        for (ObjMesh.FaceVertex fv : face.vertices()) {
            float[] n = MODEL.getNormal(fv.normalIndex());
            x += n[0];
            y += n[1];
            z += n[2];
        }

        float length = (float)Math.sqrt(x * x + y * y + z * z);
        if (length == 0.0F) {
            return new float[] {0.0F, 1.0F, 0.0F};
        }

        return new float[] {x / length, y / length, z / length};
    }

    private static int resolveFaceLight(ObjMesh.Face face, SlantBoardRenderState state) {
        float[] normal = averageNormal(face);

        float nx = normal[0];
        float ny = normal[1];
        float nz = normal[2];

        // Special-case the brim:
        // only the single face touching the block edge should use edge lighting.
        // every other brim face should use the block's inner light.
        if ("brim".equals(face.objectName())) {
            if (nz < -0.75F) {
                return state.northLight;
            }

            return state.selfLight;
        }

        // Base behavior stays exactly as it already works.
        if (ny < -0.75F) {
            return state.downLight;
        }

        if (nz < -0.75F) {
            return state.northLight;
        }
        if (nz > 0.75F) {
            return state.southLight;
        }
        if (nx > 0.75F) {
            return state.eastLight;
        }
        if (nx < -0.75F) {
            return state.westLight;
        }

        return state.selfLight;
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

        SlantBoardRenderPipelines.submit(
                queue,
                poseStack,
                state.wood,
                face -> resolveFaceLight(face, state),
                MODEL
        );

        poseStack.popPose();
    }

    private static void rotateFromFacing(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        }
    }
}