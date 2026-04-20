package myst.synthetic.client.gui;

import myst.synthetic.MystcraftItems;
import myst.synthetic.client.screen.DisplayLecternScreen;
import myst.synthetic.item.BookBookmarkUtil;
import myst.synthetic.item.BookmarkColorUtil;
import myst.synthetic.menu.DisplayContainerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.world.item.TooltipFlag;

public class SingleSlotScreen extends AbstractContainerScreen<DisplayContainerMenu> {

    private static final Identifier SINGLE_SLOT_TEXTURE = Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/single_slot.png");
    private static final Identifier LINK_BOOK_COVER = Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/bookui_cover.png");
    private static final Identifier LINK_BOOK_PAGE_LEFT = Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/bookui_pagel.png");
    private static final Identifier BOOKMARK_TEXTURE = Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/bookui_bookmark.png");
    private static final Identifier BOOKMARK_OVERLAY_TEXTURE = Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/bookui_bookmark_overlay.png");

    private static final int SINGLE_WIDTH = 176;
    private static final int SINGLE_HEIGHT = 166;

    private static final int LINK_BOOK_WIDTH = 327;
    private static final int LINK_BOOK_HEIGHT = 199;

    private static final int BOOKMARK_X = 100;
    private static final int BOOKMARK_Y = 2;
    private static final int BOOKMARK_W = 46;
    private static final int BOOKMARK_H = 162;

    private final Inventory playerInventory;
    private final Component screenTitle;

    public SingleSlotScreen(DisplayContainerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.playerInventory = playerInventory;
        this.screenTitle = title;
        this.imageWidth = SINGLE_WIDTH;
        this.imageHeight = SINGLE_HEIGHT;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        updateLayout();
        super.init();
        updateLayout();
    }

