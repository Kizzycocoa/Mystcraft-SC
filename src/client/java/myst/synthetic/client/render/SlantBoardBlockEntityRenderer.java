package myst.synthetic.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import myst.synthetic.block.BlockSlantBoard;
import myst.synthetic.block.entity.BlockEntitySlantBoard;
import myst.synthetic.block.entity.DisplayContentType;
import myst.synthetic.client.render.model.ObjMesh;
import myst.synthetic.client.render.model.ObjMeshLoader;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SlantBoardBlockEntityRenderer
        implements BlockEntityRenderer<BlockEntitySlantBoard, SlantBoardRenderState> {

    private static final ObjMesh MODEL = ObjMeshLoader.load(
            Identifier.fromNamespaceAndPath("mystcraft-sc", "models/block/slant_board.obj")
    );

    private final ItemModelResolver itemModelResolver;

    public SlantBoardBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
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
        state.contentType = blockEntity.getContentType();

        ItemStack stored = DisplayItemRenderHelper.canonicalizeForDisplay(
                blockEntity.getStoredItem(),
                state.contentType
        );

        state.displayedStack = stored;
        state.hasDisplayItem = !stored.isEmpty();

        if (blockEntity.getLevel() != null) {
            var level = blockEntity.getLevel();
            var pos = blockEntity.getBlockPos();

            state.selfLight = LevelRenderer.getLightColor(level, pos);
            state.northLight = LevelRenderer.getLightColor(level, pos.north());
            state.southLight = LevelRenderer.getLightColor(level, pos.south());
            state.eastLight = LevelRenderer.getLightColor(level, pos.east());
            state.westLight = LevelRenderer.getLightColor(level, pos.west());
            state.downLight = LevelRenderer.getLightColor(level, pos.below());

            DisplayItemRenderHelper.prepareTopItem(
                    this.itemModelResolver,
                    state.displayedItem,
                    stored,
                    level
            );
        } else {
            state.selfLight = 0;
            state.northLight = 0;
            state.southLight = 0;
            state.eastLight = 0;
            state.westLight = 0;
            state.downLight = 0;
            state.displayedItem.clear();
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

    private static float[] rotateNormal(float[] normal, Direction facing) {
        float nx = normal[0];
        float ny = normal[1];
        float nz = normal[2];

        switch (facing) {
            case NORTH -> {
            }
            case SOUTH -> {
                nx = -nx;
                nz = -nz;
            }
            case WEST -> {
                float oldNx = nx;
                nx = nz;
                nz = -oldNx;
            }
            case EAST -> {
                float oldNx = nx;
                nx = -nz;
                nz = oldNx;
            }
        }

        return new float[] { nx, ny, nz };
    }

    private static int resolveFaceLight(ObjMesh.Face face, SlantBoardRenderState state) {
        float[] localNormal = averageNormal(face);
        float[] worldNormal = rotateNormal(localNormal, state.facing);

        float nx = worldNormal[0];
        float ny = worldNormal[1];
        float nz = worldNormal[2];

        if ("brim".equals(face.objectName())) {
            if (localNormal[2] < -0.75F) {
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
            }

            return state.selfLight;
        }

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

        poseStack.translate(0.5F, 0.0F, 0.5F);
        rotateFromFacing(poseStack, state.facing);

        poseStack.pushPose();
        poseStack.scale(1.1428572F, 1.4071103F, 1.0666667F);
        poseStack.translate(0.0F, -0.0063086664F, -0.03125F);

        SlantBoardRenderPipelines.submitWorld(
                queue,
                poseStack,
                state.wood,
                face -> resolveFaceLight(face, state),
                MODEL
        );

        poseStack.popPose();

        if (state.hasDisplayItem) {
            poseStack.pushPose();

            // Found empirically to match the board angle.
            poseStack.translate(0.0F, 0.50F, 0.0F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(-109.06F));

            if (DisplayItemRenderHelper.isBookLike(state.contentType)) {
                poseStack.scale(0.8F, 0.8F, 0.8F);

                DisplayItemRenderHelper.submitPreparedItem(
                        state.displayedItem,
                        poseStack,
                        queue,
                        state.selfLight
                );
            } else {
                poseStack.translate(0.0F, 0.20F, 0.0F);
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

                // These values lift the flat item out of the board after the board-pitch rotation.
                poseStack.translate(0.0F, 0.32F, 0.01F);
                poseStack.scale(0.85F, 0.85F, 0.85F);

                DisplayItemRenderHelper.submitPreparedItem(
                        state.displayedItem,
                        poseStack,
                        queue,
                        state.selfLight
                );
            }

            poseStack.popPose();
        }

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