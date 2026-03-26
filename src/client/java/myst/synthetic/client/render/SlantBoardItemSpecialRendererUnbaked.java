package myst.synthetic.client.render;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.special.SpecialModelRenderer;

public record SlantBoardItemSpecialRendererUnbaked() implements SpecialModelRenderer.Unbaked {

    public static final SlantBoardItemSpecialRendererUnbaked INSTANCE =
            new SlantBoardItemSpecialRendererUnbaked();

    public static final MapCodec<SlantBoardItemSpecialRendererUnbaked> MAP_CODEC =
            MapCodec.unit(INSTANCE);

    @Override
    public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext bakingContext) {
        return new SlantBoardItemSpecialRenderer();
    }

    @Override
    public MapCodec<? extends SpecialModelRenderer.Unbaked> type() {
        return MAP_CODEC;
    }
}