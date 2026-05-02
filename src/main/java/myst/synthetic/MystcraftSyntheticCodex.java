package myst.synthetic;

import myst.synthetic.config.MystcraftConfig;
import myst.synthetic.recipe.MystcraftRecipeSerializers;
import myst.synthetic.world.StructurePoolAdder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import myst.synthetic.component.MystcraftDataComponents;
import myst.synthetic.page.symbol.MystcraftPageSymbols;
import myst.synthetic.page.word.MystcraftPageWords;
import myst.synthetic.page.glyph.MystcraftGlyphComponents;
import myst.synthetic.ink.InkFluidInteractions;
import myst.synthetic.ink.InkStatusEffects;
import myst.synthetic.page.symbol.source.MystcraftGeneratedPageSymbols;
import myst.synthetic.item.MystcraftCauldronInteractions;
import myst.synthetic.world.dimension.PendingAgeTeleportManager;
import myst.synthetic.page.symbol.DatapackPageLoader;
import myst.synthetic.page.loot.PageLootPools;
import myst.synthetic.command.MystcraftCommands;
import myst.synthetic.world.dimension.AgeDimensionManager;
import myst.synthetic.world.age.AgeRenderDataSynchronizer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class MystcraftSyntheticCodex implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("mystcraft-sc");

	@Override
	public void onInitialize() {
		MystcraftConfig.load();
		PendingAgeTeleportManager.initialize();
        //AgeChunkTerrainBootstrapper.initialize();

		MystcraftDataComponents.initialize();

		MystcraftGlyphComponents.initialize();
		MystcraftPageWords.initialize();
		MystcraftPageSymbols.initialize();

		DatapackPageLoader.initialize();
		PageLootPools.initialize();

		MystcraftBlocks.initialize();
		MystcraftEntities.initialize();
		MystcraftBlockEntities.initialize();
		MystcraftMenus.initialize();
		MystcraftBannerPatterns.initialize();
		MystcraftItems.initialize();
		MystcraftItemGroups.initialize();
		MystcraftRecipeSerializers.initialize();
		MystcraftCauldronInteractions.initialize();
		MystcraftNetworking.initialize();
		MystcraftCommands.initialize();
		MystcraftPoiTypes.initialize();
		MystcraftVillagerProfessions.initialize();
		MystcraftVillagerTrades.initialize();
		MystcraftFluids.initialize();
		InkFluidInteractions.initialize();
		InkStatusEffects.initialize();


		MystcraftGeneratedPageSymbols.initializeStatic();

		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			MystcraftGeneratedPageSymbols.initializeBiomes(server.registryAccess());
			StructurePoolAdder.inject(server);
			LOGGER.info("Mystcraft age registry system ready.");
		});
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			new AgeDimensionManager().bootstrapSavedAges(server);
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
				server.execute(() -> AgeRenderDataSynchronizer.sendForCurrentLevel(handler.player))
		);
	}
}