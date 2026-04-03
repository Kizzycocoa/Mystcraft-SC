package myst.synthetic.client.gui;

import myst.synthetic.MystcraftItems;
import myst.synthetic.menu.DisplayContainerMenu;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;

import java.util.List;

public class SingleSlotScreen extends AbstractContainerScreen<DisplayContainerMenu> {

    private static final Identifier SINGLE_SLOT_TEXTURE = Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/single_slot.png");

    private static final Identifier BOOK_COVER = Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/bookui_cover.png");
    private static final Identifier BOOK_PAGE_LEFT = Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/bookui_pagel.png");
    private static final Identifier BOOK_PAGE_RIGHT = Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/bookui_pager.png");

    private static final int SINGLE_WIDTH = 176;
    private static final int SINGLE_HEIGHT = 166;

    private static final int BOOK_WIDTH = 327;
    private static final int BOOK_HEIGHT = 199;

    private static final int PANEL_X = 173;
    private static final int PANEL_Y = 20;
    private static final int PANEL_W = 132;
    private static final int PANEL_H = 83;

    private static final int TAKE_BUTTON_X = 36;
    private static final int TAKE_BUTTON_Y = 167;
    private static final int TAKE_BUTTON_W = 88;
    private static final int TAKE_BUTTON_H = 20;

    private static final int PREV_BUTTON_X = 126;
    private static final int PREV_BUTTON_Y = 167;
    private static final int PREV_BUTTON_W = 20;
    private static final int PREV_BUTTON_H = 20;

    private static final int NEXT_BUTTON_X = 150;
    private static final int NEXT_BUTTON_Y = 167;
    private static final int NEXT_BUTTON_W = 20;
    private static final int NEXT_BUTTON_H = 20;

    private static final int LEFT_PAGE_TEXT_X = 24;
    private static final int LEFT_PAGE_TEXT_Y = 18;
    private static final int LEFT_PAGE_TEXT_WIDTH = 114;
    private static final int LEFT_PAGE_TEXT_HEIGHT = 128;

    public SingleSlotScreen(DisplayContainerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
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
        updateLayout();
    }

