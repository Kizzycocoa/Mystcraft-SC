package myst.synthetic.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;

import myst.synthetic.menu.InkMixerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.client.input.MouseButtonEvent;

public class InkMixerScreen extends AbstractContainerScreen<InkMixerMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/inkmixer.png");

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
        // Basin backing first
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                this.leftPos + BASIN_X,
                this.topPos + BASIN_Y,
                179,
                16,
                BASIN_W,
                BASIN_H,
                256,
                256
        );

        // Ink / mixed fill second
        if (this.menu.hasInk()) {
            int fillColor = this.menu.getStoredPropertyCount() > 0 ? 0xFF181830 : 0xFF101018;
            guiGraphics.fill(
                    this.leftPos + BASIN_X,
                    this.topPos + BASIN_Y,
                    this.leftPos + BASIN_X + BASIN_W,
                    this.topPos + BASIN_Y + BASIN_H,
                    fillColor
            );
        }

        // Full GUI frame last
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
            boolean wholeStack = event.button() == 0;
            boolean single = event.button() == 1;

            if (wholeStack || single) {
                if (this.menu.consumeHeldIngredient(this.minecraft.player, wholeStack)) {
                    return true;
                }
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
                    Component.translatable("screen.mystcraft-sc.ink_mixer.basin_tooltip")
                            .getVisualOrderText()
            ));

            int propertyCount = this.menu.getStoredPropertyCount();
            if (propertyCount > 0) {
                tooltip.add(new ClientTextTooltip(
                        Component.literal("Stored properties: " + propertyCount)
                                .getVisualOrderText()
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
}