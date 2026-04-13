package myst.synthetic;

import java.util.function.Function;

import myst.synthetic.component.MystcraftDataComponents;
import myst.synthetic.component.PageDataComponent;
import myst.synthetic.component.PortfolioDataComponent;
import myst.synthetic.item.ItemFolder;
import myst.synthetic.item.ItemPortfolio;
import myst.synthetic.item.ItemInkVial;
import myst.synthetic.item.ItemLinkbook;
import myst.synthetic.item.ItemPage;
import myst.synthetic.item.ItemUnlinkedLinkbook;
import myst.synthetic.item.ItemWritingDeskTop;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.core.component.DataComponents;
import myst.synthetic.item.ItemBookmark;

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

    public static final Item AGEBOOK = register("agebook", Item::new, new Item.Properties().stacksTo(1));
    public static final Item LINKBOOK = register("linkbook", ItemLinkbook::new, new Item.Properties().stacksTo(1));
    public static final Item UNLINKEDBOOK = register("unlinkedbook", ItemUnlinkedLinkbook::new, new Item.Properties().stacksTo(16));
    public static final Item BOOSTER = register("booster", Item::new, new Item.Properties());

    public static final Item FOLDER = register(
            "folder",
            ItemFolder::new,
            new Item.Properties()
                    .stacksTo(32)
                    .component(MystcraftDataComponents.FOLDER_DATA, myst.synthetic.component.FolderDataComponent.EMPTY)
    );
    public static final Item PORTFOLIO =
            register(
                    "portfolio",
                    ItemPortfolio::new,
                    new Item.Properties()
                            .stacksTo(1)
                            .component(
                                    MystcraftDataComponents.PORTFOLIO_DATA,
                                    PortfolioDataComponent.EMPTY
                            )
            );
    public static final Item VIAL = register("vial", ItemInkVial::new, new Item.Properties().stacksTo(16));
    public static final Item BOOKMARK = register(
            "bookmark",
            ItemBookmark::new,
            new Item.Properties().stacksTo(16)
    );
    public static final Item BAHRO_LEATHER = register("bahro_leather", Item::new, new Item.Properties());

    public static final Item MYST_POETRY_BANNER_PATTERN = register(
            "myst_poetry_banner_pattern",
            Item::new,
            new Item.Properties()
                    .stacksTo(1)
                    .component(DataComponents.PROVIDES_BANNER_PATTERNS, MystcraftBannerPatterns.MYST_POETRY_PATTERN_ITEM)
    );

    public static final Item MYST_NUMEROLOGY_BANNER_PATTERN = register(
            "myst_numerology_banner_pattern",
            Item::new,
            new Item.Properties()
                    .stacksTo(1)
                    .component(DataComponents.PROVIDES_BANNER_PATTERNS, MystcraftBannerPatterns.MYST_NUMEROLOGY_PATTERN_ITEM)
    );

    public static final Item GLASSES = register(
            "glasses",
            Item::new,
            new Item.Properties().humanoidArmor(MystcraftArmorMaterials.GLASSES, ArmorType.HELMET).stacksTo(1)
    );

    public static final Item WRITING_DESK_TOP = register(
            "writingdesk_top",
            ItemWritingDeskTop::new,
            new Item.Properties()
    );

    public static final Item PAGE = register(
            "page",
            ItemPage::new,
            new Item.Properties().component(MystcraftDataComponents.PAGE_DATA, PageDataComponent.EMPTY)
    );

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(LINKBOOK);
            entries.accept(FOLDER);
            entries.accept(PAGE);
            entries.accept(VIAL);
            entries.accept(MYST_POETRY_BANNER_PATTERN);
            entries.accept(MYST_NUMEROLOGY_BANNER_PATTERN);
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> {
            entries.accept(GLASSES);
        });
    }
}