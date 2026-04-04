package myst.synthetic.client.render;

import myst.synthetic.block.entity.DisplayContentType;
import myst.synthetic.block.property.WoodType;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemStack;

public class BookstandRenderState extends BlockEntityRenderState {
    public int rotationIndex = 0;
    public WoodType wood = WoodType.OAK;
    public int packedLight = 0;

    public final ItemStackRenderState displayedItem = new ItemStackRenderState();
    public ItemStack displayedStack = ItemStack.EMPTY;
    public DisplayContentType contentType = DisplayContentType.EMPTY;
    public boolean hasDisplayItem = false;
}