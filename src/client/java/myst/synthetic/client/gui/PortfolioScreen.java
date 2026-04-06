package myst.synthetic.client.gui;

import myst.synthetic.menu.PortfolioMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;

public class PortfolioScreen extends AbstractContainerScreen<PortfolioMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/notebook.png");

    private static final int GRID_X = 25;
    private static final int GRID_Y = 17;
    private static final int CELL_SIZE = 18;

    private Button prevButton;
    private Button nextButton;

    public PortfolioScreen(PortfolioMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 72;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();

        this.prevButton = this.addRenderableWidget(
                Button.builder(Component.literal("<"), button -> {
                            if (this.minecraft != null && this.minecraft.gameMode != null) {
                                this.minecraft.gameMode.handleInventoryButtonClick(
                                        this.menu.containerId,
                                        PortfolioMenu.BUTTON_PREV_PAGE
                                );
                            }
                        })
                        .pos(this.leftPos + 8, this.topPos + 54)
                        .size(14, 20)
                        .build()
        );

        this.nextButton = this.addRenderableWidget(
                Button.builder(Component.literal(">"), button -> {
                            if (this.minecraft != null && this.minecraft.gameMode != null) {
                                this.minecraft.gameMode.handleInventoryButtonClick(
                                        this.menu.containerId,
                                        PortfolioMenu.BUTTON_NEXT_PAGE
                                );
                            }
                        })
                        .pos(this.leftPos + this.imageWidth - 22, this.topPos + 54)
                        .size(14, 20)
                        .build()
        );

        this.updateButtons();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.updateButtons();
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

        this.drawPortfolioGrid(guiGraphics, mouseX, mouseY);
        this.drawPlayerInventoryFrames(guiGraphics);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x4A3927, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x4A3927, false);

        String pageText = (this.menu.getScreenPage() + 1) + "/" + this.menu.getScreenCount();
        int pageTextWidth = this.font.width(pageText);
        guiGraphics.drawString(this.font, pageText, this.imageWidth - 8 - pageTextWidth, 6, 0x4A3927, false);

        if (this.menu.getStoredCount() <= 0) {
            Component empty = Component.translatable("screen.mystcraft-sc.portfolio.empty");
            int width = this.font.width(empty);
            guiGraphics.drawString(
                    this.font,
                    empty,
                    (this.imageWidth - width) / 2,
                    50,
                    0x4A3927,
                    false
            );
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            int localIndex = this.getHoveredPageIndex((int) event.x(), (int) event.y());
            if (localIndex >= 0) {
                this.minecraft.gameMode.handleInventoryButtonClick(
                        this.menu.containerId,
                        PortfolioMenu.BUTTON_TAKE_VISIBLE_START + localIndex
                );
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        int hovered = this.getHoveredPageIndex(mouseX, mouseY);

        if (hovered >= 0 && this.minecraft != null && this.minecraft.player != null) {
            ItemStack stack = this.menu.getVisiblePage(hovered);

            if (!stack.isEmpty()) {
                List<ClientTooltipComponent> tooltip = new ArrayList<>();

                for (var line : stack.getTooltipLines(
                        net.minecraft.world.item.Item.TooltipContext.EMPTY,
                        this.minecraft.player,
                        TooltipFlag.Default.NORMAL
                )) {
                    tooltip.add(new ClientTextTooltip(line.getVisualOrderText()));
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
        }
    }

    private void updateButtons() {
        if (this.prevButton != null) {
            this.prevButton.active = this.menu.getScreenPage() > 0;
        }

        if (this.nextButton != null) {
            this.nextButton.active = this.menu.getScreenPage() < this.menu.getScreenCount() - 1;
        }
    }

    private void drawPortfolioGrid(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (int row = 0; row < PortfolioMenu.GRID_ROWS; row++) {
            for (int column = 0; column < PortfolioMenu.GRID_COLUMNS; column++) {
                int localIndex = column + row * PortfolioMenu.GRID_COLUMNS;
                int x = this.leftPos + GRID_X + column * CELL_SIZE;
                int y = this.topPos + GRID_Y + row * CELL_SIZE;

                this.drawSlot(guiGraphics, x, y);

                ItemStack stack = this.menu.getVisiblePage(localIndex);
                if (!stack.isEmpty()) {
                    guiGraphics.renderItem(stack, x + 1, y + 1);
                    guiGraphics.renderItemDecorations(this.font, stack, x + 1, y + 1);
                }

                if (mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE) {
                    guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0x80FFFFFF);
                }
            }
        }
    }

    private void drawPlayerInventoryFrames(GuiGraphics guiGraphics) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.drawSlot(guiGraphics, this.leftPos + 7 + column * 18, this.topPos + 83 + row * 18);
            }
        }

        for (int column = 0; column < 9; column++) {
            this.drawSlot(guiGraphics, this.leftPos + 7 + column * 18, this.topPos + 141);
        }
    }

    private void drawSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + 18, y + 18, 0xFF8C7A5B);
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF241A12);
        guiGraphics.fill(x + 2, y + 2, x + 16, y + 16, 0xFFB49B75);
    }

    private int getHoveredPageIndex(int mouseX, int mouseY) {
        for (int row = 0; row < PortfolioMenu.GRID_ROWS; row++) {
            for (int column = 0; column < PortfolioMenu.GRID_COLUMNS; column++) {
                int localIndex = column + row * PortfolioMenu.GRID_COLUMNS;
                int x = this.leftPos + GRID_X + column * CELL_SIZE;
                int y = this.topPos + GRID_Y + row * CELL_SIZE;

                if (mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE) {
                    if (localIndex < this.menu.getVisibleCount()) {
                        return localIndex;
                    }
                    return -1;
                }
            }
        }

        return -1;
    }
}