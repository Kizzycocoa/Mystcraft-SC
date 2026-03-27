package myst.synthetic;

import java.util.function.Function;

import myst.synthetic.block.BlockDecay;
import myst.synthetic.block.BlockStarFissure;
import myst.synthetic.block.BlockWritingDesk;
import myst.synthetic.item.DecayBlockItem;
import myst.synthetic.item.ItemWritingDesk;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import myst.synthetic.block.BlockSlantBoard;
import net.minecraft.world.level.block.Blocks;
import myst.synthetic.item.ItemSlantBoard;

public class MystcraftBlocks {

    public static <T extends Block> T register(
            String name,
            Function<BlockBehaviour.Properties, T> blockFactory,
            BlockBehaviour.Properties blockSettings
    ) {
        return register(name, blockFactory, blockSettings, BlockItem::new);
    }

    public static <T extends Block> T register(
            String name,
            Function<BlockBehaviour.Properties, T> blockFactory,
            BlockBehaviour.Properties blockSettings,
            BlockItemFactory itemFactory
    ) {
        Identifier id = Identifier.fromNamespaceAndPath("mystcraft-sc", name);

        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        T block = blockFactory.apply(blockSettings.setId(blockKey));
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        Item.Properties itemSettings = new Item.Properties().useBlockDescriptionPrefix().setId(itemKey);
        Registry.register(BuiltInRegistries.ITEM, itemKey, itemFactory.create(block, itemSettings));

        return block;
    }

    @FunctionalInterface
    public interface BlockItemFactory {
        BlockItem create(Block block, Item.Properties properties);
    }

    public static final Block CRYSTAL = register(
            "crystal",
            Block::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.ICE).strength(1.0F)
    );

    public static final Block BLOCKDECAY = register(
            "blockdecay",
            BlockDecay::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(0.8F),
            DecayBlockItem::new
    );

    public static final Block STARFISSURE = register(
            "blockstarfissure",
            BlockStarFissure::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(-1.0F, 3600000.0F)
                    .lightLevel(state -> 6)
                    .noCollision()
                    .noOcclusion()
    );

    public static final BlockWritingDesk WRITING_DESK_BLOCK = register(
            "writingdesk",
            BlockWritingDesk::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5F)
                    .sound(SoundType.WOOD)
                    .noOcclusion(),
            (block, properties) -> new ItemWritingDesk(block, properties.stacksTo(1))
    );
    public static final Block SLANT_BOARD_BLOCK = register(
            "slant_board",
            BlockSlantBoard::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.LECTERN),
            ItemSlantBoard::new
    );

    public static void initialize() {
    }
}