    private void updateLayout() {
        boolean bookMode = isLinkBookMode() || isLecternBookMode();

        this.imageWidth = bookMode ? BOOK_WIDTH : SINGLE_WIDTH;
        this.imageHeight = bookMode ? BOOK_HEIGHT : SINGLE_HEIGHT;
        this.inventoryLabelY = this.imageHeight - 94;

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    private boolean isLinkBookMode() {
        ItemStack stack = this.menu.getDisplayStack();
        return stack.is(MystcraftItems.LINKBOOK) || stack.is(MystcraftItems.AGEBOOK);
    }

    private boolean isLecternBookMode() {
        return this.menu.isLecternBookMode();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        if (isLinkBookMode()) {
            renderLinkBookBackground(guiGraphics);
        } else if (isLecternBookMode()) {
            renderLecternBackground(guiGraphics, mouseX, mouseY);
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

        drawBookBackground(guiGraphics, x, y);

        guiGraphics.fill(x + PANEL_X, y + PANEL_Y, x + PANEL_X + PANEL_W, y + PANEL_Y + PANEL_H, 0xFF101020);
        guiGraphics.fill(x + PANEL_X + 1, y + PANEL_Y + 1, x + PANEL_X + PANEL_W - 1, y + PANEL_Y + PANEL_H - 1, 0xFF1E2B38);

        drawLinkBookText(guiGraphics);
    }

    private void renderLecternBackground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        drawBookBackground(guiGraphics, x, y);
        drawLecternPage(guiGraphics);
        drawTakeButton(guiGraphics, mouseX, mouseY);
        drawPrevButton(guiGraphics, mouseX, mouseY);
        drawNextButton(guiGraphics, mouseX, mouseY);
        drawPageNumber(guiGraphics);
    }

    private void drawBookBackground(GuiGraphics guiGraphics, int x, int y) {
        drawRegion(guiGraphics, BOOK_COVER, x + 0,   y + 7, 152, 0,  34, 192, 256, 256);
        drawRegion(guiGraphics, BOOK_COVER, x + 34,  y + 7, 49,  0, 103, 192, 256, 256);
        drawRegion(guiGraphics, BOOK_COVER, x + 137, y + 7, 45,  0,   4, 192, 256, 256);
        drawRegion(guiGraphics, BOOK_COVER, x + 141, y + 7, 0,   0, 186, 192, 256, 256);

        drawRegion(guiGraphics, BOOK_PAGE_LEFT, x + 7, y + 0, 0, 0, 156, 195, 256, 256);
        drawRegion(guiGraphics, BOOK_PAGE_RIGHT, x + 163, y + 0, 0, 0, 156, 195, 256, 256);
    }

    private void drawTakeButton(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        boolean hovered = isMouseOverTakeButton(mouseX, mouseY);

        int left = this.leftPos + TAKE_BUTTON_X;
        int top = this.topPos + TAKE_BUTTON_Y;
        int right = left + TAKE_BUTTON_W;
        int bottom = top + TAKE_BUTTON_H;

        guiGraphics.fill(left, top, right, bottom, hovered ? 0xFFA0A0A0 : 0xFF7A7A7A);
        guiGraphics.fill(left + 1, top + 1, right - 1, bottom - 1, hovered ? 0xFF5E5E5E : 0xFF444444);

        Component text = Component.literal("Take Book");
        int textX = left + (TAKE_BUTTON_W - this.font.width(text)) / 2;
        int textY = top + 6;
        guiGraphics.drawString(this.font, text, textX, textY, 0xFFFFFF, false);
    }

    private void drawPrevButton(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int currentPage = this.menu.getCurrentPage();

        boolean enabled = isLecternBookMode() && currentPage > 0;
        boolean hovered = enabled && isMouseOverPrevButton(mouseX, mouseY);

        int left = this.leftPos + PREV_BUTTON_X;
        int top = this.topPos + PREV_BUTTON_Y;
        int right = left + PREV_BUTTON_W;
        int bottom = top + PREV_BUTTON_H;

        guiGraphics.fill(left, top, right, bottom, enabled ? (hovered ? 0xFFA0A0A0 : 0xFF7A7A7A) : 0xFF555555);
        guiGraphics.fill(left + 1, top + 1, right - 1, bottom - 1, enabled ? (hovered ? 0xFF5E5E5E : 0xFF444444) : 0xFF2F2F2F);

        guiGraphics.drawString(this.font, Component.literal("<"), left + 7, top + 6, enabled ? 0xFFFFFF : 0xFF8A8A8A, false);
    }

    private void drawNextButton(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int currentPage = this.menu.getCurrentPage();
        int pageCount = this.menu.getPageCount();

        boolean enabled = isLecternBookMode() && currentPage < pageCount - 1;
        boolean hovered = enabled && isMouseOverNextButton(mouseX, mouseY);

        int left = this.leftPos + NEXT_BUTTON_X;
        int top = this.topPos + NEXT_BUTTON_Y;
        int right = left + NEXT_BUTTON_W;
        int bottom = top + NEXT_BUTTON_H;

        guiGraphics.fill(left, top, right, bottom, enabled ? (hovered ? 0xFFA0A0A0 : 0xFF7A7A7A) : 0xFF555555);
        guiGraphics.fill(left + 1, top + 1, right - 1, bottom - 1, enabled ? (hovered ? 0xFF5E5E5E : 0xFF444444) : 0xFF2F2F2F);

        guiGraphics.drawString(this.font, Component.literal(">"), left + 7, top + 6, enabled ? 0xFFFFFF : 0xFF8A8A8A, false);
    }

    private void drawPageNumber(GuiGraphics guiGraphics) {
        int currentPage = this.menu.getCurrentPage() + 1;
        int pageCount = this.menu.getPageCount();

        Component text = Component.literal(currentPage + " / " + pageCount);
        int x = this.leftPos + 204;
        int y = this.topPos + 170;

        guiGraphics.drawString(this.font, text, x, y, 0xFF3F2A17, false);
    }

    private void drawLecternPage(GuiGraphics guiGraphics) {
        Component pageText = getCurrentLecternPageText();
        Font font = this.font;

        int startX = this.leftPos + LEFT_PAGE_TEXT_X;
        int startY = this.topPos + LEFT_PAGE_TEXT_Y;

        List<FormattedCharSequence> lines = font.split(pageText, LEFT_PAGE_TEXT_WIDTH);
        int maxLines = LEFT_PAGE_TEXT_HEIGHT / 9;

        for (int i = 0; i < lines.size() && i < maxLines; i++) {
            guiGraphics.drawString(font, lines.get(i), startX, startY + i * 9, 0xFF3F2A17);
        }
    }

    private Component getCurrentLecternPageText() {
        ItemStack stack = this.menu.getDisplayStack();
        int currentPage = this.menu.getCurrentPage();

        if (stack.is(Items.WRITTEN_BOOK)) {
            WrittenBookContent content = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
            if (content == null) {
                return Component.empty();
            }

            List<Component> pages = content.getPages(false);
            if (pages.isEmpty()) {
                return Component.empty();
            }

            int index = Math.max(0, Math.min(currentPage, pages.size() - 1));
            return pages.get(index);
        }

        if (stack.is(Items.WRITABLE_BOOK)) {
            WritableBookContent content = stack.get(DataComponents.WRITABLE_BOOK_CONTENT);
            if (content == null) {
                return Component.empty();
            }

            List<String> pages = content.getPages(false).toList();
            if (pages.isEmpty()) {
                return Component.empty();
            }

            int index = Math.max(0, Math.min(currentPage, pages.size() - 1));
            return Component.literal(pages.get(index));
        }

        return Component.empty();
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
            int panelCenterX = x + PANEL_X + (PANEL_W / 2);
            int width = this.font.width(ageName);

            guiGraphics.drawString(
                    this.font,
                    ageName,
                    panelCenterX - (width / 2),
                    y + PANEL_Y + PANEL_H + 6,
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

    private boolean isMouseOverTakeButton(double mouseX, double mouseY) {
        return isLecternBookMode()
                && mouseX >= this.leftPos + TAKE_BUTTON_X
                && mouseX < this.leftPos + TAKE_BUTTON_X + TAKE_BUTTON_W
                && mouseY >= this.topPos + TAKE_BUTTON_Y
                && mouseY < this.topPos + TAKE_BUTTON_Y + TAKE_BUTTON_H;
    }

    private boolean isMouseOverPrevButton(double mouseX, double mouseY) {
        return isLecternBookMode()
                && mouseX >= this.leftPos + PREV_BUTTON_X
                && mouseX < this.leftPos + PREV_BUTTON_X + PREV_BUTTON_W
                && mouseY >= this.topPos + PREV_BUTTON_Y
                && mouseY < this.topPos + PREV_BUTTON_Y + PREV_BUTTON_H;
    }

    private boolean isMouseOverNextButton(double mouseX, double mouseY) {
        return isLecternBookMode()
                && mouseX >= this.leftPos + NEXT_BUTTON_X
                && mouseX < this.leftPos + NEXT_BUTTON_X + NEXT_BUTTON_W
                && mouseY >= this.topPos + NEXT_BUTTON_Y
                && mouseY < this.topPos + NEXT_BUTTON_Y + NEXT_BUTTON_H;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.minecraft != null && this.minecraft.gameMode != null && isLecternBookMode()) {
            if (isMouseOverTakeButton(event.x(), event.y())) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, DisplayContainerMenu.BUTTON_TAKE_BOOK);
                return true;
            }

            if (isMouseOverPrevButton(event.x(), event.y())) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, DisplayContainerMenu.BUTTON_PREV_PAGE);
                return true;
            }

            if (isMouseOverNextButton(event.x(), event.y())) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, DisplayContainerMenu.BUTTON_NEXT_PAGE);
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
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}