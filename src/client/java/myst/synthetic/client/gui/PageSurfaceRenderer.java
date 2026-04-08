package myst.synthetic.client.gui;

import myst.synthetic.client.render.PageCardRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class PageSurfaceRenderer {

    private static final Identifier SCROLLBAR_TEXTURE =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/scrollbar.png");

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
            Font font,
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

            PageCardRenderer.drawPageCard(guiGraphics, drawX, drawY, entry.stack(), entry.placeholder(), hovered);

            if (entry.count() > 1) {
                guiGraphics.drawString(
                        font,
                        Integer.toString(entry.count()),
                        drawX + 1,
                        drawY + 31,
                        0xFFFFFFFF,
                        false
                );
            }
        }

        guiGraphics.disableScissor();
    }

    public static void drawScrollbar(GuiGraphics guiGraphics, int guiLeft, int guiTop, int scroll, int maxScroll) {
        int x = guiLeft + SCROLLBAR_X;
        int y = guiTop + SCROLLBAR_Y;

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                SCROLLBAR_TEXTURE,
                x,
                y,
                0,
                0,
                20,
                4,
                32,
                32
        );

        int middleY = y + 4;
        int middleHeight = SCROLLBAR_HEIGHT - 8;
        for (int dy = 0; dy < middleHeight; dy += 22) {
            int sliceHeight = Math.min(22, middleHeight - dy);
            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    SCROLLBAR_TEXTURE,
                    x,
                    middleY + dy,
                    0,
                    4,
                    20,
                    sliceHeight,
                    32,
                    32
            );
        }

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                SCROLLBAR_TEXTURE,
                x,
                y + SCROLLBAR_HEIGHT - 4,
                0,
                26,
                20,
                4,
                32,
                32
        );

        int sliderTop = y + 4;
        int sliderBottom = y + SCROLLBAR_HEIGHT - 15;
        float sliderPos = maxScroll <= 0 ? 0.0F : scroll / (float) maxScroll;
        if (sliderPos < 0.0F) sliderPos = 0.0F;
        if (sliderPos > 1.0F) sliderPos = 1.0F;

        int knobY = sliderTop + Math.round((sliderBottom - sliderTop) * sliderPos);

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                SCROLLBAR_TEXTURE,
                x + 4,
                knobY,
                20,
                0,
                12,
                15,
                32,
                32
        );
    }

    public static int getHoveredOrderedSlot(int guiLeft, int guiTop, int mouseX, int mouseY, int scroll) {
        int relX = mouseX - (guiLeft + SURFACE_X);
        int relY = mouseY - (guiTop + SURFACE_Y);

        if (relX < 0 || relY < 0 || relX >= SURFACE_PAGE_WIDTH || relY >= SURFACE_HEIGHT) {
            return -1;
        }

        relY += scroll;

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

    public static SurfaceEntry getHoveredEntry(
            int guiLeft,
            int guiTop,
            int mouseX,
            int mouseY,
            int scroll,
            List<SurfaceEntry> entries
    ) {
        for (SurfaceEntry entry : entries) {
            int drawX = guiLeft + SURFACE_X + entry.x();
            int drawY = guiTop + SURFACE_Y + entry.y() - scroll;

            if (mouseX >= drawX && mouseX < drawX + PAGE_WIDTH
                    && mouseY >= drawY && mouseY < drawY + PAGE_HEIGHT) {
                return entry;
            }
        }

        return null;
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

    public static boolean isOverPageArea(int guiLeft, int guiTop, int mouseX, int mouseY) {
        int relX = mouseX - (guiLeft + SURFACE_X);
        int relY = mouseY - (guiTop + SURFACE_Y);

        return relX >= 0
                && relY >= 0
                && relX < SURFACE_PAGE_WIDTH
                && relY < SURFACE_HEIGHT;
    }

    public record SurfaceEntry(
            int slotIndex,
            ItemStack stack,
            boolean placeholder,
            int count,
            String searchName,
            int x,
            int y
    ) {
    }
}