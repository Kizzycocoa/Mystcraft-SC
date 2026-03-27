package myst.synthetic;

import java.util.stream.Collectors;

import myst.synthetic.block.BlockWritingDesk;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;

public final class MystcraftPoiTypes {

    public static final Identifier ARCHIVIST_DESK_ID =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "archivist_desk");

    public static final ResourceKey<PoiType> ARCHIVIST_DESK_KEY =
            ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, ARCHIVIST_DESK_ID);

    public static final PoiType ARCHIVIST_DESK = PointOfInterestHelper.register(
            ARCHIVIST_DESK_ID,
            1,
            1,
            MystcraftBlocks.WRITING_DESK_BLOCK.getStateDefinition().getPossibleStates().stream()
                    .filter(BlockWritingDesk::isAnchor)
                    .collect(Collectors.toSet())
    );

    private MystcraftPoiTypes() {
    }

    public static void initialize() {
    }
}