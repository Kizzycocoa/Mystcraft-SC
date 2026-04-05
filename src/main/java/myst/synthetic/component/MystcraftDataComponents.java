package myst.synthetic.component;

import myst.synthetic.MystcraftSyntheticCodex;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class MystcraftDataComponents {

    public static final DataComponentType<PageDataComponent> PAGE_DATA = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath("mystcraft-sc", "page_data"),
            DataComponentType.<PageDataComponent>builder()
                    .persistent(PageDataComponent.CODEC)
                    .build()
    );

    public static final DataComponentType<FolderDataComponent> FOLDER_DATA = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath("mystcraft-sc", "folder_data"),
            DataComponentType.<FolderDataComponent>builder()
                    .persistent(FolderDataComponent.CODEC)
                    .build()
    );

    private MystcraftDataComponents() {
    }

    public static void initialize() {
        MystcraftSyntheticCodex.LOGGER.info("Registering Mystcraft data components.");
    }
}