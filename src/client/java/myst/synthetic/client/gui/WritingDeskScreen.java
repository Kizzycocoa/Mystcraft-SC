package myst.synthetic.client.gui;

import myst.synthetic.MystcraftItems;
import myst.synthetic.block.entity.BlockEntityDesk;
import myst.synthetic.item.DeskItemBehaviors;
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
import net.minecraft.client.input.CharacterEvent;
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

    private static final int FULL_GUI_WIDTH = 409;
    private static final int FULL_GUI_HEIGHT = 185;

    private static final int LEFT_TABS_X = 0;
    private static final int LEFT_TABS_Y = 20;
    private static final int LEFT_TAB_W = 58;
    private static final int LEFT_TAB_H = 40;
    private static final int LEFT_TAB_STEP = 37;
    private static final int TAB_COUNT = WritingDeskMenu.VISIBLE_TAB_COUNT;

    private static final int LEFT_ARROWS_X = 0;
    private static final int LEFT_ARROWS_Y = 168;
    private static final int LEFT_ARROWS_W = 58;
    private static final int LEFT_ARROWS_H = 17;

    private static final int SURFACE_OFFSET_X = 58;

    private static final int DESK_PANEL_X = 233;
    private static final int DESK_PANEL_Y = 20;
    private static final int DESK_PANEL_W = 176;
    private static final int DESK_PANEL_H = 166;

    private static final int DESK_TEXTURE_W = 256;
    private static final int DESK_TEXTURE_H = 256;

    private static final int TITLE_BOX_X = DESK_PANEL_X + 28;
    private static final int TITLE_BOX_Y = DESK_PANEL_Y + 61;
    private static final int TITLE_BOX_W = 100;
    private static final int TITLE_BOX_H = 14;

    private static final int PREVIEW_X = DESK_PANEL_X + 27;
    private static final int PREVIEW_Y = DESK_PANEL_Y + 6;
    private static final int PREVIEW_W = 101;
    private static final int PREVIEW_H = 50;

    private static final int INK_METER_X = DESK_PANEL_X + 132;
    private static final int INK_METER_Y = DESK_PANEL_Y + 7;
    private static final int INK_METER_W = 16;
    private static final int INK_METER_H = 70;

    private EditBox titleBox;
    private String lastSentTitle = "";

    public WritingDeskScreen(WritingDeskMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = FULL_GUI_WIDTH;
        this.imageHeight = FULL_GUI_HEIGHT;
        this.inventoryLabelX = 0;
        this.inventoryLabelY = 10000;
        this.titleLabelX = 0;
        this.titleLabelY = 10000;
    }

    @Override
    protected void init() {
        super.init();

        if (this.sortButton != null) {
            this.sortButton.setX(this.leftPos + 58);
            this.sortButton.setY(this.topPos);
            this.sortButton.setWidth(18);
            this.sortButton.setHeight(18);
        }

        if (this.allButton != null) {
            this.allButton.setX(this.leftPos + 76);
            this.allButton.setY(this.topPos);
            this.allButton.setWidth(18);
            this.allButton.setHeight(18);
        }

        if (this.searchBox != null) {
            this.searchBox.setX(this.leftPos + 98);
            this.searchBox.setY(this.topPos);
            this.searchBox.setWidth(130);
            this.searchBox.setHeight(18);
        }

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
        this.titleBox.setTextColor(0xFF202020);
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
        this.drawSearchFrame(guiGraphics);

        this.drawLeftTabStrip(guiGraphics, mouseX, mouseY);

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

        this.drawTitleFrame(guiGraphics);
        this.drawTargetPreview(guiGraphics, mouseX, mouseY);
        this.drawInkMeter(guiGraphics);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    protected boolean isMouseOverSearchBox(int mouseX, int mouseY) {
        return mouseX >= this.leftPos + 98
                && mouseX < this.leftPos + 228
                && mouseY >= this.topPos
                && mouseY < this.topPos + 18;
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
    public boolean keyPressed(KeyEvent input) {
        if (this.searchBox != null && this.searchBox.isFocused()) {
            if (input.key() == 256) {
                return super.keyPressed(input);
            }

            if (this.searchBox.keyPressed(input)) {
                return true;
            }

            return true;
        }

        if (this.titleBox != null && this.titleBox.isFocused()) {
            if (input.key() == 256) {
                return super.keyPressed(input);
            }

            if (this.titleBox.keyPressed(input)) {
                this.pushTitleIfChanged();
                return true;
            }

            return true;
        }

        if (input.key() == 257 && this.minecraft != null && this.minecraft.gameMode != null && this.menu.canUseLink()) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, WritingDeskMenu.BUTTON_USE_LINK);
            return true;
        }

        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (this.searchBox != null && this.searchBox.isFocused()) {
            if (this.searchBox.charTyped(input)) {
                return true;
            }
            return true;
        }

        if (this.titleBox != null && this.titleBox.isFocused()) {
            if (this.titleBox.charTyped(input)) {
                this.pushTitleIfChanged();
                return true;
            }
            return true;
        }

        return super.charTyped(input);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int ix = (int) mouseX;
        int iy = (int) mouseY;

        if (this.isOverLeftArrowArea(ix, iy)) {
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

        if (this.searchBox != null && this.isMouseOverSearchBox(mouseX, mouseY)) {
            this.setFocused(this.searchBox);
            this.searchBox.setFocused(true);
            if (this.titleBox != null) {
                this.titleBox.setFocused(false);
            }

            if (event.button() == 1) {
                this.searchBox.setValue("");
                return true;
            }

            if (this.searchBox.mouseClicked(event, doubleClick)) {
                return true;
            }

            return true;
        }

        if (this.titleBox != null && this.isOverTitleBox(mouseX, mouseY)) {
            this.setFocused(this.titleBox);
            this.titleBox.setFocused(true);
            if (this.searchBox != null) {
                this.searchBox.setFocused(false);
            }
            return this.titleBox.mouseClicked(event, doubleClick) || true;
        } else if (this.titleBox != null) {
            this.titleBox.setFocused(false);
        }

        if (this.handleArrowClick(mouseX, mouseY)) {
            return true;
        }

        if (this.handleTabClick(mouseX, mouseY, event)) {
            return true;
        }

        if (this.handleSurfaceInsertClick(mouseX, mouseY, event)) {
            return true;
        }

        if (this.isOverScrollbar(mouseX, mouseY)) {
            this.draggingScrollbar = true;
            this.scrollToMouse(mouseY);
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
    public void removed() {
        this.pushTitleIfChanged();
        super.removed();
    }

    private void drawSearchFrame(GuiGraphics guiGraphics) {
        guiGraphics.fill(
                this.leftPos + 98,
                this.topPos,
                this.leftPos + 228,
                this.topPos + 18,
                0xFFA0A0A0
        );
        guiGraphics.fill(
                this.leftPos + 99,
                this.topPos + 1,
                this.leftPos + 227,
                this.topPos + 17,
                0xFF000000
        );
    }

    private void drawLeftTabStrip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (int i = 0; i < TAB_COUNT; i++) {
            int absoluteTab = this.menu.getFirstVisibleTab() + i;
            int x = this.leftPos + LEFT_TABS_X;
            int y = this.topPos + LEFT_TABS_Y + i * LEFT_TAB_STEP;

            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    DESK_TEXTURE,
                    x,
                    y,
                    0,
                    166,
                    LEFT_TAB_W,
                    LEFT_TAB_H,
                    DESK_TEXTURE_W,
                    DESK_TEXTURE_H
            );

            if (absoluteTab == this.menu.getActiveTab()) {
                guiGraphics.fill(x + 1, y + 1, x + LEFT_TAB_W - 1, y + LEFT_TAB_H - 1, 0x10FFFFFF);
            }

            ItemStack stack = this.menu.getVisibleTabStack(i);
            if (!stack.isEmpty()) {
                guiGraphics.renderItem(stack, x + 37, y + 14);
                guiGraphics.renderItemDecorations(this.font, stack, x + 37, y + 14);

                String name = stack.getHoverName().getString();
                if (name.length() > 11) {
                    name = name.substring(0, 11);
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

            if (mouseX >= x && mouseX < x + LEFT_TAB_W && mouseY >= y && mouseY < y + LEFT_TAB_H && !stack.isEmpty() && this.minecraft != null && this.minecraft.player != null) {
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

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                DESK_TEXTURE,
                this.leftPos + LEFT_ARROWS_X,
                this.topPos + LEFT_ARROWS_Y,
                0,
                205,
                LEFT_ARROWS_W,
                LEFT_ARROWS_H,
                DESK_TEXTURE_W,
                DESK_TEXTURE_H
        );
    }

    private void drawTitleFrame(GuiGraphics guiGraphics) {
        guiGraphics.fill(
                this.leftPos + TITLE_BOX_X - 1,
                this.topPos + TITLE_BOX_Y - 1,
                this.leftPos + TITLE_BOX_X + TITLE_BOX_W + 1,
                this.topPos + TITLE_BOX_Y + TITLE_BOX_H + 1,
                0xFF9E6A1C
        );
        guiGraphics.fill(
                this.leftPos + TITLE_BOX_X,
                this.topPos + TITLE_BOX_Y,
                this.leftPos + TITLE_BOX_X + TITLE_BOX_W,
                this.topPos + TITLE_BOX_Y + TITLE_BOX_H,
                0xFF000000
        );
    }

    private void drawTargetPreview(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        ItemStack target = this.menu.getTargetStack();
        if (target.isEmpty()) {
            return;
        }

        List<ItemStack> pages = DeskItemBehaviors.getPages(this.minecraft.player, target);
        if (pages.isEmpty()) {
            return;
        }

        int clipLeft = this.leftPos + PREVIEW_X;
        int clipTop = this.topPos + PREVIEW_Y;
        int clipRight = clipLeft + PREVIEW_W;
        int clipBottom = clipTop + PREVIEW_H;

        guiGraphics.enableScissor(clipLeft, clipTop, clipRight, clipBottom);

        int drawX = clipLeft + 5;
        int drawY = clipTop + 5;

        int shown = Math.min(4, pages.size());
        for (int i = 0; i < shown; i++) {
            ItemStack page = pages.get(i);
            if (page.isEmpty()) {
                continue;
            }

            myst.synthetic.client.render.PageCardRenderer.drawPageCard(guiGraphics, drawX + i * 22, drawY, page, false, false);
        }

        guiGraphics.disableScissor();
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

    private boolean isOverTitleBox(int mouseX, int mouseY) {
        return mouseX >= this.leftPos + TITLE_BOX_X
                && mouseX < this.leftPos + TITLE_BOX_X + TITLE_BOX_W
                && mouseY >= this.topPos + TITLE_BOX_Y
                && mouseY < this.topPos + TITLE_BOX_Y + TITLE_BOX_H;
    }

    private boolean isOverLeftArrowArea(int mouseX, int mouseY) {
        return mouseX >= this.leftPos + LEFT_ARROWS_X
                && mouseX < this.leftPos + LEFT_ARROWS_X + LEFT_ARROWS_W
                && mouseY >= this.topPos + LEFT_ARROWS_Y
                && mouseY < this.topPos + LEFT_ARROWS_Y + LEFT_ARROWS_H;
    }

    private boolean handleArrowClick(int mouseX, int mouseY) {
        if (this.minecraft == null || this.minecraft.gameMode == null) {
            return false;
        }

        if (!this.isOverLeftArrowArea(mouseX, mouseY)) {
            return false;
        }

        int localY = mouseY - (this.topPos + LEFT_ARROWS_Y);
        if (localY < LEFT_ARROWS_H / 2) {
            this.shiftTabWindow(-1);
        } else {
            this.shiftTabWindow(1);
        }
        return true;
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
            int x = this.leftPos + LEFT_TABS_X;
            int y = this.topPos + LEFT_TABS_Y + i * LEFT_TAB_STEP;

            if (mouseX < x || mouseX >= x + LEFT_TAB_W || mouseY < y || mouseY >= y + LEFT_TAB_H) {
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
}