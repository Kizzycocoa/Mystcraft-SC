package myst.synthetic.client.gui;

import myst.synthetic.menu.InkMixerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class InkMixerScreen extends AbstractContainerScreen<InkMixerMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/inkmixer.png");

    public InkMixerScreen(InkMixerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 181;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
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

        if (this.menu.hasInk()) {
            guiGraphics.fill(
                    this.leftPos + 54,
                    this.topPos + 16,
                    this.leftPos + 120,
                    this.topPos + 81,
                    0xAA101018
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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}