package myst.synthetic;

import myst.synthetic.config.MystcraftConfig;
import myst.synthetic.recipe.MystcraftRecipeSerializers;
import myst.synthetic.world.StructurePoolAdder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import myst.synthetic.component.MystcraftDataComponents;

public class MystcraftSyntheticCodex implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("mystcraft-sc");

	@Override
	public void onInitialize() {
		MystcraftConfig.load();

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
		MystcraftDataComponents.initialize();


		ServerLifecycleEvents.SERVER_STARTING.register(StructurePoolAdder::inject);

		LOGGER.info("Mystcraft config directory: {}", MystcraftConfig.getConfigDir());
		LOGGER.info("Mystcraft: The Synthetic Codex initialized.");
	}
}