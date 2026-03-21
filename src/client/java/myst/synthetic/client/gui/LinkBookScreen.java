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
import net.minecraft.client.renderer.RenderPipelines;
import myst.synthetic.network.LinkBookUsePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public class LinkBookScreen extends Screen {

    private static final Identifier BOOK_COVER = Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/bookui_cover.png");
    private static final Identifier BOOK_PAGE_LEFT = Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/bookui_pagel.png");
    private static final Identifier BOOK_PAGE_RIGHT = Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/bookui_pager.png");
    private static final Identifier BOOK_PAGE_RIGHT_FULL = Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/bookui_rpage_full.png");

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
        drawBook(guiGraphics);
        drawBookText(guiGraphics);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
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

    private void drawBook(GuiGraphics guiGraphics) {
        int x = this.leftPos;
        int y = this.topPos;

        // Legacy cover assembly from bookui_cover.png (256x256)
        drawRegion(guiGraphics, BOOK_COVER, x + 0,   y + 7, 152, 0,  34, 192, 256, 256);
        drawRegion(guiGraphics, BOOK_COVER, x + 34,  y + 7, 49,  0, 103, 192, 256, 256);
        drawRegion(guiGraphics, BOOK_COVER, x + 137, y + 7, 45,  0,   4, 192, 256, 256);
        drawRegion(guiGraphics, BOOK_COVER, x + 141, y + 7, 0,   0, 186, 192, 256, 256);

        // Left page (legacy always shown on page 0 book view)
        drawRegion(guiGraphics, BOOK_PAGE_LEFT, x + 7, y + 0, 0, 0, 156, 195, 256, 256);

        // Right page: use panel page for now, since this is the linking panel view
        drawRegion(guiGraphics, BOOK_PAGE_RIGHT, x + 163, y + 0, 0, 0, 156, 195, 256, 256);

        // Dummy link panel fill, inside the exact legacy hotspot region
        guiGraphics.fill(x + PANEL_X, y + PANEL_Y, x + PANEL_X + PANEL_W, y + PANEL_Y + PANEL_H, 0xFF101020);
        guiGraphics.fill(x + PANEL_X + 1, y + PANEL_Y + 1, x + PANEL_X + PANEL_W - 1, y + PANEL_Y + PANEL_H - 1, 0xFF1E2B38);
    }

    private void drawBookText(GuiGraphics guiGraphics) {
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

        // Title
        guiGraphics.drawString(
                this.font,
                title,
                x + 40,
                y + 40,
                0xFF3F2A17,
                false
        );

        // Author (half scale like legacy)
        guiGraphics.pose().pushMatrix();

        guiGraphics.pose().translate(x + 50, y + 50);
        guiGraphics.pose().scale(0.5f, 0.5f);

        guiGraphics.drawString(
                this.font,
                author,
                0,
                0,
                0xFF3F2A17,
                false
        );

        guiGraphics.pose().popMatrix();

        // Age name centered inside linking panel
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
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }

        boolean mainHand = client.player.getMainHandItem().is(this.bookStack.getItem());
        ClientPlayNetworking.send(new LinkBookUsePayload(mainHand));

        this.onClose();
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