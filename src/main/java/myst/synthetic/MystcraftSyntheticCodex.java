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

public class MystcraftSyntheticCodex implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("mystcraft-sc");

	@Override
	public void onInitialize() {
		MystcraftConfig.load();

		MystcraftDataComponents.initialize();

		MystcraftGlyphComponents.initialize();
		MystcraftPageWords.initialize();
		MystcraftPageSymbols.initialize();

		MystcraftBlocks.initialize();
		MystcraftEntities.initialize();
		MystcraftBlockEntities.initialize();
		MystcraftMenus.initialize();
		MystcraftItems.initialize();
		MystcraftItemGroups.initialize();
		MystcraftRecipeSerializers.initialize();
		MystcraftNetworking.initialize();
		MystcraftPoiTypes.initialize();
		MystcraftVillagerProfessions.initialize();
		MystcraftVillagerTrades.initialize();

		ServerLifecycleEvents.SERVER_STARTING.register(StructurePoolAdder::inject);
	}
}