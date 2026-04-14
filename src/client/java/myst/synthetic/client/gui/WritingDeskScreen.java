package myst.synthetic.client.gui;

import myst.synthetic.MystcraftItems;
import myst.synthetic.block.entity.BlockEntityDesk;
import myst.synthetic.menu.WritingDeskMenu;
import myst.synthetic.network.WritingDeskTitlePayload;
import myst.synthetic.page.Page;
import myst.synthetic.page.symbol.PageSymbolRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;

public class WritingDeskScreen extends PageBrowserScreen<WritingDeskMenu> {

    /**
     * The writing desk texture is a 256x256 atlas image,
     * but the logical GUI is wider because the page-browser
     * base class renders the left browser area and the desk
     * screen adds the right-hand work area.
     */
    private static final int FULL_WIDTH = 356;
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;

    private static final int TAB_ICON_X = 150;
    private static final int TAB_ICON_Y = 14;
    private static final int TAB_ICON_SPACING = 37;
    private static final int TAB_COUNT = WritingDeskMenu.VISIBLE_TAB_COUNT;

    private EditBox titleBox;
    private String lastSentTitle = "";

    public WritingDeskScreen(WritingDeskMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = FULL_WIDTH;
        this.imageHeight = 256;
        this.inventoryLabelX = 188;
        this.inventoryLabelY = 92;
        this.titleLabelX = 188;
        this.titleLabelY = 92;
    }

    @Override
    protected void init() {
        super.init();

        this.titleBox = new EditBox(
                this.font,
                this.leftPos + 210,
                this.topPos + 7,
                112,
                12,
                Component.translatable("container.mystcraft-sc.writing_desk.title")
        );
        this.titleBox.setCanLoseFocus(true);
        this.titleBox.setBordered(false);
        this.titleBox.setTextColor(0xFFE0E0E0);
        this.titleBox.setTextColorUneditable(0xFF707070);
        this.addRenderableWidget(this.titleBox);

        this.pullTitleFromTarget();
        this.lastSentTitle = this.titleBox.getValue();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.pullTitleFromTargetIfNeeded();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        /*
         * Draw the whole desk texture once, using the actual atlas size.
         * The old broken version used FULL_WIDTH here, which tried to sample
         * 356 pixels from a 256-pixel-wide texture and garbled the result.
         */
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                DESK_TEXTURE,
                this.leftPos,
                this.topPos,
                0,
                0,
                TEXTURE_WIDTH,
                this.imageHeight,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );

        /*
         * Let the page-browser base render its page entries/search overlay logic,
         * but keep our own desk texture as the real background.
         */
        super.renderBg(guiGraphics, partialTick, mouseX, mouseY);

        this.drawTabStrip(guiGraphics, mouseX, mouseY);
        this.drawDeskStatus(guiGraphics);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    protected void buildOwnedEntries(List<DisplayEntry> built, String normalizedSearch) {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        List<ItemStack> pages = this.menu.getActiveTabPages(this.minecraft.player);
        for (int i = 0; i < pages.size(); i++) {
            ItemStack stack = pages.get(i);
            if (stack.isEmpty()) {
                continue;
            }

            String searchName = this.getSearchName(stack);
            if (!normalizedSearch.isEmpty() && !normalize(searchName).contains(normalizedSearch)) {
                continue;
            }

            DisplayEntry existing = null;
            for (DisplayEntry entry : built) {
                if (entry.absoluteIndex >= 0 && ItemStack.isSameItemSameComponents(entry.stack, stack)) {
                    existing = entry;
                    break;
                }
            }

            if (existing != null) {
                existing.count++;
            } else {
                built.add(new DisplayEntry(stack.copy(), i, 1, searchName));
            }
        }
    }

