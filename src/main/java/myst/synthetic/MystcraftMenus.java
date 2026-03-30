package myst.synthetic;

import myst.synthetic.menu.InkMixerMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class MystcraftMenus {

    public static final MenuType<InkMixerMenu> INK_MIXER = Registry.register(
            BuiltInRegistries.MENU,
            Identifier.fromNamespaceAndPath("mystcraft-sc", "ink_mixer"),
            new MenuType<>(InkMixerMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );

    private MystcraftMenus() {
    }

    public static void initialize() {
    }
}