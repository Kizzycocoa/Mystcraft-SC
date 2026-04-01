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

    public static final FlowingFluid BLACK_INK = Registry.register(
            BuiltInRegistries.FLUID,
            Identifier.fromNamespaceAndPath("mystcraft-sc", "black_ink"),
            new InkFluid.Still()
    );

    public static final FlowingFluid FLOWING_BLACK_INK = Registry.register(
            BuiltInRegistries.FLUID,
            Identifier.fromNamespaceAndPath("mystcraft-sc", "flowing_black_ink"),
            new InkFluid.Flowing()
    );

    public static final Block BLACK_INK_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("mystcraft-sc", "black_ink_block")),
            new LiquidBlock(
                    BLACK_INK,
                    BlockBehaviour.Properties.of()
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
            ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("mystcraft-sc", "ink_bucket")),
            new BucketItem(
                    BLACK_INK,
                    new Item.Properties()
                            .stacksTo(1)
                            .craftRemainder(Items.BUCKET)
            )
    );

    private MystcraftFluids() {
    }

    public static void initialize() {
    }
}