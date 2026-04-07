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

    public static final Identifier PAGE_BACKGROUND =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/item/page_background.png");

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

    public static void drawPlaceholderPage(GuiGraphics guiGraphics, int x, int y, boolean hovered) {
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                PAGE_BACKGROUND,
                x,
                y,
                0,
                0,
                PAGE_WIDTH,
                PAGE_HEIGHT,
                16,
                16
        );

        guiGraphics.fill(x, y, x + PAGE_WIDTH, y + PAGE_HEIGHT, 0xAA111111);

        if (hovered) {
            guiGraphics.fill(x, y, x + PAGE_WIDTH, y + PAGE_HEIGHT, 0x40FFFFFF);
        }
    }

    public static void drawRealPage(GuiGraphics guiGraphics, int x, int y, ItemStack stack, boolean hovered) {
        Identifier texture = getPageTexture(stack);

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                x,
                y,
                0,
                0,
                PAGE_WIDTH,
                PAGE_HEIGHT,
                16,
                16
        );

        if (hovered) {
            guiGraphics.fill(x, y, x + PAGE_WIDTH, y + PAGE_HEIGHT, 0x30FFFFFF);
        }
    }

    public static void drawScrollbar(GuiGraphics guiGraphics, int guiLeft, int guiTop, int scroll, int maxScroll) {
        int x = guiLeft + SCROLLBAR_X;
        int y = guiTop + SCROLLBAR_Y;

        guiGraphics.fill(x + 7, y, x + 13, y + SCROLLBAR_HEIGHT, 0xFF5A5A5A);

        int knobHeight = 16;
        int knobTravel = SCROLLBAR_HEIGHT - knobHeight;
        int knobOffset = maxScroll <= 0 ? 0 : (scroll * knobTravel) / maxScroll;

        guiGraphics.fill(x + 4, y + knobOffset, x + 16, y + knobOffset + knobHeight, 0xFFCFCFCF);
        guiGraphics.fill(x + 5, y + knobOffset + 1, x + 15, y + knobOffset + knobHeight - 1, 0xFF9A9A9A);
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
        int clipBottom = clipTop + SURFACE_HEIGHT;

        for (SurfaceEntry entry : entries) {
            int drawX = guiLeft + SURFACE_X + entry.x();
            int drawY = guiTop + SURFACE_Y + entry.y() - scroll;

            if (drawY + PAGE_HEIGHT < clipTop || drawY > clipBottom) {
                continue;
            }

            boolean hovered =
                    mouseX >= drawX && mouseX < drawX + PAGE_WIDTH
                            && mouseY >= drawY && mouseY < drawY + PAGE_HEIGHT;

            if (entry.placeholder()) {
                drawPlaceholderPage(guiGraphics, drawX, drawY, hovered);
            } else {
                drawRealPage(guiGraphics, drawX, drawY, entry.stack(), hovered);
            }
        }
    }

    public static int getHoveredOrderedSlot(int guiLeft, int guiTop, int mouseX, int mouseY, int scroll) {
        int relX = mouseX - (guiLeft + SURFACE_X);
        int relY = mouseY - (guiTop + SURFACE_Y) + scroll;

        if (relX < 0 || relY < 0 || relX >= SURFACE_PAGE_WIDTH || relY >= SURFACE_HEIGHT + scroll) {
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

    public static Identifier getPageTexture(ItemStack stack) {
        if (stack.isEmpty()) {
            return PAGE_BACKGROUND;
        }

        if (Page.isLinkPanel(stack)) {
            return PageRenderCache.getTexture(new PageRenderKey(PageRenderKey.Kind.LINK_PANEL, null));
        }

        Identifier symbol = Page.getSymbol(stack);
        if (symbol != null) {
            return PageRenderCache.getTexture(new PageRenderKey(PageRenderKey.Kind.SYMBOL, symbol));
        }

        return PageRenderCache.getTexture(new PageRenderKey(PageRenderKey.Kind.BLANK, null));
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