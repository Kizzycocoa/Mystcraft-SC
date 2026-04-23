package myst.synthetic.client.gui;

import myst.synthetic.client.page.PageRenderCache;
import myst.synthetic.client.page.PageTextureCompositor;
import myst.synthetic.client.render.PageRenderKey;
import myst.synthetic.component.AgebookDataComponent;
import myst.synthetic.item.BookBookmarkUtil;
import myst.synthetic.item.BookmarkColorUtil;
import myst.synthetic.item.ItemAgebook;
import myst.synthetic.network.DisplayContainerExtractPayload;
import myst.synthetic.network.LinkBookBookmarkExtractPayload;
import myst.synthetic.network.LinkBookUsePayload;
import myst.synthetic.page.Page;
import myst.synthetic.page.symbol.PageSymbol;
import myst.synthetic.page.symbol.PageSymbolRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LinkBookScreen extends Screen {

    private static final Identifier BOOK_COVER = Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/bookui_cover.png");
    private static final Identifier BOOK_PAGE_LEFT = Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/bookui_pagel.png");
    private static final Identifier BOOKMARK_TEXTURE = Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/bookui_bookmark.png");
    private static final Identifier BOOKMARK_OVERLAY_TEXTURE = Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/bookui_bookmark_overlay.png");

    private static final int GUI_WIDTH = 327;
    private static final int GUI_HEIGHT = 199;

    private static final int CONTAINER_SLOT_X = 41;
    private static final int CONTAINER_SLOT_Y = 21;

    private static final int BOOKMARK_X = 100;
    private static final int BOOKMARK_Y = 2;
    private static final int BOOKMARK_W = 46;
    private static final int BOOKMARK_H = 162;

    private static final int PAGE_CONTENT_X = 171;
    private static final int PAGE_CONTENT_Y = 25;
    private static final int PAGE_CONTENT_SIZE = 140;

    private final ItemStack bookStack;
    @Nullable
    private final BlockPos containerPos;

    private int leftPos;
    private int topPos;
    private int currentPageIndex = 0;

    public LinkBookScreen(ItemStack bookStack) {
        this(bookStack, null);
    }

    public LinkBookScreen(ItemStack bookStack, @Nullable BlockPos containerPos) {
        super(Component.literal("Book"));
        this.bookStack = bookStack;
        this.containerPos = containerPos;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - GUI_WIDTH) / 2;
        this.topPos = (this.height - GUI_HEIGHT) / 2;
        this.currentPageIndex = Math.max(0, Math.min(this.currentPageIndex, getPageCount()));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (input.key() == 263) { // left
            pageLeft();
            return true;
        }

        if (input.key() == 262) { // right
            pageRight();
            return true;
        }

        return super.keyPressed(input);
    }

    private void renderItemTooltip(GuiGraphics guiGraphics, ItemStack stack, int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || stack.isEmpty()) {
            return;
        }

        List<ClientTooltipComponent> tooltip = new ArrayList<>();

        for (var line : stack.getTooltipLines(
                net.minecraft.world.item.Item.TooltipContext.EMPTY,
                minecraft.player,
                TooltipFlag.Default.NORMAL
        )) {
            tooltip.add(new ClientTextTooltip(line.getVisualOrderText()));
        }

        guiGraphics.renderTooltip(
                this.font,
                tooltip,
                mouseX,
                mouseY,
                DefaultTooltipPositioner.INSTANCE,
                null
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        drawBook(guiGraphics, mouseX, mouseY);
        drawBookText(guiGraphics);

        boolean hoveringContainer = false;
        boolean hoveringBookmark = drawBookmarkIfPresent(guiGraphics, mouseX, mouseY);
        boolean hoveringPage = isMouseOverCurrentPage(mouseX, mouseY);

        if (this.containerPos != null) {
            hoveringContainer = drawContainerSlot(guiGraphics, mouseX, mouseY);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (hoveringContainer && !this.bookStack.isEmpty()) {
            renderItemTooltip(guiGraphics, this.bookStack, mouseX, mouseY);
        } else if (hoveringBookmark) {
            ItemStack bookmark = BookBookmarkUtil.getBookmark(this.bookStack);
            if (!bookmark.isEmpty()) {
                renderItemTooltip(guiGraphics, bookmark, mouseX, mouseY);
            }
        } else if (hoveringPage) {
            ItemStack page = getCurrentPageStack();
            if (!page.isEmpty()) {
                renderItemTooltip(guiGraphics, page, mouseX, mouseY);
            }
        }
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

    private void drawBook(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        drawRegion(guiGraphics, BOOK_COVER, x + 0,   y + 7, 152, 0, 34, 192, 256, 256);
        drawRegion(guiGraphics, BOOK_COVER, x + 34,  y + 7, 49,  0, 103, 192, 256, 256);
        drawRegion(guiGraphics, BOOK_COVER, x + 137, y + 7, 45,  0,   4, 192, 256, 256);
        drawRegion(guiGraphics, BOOK_COVER, x + 141, y + 7, 0,   0, 186, 192, 256, 256);

        if (isAgebook()) {
            drawRegion(guiGraphics, BOOK_COVER, x + 0,   y + 7, 186, 0, 34, 192, 256, 256);
            drawRegion(guiGraphics, BOOK_COVER, x + 293, y + 7, 186, 0, 34, 192, 256, 256);
        }

        if (this.currentPageIndex > 0) {
            drawRegion(guiGraphics, BOOK_PAGE_LEFT, x + 7, y + 0, 0, 0, 156, 195, 256, 256);
        }

        ItemStack page = getCurrentPageStack();
        if (!page.isEmpty() && Page.isLinkPanel(page)) {
            LinkPanelGuiRenderer.drawCutoutPanelPage(guiGraphics, x, y, getBookTag());
        } else if (!page.isEmpty()) {
            LinkPanelGuiRenderer.drawSolidRightPage(guiGraphics, x, y);
            drawStoredPage(guiGraphics, x, y, page);
        }

        if (this.currentPageIndex == 0 && hasBookSlotVisible()) {
            drawRegion(guiGraphics, BOOK_COVER, x + 40, y + 20, 156, 0, 18, 18, 256, 256);
        }

        String pageText = this.currentPageIndex + "/" + getPageCount();
        int textWidth = this.font.width(pageText);
        guiGraphics.drawString(this.font, pageText, x + 165 - (textWidth / 2), y + 185, 0xFF000000, false);
    }

    private void drawStoredPage(GuiGraphics guiGraphics, int guiLeft, int guiTop, ItemStack page) {
        Identifier texture = getStoredPageTexture(page);

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                guiLeft + PAGE_CONTENT_X,
                guiTop + PAGE_CONTENT_Y,
                0,
                0,
                PAGE_CONTENT_SIZE,
                PAGE_CONTENT_SIZE,
                PageTextureCompositor.CONTENT_SIZE,
                PageTextureCompositor.CONTENT_SIZE,
                PageTextureCompositor.CONTENT_SIZE,
                PageTextureCompositor.CONTENT_SIZE
        );
    }

    private Identifier getStoredPageTexture(ItemStack page) {
        if (Page.isLinkPanel(page)) {
            return PageRenderCache.getTexture(new PageRenderKey(PageRenderKey.Kind.LINK_PANEL_CONTENT, null));
        }

        Identifier symbolId = Page.getSymbol(page);
        if (symbolId != null) {
            return PageRenderCache.getTexture(new PageRenderKey(PageRenderKey.Kind.SYMBOL_CONTENT, symbolId));
        }

        return PageRenderCache.getTexture(new PageRenderKey(PageRenderKey.Kind.BLANK_CONTENT, null));
    }

    private void drawBookText(GuiGraphics guiGraphics) {
        if (this.currentPageIndex != 0) {
            return;
        }

        int x = this.leftPos;
        int y = this.topPos;

        String title = "Linking Book";
        List<String> authors = new ArrayList<>();
        String ageName = "";

        if (isAgebook()) {
            AgebookDataComponent data = ItemAgebook.getData(this.bookStack);
            title = data.displayName().isBlank() ? "Descriptive Book" : data.displayName();
            authors.addAll(data.authorsCopy());
            ageName = data.displayName();
        } else {
            CompoundTag tag = getBookTag();
            if (tag != null) {
                title = tag.getString("DisplayName").orElse("Linking Book");
                String author = tag.getString("Author").orElse("");
                if (!author.isBlank()) {
                    authors.add(author);
                }
                ageName = tag.getString("AgeName").orElse("");
            }
        }

        guiGraphics.drawString(this.font, title, x + 40, y + 40, 0xFF3F2A17, false);

        int textY = 50;
        for (String author : authors) {
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(x + 50, y + textY);
            guiGraphics.pose().scale(0.5f, 0.5f);
            guiGraphics.drawString(this.font, author, 0, 0, 0xFF3F2A17, false);
            guiGraphics.pose().popMatrix();
            textY += 5;
        }

        if (!ageName.isEmpty()) {
            int panelCenterX = x + LinkPanelGuiRenderer.PANEL_X + (LinkPanelGuiRenderer.PANEL_W / 2);
            int width = this.font.width(ageName);

            guiGraphics.drawString(
                    this.font,
                    ageName,
                    panelCenterX - (width / 2),
                    y + LinkPanelGuiRenderer.PANEL_Y + LinkPanelGuiRenderer.PANEL_H + 6,
                    0xFF3F2A17,
                    false
            );
        }
    }

    private boolean drawBookmarkIfPresent(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ItemStack bookmark = BookBookmarkUtil.getBookmark(this.bookStack);
        if (bookmark.isEmpty()) {
            return false;
        }

        int x = this.leftPos + BOOKMARK_X;
        int y = this.topPos + BOOKMARK_Y;
        int color = 0xFF000000 | BookmarkColorUtil.getColor(bookmark);

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                BOOKMARK_TEXTURE,
                x,
                y,
                0.0F,
                0.0F,
                BOOKMARK_W,
                BOOKMARK_H,
                64,
                256,
                color
        );

        drawRegion(guiGraphics, BOOKMARK_OVERLAY_TEXTURE, x, y, 0, 0, BOOKMARK_W, BOOKMARK_H, 64, 256);

        return mouseX >= x && mouseX < x + BOOKMARK_W && mouseY >= y && mouseY < y + BOOKMARK_H;
    }

    private boolean drawContainerSlot(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int slotLeft = this.leftPos + CONTAINER_SLOT_X;
        int slotTop = this.topPos + CONTAINER_SLOT_Y;

        guiGraphics.fill(slotLeft - 1, slotTop - 1, slotLeft + 17, slotTop + 17, 0xFFA0A0A0);
        guiGraphics.fill(slotLeft, slotTop, slotLeft + 16, slotTop + 16, 0xFF2A2A2A);

        if (!this.bookStack.isEmpty()) {
            guiGraphics.renderItem(this.bookStack, slotLeft, slotTop);
            guiGraphics.renderItemDecorations(this.font, this.bookStack, slotLeft, slotTop);
        }

        boolean hovering = mouseX >= slotLeft && mouseX < slotLeft + 16 && mouseY >= slotTop && mouseY < slotTop + 16;

        if (hovering) {
            guiGraphics.fill(slotLeft, slotTop, slotLeft + 16, slotTop + 16, 0x80FFFFFF);
        }

        return hovering;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        double mouseX = mouseButtonEvent.x();
        double mouseY = mouseButtonEvent.y();

        if (isMouseOverBookmark(mouseX, mouseY)) {
            onBookmarkClicked();
            return true;
        }

        if (this.containerPos != null) {
            int slotLeft = this.leftPos + CONTAINER_SLOT_X;
            int slotTop = this.topPos + CONTAINER_SLOT_Y;
            int slotRight = slotLeft + 16;
            int slotBottom = slotTop + 16;

            if (mouseX >= slotLeft && mouseX <= slotRight && mouseY >= slotTop && mouseY <= slotBottom) {
                ClientPlayNetworking.send(new DisplayContainerExtractPayload(this.containerPos));
                this.onClose();
                return true;
            }
        }

        int guiLeft = this.leftPos;
        int guiTop = this.topPos;

        int panelLeft = guiLeft + LinkPanelGuiRenderer.PANEL_X;
        int panelTop = guiTop + LinkPanelGuiRenderer.PANEL_Y;
        int panelRight = panelLeft + LinkPanelGuiRenderer.PANEL_W;
        int panelBottom = panelTop + LinkPanelGuiRenderer.PANEL_H;

        if (this.currentPageIndex == 0
                && mouseX >= panelLeft && mouseX <= panelRight
                && mouseY >= panelTop && mouseY <= panelBottom) {
            onLinkPanelClicked();
            return true;
        }

        if (mouseX >= guiLeft && mouseX <= guiLeft + 156
                && mouseY >= guiTop && mouseY <= guiTop + 195) {
            pageLeft();
            return true;
        }

        if (mouseX >= guiLeft + 158 && mouseX <= guiLeft + 312
                && mouseY >= guiTop && mouseY <= guiTop + 195) {
            pageRight();
            return true;
        }

        return super.mouseClicked(mouseButtonEvent, bl);
    }

    private boolean isMouseOverBookmark(double mouseX, double mouseY) {
        if (!BookBookmarkUtil.hasBookmark(this.bookStack)) {
            return false;
        }

        int x = this.leftPos + BOOKMARK_X;
        int y = this.topPos + BOOKMARK_Y;
        return mouseX >= x && mouseX < x + BOOKMARK_W && mouseY >= y && mouseY < y + BOOKMARK_H;
    }

    private boolean isMouseOverCurrentPage(int mouseX, int mouseY) {
        ItemStack page = getCurrentPageStack();
        if (page.isEmpty()) {
            return false;
        }

        int x = this.leftPos + PAGE_CONTENT_X;
        int y = this.topPos + PAGE_CONTENT_Y;
        return mouseX >= x && mouseX < x + PAGE_CONTENT_SIZE && mouseY >= y && mouseY < y + PAGE_CONTENT_SIZE;
    }

    private void onBookmarkClicked() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }

        boolean mainHand = client.player.getMainHandItem().is(this.bookStack.getItem());
        ClientPlayNetworking.send(new LinkBookBookmarkExtractPayload(mainHand));
        BookBookmarkUtil.removeBookmark(this.bookStack);
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

    private void pageLeft() {
        this.currentPageIndex--;
        if (this.currentPageIndex < 0) {
            this.currentPageIndex = 0;
        }
    }

    private void pageRight() {
        this.currentPageIndex++;
        if (this.currentPageIndex > getPageCount()) {
            this.currentPageIndex = getPageCount();
        }
    }

    private int getPageCount() {
        if (isAgebook()) {
            return ItemAgebook.getPages(this.bookStack).size();
        }
        return 1;
    }

    private ItemStack getCurrentPageStack() {
        if (!isAgebook()) {
            return this.currentPageIndex == 0 ? this.bookStack : ItemStack.EMPTY;
        }

        List<ItemStack> pages = ItemAgebook.getPages(this.bookStack);
        if (this.currentPageIndex < 0 || this.currentPageIndex >= pages.size()) {
            return ItemStack.EMPTY;
        }

        return pages.get(this.currentPageIndex);
    }

    private boolean hasBookSlotVisible() {
        return this.containerPos != null;
    }

    private boolean isAgebook() {
        return !this.bookStack.isEmpty() && this.bookStack.has(myst.synthetic.component.MystcraftDataComponents.AGEBOOK_DATA);
    }

    private CompoundTag getBookTag() {
        CustomData customData = this.bookStack.get(DataComponents.CUSTOM_DATA);
        return customData != null ? customData.copyTag() : null;
    }
}