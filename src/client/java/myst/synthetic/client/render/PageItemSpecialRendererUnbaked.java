package myst.synthetic.client.render;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.special.SpecialModelRenderer;

public record PageItemSpecialRendererUnbaked() implements SpecialModelRenderer.Unbaked {

    public static final PageItemSpecialRendererUnbaked INSTANCE =
            new PageItemSpecialRendererUnbaked();

    public static final MapCodec<PageItemSpecialRendererUnbaked> MAP_CODEC =
            MapCodec.unit(INSTANCE);

    @Override
    public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext bakingContext) {
        return new PageItemSpecialRenderer();
    }

    @Override
    public MapCodec<? extends SpecialModelRenderer.Unbaked> type() {
        return MAP_CODEC;
    }
}