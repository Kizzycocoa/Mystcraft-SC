package myst.synthetic.world.terrain;

import myst.synthetic.world.age.AgeSpec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class FlatTerrainResolver {

    public FlatTerrainSettings resolve(AgeSpec spec, RegistryAccess registryAccess) {
        Identifier biomeId = spec.resolvedBiome();

        int groundLevel = resolveGroundLevel(biomeId);
        int seaLevel = Math.max(groundLevel, 63);

        BlockState fillBlock = Blocks.STONE.defaultBlockState();
        BlockState fluidBlock = Blocks.WATER.defaultBlockState();

        return new FlatTerrainSettings(
                groundLevel,
                fillBlock,
                fluidBlock,
                seaLevel,
                spec.bedrockProfile()
        );
    }

    private int resolveGroundLevel(Identifier biomeId) {
        if (biomeId == null) {
            return 64;
        }

        String path = biomeId.getPath();

        if (path.contains("deep_dark")) {
            return 48;
        }

        if (path.contains("ocean")) {
            return 48;
        }

        if (path.contains("river")) {
            return 58;
        }

        if (path.contains("desert")) {
            return 64;
        }

        if (path.contains("badlands")) {
            return 68;
        }

        if (path.contains("mountain") || path.contains("peak") || path.contains("slopes")) {
            return 96;
        }

        if (path.contains("grove")) {
            return 72;
        }

        return 64;
    }
}