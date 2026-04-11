package myst.synthetic.client.render;

import myst.synthetic.block.entity.DisplayContentType;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.AttachFace;

public class BookReceptacleRenderState extends BlockEntityRenderState {
    public AttachFace face = AttachFace.WALL;
    public Direction facing = Direction.SOUTH;
    public boolean hasBook = false;
    public DisplayContentType contentType = DisplayContentType.EMPTY;
    public ItemStack displayedStack = ItemStack.EMPTY;
    public int packedLight = 0;
}