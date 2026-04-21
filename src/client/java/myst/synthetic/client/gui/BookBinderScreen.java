package myst.synthetic.client.gui;

import myst.synthetic.client.render.PageCardRenderer;
import myst.synthetic.menu.BookBinderMenu;
import myst.synthetic.network.BookBinderTitlePayload;
import myst.synthetic.page.Page;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;

public class BookBinderScreen extends AbstractContainerScreen<BookBinderMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/pagebinder.png");

    private static final int GUI_W = 176;
    private static final int GUI_H = 181;

    private static final int TITLE_X = 7;
    private static final int TITLE_Y = 9;
    private static final int TITLE_W = GUI_W - 60;
    private static final int TITLE_H = 14;

    private static final int STRIP_X = 7;
    private static final int STRIP_Y = 45;
    private static final int STRIP_W = GUI_W - 14;
    private static final int STRIP_H = 40;

    private static final int ARROW_W = STRIP_H / 5; // legacy-style = 8
    private static final int PAGE_W = 25;
    private static final int PAGE_H = 34;
    private static final int PAGE_GAP = 2;

    private static final int MISSING_ICON_X = 27;
    private static final int MISSING_ICON_Y = 26;
    private static final int MISSING_ICON_W = 18;
    private static final int MISSING_ICON_H = 18;

    private static final int MISSING_ICON_U = 176;
    private static final int MISSING_ICON_V = 0;
    private static final int MISSING_ICON_SRC_W = 30;
    private static final int MISSING_ICON_SRC_H = 40;

    private EditBox titleBox;
    private int scroll = 0;
    private String lastSentTitle = "";

    public BookBinderScreen(BookBinderMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = GUI_W;
        this.imageHeight = GUI_H;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 87;
        this.titleLabelX = 0;
        this.titleLabelY = 10000;
    }

    @Override
    protected void init() {
        super.init();

        this.titleBox = new EditBox(
                this.font,
                this.leftPos + TITLE_X + 2,
                this.topPos + TITLE_Y + 2,
                TITLE_W - 4,
                TITLE_H - 4,
                Component.literal("Book Title")
        );
        this.titleBox.setBordered(false);
        this.titleBox.setMaxLength(21);
        this.titleBox.setValue(this.menu.getSyncedTitle());
        this.lastSentTitle = this.titleBox.getValue();
        this.addRenderableWidget(this.titleBox);
    }

    @Override
    public void containerTick() {
        super.containerTick();

        String synced = this.menu.getSyncedTitle();
        if (!this.titleBox.isFocused() && !synced.equals(this.titleBox.getValue())) {
            this.titleBox.setValue(synced);
            this.lastSentTitle = synced;
        }

        int maxScroll = Math.max(0, this.menu.getPageCount() - 1);
        this.scroll = Math.max(0, Math.min(this.scroll, maxScroll));
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (this.titleBox != null && this.titleBox.isFocused()) {
            if (this.titleBox.charTyped(input)) {
                this.pushTitle();
                return true;
            }
            return true;
        }

        return super.charTyped(input);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (this.titleBox != null && this.titleBox.isFocused()) {
            if (input.key() == 256) {
                return super.keyPressed(input);
            }

            if (this.titleBox.keyPressed(input)) {
                this.pushTitle();
                return true;
            }

            return true;
        }

        if (input.key() == 263) { // left
            this.scroll = Math.max(0, this.scroll - 1);
            return true;
        }

        if (input.key() == 262) { // right
            int maxScroll = Math.max(0, this.menu.getPageCount() - 1);
            this.scroll = Math.min(maxScroll, this.scroll + 1);
            return true;
        }

        return super.keyPressed(input);
    }

    private void pushTitle() {
        String value = this.titleBox.getValue();
        if (value.equals(this.lastSentTitle)) {
            return;
        }

        this.lastSentTitle = value;
        ClientPlayNetworking.send(new BookBinderTitlePayload(this.menu.containerId, value));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                this.leftPos,
                this.topPos,
                0,
                0,
                this.imageWidth,
                this.imageHeight,
                256,
                256
        );

        drawPageStrip(guiGraphics, mouseX, mouseY);
        drawMissingPanelWarning(guiGraphics, mouseX, mouseY);
    }

    private void drawPageStrip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int stripLeft = this.leftPos + STRIP_X;
        int stripTop = this.topPos + STRIP_Y;

        guiGraphics.fill(stripLeft, stripTop, stripLeft + STRIP_W, stripTop + STRIP_H, 0xAA000000);

        int hoveredIndex = getHoveredPageIndex(mouseX, mouseY);

        int drawX = stripLeft + 2;
        int drawY = stripTop + 3;

        int pageCount = this.menu.getPageCount();

        for (int visible = 0; ; visible++) {
            int x = drawX + visible * (PAGE_W + PAGE_GAP);
            if (x + PAGE_W > stripLeft + STRIP_W - ARROW_W) {
                break;
            }

            int index = this.scroll + visible;
            boolean hasPage = index < pageCount;
            ItemStack stack = hasPage ? this.menu.getPageAt(index) : ItemStack.EMPTY;

            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(x, drawY);
            guiGraphics.pose().scale(PAGE_W / (float) PageCardRenderer.CARD_WIDTH, PAGE_H / (float) PageCardRenderer.CARD_HEIGHT);
            PageCardRenderer.drawPageCard(guiGraphics, 0, 0, stack, !hasPage, hoveredIndex == index);
            guiGraphics.pose().popMatrix();
        }

        int leftArrowColor = this.scroll == 0 ? 0x33000000 : 0xAA000000;
        guiGraphics.fill(stripLeft, stripTop, stripLeft + ARROW_W, stripTop + STRIP_H, leftArrowColor);

        int rightArrowColor = (pageCount == 0 || this.scroll >= pageCount - 1) ? 0x33000000 : 0xAA000000;
        guiGraphics.fill(stripLeft + STRIP_W - ARROW_W, stripTop, stripLeft + STRIP_W, stripTop + STRIP_H, rightArrowColor);

        if (this.scroll > 0) {
            guiGraphics.drawString(this.font, "<", stripLeft + 1, stripTop + 14, 0xFFFFFFFF, false);
        }
        if (pageCount > 0 && this.scroll < pageCount - 1) {
            guiGraphics.drawString(this.font, ">", stripLeft + STRIP_W - 6, stripTop + 14, 0xFFFFFFFF, false);
        }
    }

    private void drawMissingPanelWarning(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ItemStack first = this.menu.getPageAt(0);
        boolean missing = first.isEmpty() || !Page.isLinkPanel(first);
        if (!missing) {
            return;
        }

        int x = this.leftPos + MISSING_ICON_X;
        int y = this.topPos + MISSING_ICON_Y;

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                x,
                y,
                MISSING_ICON_U,
                MISSING_ICON_V,
                MISSING_ICON_W,
                MISSING_ICON_H,
                256,
                256
        );

        float alpha = getMissingPanelPulseAlpha();
        int overlayAlpha = Math.max(0, Math.min(255, (int) (alpha * 255.0F)));
        int overlay = (overlayAlpha << 24) | (0xFF << 16) | (0x80 << 8) | 0x80;
        guiGraphics.fill(x, y, x + MISSING_ICON_W, y + MISSING_ICON_H, overlay);

        if (mouseX >= x && mouseX < x + MISSING_ICON_W && mouseY >= y && mouseY < y + MISSING_ICON_H) {
            List<ClientTooltipComponent> tooltip = new ArrayList<>();
            tooltip.add(new ClientTextTooltip(Component.literal("Missing Link Panel").getVisualOrderText()));
            tooltip.add(new ClientTextTooltip(Component.literal("Add a link panel as the first page of the book.").getVisualOrderText()));

            guiGraphics.renderTooltip(
                    this.font,
                    tooltip,
                    mouseX,
                    mouseY,
                    DefaultTooltipPositioner.INSTANCE,
                    null
            );
        }
    }

    private float getMissingPanelPulseAlpha() {
        long time = System.currentTimeMillis();
        float alpha = (time % 4000L) / 2000.0F;
        if (alpha > 1.0F) {
            alpha = 2.0F - alpha;
        }
        alpha += 0.3F;
        return Math.min(alpha, 1.0F);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderPageTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderPageTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int hoveredIndex = getHoveredPageIndex(mouseX, mouseY);
        if (hoveredIndex < 0 || hoveredIndex >= this.menu.getPageCount() || this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        ItemStack stack = this.menu.getPageAt(hoveredIndex);
        if (stack.isEmpty()) {
            return;
        }

        List<ClientTooltipComponent> tooltip = new ArrayList<>();
        for (Component line : stack.getTooltipLines(Item.TooltipContext.EMPTY, this.minecraft.player, TooltipFlag.Default.NORMAL)) {
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
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isOverStrip(mouseX, mouseY)) {
            if (scrollY > 0) {
                this.scroll = Math.max(0, this.scroll - 1);
                return true;
            }

            if (scrollY < 0) {
                int maxScroll = Math.max(0, this.menu.getPageCount() - 1);
                this.scroll = Math.min(maxScroll, this.scroll + 1);
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int mouseX = (int) event.x();
        int mouseY = (int) event.y();

        if (this.titleBox != null && this.titleBox.mouseClicked(event, doubleClick)) {
            return true;
        }

        int stripLeft = this.leftPos + STRIP_X;
        int stripTop = this.topPos + STRIP_Y;

        if (mouseX >= stripLeft && mouseX < stripLeft + ARROW_W
                && mouseY >= stripTop && mouseY < stripTop + STRIP_H) {
            this.scroll = Math.max(0, this.scroll - 1);
            return true;
        }

        if (mouseX >= stripLeft + STRIP_W - ARROW_W && mouseX < stripLeft + STRIP_W
                && mouseY >= stripTop && mouseY < stripTop + STRIP_H) {
            int maxScroll = Math.max(0, this.menu.getPageCount() - 1);
            this.scroll = Math.min(maxScroll, this.scroll + 1);
            return true;
        }

        if (isOverStrip(mouseX, mouseY) && this.minecraft != null && this.minecraft.gameMode != null) {
            int hoveredIndex = getHoveredPageIndex(mouseX, mouseY);
            int pageCount = this.menu.getPageCount();

            if (hoveredIndex >= 0 && hoveredIndex < pageCount && this.menu.getCarried().isEmpty()) {
                this.minecraft.gameMode.handleInventoryButtonClick(
                        this.menu.containerId,
                        BookBinderMenu.BUTTON_REMOVE_AT_START + hoveredIndex
                );
                return true;
            }

            int insertIndex = hoveredIndex >= 0 ? Math.min(hoveredIndex, pageCount) : pageCount;
            int base = event.button() == 1
                    ? BookBinderMenu.BUTTON_INSERT_SINGLE_AT_START
                    : BookBinderMenu.BUTTON_INSERT_AT_START;

            this.minecraft.gameMode.handleInventoryButtonClick(
                    this.menu.containerId,
                    base + insertIndex
            );
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    private int getHoveredPageIndex(int mouseX, int mouseY) {
        if (!isOverStrip(mouseX, mouseY)) {
            return -1;
        }

        int stripLeft = this.leftPos + STRIP_X;
        int stripTop = this.topPos + STRIP_Y;

        int pageStartX = stripLeft + 2;
        int pageStartY = stripTop + 3;

        for (int visible = 0; ; visible++) {
            int x = pageStartX + visible * (PAGE_W + PAGE_GAP);
            if (x + PAGE_W > stripLeft + STRIP_W - ARROW_W) {
                break;
            }

            if (mouseX >= x && mouseX < x + PAGE_W && mouseY >= pageStartY && mouseY < pageStartY + PAGE_H) {
                return this.scroll + visible;
            }
        }

        return -1;
    }

    private boolean isOverStrip(double mouseX, double mouseY) {
        return mouseX >= this.leftPos + STRIP_X
                && mouseX < this.leftPos + STRIP_X + STRIP_W
                && mouseY >= this.topPos + STRIP_Y
                && mouseY < this.topPos + STRIP_Y + STRIP_H;
    }
}