    @Override
    protected void onSurfaceClicked(DisplayEntry entry, MouseButtonEvent event) {
        if (this.minecraft == null || this.minecraft.gameMode == null) {
            return;
        }

        if (entry != null && entry.absoluteIndex >= 0) {
            if (event.hasShiftDown()) {
                this.minecraft.gameMode.handleInventoryButtonClick(
                        this.menu.containerId,
                        WritingDeskMenu.BUTTON_REMOVE_ACTIVE_TO_INVENTORY_START + entry.absoluteIndex
                );
                return;
            }

            this.minecraft.gameMode.handleInventoryButtonClick(
                    this.menu.containerId,
                    WritingDeskMenu.BUTTON_REMOVE_ACTIVE_PAGE_START + entry.absoluteIndex
            );
            return;
        }

        if (entry != null && entry.absoluteIndex < 0) {
            int symbolIndex = 0;
            for (var symbol : PageSymbolRegistry.values()) {
                if (symbol.id().equals(Page.getSymbol(entry.stack))) {
                    this.minecraft.gameMode.handleInventoryButtonClick(
                            this.menu.containerId,
                            WritingDeskMenu.BUTTON_WRITE_SYMBOL_START + symbolIndex
                    );
                    break;
                }
                symbolIndex++;
            }
        }
    }

    @Override
    protected boolean allowShowAllSymbols() {
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int mouseX = (int) event.x();
        int mouseY = (int) event.y();

        if (this.handleTabClick(mouseX, mouseY, event)) {
            return true;
        }

        if (this.handleSurfaceInsertClick(mouseX, mouseY, event)) {
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        boolean handled = super.keyPressed(input);
        this.pushTitleIfChanged();

        if (input.key() == 257 && this.minecraft != null && this.minecraft.gameMode != null && this.menu.canUseLink()) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, WritingDeskMenu.BUTTON_USE_LINK);
            return true;
        }

