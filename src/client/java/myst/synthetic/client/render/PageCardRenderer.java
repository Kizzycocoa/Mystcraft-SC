package myst.synthetic.client.render;

import myst.synthetic.client.page.PageRenderCache;
import myst.synthetic.page.Page;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class PageCardRenderer {

    public static final Identifier PAGE_CARD_TEXTURE =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/bookui_pagel.png");

    public static final int CARD_WIDTH = 30;
    public static final int CARD_HEIGHT = 40;

    // Legacy-style inner drawing window:
    // the old drawPage() used a 29x29 symbol region centered vertically on the 30x40 page.
    private static final int INNER_X = 1;
    private static final int INNER_Y = 5;
    private static final int INNER_W = 29;
    private static final int INNER_H = 29;

    private PageCardRenderer() {
    }

    public static void drawPageCard(GuiGraphics guiGraphics, int x, int y, ItemStack stack, boolean placeholder, boolean hovered) {
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                PAGE_CARD_TEXTURE,
                x,
                y,
                156,
                0,
                CARD_WIDTH,
                CARD_HEIGHT,
                256,
                256
        );

        if (placeholder) {
            guiGraphics.fill(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, 0xAA111111);
        } else {
            drawPageContents(guiGraphics, x, y, stack);
        }

        if (hovered) {
            guiGraphics.fill(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, 0x30FFFFFF);
        }
    }

    private static void drawPageContents(GuiGraphics guiGraphics, int x, int y, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        Identifier texture = getComposedTexture(stack);

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                x + INNER_X,
                y + INNER_Y,
                0,
                0,
                INNER_W,
                INNER_H,
                16,
                16
        );
    }

    private static Identifier getComposedTexture(ItemStack stack) {
        if (Page.isLinkPanel(stack)) {
            return PageRenderCache.getTexture(new PageRenderKey(PageRenderKey.Kind.LINK_PANEL, null));
        }

        Identifier symbol = Page.getSymbol(stack);
        if (symbol != null) {
            return PageRenderCache.getTexture(new PageRenderKey(PageRenderKey.Kind.SYMBOL, symbol));
        }

        return PageRenderCache.getTexture(new PageRenderKey(PageRenderKey.Kind.BLANK, null));
    }
}