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
                poseStack.translate(0.0F, 0.2F, 0.0F);
                poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(225.0F));
                poseStack.scale(0.75F, 0.75F, 0.75F);
            }
            case GROUND -> {
                poseStack.translate(0.25F, 0.0F, 0.25F);
                poseStack.scale(0.5F, 0.5F, 0.5F);
            }
            case FIXED -> {
                poseStack.translate(0.4F, 0.35F, -0.32F);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                poseStack.scale(0.8F, 0.8F, 0.8F);
            }
            case THIRD_PERSON_RIGHT_HAND -> {
                poseStack.translate(-0.21F, 0.3F, 0.16F);
                poseStack.mulPose(Axis.XP.rotationDegrees(-100.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
                poseStack.scale(0.45F, 0.45F, 0.45F);
            }
            case THIRD_PERSON_LEFT_HAND -> {
                poseStack.translate(0.21F, 0.3F, 0.16F);
                poseStack.mulPose(Axis.XP.rotationDegrees(-100.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(-180.0F));
                poseStack.scale(0.45F, 0.45F, 0.45F);
            }
            case FIRST_PERSON_RIGHT_HAND -> {
                poseStack.translate(0.2F, 0.3F, 0.0F);
                poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
                poseStack.scale(0.5F, 0.5F, 0.5F);
            }
            case FIRST_PERSON_LEFT_HAND -> {
                poseStack.translate(-0.2F, 0.3F, 0.0F);
                poseStack.mulPose(Axis.YP.rotationDegrees(-45.0F));
                poseStack.scale(0.5F, 0.5F, 0.5F);
            }
            default -> {
            }
        }

        // Match the OBJ normalization already used for the placed renderer.
        poseStack.scale(1.1428572F, 1.4071103F, 1.0666667F);
        poseStack.translate(0.0F, -0.0063086664F, -0.03125F);

        int resolvedLight = itemDisplayContext == ItemDisplayContext.GUI ? 15728880 : light;

        SlantBoardRenderPipelines.submit(
                submitNodeCollector,
                poseStack,
                WoodType.OAK,
                face -> resolvedLight,
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