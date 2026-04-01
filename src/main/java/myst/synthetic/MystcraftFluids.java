package myst.synthetic;

import myst.synthetic.fluid.InkFluid;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class MystcraftFluids {

    private static final Identifier BLACK_INK_ID =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "black_ink");

    private static final Identifier FLOWING_BLACK_INK_ID =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "flowing_black_ink");

    private static final Identifier BLACK_INK_BLOCK_ID =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "black_ink_block");

    private static final Identifier BLACK_INK_BUCKET_ID =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "ink_bucket");

    private static final ResourceKey<Block> BLACK_INK_BLOCK_KEY =
            ResourceKey.create(Registries.BLOCK, BLACK_INK_BLOCK_ID);

    private static final ResourceKey<Item> BLACK_INK_BUCKET_KEY =
            ResourceKey.create(Registries.ITEM, BLACK_INK_BUCKET_ID);

    public static final FlowingFluid BLACK_INK = Registry.register(
            BuiltInRegistries.FLUID,
            BLACK_INK_ID,
            new InkFluid.Still()
    );

    public static final FlowingFluid FLOWING_BLACK_INK = Registry.register(
            BuiltInRegistries.FLUID,
            FLOWING_BLACK_INK_ID,
            new InkFluid.Flowing()
    );

    public static final Block BLACK_INK_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            BLACK_INK_BLOCK_KEY,
            new LiquidBlock(
                    BLACK_INK,
                    BlockBehaviour.Properties.of()
                            .setId(BLACK_INK_BLOCK_KEY)
                            .mapColor(MapColor.COLOR_BLACK)
                            .replaceable()
                            .noCollision()
                            .strength(100.0F)
                            .pushReaction(PushReaction.DESTROY)
                            .noLootTable()
            )
    );

    public static final Item BLACK_INK_BUCKET = Registry.register(
            BuiltInRegistries.ITEM,
            BLACK_INK_BUCKET_KEY,
            new BucketItem(
                    BLACK_INK,
                    new Item.Properties()
                            .setId(BLACK_INK_BUCKET_KEY)
                            .stacksTo(1)
                            .craftRemainder(Items.BUCKET)
            )
    );

    private MystcraftFluids() {
    }

    public static void initialize() {
    }
}