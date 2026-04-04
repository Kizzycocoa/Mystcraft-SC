package myst.synthetic.client.gui;

import myst.synthetic.MystcraftItems;
import myst.synthetic.client.screen.DisplayLecternScreen;
import myst.synthetic.menu.DisplayContainerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class SingleSlotScreen extends AbstractContainerScreen<DisplayContainerMenu> {

    private static final Identifier SINGLE_SLOT_TEXTURE = Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/single_slot.png");

    private static final Identifier LINK_BOOK_COVER = Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/bookui_cover.png");
    private static final Identifier LINK_BOOK_PAGE_LEFT = Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/bookui_pagel.png");
    private static final Identifier LINK_BOOK_PAGE_RIGHT = Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/bookui_pager.png");

    private static final int SINGLE_WIDTH = 176;
    private static final int SINGLE_HEIGHT = 166;

    private static final int LINK_BOOK_WIDTH = 327;
    private static final int LINK_BOOK_HEIGHT = 199;

    private static final int LINK_PANEL_X = 173;
    private static final int LINK_PANEL_Y = 20;
    private static final int LINK_PANEL_W = 132;
    private static final int LINK_PANEL_H = 83;

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

        drawRegion(guiGraphics, LINK_BOOK_PAGE_LEFT, x + 7, y + 0, 0, 0, 156, 195, 256, 256);
        drawRegion(guiGraphics, LINK_BOOK_PAGE_RIGHT, x + 163, y + 0, 0, 0, 156, 195, 256, 256);

        guiGraphics.fill(x + LINK_PANEL_X, y + LINK_PANEL_Y, x + LINK_PANEL_X + LINK_PANEL_W, y + LINK_PANEL_Y + LINK_PANEL_H, 0xFF101020);
        guiGraphics.fill(x + LINK_PANEL_X + 1, y + LINK_PANEL_Y + 1, x + LINK_PANEL_X + LINK_PANEL_W - 1, y + LINK_PANEL_Y + LINK_PANEL_H - 1, 0xFF1E2B38);

        drawLinkBookText(guiGraphics);
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