package myst.synthetic;

import myst.synthetic.client.gui.InkMixerScreen;
import myst.synthetic.client.gui.LinkBookScreen;
import myst.synthetic.client.render.BookstandBlockEntityRenderer;
import myst.synthetic.client.render.SlantBoardBlockEntityRenderer;
import myst.synthetic.client.render.StarFissureBlockEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import myst.synthetic.client.render.LinkbookEntityRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import myst.synthetic.component.MystcraftDataComponents;
import net.fabricmc.fabric.api.item.v1.ComponentTooltipAppenderRegistry;
import net.minecraft.core.component.DataComponents;
import myst.synthetic.client.page.PagePreviewExporter;
import myst.synthetic.config.MystcraftConfig;
import myst.synthetic.MystcraftFluids;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.resources.Identifier;

public class MystcraftSyntheticCodexClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ComponentTooltipAppenderRegistry.addAfter(DataComponents.DAMAGE, MystcraftDataComponents.PAGE_DATA);

		BlockEntityRenderers.register(MystcraftBlockEntities.STAR_FISSURE, StarFissureBlockEntityRenderer::new);
		BlockEntityRenderers.register(MystcraftBlockEntities.SLANT_BOARD, SlantBoardBlockEntityRenderer::new);
		BlockEntityRenderers.register(MystcraftBlockEntities.BOOKSTAND, BookstandBlockEntityRenderer::new);

		EntityRenderers.register(MystcraftEntities.LINKBOOK, LinkbookEntityRenderer::new);

		BlockRenderLayerMap.putBlock(MystcraftBlocks.INK_MIXER_BLOCK, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(MystcraftBlocks.LINK_MODIFIER_BLOCK, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(MystcraftBlocks.BOOK_BINDER_BLOCK, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(MystcraftBlocks.BOOK_RECEPTACLE_BLOCK, ChunkSectionLayer.CUTOUT);

		FluidRenderHandlerRegistry.INSTANCE.register(
				MystcraftFluids.BLACK_INK,
				MystcraftFluids.FLOWING_BLACK_INK,
				new SimpleFluidRenderHandler(
						Identifier.fromNamespaceAndPath("mystcraft-sc", "block/fluid"),
						Identifier.fromNamespaceAndPath("mystcraft-sc", "block/fluid_flow"),
						0xFF191919
				)
		);

		BlockRenderLayerMap.putFluids(
				ChunkSectionLayer.TRANSLUCENT,
				MystcraftFluids.BLACK_INK,
				MystcraftFluids.FLOWING_BLACK_INK
		);

		MenuScreens.register(MystcraftMenus.INK_MIXER, InkMixerScreen::new);

		if (MystcraftConfig.getBoolean(MystcraftConfig.CATEGORY_DEBUG, "pages.export_previews", false)) {
			PagePreviewExporter.exportAllOnce();
		}

		LinkBookClientBridge.OPENER = stack -> Minecraft.getInstance().setScreen(new LinkBookScreen(stack));
	}
}