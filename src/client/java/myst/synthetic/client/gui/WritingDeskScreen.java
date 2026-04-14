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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;

public class WritingDeskScreen extends PageBrowserScreen<WritingDeskMenu> {

    private static final int SURFACE_OFFSET_X = 40;

    private static final int DESK_PANEL_X = 180;
    private static final int DESK_PANEL_Y = 0;
    private static final int DESK_PANEL_W = 208;
    private static final int DESK_PANEL_H = 164;

    private static final int DESK_TABS_X = DESK_PANEL_X + 163;
    private static final int DESK_TABS_Y = 7;

    private static final int DESK_TEXTURE_W = 256;
    private static final int DESK_TEXTURE_H = 256;

    private static final int TAB_STRIP_X = 0;
    private static final int TAB_STRIP_Y = 18;
    private static final int TAB_STRIP_W = 36;
    private static final int TAB_STRIP_H = 44;
    private static final int TAB_STRIP_SPACING = 47;
    private static final int TAB_COUNT = WritingDeskMenu.VISIBLE_TAB_COUNT;

    private static final int TITLE_BOX_X = DESK_PANEL_X + 41;
    private static final int TITLE_BOX_Y = 57;
    private static final int TITLE_BOX_W = 89;
    private static final int TITLE_BOX_H = 12;

    private static final int INK_METER_X = DESK_PANEL_X + 148;
    private static final int INK_METER_Y = 22;
    private static final int INK_METER_W = 12;
    private static final int INK_METER_H = 40;

    private EditBox titleBox;
    private String lastSentTitle = "";

    public WritingDeskScreen(WritingDeskMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = DESK_PANEL_X + DESK_PANEL_W;
        this.imageHeight = 166;
        this.inventoryLabelX = 0;
        this.inventoryLabelY = 10000;
        this.titleLabelX = 0;
        this.titleLabelY = 10000;
    }

