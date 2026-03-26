package myst.synthetic.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.Consumer;
import myst.synthetic.client.render.model.ObjMesh;
import myst.synthetic.client.render.model.ObjMeshLoader;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import myst.synthetic.block.property.WoodType;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public final class SlantBoardItemSpecialRenderer implements SpecialModelRenderer<Void> {

    private static final ObjMesh MODEL = ObjMeshLoader.load(
            Identifier.fromNamespaceAndPath("mystcraft-sc", "models/block/slant_board.obj")
    );

    @Override
    public void submit(
            @Nullable Void object,
            ItemDisplayContext itemDisplayContext,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int light,
            int overlay,
            boolean hasFoil,
            int seed
    ) {
        poseStack.pushPose();

        poseStack.translate(0.5F, 0.0F, 0.5F);

        switch (itemDisplayContext) {
            case GUI -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(20.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(-45.0F));
                poseStack.scale(0.90F, 0.90F, 0.90F);
                poseStack.translate(0.0F, 0.05F, 0.0F);
            }
            case GROUND -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(-45.0F));
                poseStack.scale(0.70F, 0.70F, 0.70F);
                poseStack.translate(0.0F, 0.02F, 0.0F);
            }
            case FIXED -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                poseStack.scale(0.85F, 0.85F, 0.85F);
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(15.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(-35.0F));
                poseStack.scale(0.80F, 0.80F, 0.80F);
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(10.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(-35.0F));
                poseStack.scale(0.80F, 0.80F, 0.80F);
            }
            default -> {
            }
        }

        // Match the OBJ normalization already used for the placed renderer.
        poseStack.scale(1.1428572F, 1.4071103F, 1.0666667F);
        poseStack.translate(0.0F, -0.0063086664F, -0.03125F);

        SlantBoardRenderPipelines.submit(
                submitNodeCollector,
                poseStack,
                WoodType.OAK,
                face -> light,
                MODEL
        );

        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        consumer.accept(new Vector3f(0.0F, 0.0F, 0.0F));
        consumer.accept(new Vector3f(1.0F, 1.0F, 1.0F));
    }

    @Override
    public @Nullable Void extractArgument(ItemStack itemStack) {
        return null;
    }
}