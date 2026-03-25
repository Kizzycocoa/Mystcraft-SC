package myst.synthetic.client.render;

import myst.synthetic.block.property.WoodType;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class SlantBoardRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public WoodType wood = WoodType.OAK;
    public int selfLight = 0;
    public int northLight = 0;
    public int southLight = 0;
    public int eastLight = 0;
    public int westLight = 0;
    public int downLight = 0;
}