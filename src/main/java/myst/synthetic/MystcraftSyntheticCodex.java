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

import myst.synthetic.page.symbol.DatapackPageLoader;

public class MystcraftSyntheticCodex implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("mystcraft-sc");

	@Override
	public void onInitialize() {
		MystcraftConfig.load();

		MystcraftDataComponents.initialize();

		MystcraftGlyphComponents.initialize();
		MystcraftPageWords.initialize();
		MystcraftPageSymbols.initialize();

		DatapackPageLoader.initialize();

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
	}
}