package myst.synthetic;

import myst.synthetic.client.gui.LinkBookScreen;
import myst.synthetic.client.render.StarFissureBlockEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class MystcraftSyntheticCodexClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		BlockEntityRenderers.register(MystcraftBlockEntities.STAR_FISSURE, StarFissureBlockEntityRenderer::new);

		LinkBookClientBridge.OPENER = stack -> Minecraft.getInstance().setScreen(new LinkBookScreen(stack));
	}
}