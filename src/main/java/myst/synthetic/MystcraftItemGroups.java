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

public class MystcraftItemGroups {

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
                        output.accept(MystcraftBlocks.WRITING_DESK_BLOCK.asItem());
                        output.accept(MystcraftItems.WRITING_DESK_TOP);

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
                        // Future pages:
                        // output.accept(MystcraftItems.PAGE);
                    })
                    .build()
    );

    public static void initialize() {
    }
}