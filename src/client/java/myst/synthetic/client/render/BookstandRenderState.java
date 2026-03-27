package myst.synthetic.client.render;

import myst.synthetic.block.property.WoodType;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class BookstandRenderState extends BlockEntityRenderState {
    public int rotationIndex = 0;
    public WoodType wood = WoodType.OAK;
    public int packedLight = 0;
}