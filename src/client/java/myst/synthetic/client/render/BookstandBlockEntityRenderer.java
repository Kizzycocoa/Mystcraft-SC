package myst.synthetic.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import myst.synthetic.block.BlockBookstand;
import myst.synthetic.block.entity.BlockEntityBookstand;
import myst.synthetic.client.render.model.ObjMesh;
import myst.synthetic.client.render.model.ObjMeshLoader;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BookstandBlockEntityRenderer
        implements BlockEntityRenderer<BlockEntityBookstand, BookstandRenderState> {

    private static final ObjMesh MODEL = ObjMeshLoader.load(
            Identifier.fromNamespaceAndPath("mystcraft-sc", "models/block/bookstand.obj")
    );

    public BookstandBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public BookstandRenderState createRenderState() {
        return new BookstandRenderState();
    }

    @Override
    public void extractRenderState(
            BlockEntityBookstand blockEntity,
            BookstandRenderState state,
            float tickProgress,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);

        state.rotationIndex = blockEntity.getBlockState().getValue(BlockBookstand.ROTATION_INDEX);
        state.wood = blockEntity.getBlockState().getValue(BlockBookstand.WOOD);

        if (blockEntity.getLevel() != null) {
            state.packedLight = LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos());
        } else {
            state.packedLight = 0;
        }
    }

    @Override
    public void submit(
            BookstandRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector queue,
            CameraRenderState cameraState
    ) {
        poseStack.pushPose();

        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-45.0F * state.rotationIndex));

        BookstandRenderPipelines.submitWorld(
                queue,
                poseStack,
                state.wood,
                face -> state.packedLight,
                MODEL
        );

        poseStack.popPose();
    }
}