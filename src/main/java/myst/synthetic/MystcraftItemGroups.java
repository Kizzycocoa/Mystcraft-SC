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
import net.minecraft.world.level.block.state.BlockState;
import myst.synthetic.block.BlockWritingDesk;
import myst.synthetic.block.property.WoodType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;

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
                        output.accept(MystcraftItems.AGEBOOK);
                        output.accept(MystcraftItems.LINKBOOK);
                        output.accept(MystcraftItems.UNLINKEDBOOK);
                        output.accept(MystcraftItems.BOOSTER);
                        output.accept(MystcraftItems.FOLDER);
                        output.accept(MystcraftItems.PORTFOLIO);
                        output.accept(MystcraftItems.GLASSES);
                        for (WoodType wood : WoodType.values()) {
                            output.accept(createDeskVariant(wood));
                        }

                        for (WoodType wood : WoodType.values()) {
                            output.accept(createDeskTopVariant(wood));
                        }

                        for (WoodType wood : WoodType.values()) {

                            ItemStack stack = new ItemStack(MystcraftBlocks.SLANT_BOARD_BLOCK);

                            CompoundTag tag = new CompoundTag();
                            tag.putString("wood", wood.getSerializedName());

                            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                            output.accept(stack);
                        }

                        output.accept(MystcraftBlocks.CRYSTAL);
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
                        output.accept(MystcraftItems.PAGE);
                    })
                    .build()
    );

    public static void initialize() {
    }
}