    @Override
    public void containerTick() {
        super.containerTick();

        if (this.menu.isLecternBookMode()) {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new DisplayLecternScreen(this.menu, this.playerInventory, this.screenTitle));
            }
            return;
        }

        updateLayout();
    }

    private void updateLayout() {
        if (isLinkBookMode()) {
            this.imageWidth = LINK_BOOK_WIDTH;
            this.imageHeight = LINK_BOOK_HEIGHT;
        } else {
            this.imageWidth = SINGLE_WIDTH;
            this.imageHeight = SINGLE_HEIGHT;
        }

        this.inventoryLabelY = this.imageHeight - 94;
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    private boolean isLinkBookMode() {
        ItemStack stack = this.menu.getDisplayStack();
        return stack.is(MystcraftItems.LINKBOOK) || stack.is(MystcraftItems.AGEBOOK);
    }

    private boolean isMouseOverLinkPanel(double mouseX, double mouseY) {
        return isLinkBookMode()
                && this.menu.getCurrentPage() == 0
                && mouseX >= this.leftPos + LinkPanelGuiRenderer.PANEL_X
                && mouseX < this.leftPos + LinkPanelGuiRenderer.PANEL_X + LinkPanelGuiRenderer.PANEL_W
                && mouseY >= this.topPos + LinkPanelGuiRenderer.PANEL_Y
                && mouseY < this.topPos + LinkPanelGuiRenderer.PANEL_Y + LinkPanelGuiRenderer.PANEL_H;
    }

    private boolean isMouseOverBookmark(double mouseX, double mouseY) {
        if (!isLinkBookMode() || this.menu.getCurrentPage() != 0) {
            return false;
        }

        ItemStack bookmark = BookBookmarkUtil.getBookmark(this.menu.getDisplayStack());
        if (bookmark.isEmpty()) {
            return false;
        }

        int x = this.leftPos + BOOKMARK_X;
        int y = this.topPos + BOOKMARK_Y;
        return mouseX >= x && mouseX < x + BOOKMARK_W && mouseY >= y && mouseY < y + BOOKMARK_H;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        if (isLinkBookMode()) {
            renderLinkBookBackground(guiGraphics);
        } else {
            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    SINGLE_SLOT_TEXTURE,
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
    }

    private void renderLinkBookBackground(GuiGraphics guiGraphics) {
        int x = this.leftPos;
        int y = this.topPos;

        drawRegion(guiGraphics, LINK_BOOK_COVER, x + 0,   y + 7, 152, 0,  34, 192, 256, 256);
        drawRegion(guiGraphics, LINK_BOOK_COVER, x + 34,  y + 7, 49,  0, 103, 192, 256, 256);
        drawRegion(guiGraphics, LINK_BOOK_COVER, x + 137, y + 7, 45,  0,   4, 192, 256, 256);
        drawRegion(guiGraphics, LINK_BOOK_COVER, x + 141, y + 7, 0,   0, 186, 192, 256, 256);

        if (this.menu.getCurrentPage() > 0) {
            drawBookmark(guiGraphics);
            drawRegion(guiGraphics, LINK_BOOK_PAGE_LEFT, x + 7, y + 0, 0, 0, 156, 195, 256, 256);
        } else {
            drawBookmark(guiGraphics);
        }

        if (this.menu.getCurrentPage() == 0) {
            LinkPanelGuiRenderer.drawCutoutPanelPage(guiGraphics, x, y, getBookTag());
            drawLinkBookText(guiGraphics);
        } else {
            LinkPanelGuiRenderer.drawSolidRightPage(guiGraphics, x, y);
        }
    }

    private void drawBookmark(GuiGraphics guiGraphics) {
        ItemStack bookmark = BookBookmarkUtil.getBookmark(this.menu.getDisplayStack());
        if (bookmark.isEmpty()) {
            return;
        }

        int x = this.leftPos + BOOKMARK_X;
        int y = this.topPos + BOOKMARK_Y;
        int color = 0xFF000000 | BookmarkColorUtil.getColor(bookmark);

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                BOOKMARK_TEXTURE,
                x,
                y,
                0.0F,
                0.0F,
                BOOKMARK_W,
                BOOKMARK_H,
                64,
                256,
                color
        );

        drawRegion(guiGraphics, BOOKMARK_OVERLAY_TEXTURE, x, y, 0, 0, BOOKMARK_W, BOOKMARK_H, 64, 256);
    }

    private void drawRegion(
            GuiGraphics guiGraphics,
            Identifier texture,
            int x,
            int y,
            int u,
            int v,
            int width,
            int height,
            int textureWidth,
            int textureHeight
    ) {
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                x,
                y,
                u,
                v,
                width,
                height,
                width,
                height,
                textureWidth,
                textureHeight
        );
    }

    private void drawLinkBookText(GuiGraphics guiGraphics) {
        int x = this.leftPos;
        int y = this.topPos;

        CompoundTag tag = getBookTag();

        String title = "Linking Book";
        String author = "";
        String ageName = "";

        if (tag != null) {
            author = tag.getString("Author").orElse("");
            ageName = tag.getString("AgeName").orElse("");
            title = tag.getString("DisplayName").orElse("Linking Book");
        }

        guiGraphics.drawString(this.font, title, x + 40, y + 40, 0xFF3F2A17, false);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x + 50, y + 50);
        guiGraphics.pose().scale(0.5f, 0.5f);
        guiGraphics.drawString(this.font, author, 0, 0, 0xFF3F2A17, false);
        guiGraphics.pose().popMatrix();

        if (!ageName.isEmpty()) {
            int panelCenterX = x + LinkPanelGuiRenderer.PANEL_X + (LinkPanelGuiRenderer.PANEL_W / 2);
            int width = this.font.width(ageName);

            guiGraphics.drawString(
                    this.font,
                    ageName,
                    panelCenterX - (width / 2),
                    y + LinkPanelGuiRenderer.PANEL_Y + LinkPanelGuiRenderer.PANEL_H + 6,
                    0xFF3F2A17,
                    false
            );
        }
    }

    private CompoundTag getBookTag() {
        ItemStack stack = this.menu.getDisplayStack();
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        return customData != null ? customData.copyTag() : null;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (isMouseOverBookmark(event.x(), event.y())) {
            if (this.minecraft != null && this.minecraft.gameMode != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(
                        this.menu.containerId,
                        DisplayContainerMenu.BUTTON_EXTRACT_BOOKMARK
                );
                return true;
            }
        }

        if (this.minecraft != null && this.minecraft.gameMode != null) {
            if (isMouseOverLinkPanel(event.x(), event.y())) {
                this.minecraft.gameMode.handleInventoryButtonClick(
                        this.menu.containerId,
                        DisplayContainerMenu.BUTTON_USE_LINKBOOK
                );
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (isMouseOverBookmark(mouseX, mouseY)) {
            ItemStack bookmark = BookBookmarkUtil.getBookmark(this.menu.getDisplayStack());
            if (!bookmark.isEmpty() && this.minecraft != null && this.minecraft.player != null) {
                List<ClientTooltipComponent> tooltip = new ArrayList<>();

                for (var line : bookmark.getTooltipLines(
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
        } else {
            this.renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }
}