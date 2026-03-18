package myst.synthetic;

import java.util.function.Function;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.ArmorMaterials;

public class MystcraftItems {

    public static <T extends Item> T register(String name, Function<Item.Properties, T> itemFactory, Item.Properties settings) {
        ResourceKey<Item> itemKey = ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath("mystcraft-sc", name)
        );

        T item = itemFactory.apply(settings.setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        return item;
    }

    // Common tab items
    public static final Item AGEBOOK = register("agebook", Item::new, new Item.Properties());
    public static final Item LINKBOOK = register("linkbook", Item::new, new Item.Properties());
    public static final Item UNLINKEDBOOK = register("unlinkedbook", Item::new, new Item.Properties());
    public static final Item BOOSTER = register("booster", Item::new, new Item.Properties());
    public static final Item FOLDER = register("folder", Item::new, new Item.Properties());
    public static final Item PORTFOLIO = register("portfolio", Item::new, new Item.Properties());
    public static final Item VIAL = register("vial", Item::new, new Item.Properties().stacksTo(16));
    public static final Item GLASSES = register("glasses", Item::new, new Item.Properties().humanoidArmor(MystcraftArmorMaterials.GLASSES, ArmorType.HELMET).stacksTo(1));
    // public static final Item PAGE = register("page", Item::new, new Item.Properties());
    public static final Item WRITING_DESK = register(
            "writingdesk",
            new ItemWritingDesk()
    );

    public static final Item WRITING_DESK_TOP = register(
            "writingdesk_top",
            new ItemWritingDeskTop()
    );

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(LINKBOOK);
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> {
            entries.accept(GLASSES);
        });
    }
}