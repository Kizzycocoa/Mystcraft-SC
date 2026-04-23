package myst.synthetic.world.terrain;

import net.minecraft.world.level.block.state.BlockState;

public record FlatTerrainSettings(
        int groundLevel,
        BlockState fillBlock,
        BlockState fluidBlock,
        int seaLevel,
        BedrockProfile bedrockProfile
) {

    public FlatTerrainSettings {
        groundLevel = Math.max(1, groundLevel);
        seaLevel = Math.max(groundLevel, seaLevel);
    }
}