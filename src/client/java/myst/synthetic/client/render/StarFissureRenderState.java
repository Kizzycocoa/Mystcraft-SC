package myst.synthetic.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class StarFissureRenderState extends BlockEntityRenderState {
    public float animationTime;
    public float seedOffset;

    // Closest CPU-side equivalent to the old 1.12 wall-clock scroll source.
    public float legacyTimeScroll;

    // Future-facing parity fields.
    // These are not all consumed yet, but they give the renderer/shader path
    // a clean place to carry explicit data instead of guessing later.
    public int topFaceIndex = 0;
    public int bottomFaceIndex = 1;

    public float cameraPosX;
    public float cameraPosY;
    public float cameraPosZ;

    public float time;
}