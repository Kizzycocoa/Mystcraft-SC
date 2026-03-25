package myst.synthetic.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import myst.synthetic.block.property.WoodType;
import myst.synthetic.client.render.model.ObjMesh;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import java.util.EnumMap;
import java.util.Map;

public final class SlantBoardRenderPipelines {

    private static final Map<WoodType, RenderType> RENDER_TYPES = new EnumMap<>(WoodType.class);

    private SlantBoardRenderPipelines() {
    }

    public static void submit(
            SubmitNodeCollector queue,
            PoseStack poseStack,
            WoodType wood,
            java.util.function.ToIntFunction<ObjMesh.Face> lightResolver,
            ObjMesh mesh
    ) {
        queue.submitCustomGeometry(
                poseStack,
                getRenderType(wood),
                (pose, consumer) -> mesh.emit(pose, consumer, lightResolver)
        );
    }

    private static RenderType getRenderType(WoodType wood) {
        return RENDER_TYPES.computeIfAbsent(wood, SlantBoardRenderPipelines::createRenderType);
    }

    private static RenderType createRenderType(WoodType wood) {
        String name = "slant_board_" + wood.getSerializedName();

        return RenderType.create(
                name,
                RenderSetup.builder(RenderPipelines.ENTITY_SOLID)
                        .withTexture("Sampler0", getTexture(wood))
                        .createRenderSetup()
        );
    }

    private static Identifier getTexture(WoodType wood) {
        return Identifier.fromNamespaceAndPath(
                "mystcraft-sc",
                "textures/block/slantboard/" + getTextureKey(wood) + "_slantboard.png"
        );
    }

    private static String getTextureKey(WoodType wood) {
        return switch (wood) {
            default -> wood.getSerializedName();
        };
    }
}