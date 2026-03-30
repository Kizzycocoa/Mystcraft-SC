package myst.synthetic;

import myst.synthetic.client.gui.LinkBookScreen;
import myst.synthetic.client.render.BookstandBlockEntityRenderer;
import myst.synthetic.client.render.SlantBoardBlockEntityRenderer;
import myst.synthetic.client.render.StarFissureBlockEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import myst.synthetic.client.render.LinkbookEntityRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public class MystcraftSyntheticCodexClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		BlockEntityRenderers.register(MystcraftBlockEntities.STAR_FISSURE, StarFissureBlockEntityRenderer::new);
		BlockEntityRenderers.register(MystcraftBlockEntities.SLANT_BOARD, SlantBoardBlockEntityRenderer::new);
		BlockEntityRenderers.register(MystcraftBlockEntities.BOOKSTAND, BookstandBlockEntityRenderer::new);

		EntityRenderers.register(MystcraftEntities.LINKBOOK, LinkbookEntityRenderer::new);

		BlockRenderLayerMap.putBlock(MystcraftBlocks.INK_MIXER_BLOCK, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(MystcraftBlocks.LINK_MODIFIER_BLOCK, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(MystcraftBlocks.BOOK_BINDER_BLOCK, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(MystcraftBlocks.BOOK_RECEPTACLE_BLOCK, ChunkSectionLayer.CUTOUT);

		LinkBookClientBridge.OPENER = stack -> Minecraft.getInstance().setScreen(new LinkBookScreen(stack));
	}
}