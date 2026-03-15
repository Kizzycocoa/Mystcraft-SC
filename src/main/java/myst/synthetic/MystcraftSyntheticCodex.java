package myst.synthetic;

import myst.synthetic.config.MystcraftConfig;
import myst.synthetic.recipe.MystcraftRecipeSerializers;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MystcraftSyntheticCodex implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("mystcraft-sc");

	@Override
	public void onInitialize() {
		MystcraftConfig.load();

		MystcraftBlocks.initialize();
		MystcraftBlockEntities.initialize();
		MystcraftItems.initialize();
		MystcraftItemGroups.initialize();
		MystcraftRecipeSerializers.initialize();

		LOGGER.info("Mystcraft config directory: {}", MystcraftConfig.getConfigDir());
		LOGGER.info("Mystcraft: The Synthetic Codex initialized.");
	}
}