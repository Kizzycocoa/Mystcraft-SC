package myst.synthetic.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.client.input.MouseButtonEvent;

public class LinkBookScreen extends Screen {

    private static final Identifier BOOK_COVER = Identifier.fromNamespaceAndPath("mystcraft-sc", "gui/bookui_cover");
    private static final Identifier BOOK_PAGE_LEFT = Identifier.fromNamespaceAndPath("mystcraft-sc", "gui/bookui_pagel");
    private static final Identifier BOOK_PAGE_RIGHT = Identifier.fromNamespaceAndPath("mystcraft-sc", "gui/bookui_pager");
    private static final Identifier BOOK_PAGE_RIGHT_FULL = Identifier.fromNamespaceAndPath("mystcraft-sc", "gui/bookui_rpage_full");

    private static final int GUI_WIDTH = 327;
    private static final int GUI_HEIGHT = 199;

    private static final int PANEL_X = 173;
    private static final int PANEL_Y = 20;
    private static final int PANEL_W = 132;
    private static final int PANEL_H = 83;

    private final ItemStack bookStack;
    private int leftPos;
    private int topPos;

    public LinkBookScreen(ItemStack bookStack) {
        super(Component.literal("Linking Book"));
        this.bookStack = bookStack;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - GUI_WIDTH) / 2;
        this.topPos = (this.height - GUI_HEIGHT) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        drawBook(guiGraphics);
        drawBookText(guiGraphics);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void drawBook(GuiGraphics guiGraphics) {
        int x = this.leftPos;
        int y = this.topPos;

        // Legacy cover layout
        guiGraphics.blit(BOOK_COVER, x + 0, y + 7, 152, 0, 34, 192, 256, 256);
        guiGraphics.blit(BOOK_COVER, x + 34, y + 7, 49, 0, 103, 192, 256, 256);
        guiGraphics.blit(BOOK_COVER, x + 137, y + 7, 45, 0, 4, 192, 256, 256);
        guiGraphics.blit(BOOK_COVER, x + 141, y + 7, 0, 0, 186, 192, 256, 256);

        // Left page
        guiGraphics.blit(BOOK_PAGE_LEFT, x + 7, y + 0, 0, 0, 156, 195, 256, 256);

        // Right page with panel layout
        guiGraphics.blit(BOOK_PAGE_RIGHT, x + 163, y + 0, 0, 0, 156, 195, 256, 256);

        // Dummy link panel area, close to legacy panel region
        guiGraphics.fill(x + PANEL_X, y + PANEL_Y, x + PANEL_X + PANEL_W, y + PANEL_Y + PANEL_H, 0xFF101020);
        guiGraphics.fill(x + PANEL_X + 1, y + PANEL_Y + 1, x + PANEL_X + PANEL_W - 1, y + PANEL_Y + PANEL_H - 1, 0xFF1E2B38);
    }

    private void drawBookText(GuiGraphics guiGraphics) {
        int x = this.leftPos;
        int y = this.topPos;

        String title = getDisplayName();
        String author = getAuthor();
        String ageName = getAgeName();

        guiGraphics.drawString(this.font, title, x + 40, y + 40, 0x000000, false);

        if (!author.isEmpty()) {
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(x + 50, y + 50);
            guiGraphics.pose().scale(0.5F, 0.5F);
            guiGraphics.drawString(this.font, author, 0, 0, 0x000000, false);
            guiGraphics.pose().popMatrix();
        }

        // Age name text on the linking panel page
        int ageTextWidth = this.font.width(ageName);
        int ageTextX = x + PANEL_X + (PANEL_W - ageTextWidth) / 2;
        int ageTextY = y + PANEL_Y + PANEL_H + 8;
        guiGraphics.drawString(this.font, ageName, ageTextX, ageTextY, 0x000000, false);

        // Page counter, matching legacy placement
        String pageText = "0/0";
        int pageWidth = this.font.width(pageText);
        guiGraphics.drawString(this.font, pageText, x + 165 - (pageWidth / 2), y + 185, 0x000000, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        double mouseX = mouseButtonEvent.x();
        double mouseY = mouseButtonEvent.y();

        int panelLeft = this.leftPos + PANEL_X;
        int panelTop = this.topPos + PANEL_Y;
        int panelRight = panelLeft + PANEL_W;
        int panelBottom = panelTop + PANEL_H;

        if (mouseX >= panelLeft && mouseX <= panelRight && mouseY >= panelTop && mouseY <= panelBottom) {
            onLinkPanelClicked();
            return true;
        }

        return super.mouseClicked(mouseButtonEvent, bl);
    }

    private void onLinkPanelClicked() {
        // Dummy for now.
        // Later this is where the teleport request packet should be sent.
    }

    private CompoundTag getBookTag() {
        CustomData customData = this.bookStack.get(DataComponents.CUSTOM_DATA);
        return customData != null ? customData.copyTag() : null;
    }

    private String getDisplayName() {
        CompoundTag tag = getBookTag();
        if (tag != null && tag.contains("DisplayName")) {
            return tag.getString("DisplayName").orElse("Linking Book");
        }
        return "Linking Book";
    }

    private String getAuthor() {
        CompoundTag tag = getBookTag();
        if (tag != null && tag.contains("Author")) {
            String author = tag.getString("Author").orElse("");
            if (!author.isEmpty()) {
                return "by " + author;
            }
        }
        return "";
    }

    private String getAgeName() {
        CompoundTag tag = getBookTag();
        if (tag != null && tag.contains("AgeName")) {
            return tag.getString("AgeName").orElse("Unknown Age");
        }
        return "Unknown Age";
    }
}