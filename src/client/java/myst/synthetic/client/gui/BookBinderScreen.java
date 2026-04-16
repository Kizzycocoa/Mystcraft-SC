package myst.synthetic.client.gui;

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
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BookBinderScreen extends AbstractContainerScreen<BookBinderMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/pagebinder.png");

    private static final int GUI_W = 176;
    private static final int GUI_H = 181;

    private static final int TITLE_X = 7;
    private static final int TITLE_Y = 9;
    private static final int TITLE_W = 116;
    private static final int TITLE_H = 14;

    private static final int STRIP_X = 7;
    private static final int STRIP_Y = 45;
    private static final int STRIP_W = 162;
    private static final int STRIP_H = 40;

    private static final int SLOT_STEP = 30;
    private static final int VISIBLE_PAGES = 5;

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

        int maxScroll = Math.max(0, this.menu.getPageCount() - VISIBLE_PAGES);
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

        drawPageStrip(guiGraphics);
        drawMissingPanelWarning(guiGraphics, mouseX, mouseY);
    }

    private void drawPageStrip(GuiGraphics guiGraphics) {
        int pageCount = this.menu.getPageCount();

        for (int visible = 0; visible < VISIBLE_PAGES; visible++) {
            int index = this.scroll + visible;
            if (index >= pageCount) {
                break;
            }

            ItemStack stack = this.menu.getPageAt(index);
            if (stack.isEmpty()) {
                continue;
            }

            int x = this.leftPos + STRIP_X + (visible * SLOT_STEP) + 7;
            int y = this.topPos + STRIP_Y + 8;

            guiGraphics.renderItem(stack, x, y);

            if (Page.isLinkPanel(stack)) {
                guiGraphics.drawString(this.font, "LP", x + 12, y + 10, 0xFF4060A0, false);
            }
        }

        if (this.scroll > 0) {
            guiGraphics.drawString(this.font, "<", this.leftPos + STRIP_X - 1, this.topPos + STRIP_Y + 14, 0xFFFFFFFF, false);
        }

        if (this.scroll + VISIBLE_PAGES < pageCount) {
            guiGraphics.drawString(this.font, ">", this.leftPos + STRIP_X + STRIP_W - 4, this.topPos + STRIP_Y + 14, 0xFFFFFFFF, false);
        }
    }

    private void drawMissingPanelWarning(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ItemStack first = this.menu.getPageAt(0);

        boolean missing = first.isEmpty() || !Page.isLinkPanel(first);
        if (!missing) {
            return;
        }

        int x1 = this.leftPos + 27;
        int y1 = this.topPos + 26;
        int x2 = x1 + 18;
        int y2 = y1 + 18;

        guiGraphics.fill(x1, y1, x2, y2, 0x88FF8080);

        if (mouseX >= x1 && mouseX < x2 && mouseY >= y1 && mouseY < y2) {
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
                int maxScroll = Math.max(0, this.menu.getPageCount() - VISIBLE_PAGES);
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

        if (mouseX >= this.leftPos + STRIP_X - 4 && mouseX < this.leftPos + STRIP_X + 6
                && mouseY >= this.topPos + STRIP_Y && mouseY < this.topPos + STRIP_Y + STRIP_H) {
            this.scroll = Math.max(0, this.scroll - 1);
            return true;
        }

        if (mouseX >= this.leftPos + STRIP_X + STRIP_W - 6 && mouseX < this.leftPos + STRIP_X + STRIP_W + 4
                && mouseY >= this.topPos + STRIP_Y && mouseY < this.topPos + STRIP_Y + STRIP_H) {
            int maxScroll = Math.max(0, this.menu.getPageCount() - VISIBLE_PAGES);
            this.scroll = Math.min(maxScroll, this.scroll + 1);
            return true;
        }

        if (isOverStrip(mouseX, mouseY) && this.minecraft != null && this.minecraft.gameMode != null) {
            int localX = mouseX - (this.leftPos + STRIP_X);
            int slot = Math.max(0, Math.min(VISIBLE_PAGES - 1, localX / SLOT_STEP));
            int index = this.scroll + slot;

            int pageCount = this.menu.getPageCount();

            if (index < pageCount && this.menu.getCarried().isEmpty()) {
                this.minecraft.gameMode.handleInventoryButtonClick(
                        this.menu.containerId,
                        BookBinderMenu.BUTTON_REMOVE_AT_START + index
                );
                return true;
            }

            int insertIndex = Math.min(index, pageCount);
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

    private boolean isOverStrip(double mouseX, double mouseY) {
        return mouseX >= this.leftPos + STRIP_X
                && mouseX < this.leftPos + STRIP_X + STRIP_W
                && mouseY >= this.topPos + STRIP_Y
                && mouseY < this.topPos + STRIP_Y + STRIP_H;
    }
}