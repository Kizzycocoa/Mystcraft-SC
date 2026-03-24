package myst.synthetic.client.render;

import myst.synthetic.block.property.WoodType;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class SlantBoardRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public WoodType wood = WoodType.OAK;
    public int packedLight = 0;
}