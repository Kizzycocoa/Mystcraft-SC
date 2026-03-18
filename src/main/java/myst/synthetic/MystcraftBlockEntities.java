package myst.synthetic;

import myst.synthetic.block.entity.StarFissureBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class MystcraftBlockEntities {

    public static final BlockEntityType<StarFissureBlockEntity> STAR_FISSURE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath("mystcraft-sc", "blockstarfissure"),
            FabricBlockEntityTypeBuilder.create(StarFissureBlockEntity::new, MystcraftBlocks.STARFISSURE).build()
    );
    public static final BlockEntityType<BlockEntityDesk> WRITING_DESK = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            id("writingdesk"),
            BlockEntityType.Builder.of(BlockEntityDesk::new, ModBlocks.WRITING_DESK_BLOCK).build(null)
    );

    public static void initialize() {
    }
}