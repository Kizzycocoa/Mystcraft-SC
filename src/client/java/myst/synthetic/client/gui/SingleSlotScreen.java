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
import net.minecraft.network.chat.CommonComponents;
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

    private static final Identifier LINK_BOOK_COVER = Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/bookui_cover.png");
    private static final Identifier LINK_BOOK_PAGE_LEFT = Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/bookui_pagel.png");
    private static final Identifier LINK_BOOK_PAGE_RIGHT = Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/bookui_pager.png");

    private static final Identifier VANILLA_BOOK_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/book.png");

    private static final int SINGLE_WIDTH = 176;
    private static final int SINGLE_HEIGHT = 166;

    private static final int LINK_BOOK_WIDTH = 327;
    private static final int LINK_BOOK_HEIGHT = 199;

    private static final int LECTERN_SCREEN_WIDTH = 256;
    private static final int LECTERN_SCREEN_HEIGHT = 224;

    private static final int LINK_PANEL_X = 173;
    private static final int LINK_PANEL_Y = 20;
    private static final int LINK_PANEL_W = 132;
    private static final int LINK_PANEL_H = 83;

    private static final int VANILLA_BOOK_X = 32;
    private static final int VANILLA_BOOK_Y = 0;
    private static final int VANILLA_BOOK_W = 192;
    private static final int VANILLA_BOOK_H = 192;

    private static final int LECTERN_PAGE_X = VANILLA_BOOK_X + 36;
    private static final int LECTERN_PAGE_Y = VANILLA_BOOK_Y + 30;
    private static final int LECTERN_PAGE_W = 114;
    private static final int LECTERN_PAGE_H = 128;

    private static final int PAGE_NUMBER_Y = VANILLA_BOOK_Y + 18;

    private static final int DONE_BUTTON_X = 30;
    private static final int DONE_BUTTON_Y = 196;
    private static final int DONE_BUTTON_W = 98;
    private static final int DONE_BUTTON_H = 20;

    private static final int TAKE_BUTTON_X = 128;
    private static final int TAKE_BUTTON_Y = 196;
    private static final int TAKE_BUTTON_W = 98;
    private static final int TAKE_BUTTON_H = 20;

    private static final int PREV_BUTTON_X = VANILLA_BOOK_X + 120;
    private static final int PREV_BUTTON_Y = VANILLA_BOOK_Y + 159;
    private static final int PREV_BUTTON_W = 12;
    private static final int PREV_BUTTON_H = 19;

    private static final int NEXT_BUTTON_X = VANILLA_BOOK_X + 140;
    private static final int NEXT_BUTTON_Y = VANILLA_BOOK_Y + 159;
    private static final int NEXT_BUTTON_W = 12;
    private static final int NEXT_BUTTON_H = 19;

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
        if (isLinkBookMode()) {
            this.imageWidth = LINK_BOOK_WIDTH;
            this.imageHeight = LINK_BOOK_HEIGHT;
        } else if (isLecternBookMode()) {
            this.imageWidth = LECTERN_SCREEN_WIDTH;
            this.imageHeight = LECTERN_SCREEN_HEIGHT;
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

        drawRegion(guiGraphics, LINK_BOOK_COVER, x + 0,   y + 7, 152, 0,  34, 192, 256, 256);
        drawRegion(guiGraphics, LINK_BOOK_COVER, x + 34,  y + 7, 49,  0, 103, 192, 256, 256);
        drawRegion(guiGraphics, LINK_BOOK_COVER, x + 137, y + 7, 45,  0,   4, 192, 256, 256);
        drawRegion(guiGraphics, LINK_BOOK_COVER, x + 141, y + 7, 0,   0, 186, 192, 256, 256);

        drawRegion(guiGraphics, LINK_BOOK_PAGE_LEFT, x + 7, y + 0, 0, 0, 156, 195, 256, 256);
        drawRegion(guiGraphics, LINK_BOOK_PAGE_RIGHT, x + 163, y + 0, 0, 0, 156, 195, 256, 256);

        guiGraphics.fill(x + LINK_PANEL_X, y + LINK_PANEL_Y, x + LINK_PANEL_X + LINK_PANEL_W, y + LINK_PANEL_Y + LINK_PANEL_H, 0xFF101020);
        guiGraphics.fill(x + LINK_PANEL_X + 1, y + LINK_PANEL_Y + 1, x + LINK_PANEL_X + LINK_PANEL_W - 1, y + LINK_PANEL_Y + LINK_PANEL_H - 1, 0xFF1E2B38);

        drawLinkBookText(guiGraphics);
    }

    private void renderLecternBackground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int bookX = this.leftPos + VANILLA_BOOK_X;
        int bookY = this.topPos + VANILLA_BOOK_Y;

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                VANILLA_BOOK_TEXTURE,
                bookX,
                bookY,
                0,
                0,
                VANILLA_BOOK_W,
                VANILLA_BOOK_H,
                192,
                192
        );

        drawLecternPageNumber(guiGraphics);
        drawLecternPage(guiGraphics);
        drawLecternArrowButton(guiGraphics, mouseX, mouseY, true);
        drawLecternArrowButton(guiGraphics, mouseX, mouseY, false);
        drawLecternBottomButton(guiGraphics, mouseX, mouseY, true);
        drawLecternBottomButton(guiGraphics, mouseX, mouseY, false);
    }

    private void drawLecternBottomButton(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean doneButton) {
        int x = this.leftPos + (doneButton ? DONE_BUTTON_X : TAKE_BUTTON_X);
        int y = this.topPos + (doneButton ? DONE_BUTTON_Y : TAKE_BUTTON_Y);
        int w = doneButton ? DONE_BUTTON_W : TAKE_BUTTON_W;
        int h = doneButton ? DONE_BUTTON_H : TAKE_BUTTON_H;

        boolean hovered = doneButton ? isMouseOverDoneButton(mouseX, mouseY) : isMouseOverTakeButton(mouseX, mouseY);

        int border = hovered ? 0xFFA0A0A0 : 0xFF7A7A7A;
        int fill = hovered ? 0xFF5E5E5E : 0xFF444444;

        guiGraphics.fill(x, y, x + w, y + h, border);
        guiGraphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);

        Component text = doneButton ? CommonComponents.GUI_DONE : Component.literal("Take Book");
        int textX = x + (w - this.font.width(text)) / 2;
        int textY = y + 6;
        guiGraphics.drawString(this.font, text, textX, textY, 0xFFFFFF, false);
    }

    private void drawLecternArrowButton(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean previous) {
        int x = this.leftPos + (previous ? PREV_BUTTON_X : NEXT_BUTTON_X);
        int y = this.topPos + (previous ? PREV_BUTTON_Y : NEXT_BUTTON_Y);
        int w = previous ? PREV_BUTTON_W : NEXT_BUTTON_W;
        int h = previous ? PREV_BUTTON_H : NEXT_BUTTON_H;

        boolean enabled = previous ? this.menu.getCurrentPage() > 0 : this.menu.getCurrentPage() < this.menu.getPageCount() - 1;
        boolean hovered = enabled && (previous ? isMouseOverPrevButton(mouseX, mouseY) : isMouseOverNextButton(mouseX, mouseY));

        int border = enabled ? (hovered ? 0xFFA0A0A0 : 0xFF7A7A7A) : 0xFF555555;
        int fill = enabled ? (hovered ? 0xFF5E5E5E : 0xFF444444) : 0xFF2F2F2F;
        int glyphColor = enabled ? 0xFFFFFFFF : 0xFF8A8A8A;

        guiGraphics.fill(x, y, x + w, y + h, border);
        guiGraphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);

        Component glyph = Component.literal(previous ? "<" : ">");
        int glyphX = x + (w - this.font.width(glyph)) / 2;
        int glyphY = y + 6;
        guiGraphics.drawString(this.font, glyph, glyphX, glyphY, glyphColor, false);
    }

    private void drawLecternPageNumber(GuiGraphics guiGraphics) {
        int currentPage = this.menu.getCurrentPage() + 1;
        int pageCount = this.menu.getPageCount();

        Component text = Component.literal("Page " + currentPage + " of " + pageCount);
        int centerX = this.leftPos + VANILLA_BOOK_X + (VANILLA_BOOK_W / 2);
        int x = centerX - (this.font.width(text) / 2);
        int y = this.topPos + PAGE_NUMBER_Y;

        guiGraphics.drawString(this.font, text, x, y, 0xFF000000, false);
    }

    private void drawLecternPage(GuiGraphics guiGraphics) {
        Component pageText = getCurrentLecternPageText();
        Font font = this.font;

        int startX = this.leftPos + LECTERN_PAGE_X;
        int startY = this.topPos + LECTERN_PAGE_Y;

        List<FormattedCharSequence> lines = font.split(pageText, LECTERN_PAGE_W);
        int maxLines = LECTERN_PAGE_H / 9;

        for (int i = 0; i < lines.size() && i < maxLines; i++) {
            guiGraphics.drawString(font, lines.get(i), startX, startY + i * 9, 0xFF000000);
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
            int panelCenterX = x + LINK_PANEL_X + (LINK_PANEL_W / 2);
            int width = this.font.width(ageName);

            guiGraphics.drawString(
                    this.font,
                    ageName,
                    panelCenterX - (width / 2),
                    y + LINK_PANEL_Y + LINK_PANEL_H + 6,
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

    private boolean isMouseOverDoneButton(double mouseX, double mouseY) {
        return isLecternBookMode()
                && mouseX >= this.leftPos + DONE_BUTTON_X
                && mouseX < this.leftPos + DONE_BUTTON_X + DONE_BUTTON_W
                && mouseY >= this.topPos + DONE_BUTTON_Y
                && mouseY < this.topPos + DONE_BUTTON_Y + DONE_BUTTON_H;
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
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            if (isLecternBookMode()) {
                if (isMouseOverDoneButton(event.x(), event.y())) {
                    this.onClose();
                    return true;
                }

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