    @Override
    protected void init() {
        super.init();

        this.titleBox = new EditBox(
                this.font,
                this.leftPos + TITLE_BOX_X,
                this.topPos + TITLE_BOX_Y,
                TITLE_BOX_W,
                TITLE_BOX_H,
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
        this.drawLegacySearchBox(guiGraphics);

        PageSurfaceRenderer.drawSurfaceBackground(guiGraphics, this.leftPos + SURFACE_OFFSET_X, this.topPos);
        PageSurfaceRenderer.drawEntries(
                guiGraphics,
                this.font,
                this.leftPos + SURFACE_OFFSET_X,
                this.topPos,
                mouseX,
                mouseY,
                this.scroll,
                this.toSurfaceEntries()
        );
        PageSurfaceRenderer.drawScrollbar(
                guiGraphics,
                this.leftPos + SURFACE_OFFSET_X,
                this.topPos,
                this.scroll,
                this.getMaxScroll()
        );

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                DESK_TEXTURE,
                this.leftPos + DESK_PANEL_X,
                this.topPos + DESK_PANEL_Y,
                0,
                0,
                DESK_PANEL_W,
                DESK_PANEL_H,
                DESK_TEXTURE_W,
                DESK_TEXTURE_H
        );

        this.drawTabStrip(guiGraphics, mouseX, mouseY);
        this.drawInkMeter(guiGraphics);
        this.drawDeskTabHighlights(guiGraphics);
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
    protected int getMaxScroll() {
        int maxBottom = 0;
        for (DisplayEntry entry : this.displayEntries) {
            int bottom = Math.round(entry.y + PageSurfaceRenderer.PAGE_HEIGHT + 6);
            if (bottom > maxBottom) {
                maxBottom = bottom;
            }
        }
        return Math.max(0, maxBottom - PageSurfaceRenderer.SURFACE_HEIGHT);
    }

    @Override
    protected void scrollToMouse(int mouseY) {
        int maxScroll = this.getMaxScroll();
        if (maxScroll <= 0) {
            this.scroll = 0;
            return;
        }

        int trackTop = this.topPos + PageSurfaceRenderer.SCROLLBAR_Y;
        int trackBottom = trackTop + PageSurfaceRenderer.SCROLLBAR_HEIGHT - 15;
        int clampedMouse = Mth.clamp(mouseY - 7, trackTop, trackBottom);
        int knobTravel = trackBottom - trackTop;
        if (knobTravel <= 0) {
            this.scroll = 0;
            return;
        }

        this.scroll = (int) (((clampedMouse - trackTop) / (float) knobTravel) * maxScroll);
    }

    @Override
    protected boolean isOverScrollbar(int mouseX, int mouseY) {
        int x = this.leftPos + SURFACE_OFFSET_X + PageSurfaceRenderer.SCROLLBAR_X;
        int y = this.topPos + PageSurfaceRenderer.SCROLLBAR_Y;
        return mouseX >= x && mouseX < x + PageSurfaceRenderer.SCROLLBAR_WIDTH
                && mouseY >= y && mouseY < y + PageSurfaceRenderer.SCROLLBAR_HEIGHT;
    }

    @Override
    protected DisplayEntry getHoveredEntry(int mouseX, int mouseY) {
        if (!PageSurfaceRenderer.isOverPageArea(this.leftPos + SURFACE_OFFSET_X, this.topPos, mouseX, mouseY)) {
            return null;
        }

        int clipTop = this.topPos + PageSurfaceRenderer.SURFACE_Y;
        int clipBottom = clipTop + PageSurfaceRenderer.SURFACE_HEIGHT;

        for (DisplayEntry entry : this.displayEntries) {
            int drawX = this.leftPos + SURFACE_OFFSET_X + PageSurfaceRenderer.SURFACE_X + Math.round(entry.x);
            int drawY = this.topPos + PageSurfaceRenderer.SURFACE_Y + Math.round(entry.y) - this.scroll;

            if (drawY + PageSurfaceRenderer.PAGE_HEIGHT <= clipTop || drawY >= clipBottom) {
                continue;
            }

            if (mouseX >= drawX && mouseX < drawX + PageSurfaceRenderer.PAGE_WIDTH
                    && mouseY >= drawY && mouseY < drawY + PageSurfaceRenderer.PAGE_HEIGHT) {
                return entry;
            }
        }

        return null;
    }

    @Override
    protected void renderPageTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!PageSurfaceRenderer.isOverPageArea(this.leftPos + SURFACE_OFFSET_X, this.topPos, mouseX, mouseY)) {
            return;
        }

