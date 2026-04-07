package myst.synthetic.client.gui;

import myst.synthetic.client.page.PageRenderCache;
import myst.synthetic.client.render.PageRenderKey;
import myst.synthetic.page.Page;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class PageSurfaceRenderer {

    public static final Identifier PAGE_CARD_TEXTURE =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/bookui_pagel.png");

    public static final int PAGE_WIDTH = 30;
    public static final int PAGE_HEIGHT = 40;
    public static final int PAGE_X_STEP = 31;
    public static final int PAGE_Y_STEP = 41;

    public static final int SURFACE_X = 0;
    public static final int SURFACE_Y = 19;
    public static final int SURFACE_PAGE_WIDTH = 156;
    public static final int SURFACE_HEIGHT = 114;

    public static final int SCROLLBAR_X = 156;
    public static final int SCROLLBAR_Y = 19;
    public static final int SCROLLBAR_WIDTH = 20;
    public static final int SCROLLBAR_HEIGHT = 114;

    public static final int COLUMNS = 5;

    private PageSurfaceRenderer() {
    }

    public static void drawSurfaceBackground(GuiGraphics guiGraphics, int guiLeft, int guiTop) {
        int x = guiLeft + SURFACE_X;
        int y = guiTop + SURFACE_Y;

        guiGraphics.fill(x, y, x + SURFACE_PAGE_WIDTH, y + SURFACE_HEIGHT, 0xAA000000);
    }

    public static void drawEntries(
            GuiGraphics guiGraphics,
            int guiLeft,
            int guiTop,
            int mouseX,
            int mouseY,
            int scroll,
            List<SurfaceEntry> entries
    ) {
        int clipLeft = guiLeft + SURFACE_X;
        int clipTop = guiTop + SURFACE_Y;
        int clipRight = clipLeft + SURFACE_PAGE_WIDTH;
        int clipBottom = clipTop + SURFACE_HEIGHT;

        guiGraphics.enableScissor(clipLeft, clipTop, clipRight, clipBottom);

        for (SurfaceEntry entry : entries) {
            int drawX = guiLeft + SURFACE_X + entry.x();
            int drawY = guiTop + SURFACE_Y + entry.y() - scroll;

            if (drawY + PAGE_HEIGHT < clipTop || drawY > clipBottom) {
                continue;
            }

            boolean hovered =
                    mouseX >= drawX && mouseX < drawX + PAGE_WIDTH
                            && mouseY >= drawY && mouseY < drawY + PAGE_HEIGHT;

            drawPageCard(guiGraphics, drawX, drawY, entry.stack(), entry.placeholder(), hovered);
        }

        guiGraphics.disableScissor();
    }

    public static void drawScrollbar(GuiGraphics guiGraphics, int guiLeft, int guiTop, int scroll, int maxScroll) {
        int x = guiLeft + SCROLLBAR_X;
        int y = guiTop + SCROLLBAR_Y;

        guiGraphics.fill(x + 6, y, x + 14, y + SCROLLBAR_HEIGHT, 0xFF9C9C9C);
        guiGraphics.fill(x + 7, y + 1, x + 13, y + SCROLLBAR_HEIGHT - 1, 0xFF5E5E5E);

        int knobHeight = 18;
        int knobTravel = SCROLLBAR_HEIGHT - knobHeight;
        int knobOffset = maxScroll <= 0 ? 0 : (scroll * knobTravel) / maxScroll;

        guiGraphics.fill(x + 2, y + knobOffset, x + 18, y + knobOffset + knobHeight, 0xFFF2F2F2);
        guiGraphics.fill(x + 3, y + knobOffset + 1, x + 17, y + knobOffset + knobHeight - 1, 0xFFBDBDBD);
        guiGraphics.fill(x + 4, y + knobOffset + 2, x + 16, y + knobOffset + knobHeight - 2, 0xFF7D7D7D);
    }

    public static int getHoveredOrderedSlot(int guiLeft, int guiTop, int mouseX, int mouseY, int scroll) {
        int relX = mouseX - (guiLeft + SURFACE_X);
        int relY = mouseY - (guiTop + SURFACE_Y) + scroll;

        if (relX < 0 || relY < 0 || relX >= SURFACE_PAGE_WIDTH) {
            return -1;
        }

        int col = relX / PAGE_X_STEP;
        int row = relY / PAGE_Y_STEP;

        if (col < 0 || col >= COLUMNS || row < 0) {
            return -1;
        }

        int localX = relX % PAGE_X_STEP;
        int localY = relY % PAGE_Y_STEP;

        if (localX >= PAGE_WIDTH || localY >= PAGE_HEIGHT) {
            return -1;
        }

        return row * COLUMNS + col;
    }

    public static int getMaxScroll(List<SurfaceEntry> entries) {
        int maxBottom = 0;

        for (SurfaceEntry entry : entries) {
            int bottom = entry.y() + PAGE_HEIGHT + 6;
            if (bottom > maxBottom) {
                maxBottom = bottom;
            }
        }

        return Math.max(0, maxBottom - SURFACE_HEIGHT);
    }

    private static void drawPageCard(GuiGraphics guiGraphics, int x, int y, ItemStack stack, boolean placeholder, boolean hovered) {
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                PAGE_CARD_TEXTURE,
                x,
                y,
                156,
                0,
                PAGE_WIDTH,
                PAGE_HEIGHT,
                256,
                256
        );

        if (placeholder) {
            guiGraphics.fill(x, y, x + PAGE_WIDTH, y + PAGE_HEIGHT, 0xAA111111);
        } else {
            drawPageContents(guiGraphics, x, y, stack);
        }

        if (hovered) {
            guiGraphics.fill(x, y, x + PAGE_WIDTH, y + PAGE_HEIGHT, 0x30FFFFFF);
        }
    }

    private static void drawPageContents(GuiGraphics guiGraphics, int x, int y, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        if (Page.isLinkPanel(stack)) {
            guiGraphics.fill(
                    x + 5,
                    y + 8,
                    x + PAGE_WIDTH - 5,
                    y + 18,
                    0xFF000000
            );
            return;
        }

        Identifier symbol = Page.getSymbol(stack);
        if (symbol != null) {
            Identifier texture = PageRenderCache.getTexture(new PageRenderKey(PageRenderKey.Kind.SYMBOL, symbol));

            int symbolSize = 16;
            int drawX = x + (PAGE_WIDTH - symbolSize) / 2;
            int drawY = y + 12;

            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    texture,
                    drawX,
                    drawY,
                    0,
                    0,
                    symbolSize,
                    symbolSize,
                    symbolSize,
                    symbolSize
            );
        }
    }

    public record SurfaceEntry(
            int slotIndex,
            ItemStack stack,
            boolean placeholder,
            int x,
            int y
    ) {
    }
}