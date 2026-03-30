package myst.synthetic.client.gui;

import java.util.ArrayList;
import java.util.List;

import myst.synthetic.client.render.DniColorRenderer;
import myst.synthetic.menu.InkMixerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class InkMixerScreen extends AbstractContainerScreen<InkMixerMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/inkmixer.png");
    private static final Identifier FLUID_TEXTURE =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/block/fluid.png");

    private static final int BASIN_X = 54;
    private static final int BASIN_Y = 16;
    private static final int BASIN_W = 66;
    private static final int BASIN_H = 65;

    public InkMixerScreen(InkMixerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 181;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int basinLeft = this.leftPos + BASIN_X;
        int basinTop = this.topPos + BASIN_Y;

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                basinLeft,
                basinTop,
                179,
                16,
                BASIN_W,
                BASIN_H,
                256,
                256
        );

        if (this.menu.hasInk()) {
            renderInkBasin(guiGraphics, basinLeft, basinTop);
        }

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                this.leftPos,
                this.topPos,
                0,
                0,
                this.imageWidth,
                this.imageHeight,
                256,
                256
        );
    }

    private void renderInkBasin(GuiGraphics guiGraphics, int x, int y) {
        int mixedRgb = this.menu.getMixedColorRgb();
        int overlayAlpha = this.menu.getMixedOverlayAlpha();

        renderFluidTexture(guiGraphics, x, y, BASIN_W, BASIN_H);

        // Darken the fluid into black ink while still preserving the texture highlights.
        guiGraphics.fill(x, y, x + BASIN_W, y + BASIN_H, 0xB0000000);
        guiGraphics.fill(x, y, x + BASIN_W, y + BASIN_H, 0x70000000);

        if (overlayAlpha > 0) {
            int topColor = (Math.max(40, overlayAlpha / 2) << 24) | brighten(mixedRgb, 0.10F);
            int bottomColor = (Math.min(255, overlayAlpha + 70) << 24) | darken(mixedRgb, 0.08F);

            guiGraphics.fillGradient(
                    x,
                    y,
                    x + BASIN_W,
                    y + BASIN_H,
                    topColor,
                    bottomColor
            );

            DniColorRenderer.render(
                    guiGraphics,
                    mixedRgb,
                    x + (BASIN_W / 2) + 1,
                    y + (BASIN_H / 2) + 1,
                    20
            );
        }
    }

    private void renderFluidTexture(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        for (int drawX = 0; drawX < width; drawX += 16) {
            int w = Math.min(16, width - drawX);
            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    FLUID_TEXTURE,
                    x + drawX,
                    y,
                    0,
                    0,
                    w,
                    height,
                    16,
                    64
            );
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);

        Component basinText = this.menu.hasInk()
                ? Component.translatable("screen.mystcraft-sc.ink_mixer.basin_full")
                : Component.translatable("screen.mystcraft-sc.ink_mixer.basin_empty");
        guiGraphics.drawString(this.font, basinText, 54, 6, 0x404040, false);

        int propertyCount = this.menu.getStoredPropertyCount();
        if (propertyCount > 0) {
            guiGraphics.drawString(this.font, "Properties: " + propertyCount, 54, 84, 0x404040, false);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.isMouseOverBasin(event.x(), event.y())) {
            int buttonId = -1;

            if (event.button() == 0) {
                buttonId = 1;
            } else if (event.button() == 1) {
                buttonId = 0;
            }

            if (buttonId >= 0 && this.minecraft != null && this.minecraft.gameMode != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (this.isMouseOverBasin(mouseX, mouseY)) {
            List<ClientTooltipComponent> tooltip = new ArrayList<>();

            tooltip.add(new ClientTextTooltip(
                    Component.translatable("screen.mystcraft-sc.ink_mixer.basin_tooltip").getVisualOrderText()
            ));

            int propertyCount = this.menu.getStoredPropertyCount();
            if (propertyCount > 0) {
                tooltip.add(new ClientTextTooltip(
                        Component.literal("Stored properties: " + propertyCount).getVisualOrderText()
                ));
            }

            guiGraphics.renderTooltip(
                    this.font,
                    tooltip,
                    mouseX,
                    mouseY,
                    DefaultTooltipPositioner.INSTANCE,
                    null
            );
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private boolean isMouseOverBasin(double mouseX, double mouseY) {
        return mouseX >= this.leftPos + BASIN_X
                && mouseX < this.leftPos + BASIN_X + BASIN_W
                && mouseY >= this.topPos + BASIN_Y
                && mouseY < this.topPos + BASIN_Y + BASIN_H;
    }

    private static int brighten(int rgb, float amount) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        r = clamp255((int)(r + ((255 - r) * amount)));
        g = clamp255((int)(g + ((255 - g) * amount)));
        b = clamp255((int)(b + ((255 - b) * amount)));

        return (r << 16) | (g << 8) | b;
    }

    private static int darken(int rgb, float amount) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        r = clamp255((int)(r * (1.0F - amount)));
        g = clamp255((int)(g * (1.0F - amount)));
        b = clamp255((int)(b * (1.0F - amount)));

        return (r << 16) | (g << 8) | b;
    }

    private static int clamp255(int value) {
        return Math.max(0, Math.min(255, value));
    }
}