        DisplayEntry entry = this.getHoveredEntry(mouseX, mouseY);
        if (entry == null || entry.stack.isEmpty() || this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        List<ClientTooltipComponent> tooltip = new ArrayList<>();

        for (var line : entry.stack.getTooltipLines(
                net.minecraft.world.item.Item.TooltipContext.EMPTY,
                this.minecraft.player,
                TooltipFlag.Default.NORMAL
        )) {
            tooltip.add(new ClientTextTooltip(line.getVisualOrderText()));
        }

        if (entry.count > 1) {
            tooltip.add(new ClientTextTooltip(
                    Component.translatable("screen.mystcraft-sc.page_browser.copies", entry.count).getVisualOrderText()
            ));
        } else if (entry.absoluteIndex < 0) {
            tooltip.add(new ClientTextTooltip(
                    Component.translatable("screen.mystcraft-sc.page_browser.preview_only").getVisualOrderText()
            ));
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
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int ix = (int) mouseX;
        int iy = (int) mouseY;

        if (this.isOverTabStrip(ix, iy)) {
            if (scrollY > 0) {
                this.shiftTabWindow(-1);
                return true;
            }
            if (scrollY < 0) {
                this.shiftTabWindow(1);
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
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

        if (PageSurfaceRenderer.isOverPageArea(this.leftPos + SURFACE_OFFSET_X, this.topPos, mouseX, mouseY)) {
            DisplayEntry hovered = this.getHoveredEntry(mouseX, mouseY);
            this.onSurfaceClicked(hovered, event);
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

    private boolean isOverTabStrip(int mouseX, int mouseY) {
        int x1 = this.leftPos + TAB_STRIP_X;
        int y1 = this.topPos + TAB_STRIP_Y;
        int x2 = x1 + TAB_STRIP_W;
        int y2 = y1 + TAB_COUNT * TAB_STRIP_SPACING;
        return mouseX >= x1 && mouseX < x2 && mouseY >= y1 && mouseY < y2;
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
            int absoluteTab = this.menu.getFirstVisibleTab() + i;
            int x = this.leftPos + TAB_STRIP_X;
            int y = this.topPos + TAB_STRIP_Y + i * TAB_STRIP_SPACING;

            if (mouseX < x || mouseX >= x + TAB_STRIP_W || mouseY < y || mouseY >= y + TAB_STRIP_H) {
                continue;
            }

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

        return false;
    }

    private boolean handleSurfaceInsertClick(int mouseX, int mouseY, MouseButtonEvent event) {
        if (this.minecraft == null || this.minecraft.gameMode == null) {
            return false;
        }

        if (this.menu.getCarried().isEmpty() || !this.menu.getCarried().is(MystcraftItems.PAGE)) {
            return false;
        }

        if (!PageSurfaceRenderer.isOverPageArea(this.leftPos + SURFACE_OFFSET_X, this.topPos, mouseX, mouseY)) {
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

            int x = this.leftPos + TAB_STRIP_X;
            int y = this.topPos + TAB_STRIP_Y + i * TAB_STRIP_SPACING;

            int bg = absoluteTab == this.menu.getActiveTab() ? 0xE0E0E0E0 : 0xC8B8B8B8;
            int border = absoluteTab == this.menu.getActiveTab() ? 0xFFFFFFFF : 0xFF808080;

            guiGraphics.fill(x, y, x + TAB_STRIP_W, y + TAB_STRIP_H, border);
            guiGraphics.fill(x + 1, y + 1, x + TAB_STRIP_W - 1, y + TAB_STRIP_H - 1, bg);

            guiGraphics.fill(x + 7, y + 6, x + 25, y + 24, 0xFF6E6E6E);
            guiGraphics.fill(x + 8, y + 7, x + 24, y + 23, 0xFF000000);

            if (!stack.isEmpty()) {
                guiGraphics.renderItem(stack, x + 8, y + 7);

                String name = stack.getHoverName().getString();
                if (name.length() > 12) {
                    name = name.substring(0, 12);
                }

                guiGraphics.drawString(
                        this.font,
                        name,
                        x + 2,
                        y + 28,
                        0xFF202020,
                        false
                );
            }

            if (mouseX >= x && mouseX < x + TAB_STRIP_W && mouseY >= y && mouseY < y + TAB_STRIP_H && !stack.isEmpty() && this.minecraft != null && this.minecraft.player != null) {
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

    private void drawInkMeter(GuiGraphics guiGraphics) {
        guiGraphics.fill(
                this.leftPos + INK_METER_X,
                this.topPos + INK_METER_Y,
                this.leftPos + INK_METER_X + INK_METER_W,
                this.topPos + INK_METER_Y + INK_METER_H,
                0xFF111111
        );

        if (this.menu.hasInk()) {
            guiGraphics.fill(
                    this.leftPos + INK_METER_X + 1,
                    this.topPos + INK_METER_Y + 1,
                    this.leftPos + INK_METER_X + INK_METER_W - 1,
                    this.topPos + INK_METER_Y + INK_METER_H - 1,
                    0xFF202020
            );
        }
    }

    private void drawDeskTabHighlights(GuiGraphics guiGraphics) {
        for (int i = 0; i < TAB_COUNT; i++) {
            int absoluteTab = this.menu.getFirstVisibleTab() + i;
            if (absoluteTab != this.menu.getActiveTab()) {
                continue;
            }

            int y = this.topPos + DESK_TABS_Y + i * 38;
            guiGraphics.fill(
                    this.leftPos + DESK_TABS_X,
                    y,
                    this.leftPos + DESK_TABS_X + 14,
                    y + 30,
                    0x30FFFFFF
            );
        }
    }
}