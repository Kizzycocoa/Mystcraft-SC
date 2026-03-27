package myst.synthetic.client.render;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.special.SpecialModelRenderer;

public record BookstandItemSpecialRendererUnbaked() implements SpecialModelRenderer.Unbaked {

    public static final BookstandItemSpecialRendererUnbaked INSTANCE =
            new BookstandItemSpecialRendererUnbaked();

    public static final MapCodec<BookstandItemSpecialRendererUnbaked> MAP_CODEC =
            MapCodec.unit(INSTANCE);

    @Override
    public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext bakingContext) {
        return new BookstandItemSpecialRenderer();
    }

    @Override
    public MapCodec<? extends SpecialModelRenderer.Unbaked> type() {
        return MAP_CODEC;
    }
}