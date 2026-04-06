package myst.synthetic;

import myst.synthetic.menu.DisplayContainerMenu;
import myst.synthetic.menu.FolderMenu;
import myst.synthetic.menu.InkMixerMenu;
import myst.synthetic.menu.PortfolioMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class MystcraftMenus {

    public static final MenuType<DisplayContainerMenu> DISPLAY_CONTAINER = Registry.register(
            BuiltInRegistries.MENU,
            Identifier.fromNamespaceAndPath("mystcraft-sc", "display_container"),
            new MenuType<>(DisplayContainerMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );

    public static final MenuType<InkMixerMenu> INK_MIXER = Registry.register(
            BuiltInRegistries.MENU,
            Identifier.fromNamespaceAndPath("mystcraft-sc", "ink_mixer"),
            new MenuType<>(InkMixerMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );

    public static final MenuType<FolderMenu> FOLDER = Registry.register(
            BuiltInRegistries.MENU,
            Identifier.fromNamespaceAndPath("mystcraft-sc", "folder"),
            new MenuType<>(FolderMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );

    public static final MenuType<PortfolioMenu> PORTFOLIO = Registry.register(
            BuiltInRegistries.MENU,
            Identifier.fromNamespaceAndPath("mystcraft-sc", "portfolio"),
            new MenuType<>(PortfolioMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );

    private MystcraftMenus() {
    }

    public static void initialize() {
    }
}