package myst.synthetic.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import myst.synthetic.block.BlockBookReceptacle;
import myst.synthetic.block.entity.BlockEntityBookReceptacle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BookReceptacleBlockEntityRenderer
        implements BlockEntityRenderer<BlockEntityBookReceptacle, BookReceptacleRenderState> {

    private final BookReceptacleBookRenderHelper bookRenderHelper;

    public BookReceptacleBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.bookRenderHelper = new BookReceptacleBookRenderHelper();
    }

    @Override
    public BookReceptacleRenderState createRenderState() {
        return new BookReceptacleRenderState();
    }

    @Override
    public void extractRenderState(
            BlockEntityBookReceptacle blockEntity,
            BookReceptacleRenderState state,
            float tickProgress,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);

        BlockState blockState = blockEntity.getBlockState();
        state.face = blockState.getValue(BlockBookReceptacle.FACE);
        state.facing = blockState.getValue(BlockBookReceptacle.FACING);
        state.displayedStack = blockEntity.getBook();
        state.hasBook = !state.displayedStack.isEmpty();
        state.contentType = blockEntity.getContentType();

        if (blockEntity.getLevel() != null) {
            state.packedLight = LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos());
        } else {
            state.packedLight = 0;
        }
    }

    @Override
    public void submit(
            BookReceptacleRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector queue,
            CameraRenderState cameraState
    ) {
        if (!state.hasBook) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);

        applyMountRotation(poseStack, state.face, state.facing);

        // Initial placement guess. This is the part you will likely tweak in-game.
        poseStack.translate(0.0F, 0.01F, -0.16F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        poseStack.scale(0.48F, 0.48F, 0.48F);

        this.bookRenderHelper.submitBook(
                state.contentType,
                state.displayedStack,
                poseStack,
                queue,
                state.packedLight
        );

        poseStack.popPose();
    }

    private static void applyMountRotation(PoseStack poseStack, AttachFace face, net.minecraft.core.Direction facing) {
        switch (face) {
            case WALL -> {
                switch (facing) {
                    case NORTH -> {
                    }
                    case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                    case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                    case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
                    default -> {
                    }
                }
            }
            case FLOOR -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                switch (facing) {
                    case NORTH -> poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
                    case SOUTH -> {
                    }
                    case WEST -> poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
                    case EAST -> poseStack.mulPose(Axis.ZP.rotationDegrees(270.0F));
                    default -> {
                    }
                }
            }
            case CEILING -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                switch (facing) {
                    case NORTH -> {
                    }
                    case SOUTH -> poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
                    case WEST -> poseStack.mulPose(Axis.ZP.rotationDegrees(270.0F));
                    case EAST -> poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
                    default -> {
                    }
                }
            }
        }
    }
}