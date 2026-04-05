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
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BookstandBlockEntityRenderer
        implements BlockEntityRenderer<BlockEntityBookstand, BookstandRenderState> {

    private static final ObjMesh MODEL = ObjMeshLoader.load(
            Identifier.fromNamespaceAndPath("mystcraft-sc", "models/block/bookstand.obj")
    );

    private final ItemModelResolver itemModelResolver;
    private final BookstandBookRenderHelper bookRenderHelper;

    public BookstandBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
        this.bookRenderHelper = new BookstandBookRenderHelper();
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
        state.contentType = blockEntity.getContentType();

        ItemStack stored = DisplayItemRenderHelper.canonicalizeForDisplay(
                blockEntity.getStoredItem(),
                state.contentType
        );

        state.displayedStack = stored;
        state.hasDisplayItem = !stored.isEmpty();

        if (blockEntity.getLevel() != null) {
            state.packedLight = LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos());

            // Keep this for completeness, but book-like items won't use the item path now.
            DisplayItemRenderHelper.prepareTopItem(
                    this.itemModelResolver,
                    state.displayedItem,
                    stored,
                    blockEntity.getLevel()
            );
        } else {
            state.packedLight = 0;
            state.displayedItem.clear();
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

        if (state.hasDisplayItem && DisplayItemRenderHelper.isBookLike(state.contentType)) {
            poseStack.pushPose();

            /*
             * Legacy RenderBookstand:
             * translate(0, 0.55F, 0)
             * rotate(90 + 45 * rotationIndex, Y)
             * rotate(120F, Z)
             * scale(0.8F)
             *
             * We already applied the stand rotation above:
             *   -45 * rotationIndex
             *
             * So this local book transform only needs the fixed book orientation.
             * This should be close immediately, and any tiny tweak can be done here
             * without touching the slant-board code.
             */
            poseStack.translate(-0.06F, 0.5F, 0.1F);
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(120.0F));
            poseStack.scale(0.8F, 0.8F, 0.8F);

            this.bookRenderHelper.submitBook(
                    state.contentType,
                    state.displayedStack,
                    poseStack,
                    queue,
                    state.packedLight
            );

            poseStack.popPose();
        }

        poseStack.popPose();
    }
}