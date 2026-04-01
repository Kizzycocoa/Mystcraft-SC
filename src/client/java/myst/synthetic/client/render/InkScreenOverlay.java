package myst.synthetic.client.render;

import myst.synthetic.ink.InkExposure;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class InkScreenOverlay {

    private static final Identifier ID =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "ink_overlay");

    private static final Identifier FLUID_TEXTURE =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/block/fluid.png");

    private InkScreenOverlay() {
    }

    public static void initialize() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                ID,
                (guiGraphics, deltaTracker) -> {
                    Minecraft minecraft = Minecraft.getInstance();

                    if (minecraft.player == null || minecraft.level == null) {
                        return;
                    }

                    if (!InkExposure.isEyeInInk(minecraft.player)) {
                        return;
                    }

                    renderOverlay(
                            guiGraphics,
                            minecraft.getWindow().getGuiScaledWidth(),
                            minecraft.getWindow().getGuiScaledHeight()
                    );
                }
        );
    }

    private static void renderOverlay(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        int frame = (int) ((System.currentTimeMillis() / 120L) % 32L);
        int v = frame * 16;

        for (int x = 0; x < screenWidth; x += 16) {
            for (int y = 0; y < screenHeight; y += 16) {
                int w = Math.min(16, screenWidth - x);
                int h = Math.min(16, screenHeight - y);

                guiGraphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        FLUID_TEXTURE,
                        x,
                        y,
                        0,
                        v,
                        w,
                        h,
                        16,
                        512
                );
            }
        }

        guiGraphics.fill(0, 0, screenWidth, screenHeight, 0xCC191919);
        guiGraphics.fill(0, 0, screenWidth, screenHeight, 0x66191919);
    }
}