package myst.synthetic.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import myst.synthetic.entity.EntityLinkbook;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;

public class LinkbookEntityRenderer extends EntityRenderer<EntityLinkbook, EntityRenderState> {

    public LinkbookEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.15F;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void extractRenderState(EntityLinkbook entity, EntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
    }

    @Override
    public void submit(
            EntityRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraState
    ) {
        // Temporary: compile-safe no-op renderer.
        // Once this builds cleanly, we can replace this with proper item submission.
    }
}