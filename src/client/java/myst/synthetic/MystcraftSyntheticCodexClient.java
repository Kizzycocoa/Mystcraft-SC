package myst.synthetic;

import myst.synthetic.block.entity.BlockEntityBookReceptacle;
import myst.synthetic.client.gui.FolderScreen;
import myst.synthetic.client.gui.InkMixerScreen;
import myst.synthetic.client.gui.LinkBookScreen;
import myst.synthetic.client.gui.PortfolioScreen;
import myst.synthetic.client.gui.SingleSlotScreen;
import myst.synthetic.client.page.PagePreviewExporter;
import myst.synthetic.client.page.PageRenderCache;
import myst.synthetic.client.render.BookReceptacleBlockEntityRenderer;
import myst.synthetic.client.render.BookstandBlockEntityRenderer;
import myst.synthetic.client.render.InkScreenOverlay;
import myst.synthetic.client.render.LinkbookEntityRenderer;
import myst.synthetic.client.render.SlantBoardBlockEntityRenderer;
import myst.synthetic.client.render.StarFissureBlockEntityRenderer;
import myst.synthetic.component.MystcraftDataComponents;
import myst.synthetic.config.MystcraftConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.item.v1.ComponentTooltipAppenderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MystcraftSyntheticCodexClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ComponentTooltipAppenderRegistry.addAfter(DataComponents.DAMAGE, MystcraftDataComponents.PAGE_DATA);
		ComponentTooltipAppenderRegistry.addAfter(DataComponents.CUSTOM_NAME, MystcraftDataComponents.FOLDER_DATA);
		ComponentTooltipAppenderRegistry.addAfter(DataComponents.CUSTOM_NAME, MystcraftDataComponents.PORTFOLIO_DATA);

		BlockEntityRenderers.register(MystcraftBlockEntities.STAR_FISSURE, StarFissureBlockEntityRenderer::new);
		BlockEntityRenderers.register(MystcraftBlockEntities.SLANT_BOARD, SlantBoardBlockEntityRenderer::new);
		BlockEntityRenderers.register(MystcraftBlockEntities.BOOKSTAND, BookstandBlockEntityRenderer::new);

		EntityRenderers.register(MystcraftEntities.LINKBOOK, LinkbookEntityRenderer::new);

		BlockRenderLayerMap.putBlock(MystcraftBlocks.INK_MIXER_BLOCK, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(MystcraftBlocks.LINK_MODIFIER_BLOCK, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(MystcraftBlocks.BOOK_BINDER_BLOCK, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(MystcraftBlocks.BOOK_RECEPTACLE_BLOCK, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(MystcraftBlocks.LINK_PORTAL, ChunkSectionLayer.TRANSLUCENT);

		ColorProviderRegistry.BLOCK.register(
				(state, view, pos, tintIndex) -> {
					if (tintIndex != 0) {
						return 0xFFFFFF;
					}
					if (view == null || pos == null) {
						return 0xFFFFFF;
					}

					BlockEntity blockEntity = view.getBlockEntity(pos);
					if (blockEntity instanceof BlockEntityBookReceptacle receptacle) {
						return receptacle.getPortalColor();
					}

					BlockPos ownerPos = findPortalOwnerPos(view, pos);
					if (ownerPos == null) {
						return 0xFFFFFF;
					}

					BlockEntity ownerEntity = view.getBlockEntity(ownerPos);
					if (ownerEntity instanceof BlockEntityBookReceptacle receptacle) {
						return receptacle.getPortalColor();
					}

					return 0xFFFFFF;
				},
				MystcraftBlocks.LINK_PORTAL
		);

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
		MenuScreens.register(MystcraftMenus.DISPLAY_CONTAINER, SingleSlotScreen::new);
		MenuScreens.register(MystcraftMenus.FOLDER, FolderScreen::new);
		MenuScreens.register(MystcraftMenus.PORTFOLIO, PortfolioScreen::new);

		BlockEntityRenderers.register(
				MystcraftBlockEntities.BOOK_RECEPTACLE,
				BookReceptacleBlockEntityRenderer::new
		);
		InkScreenOverlay.initialize();

		ClientTickEvents.END_CLIENT_TICK.register(new ClientTickEvents.EndTick() {
			private boolean done = false;

			@Override
			public void onEndTick(Minecraft client) {
				if (done) {
					return;
				}

				if (client.level == null && client.screen == null) {
					return;
				}

				PageRenderCache.prewarmAll();
				done = true;
			}
		});

		if (MystcraftConfig.getBoolean(MystcraftConfig.CATEGORY_DEBUG, "pages.export_previews", false)) {
			PagePreviewExporter.exportAllOnce();
		}

		LinkBookClientBridge.OPENER = stack -> Minecraft.getInstance().setScreen(new LinkBookScreen(stack));
	}

	private static BlockPos findPortalOwnerPos(BlockAndTintGetter view, BlockPos startPos) {
		BlockPos current = startPos;
		int maxSteps = 256;

		for (int i = 0; i < maxSteps; i++) {
			BlockEntity blockEntity = view.getBlockEntity(current);
			if (blockEntity instanceof BlockEntityBookReceptacle) {
				return current;
			}

			var state = view.getBlockState(current);

			if (state.is(MystcraftBlocks.LINK_PORTAL)) {
				current = current.relative(state.getValue(myst.synthetic.block.BlockLinkPortal.SOURCE_DIRECTION));
				continue;
			}

			if (state.is(MystcraftBlocks.CRYSTAL)) {
				current = current.relative(state.getValue(myst.synthetic.block.BlockCrystal.SOURCE_DIRECTION));
				continue;
			}

			return null;
		}

		return null;
	}
}