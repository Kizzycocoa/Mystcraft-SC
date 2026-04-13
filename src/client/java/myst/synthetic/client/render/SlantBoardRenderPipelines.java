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
import java.util.function.ToIntFunction;

public final class SlantBoardRenderPipelines {

    private SlantBoardRenderPipelines() {
    }

    private static final Map<WoodType, RenderType> WORLD_RENDER_TYPES = new EnumMap<>(WoodType.class);
    private static final Map<WoodType, RenderType> GUI_RENDER_TYPES = new EnumMap<>(WoodType.class);

    private static RenderType getWorldRenderType(WoodType wood) {
        return WORLD_RENDER_TYPES.computeIfAbsent(wood, SlantBoardRenderPipelines::createWorldRenderType);
    }

    private static RenderType getGuiRenderType(WoodType wood) {
        return GUI_RENDER_TYPES.computeIfAbsent(wood, SlantBoardRenderPipelines::createGuiRenderType);
    }

    private static RenderType createWorldRenderType(WoodType wood) {
        String name = "slant_board_world_" + wood.getSerializedName();

        return RenderType.create(
                name,
                RenderSetup.builder(RenderPipelines.CUTOUT_BLOCK)
                        .withTexture("Sampler0", getTexture(wood))
                        .useLightmap()
                        .createRenderSetup()
        );
    }

    private static RenderType createGuiRenderType(WoodType wood) {
        String name = "slant_board_gui_" + wood.getSerializedName();

        return RenderType.create(
                name,
                RenderSetup.builder(RenderPipelines.ENTITY_SOLID_Z_OFFSET_FORWARD)
                        .withTexture("Sampler0", getTexture(wood))
                        .createRenderSetup()
        );
    }

    public static void submitWorld(
            SubmitNodeCollector queue,
            PoseStack poseStack,
            WoodType wood,
            ToIntFunction<ObjMesh.Face> lightResolver,
            ObjMesh mesh
    ) {
        queue.submitCustomGeometry(
                poseStack,
                getWorldRenderType(wood),
                (pose, consumer) -> mesh.emit(pose, consumer, lightResolver)
        );
    }

    public static void submitGui(
            SubmitNodeCollector queue,
            PoseStack poseStack,
            WoodType wood,
            ToIntFunction<ObjMesh.Face> lightResolver,
            ObjMesh mesh
    ) {
        queue.submitCustomGeometry(
                poseStack,
                getGuiRenderType(wood),
                (pose, consumer) -> mesh.emit(pose, consumer, lightResolver)
        );
    }

    private static Identifier getTexture(WoodType wood) {
        return Identifier.fromNamespaceAndPath(
                "mystcraft-sc",
                "textures/block/slantboard/" + wood.getSerializedName() + "_slantboard.png"
        );
    }
}