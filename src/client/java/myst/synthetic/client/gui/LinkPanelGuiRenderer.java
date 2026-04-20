package myst.synthetic.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public final class LinkPanelGuiRenderer {

    public static final Identifier BOOK_PAGE_RIGHT =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/bookui_pager.png");

    public static final Identifier BOOK_PAGE_RIGHT_FULL =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/bookui_rpage_full.png");

    public static final int PANEL_X = 173;
    public static final int PANEL_Y = 20;
    public static final int PANEL_W = 132;
    public static final int PANEL_H = 83;

    private static final int PANEL_GRADIENT_TOP = 0xFF000044;
    private static final int PANEL_GRADIENT_BOTTOM = 0xFF006666;
    private static final int PANEL_BLACK = 0xFF000000;

    private LinkPanelGuiRenderer() {
    }

    public static void drawCutoutPanelPage(GuiGraphics guiGraphics, int guiLeft, int guiTop, @Nullable CompoundTag bookTag) {
        int panelLeft = guiLeft + PANEL_X;
        int panelTop = guiTop + PANEL_Y;
        int panelRight = panelLeft + PANEL_W;
        int panelBottom = panelTop + PANEL_H;

        if (hasLinkedTarget(bookTag)) {
            guiGraphics.fillGradient(
                    panelLeft,
                    panelTop,
                    panelRight,
                    panelBottom,
                    PANEL_GRADIENT_TOP,
                    PANEL_GRADIENT_BOTTOM
            );
        } else {
            guiGraphics.fill(panelLeft, panelTop, panelRight, panelBottom, PANEL_BLACK);
        }

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                BOOK_PAGE_RIGHT,
                guiLeft + 163,
                guiTop,
                0,
                0,
                156,
                195,
                256,
                256
        );
    }

    public static void drawSolidRightPage(GuiGraphics guiGraphics, int guiLeft, int guiTop) {
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                BOOK_PAGE_RIGHT_FULL,
                guiLeft + 163,
                guiTop,
                0,
                0,
                156,
                195,
                256,
                256
        );
    }

    private static boolean hasLinkedTarget(@Nullable CompoundTag tag) {
        if (tag == null) {
            return false;
        }

        if (!tag.contains("Dimension")) {
            return false;
        }

        String dimension = tag.getString("Dimension").orElse("");
        if (dimension.isBlank()) {
            return false;
        }

        return tag.contains("SpawnX") && tag.contains("SpawnY") && tag.contains("SpawnZ");
    }
}