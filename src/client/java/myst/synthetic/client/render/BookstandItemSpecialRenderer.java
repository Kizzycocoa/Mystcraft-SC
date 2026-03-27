package myst.synthetic.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.Consumer;
import myst.synthetic.block.property.WoodType;
import myst.synthetic.client.render.model.ObjMesh;
import myst.synthetic.client.render.model.ObjMeshLoader;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public final class BookstandItemSpecialRenderer implements SpecialModelRenderer<WoodType> {

    private static final ObjMesh MODEL = ObjMeshLoader.load(
            Identifier.fromNamespaceAndPath("mystcraft-sc", "models/block/bookstand.obj")
    );

    @Override
    public void submit(
            @Nullable WoodType wood,
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
                poseStack.translate(-0.75F, 0.1F, 0.0F);
                poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(225.0F));
            }
            case GROUND -> {
                poseStack.translate(0.25F, 0.0F, 0.25F);
                poseStack.scale(0.5F, 0.5F, 0.5F);
            }
            case FIXED -> {
                poseStack.translate(0.5F, 0.0F, 0.41F);
            }
            case THIRD_PERSON_RIGHT_HAND -> {
                poseStack.translate(-0.21F, 0.05F, -0.3F);
                poseStack.mulPose(Axis.XP.rotationDegrees(-190.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
                poseStack.scale(0.45F, 0.45F, 0.45F);
            }
            case THIRD_PERSON_LEFT_HAND -> {
                poseStack.translate(0.21F, 0.05F, -0.3F);
                poseStack.mulPose(Axis.XP.rotationDegrees(-190.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(-180.0F));
                poseStack.scale(0.45F, 0.45F, 0.45F);
            }
            case FIRST_PERSON_RIGHT_HAND -> {
                poseStack.translate(0.2F, 0.1F, 0.0F);
                poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
                poseStack.scale(0.5F, 0.5F, 0.5F);
            }
            case FIRST_PERSON_LEFT_HAND -> {
                poseStack.translate(-0.2F, 0.1F, 0.0F);
                poseStack.mulPose(Axis.YP.rotationDegrees(-45.0F));
                poseStack.scale(0.5F, 0.5F, 0.5F);
            }
            default -> {
            }
        }

        WoodType resolvedWood = wood != null ? wood : WoodType.OAK;

        if (itemDisplayContext == ItemDisplayContext.GUI) {
            BookstandRenderPipelines.submitGui(
                    submitNodeCollector,
                    poseStack,
                    resolvedWood,
                    face -> LightTexture.FULL_BRIGHT,
                    MODEL
            );
        } else {
            BookstandRenderPipelines.submitWorld(
                    submitNodeCollector,
                    poseStack,
                    resolvedWood,
                    face -> LightTexture.FULL_BRIGHT,
                    MODEL
            );
        }

        poseStack.popPose();
    }

    private static WoodType getWoodTypeFromStack(ItemStack stack) {
        var customData = stack.get(DataComponents.CUSTOM_DATA);

        if (customData == null) {
            return WoodType.OAK;
        }

        String name = customData.copyTag().getString("wood").orElse("oak");

        for (WoodType type : WoodType.values()) {
            if (type.getSerializedName().equals(name)) {
                return type;
            }
        }

        return WoodType.OAK;
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        consumer.accept(new Vector3f(0.0F, 0.0F, 0.0F));
        consumer.accept(new Vector3f(1.0F, 1.0F, 1.0F));
    }

    @Override
    public @Nullable WoodType extractArgument(ItemStack itemStack) {
        return getWoodTypeFromStack(itemStack);
    }
}