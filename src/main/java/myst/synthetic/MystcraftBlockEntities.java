package myst.synthetic;

import myst.synthetic.block.entity.BlockEntityDesk;
import myst.synthetic.block.entity.StarFissureBlockEntity;
import myst.synthetic.block.entity.BlockEntitySlantBoard;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;
import myst.synthetic.block.entity.BlockEntityBookstand;

public final class MystcraftBlockEntities {

    public static final BlockEntityType<StarFissureBlockEntity> STAR_FISSURE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath("mystcraft-sc", "blockstarfissure"),
            FabricBlockEntityTypeBuilder.create(StarFissureBlockEntity::new, MystcraftBlocks.STARFISSURE).build()
    );

    public static final BlockEntityType<BlockEntityDesk> WRITING_DESK = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath("mystcraft-sc", "writingdesk"),
            FabricBlockEntityTypeBuilder.create(BlockEntityDesk::new, MystcraftBlocks.WRITING_DESK_BLOCK).build()
    );

    public static final BlockEntityType<BlockEntitySlantBoard> SLANT_BOARD = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath("mystcraft-sc", "slant_board"),
            FabricBlockEntityTypeBuilder.create(BlockEntitySlantBoard::new, MystcraftBlocks.SLANT_BOARD_BLOCK).build()
    );

    public static final BlockEntityType<BlockEntityBookstand> BOOKSTAND = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath("mystcraft-sc", "bookstand"),
            FabricBlockEntityTypeBuilder.create(BlockEntityBookstand::new, MystcraftBlocks.BOOKSTAND_BLOCK).build()
    );

    public static void initialize() {
    }
}