package myst.synthetic;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import myst.synthetic.block.DecayType;
import myst.synthetic.block.BlockDecay;
import myst.synthetic.block.property.WoodType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import myst.synthetic.api.hook.LinkPropertyAPI;
import myst.synthetic.page.Page;
import myst.synthetic.page.symbol.PageSymbol;
import myst.synthetic.page.symbol.PageSymbolRegistry;
import myst.synthetic.block.BlockCrystal;
import myst.synthetic.block.BlockBookReceptacle;
import myst.synthetic.block.property.CrystalColor;
import net.minecraft.world.item.component.BlockItemStateProperties;

import java.util.List;

public class MystcraftItemGroups {

    private static ItemStack createWoodVariant(ItemStack stack, WoodType wood, boolean top) {

        CompoundTag tag = new CompoundTag();
        tag.putString("wood", wood.getSerializedName());

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        String modelPath = top
                ? "writingdesk_top/" + getWoodModelKey(wood)
                : "writingdesk/" + getWoodModelKey(wood);

        stack.set(
                DataComponents.ITEM_MODEL,
                Identifier.fromNamespaceAndPath("mystcraft-sc", modelPath)
        );

        return stack;
    }

    public static ItemStack createDeskVariant(WoodType wood) {
        return createWoodVariant(
                new ItemStack(MystcraftBlocks.WRITING_DESK_BLOCK),
                wood,
                false
        );
    }

    public static ItemStack createDeskTopVariant(WoodType wood) {
        return createWoodVariant(
                new ItemStack(MystcraftItems.WRITING_DESK_TOP),
                wood,
                true
        );
    }
    public static ItemStack createSlantBoardVariant(WoodType wood) {
        ItemStack stack = new ItemStack(MystcraftBlocks.SLANT_BOARD_BLOCK);

        CompoundTag tag = new CompoundTag();
        tag.putString("wood", wood.getSerializedName());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        return stack;
    }


    public static ItemStack createBookstandVariant(WoodType wood) {
        ItemStack stack = new ItemStack(MystcraftBlocks.BOOKSTAND_BLOCK);

        CompoundTag tag = new CompoundTag();
        tag.putString("wood", wood.getSerializedName());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        return stack;
    }

    public static ItemStack createCrystalVariant(CrystalColor color) {
        ItemStack stack = new ItemStack(MystcraftBlocks.CRYSTAL);
        stack.set(
                DataComponents.BLOCK_STATE,
                BlockItemStateProperties.EMPTY.with(BlockCrystal.COLOR, color)
        );
        stack.set(
                DataComponents.ITEM_NAME,
                Component.translatable("item.mystcraft-sc.crystal." + color.getSerializedName())
        );
        return stack;
    }

    public static ItemStack createBookReceptacleVariant(CrystalColor color) {
        ItemStack stack = new ItemStack(MystcraftBlocks.BOOK_RECEPTACLE_BLOCK);
        stack.set(
                DataComponents.BLOCK_STATE,
                BlockItemStateProperties.EMPTY.with(BlockBookReceptacle.COLOR, color)
        );
        stack.set(
                DataComponents.ITEM_NAME,
                Component.translatable("item.mystcraft-sc.book_receptacle." + color.getSerializedName())
        );
        return stack;
    }

    private static String getWoodModelKey(WoodType wood) {
        return switch (wood) {
            default -> wood.getSerializedName();
        };
    }

    public static final CreativeModeTab MYSTCRAFT_COMMON = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath("mystcraft-sc", "common"),
            FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup.mystcraft-sc.common"))
                    .icon(() -> new ItemStack(MystcraftItems.AGEBOOK))
                    .displayItems((parameters, output) -> {
                        output.accept(MystcraftItems.VIAL);
                        output.accept(MystcraftFluids.BLACK_INK_BUCKET);
                        output.accept(MystcraftItems.AGEBOOK);
                        output.accept(MystcraftItems.LINKBOOK);
                        output.accept(MystcraftItems.UNLINKEDBOOK);
                        output.accept(MystcraftItems.BOOKMARK);
                        output.accept(MystcraftItems.BOOSTER);
                        output.accept(MystcraftItems.FOLDER);
                        output.accept(MystcraftItems.PORTFOLIO);
                        output.accept(MystcraftItems.BAHRO_LEATHER);
                        output.accept(MystcraftItems.MYST_POETRY_BANNER_PATTERN);
                        output.accept(MystcraftItems.MYST_NUMEROLOGY_BANNER_PATTERN);
                        output.accept(MystcraftItems.GLASSES);

                        for (WoodType wood : WoodType.values()) {
                            output.accept(createDeskVariant(wood));
                        }

                        for (WoodType wood : WoodType.values()) {
                            output.accept(createDeskTopVariant(wood));
                        }

                        for (WoodType wood : WoodType.values()) {
                            output.accept(createSlantBoardVariant(wood));
                        }

                        for (WoodType wood : WoodType.values()) {
                            output.accept(createBookstandVariant(wood));
                        }

                        output.accept(MystcraftBlocks.INK_MIXER_BLOCK);
                        output.accept(MystcraftBlocks.LINK_MODIFIER_BLOCK);
                        output.accept(MystcraftBlocks.BOOK_BINDER_BLOCK);

                        for (CrystalColor color : CrystalColor.values()) {
                            output.accept(createCrystalVariant(color));
                        }
                        for (CrystalColor color : CrystalColor.values()) {
                            output.accept(createBookReceptacleVariant(color));
                        }

                        for (DecayType type : DecayType.values()) {

                            ItemStack stack = new ItemStack(MystcraftBlocks.BLOCKDECAY);

                            stack.set(
                                    net.minecraft.core.component.DataComponents.BLOCK_STATE,
                                    net.minecraft.world.item.component.BlockItemStateProperties.EMPTY
                                            .with(BlockDecay.DECAY, type)
                            );

                            output.accept(stack);
                        }
                        output.accept(MystcraftBlocks.STARFISSURE);
                    })
                    .build()
    );


    public static final CreativeModeTab MYSTCRAFT_PAGES = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath("mystcraft-sc", "pages"),
            FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup.mystcraft-sc.pages"))
                    .icon(() -> new ItemStack(MystcraftItems.AGEBOOK)) // swap to AGEBOOK later if desired
                    .displayItems((parameters, output) -> {
                        output.accept(Page.createPage());
                        output.accept(Page.createLinkPage());
                        output.accept(Page.createLinkPage(LinkPropertyAPI.FLAG_DISARM));
                        output.accept(Page.createLinkPage(LinkPropertyAPI.FLAG_GENERATE_PLATFORM));
                        output.accept(Page.createLinkPage(LinkPropertyAPI.FLAG_INTRA_LINKING));
                        output.accept(Page.createLinkPage(LinkPropertyAPI.FLAG_INTRA_LINKING_ONLY));
                        output.accept(Page.createLinkPage(LinkPropertyAPI.FLAG_MAINTAIN_MOMENTUM));

                        for (PageSymbol symbol : PageSymbolRegistry.values()) {
                            output.accept(Page.createSymbolPage(symbol.id()));
                        }
                    })
                    .build()
    );

    public static void initialize() {
    }
}