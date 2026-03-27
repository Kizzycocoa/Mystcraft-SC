package myst.synthetic;

import myst.synthetic.client.gui.LinkBookScreen;
import myst.synthetic.client.render.SlantBoardBlockEntityRenderer;
import myst.synthetic.client.render.StarFissureBlockEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import myst.synthetic.client.render.SlantBoardRenderPipelines;

public class MystcraftSyntheticCodexClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {

		BlockEntityRenderers.register(MystcraftBlockEntities.STAR_FISSURE, StarFissureBlockEntityRenderer::new);
		BlockEntityRenderers.register(MystcraftBlockEntities.SLANT_BOARD, SlantBoardBlockEntityRenderer::new);

		SlantBoardRenderPipelines.warmUpTextures();


		LinkBookClientBridge.OPENER = stack -> Minecraft.getInstance().setScreen(new LinkBookScreen(stack));
	}
}