        return handled;
    }

    @Override
    public boolean charTyped(net.minecraft.client.input.CharacterEvent input) {
        boolean handled = super.charTyped(input);
        this.pushTitleIfChanged();
        return handled;
    }

    @Override
    public void removed() {
        this.pushTitleIfChanged();
        super.removed();
    }

    private void pullTitleFromTarget() {
        if (this.titleBox == null) {
            return;
        }

        ItemStack target = this.menu.getTargetStack();
        String current = target.get(DataComponents.CUSTOM_NAME) != null
                ? target.get(DataComponents.CUSTOM_NAME).getString()
                : "";

        this.titleBox.setValue(current);
    }

    private void pullTitleFromTargetIfNeeded() {
        if (this.titleBox == null || this.titleBox.isFocused()) {
            return;
        }

        ItemStack target = this.menu.getTargetStack();
        String current = target.get(DataComponents.CUSTOM_NAME) != null
                ? target.get(DataComponents.CUSTOM_NAME).getString()
                : "";

        if (!current.equals(this.titleBox.getValue())) {
            this.titleBox.setValue(current);
            this.lastSentTitle = current;
        }
    }

    private void pushTitleIfChanged() {
        if (this.titleBox == null) {
            return;
        }

        String value = this.titleBox.getValue();
        if (value.equals(this.lastSentTitle)) {
            return;
        }

        this.lastSentTitle = value;
        ClientPlayNetworking.send(new WritingDeskTitlePayload(this.menu.containerId, value));
    }

    private void shiftTabWindow(int delta) {
        if (this.minecraft == null || this.minecraft.gameMode == null) {
            return;
        }

        int first = Math.max(
                0,
                Math.min(BlockEntityDesk.TAB_SLOT_COUNT - TAB_COUNT, this.menu.getFirstVisibleTab() + delta)
        );

        this.minecraft.gameMode.handleInventoryButtonClick(
                this.menu.containerId,
                WritingDeskMenu.BUTTON_SET_FIRST_TAB_START + first
        );
    }

    private boolean handleTabClick(int mouseX, int mouseY, MouseButtonEvent event) {
        if (this.minecraft == null || this.minecraft.gameMode == null) {
            return false;
        }

        for (int i = 0; i < TAB_COUNT; i++) {
            int x = this.leftPos + TAB_ICON_X;
            int y = this.topPos + TAB_ICON_Y + i * TAB_ICON_SPACING;

            if (mouseX < x || mouseX >= x + 18 || mouseY < y || mouseY >= y + 18) {
                continue;
            }

            int absoluteTab = this.menu.getFirstVisibleTab() + i;
            if (!this.menu.getCarried().isEmpty() && this.menu.getCarried().is(MystcraftItems.PAGE)) {
                this.minecraft.gameMode.handleInventoryButtonClick(
                        this.menu.containerId,
                        WritingDeskMenu.BUTTON_ADD_CARRIED_TO_TAB_START + absoluteTab
                );
            } else {
                this.minecraft.gameMode.handleInventoryButtonClick(
                        this.menu.containerId,
                        WritingDeskMenu.BUTTON_SELECT_TAB_START + absoluteTab
                );
            }
            return true;
        }

        /*
         * Scroll upper arrow
         */
        int upX = this.leftPos + TAB_ICON_X + 26;
        int upY = this.topPos + 3;
        if (mouseX >= upX && mouseX < upX + 12 && mouseY >= upY && mouseY < upY + 12) {
            this.shiftTabWindow(-1);
            return true;
        }

        /*
         * Scroll lower arrow
         */
        int downX = this.leftPos + TAB_ICON_X + 26;
        int downY = this.topPos + 157;
        if (mouseX >= downX && mouseX < downX + 12 && mouseY >= downY && mouseY < downY + 12) {
            this.shiftTabWindow(1);
            return true;
        }

        return false;
    }

    private boolean handleSurfaceInsertClick(int mouseX, int mouseY, MouseButtonEvent event) {
        if (this.minecraft == null || this.minecraft.gameMode == null) {
            return false;
        }

        if (this.menu.getCarried().isEmpty() || !this.menu.getCarried().is(MystcraftItems.PAGE)) {
            return false;
        }

        if (!PageSurfaceRenderer.isOverPageArea(this.leftPos, this.topPos, mouseX, mouseY)) {
            return false;
        }

        int index = this.displayEntries.size();
        DisplayEntry hovered = this.getHoveredEntry(mouseX, mouseY);
        if (hovered != null && hovered.absoluteIndex >= 0) {
            index = hovered.absoluteIndex;
        }

        this.minecraft.gameMode.handleInventoryButtonClick(
                this.menu.containerId,
                WritingDeskMenu.BUTTON_PLACE_CARRIED_AT_START + index
        );
        return true;
    }

    private void drawTabStrip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (int i = 0; i < TAB_COUNT; i++) {
            int absoluteTab = this.menu.getFirstVisibleTab() + i;
            ItemStack stack = this.menu.getVisibleTabStack(i);
            int x = this.leftPos + TAB_ICON_X;
            int y = this.topPos + TAB_ICON_Y + i * TAB_ICON_SPACING;

            guiGraphics.fill(
                    x - 1,
                    y - 1,
                    x + 17,
                    y + 17,
                    absoluteTab == this.menu.getActiveTab() ? 0x80FFFFFF : 0x40000000
            );

            if (!stack.isEmpty()) {
                guiGraphics.renderItem(stack, x, y);
                guiGraphics.renderItemDecorations(this.font, stack, x, y);
            }

            if (mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18 && !stack.isEmpty()) {
                List<ClientTooltipComponent> tooltip = new ArrayList<>();

                for (var line : stack.getTooltipLines(
                        net.minecraft.world.item.Item.TooltipContext.EMPTY,
                        this.minecraft.player,
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
        }
    }

    private void drawDeskStatus(GuiGraphics guiGraphics) {
        guiGraphics.drawString(
                this.font,
                Component.literal(this.menu.hasInk() ? "Ink: Full" : "Ink: Empty"),
                this.leftPos + 278,
                this.topPos + 61,
                0xFFE0E0E0,
                false
        );

        guiGraphics.drawString(
                this.font,
                Component.literal("Tab " + (this.menu.getActiveTab() + 1)),
                this.leftPos + 210,
                this.topPos + 61,
                0xFFE0E0E0,
                false
        );

        if (this.menu.canUseLink()) {
            guiGraphics.drawString(
                    this.font,
                    Component.literal("Enter: link"),
                    this.leftPos + 266,
                    this.topPos + 7,
                    0xFFB0E0FF,
                    false
            );
        }
    }
}