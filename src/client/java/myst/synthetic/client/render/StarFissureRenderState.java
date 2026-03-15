package myst.synthetic.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;

public class StarFissureRenderState extends BlockEntityRenderState {
    public float animationTime;
    public float seedOffset;
    public BlockPos debugPos = BlockPos.ZERO;
}