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
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;

public class WritingDeskScreen extends PageBrowserScreen<WritingDeskMenu> {

    private static final Identifier FLUID_TEXTURE =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/block/fluid.png");
    private static final Identifier SCROLLBAR_TEXTURE =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/scrollbar.png");

    private static final int FULL_GUI_WIDTH = 409;
    private static final int FULL_GUI_HEIGHT = 186;

    private static final int SURFACE_OFFSET_X = 58;
    private static final int SURFACE_X = 0;
    private static final int SURFACE_Y = 20;
    private static final int SURFACE_PAGE_WIDTH = 156;
    private static final int SURFACE_HEIGHT = 166;

    private static final int LEFT_ARROW_TOP_X = 0;
    private static final int LEFT_ARROW_TOP_Y = SURFACE_Y;
    private static final int LEFT_ARROW_W = 58;
    private static final int LEFT_ARROW_H = 9;

    private static final int LEFT_TABS_X = 0;
    private static final int LEFT_TABS_Y = LEFT_ARROW_TOP_Y + LEFT_ARROW_H;
    private static final int LEFT_TAB_W = 58;
    private static final int LEFT_TAB_H = 37;
    private static final int LEFT_TAB_STEP = 37;
    private static final int TAB_COUNT = WritingDeskMenu.VISIBLE_TAB_COUNT;

    private static final int LEFT_TAB_SLOT_X = 37;
    private static final int LEFT_TAB_SLOT_Y = 5;
    private static final int LEFT_TAB_SLOT_W = 16;
    private static final int LEFT_TAB_SLOT_H = 16;

    private static final int TAB_STATE_U_OFFSET = 58;
    private static final int ARROW_STATE_U_OFFSET = 58;

    private static final int LEFT_ARROW_BOTTOM_X = 0;
    private static final int LEFT_ARROW_BOTTOM_Y = SURFACE_Y + SURFACE_HEIGHT - 9;

    private static final int SCROLLBAR_X = 156;
    private static final int SCROLLBAR_Y = 20;
    private static final int SCROLLBAR_WIDTH = 20;
    private static final int SCROLLBAR_HEIGHT = 166;

    private static final int PAGE_WIDTH = 30;
    private static final int PAGE_HEIGHT = 40;

    private static final int DESK_PANEL_X = 233;
    private static final int DESK_PANEL_Y = 20;
    private static final int DESK_PANEL_W = 176;
    private static final int DESK_PANEL_H = 166;

    private static final int DESK_TEXTURE_W = 256;
    private static final int DESK_TEXTURE_H = 256;

    private static final int SEARCH_BUTTON_X = 58;
    private static final int SEARCH_BOX_X = 95;
    private static final int SEARCH_BOX_Y = 1;
    private static final int SEARCH_BOX_W = 138;
    private static final int SEARCH_BOX_H = 16;

    private static final int TITLE_BOX_X = DESK_PANEL_X + 28;
    private static final int TITLE_BOX_Y = DESK_PANEL_Y + 61;
    private static final int TITLE_BOX_W = 100;
    private static final int TITLE_BOX_H = 14;

    private static final int PREVIEW_X = DESK_PANEL_X + 27;
    private static final int PREVIEW_Y = DESK_PANEL_Y + 6;
    private static final int PREVIEW_W = 101;
    private static final int PREVIEW_H = 50;

    private static final int PREVIEW_PAGE_WIDTH = 33;
    private static final int PREVIEW_PAGE_HEIGHT = 44;
    private static final int PREVIEW_PAGE_X = 0;
    private static final int PREVIEW_PAGE_Y = 3;
    private static final int PREVIEW_PAGE_SPACING = 34;
    private static final int PREVIEW_VISIBLE_COUNT = 3;

    private static final int PREVIEW_ARROW_W = 10;
    private static final int PREVIEW_ARROW_H = PREVIEW_H;

    private static final int PREVIEW_LEFT_ARROW_X = PREVIEW_X;
    private static final int PREVIEW_RIGHT_ARROW_X = PREVIEW_X + PREVIEW_W - PREVIEW_ARROW_W;
    private static final int PREVIEW_ARROW_Y = PREVIEW_Y;

    private int previewScroll = 0;

    private static final int INK_METER_X = DESK_PANEL_X + 132;
    private static final int INK_METER_Y = DESK_PANEL_Y + 7;
    private static final int INK_METER_W = 16;
    private static final int INK_METER_H = 70;

    private int hoveredFolderOrderedSlot = -1;

    private EditBox titleBox;
    private String lastSentTitle = "";

    private List<ClientTooltipComponent> pendingTabTooltip;
    private int pendingTabTooltipX;
    private int pendingTabTooltipY;

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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.pendingTabTooltip = null;

        int previousHovered = this.hoveredFolderOrderedSlot;
        this.hoveredFolderOrderedSlot = -1;

        ItemStack activeTabStack = this.menu.getTabStack(this.menu.getActiveTab());
        boolean folderMode =
                activeTabStack.is(myst.synthetic.MystcraftItems.FOLDER)
                        && !this.sortAlphabetically
                        && this.searchBox != null
                        && this.searchBox.getValue().isEmpty();

        if (folderMode) {
            int hovered = this.getHoveredOrderedFolderSlot(mouseX, mouseY);
            if (hovered >= 0) {
                this.hoveredFolderOrderedSlot = hovered;
            }
        }

        if (previousHovered != this.hoveredFolderOrderedSlot) {
            this.rebuildDisplayEntries();
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (this.pendingTabTooltip != null) {
            guiGraphics.renderTooltip(
                    this.font,
                    this.pendingTabTooltip,
                    this.pendingTabTooltipX,
                    this.pendingTabTooltipY,
                    DefaultTooltipPositioner.INSTANCE,
                    null
            );
        }

        if (this.isMouseOverInkMeter(mouseX, mouseY)) {
            List<ClientTooltipComponent> tooltip = new ArrayList<>();
            tooltip.add(new ClientTextTooltip(
                    Component.literal("Black Ink: " + this.menu.getInkAmount() + "ml / " + BlockEntityDesk.INK_TANK_CAPACITY + "ml").getVisualOrderText()
            ));

            guiGraphics.renderTooltip(
                    this.font,
                    tooltip,
                    mouseX,
                    mouseY,
                    DefaultTooltipPositioner.INSTANCE,
                    null
            );
        } else {
            this.renderPreviewTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    @Override
    protected void init() {
        super.init();

        if (this.sortButton != null) {
            this.sortButton.setX(this.leftPos + SEARCH_BUTTON_X);
            this.sortButton.setY(this.topPos);
            this.sortButton.setWidth(18);
            this.sortButton.setHeight(18);
        }

        if (this.allButton != null) {
            this.allButton.setX(this.leftPos + SEARCH_BUTTON_X + 18);
            this.allButton.setY(this.topPos);
            this.allButton.setWidth(18);
            this.allButton.setHeight(18);
        }

        if (this.searchBox != null) {
            this.searchBox.setX(this.leftPos + SEARCH_BOX_X + 4);
            this.searchBox.setY(this.topPos + SEARCH_BOX_Y + 4);
            this.searchBox.setWidth(SEARCH_BOX_W - 8);
            this.searchBox.setHeight(10);
            this.searchBox.setBordered(false);
            this.searchBox.setTextColor(0xFFE0E0E0);
            this.searchBox.setTextColorUneditable(0xFF707070);
            this.searchBox.setCanLoseFocus(true);
        }

        this.titleBox = new EditBox(
                this.font,
                this.leftPos + TITLE_BOX_X + 5,
                this.topPos + TITLE_BOX_Y + 3,
                TITLE_BOX_W - 10,
                10,
                Component.translatable("container.mystcraft-sc.writing_desk.title")
        );
        this.titleBox.setCanLoseFocus(true);
        this.titleBox.setBordered(false);
        this.titleBox.setTextColor(0xFFFFFFFF);
        this.titleBox.setTextColorUneditable(0xFFC0C0C0);
        this.addRenderableWidget(this.titleBox);

        this.pullTitleFromTarget();
        this.lastSentTitle = this.titleBox.getValue();
    }

    @Override
    protected void renderSearchGhostText(GuiGraphics guiGraphics) {
        if (this.searchBox == null || !this.searchBox.getValue().isEmpty()) {
            return;
        }

        guiGraphics.drawString(
                this.font,
                "Search...",
                this.leftPos + SEARCH_BOX_X + 4,
                this.topPos + SEARCH_BOX_Y + 4,
                0xFF707070,
                false
        );
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
        this.drawSurfaceBackground(guiGraphics);
        this.drawSurfaceEntries(guiGraphics, mouseX, mouseY);
        this.drawSurfaceScrollbar(guiGraphics);

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
        this.drawTargetPreview(guiGraphics);
        this.drawInkMeter(guiGraphics);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    protected boolean isMouseOverSearchBox(int mouseX, int mouseY) {
        return mouseX >= this.leftPos + SEARCH_BOX_X
                && mouseX < this.leftPos + SEARCH_BOX_X + SEARCH_BOX_W
                && mouseY >= this.topPos + SEARCH_BOX_Y
                && mouseY < this.topPos + SEARCH_BOX_Y + SEARCH_BOX_H;
    }

    @Override
    protected void buildOwnedEntries(List<DisplayEntry> built, String normalizedSearch) {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        ItemStack tabStack = this.menu.getTabStack(this.menu.getActiveTab());

        // Folder: preserve exact slot order and only show blanks when appropriate.
        if (tabStack.is(myst.synthetic.MystcraftItems.FOLDER)) {
            var slots = myst.synthetic.item.ItemFolder.createInventory(tabStack);

            int lastFilled = -1;
            for (int i = 0; i < slots.size(); i++) {
                if (!slots.get(i).isEmpty()) {
                    lastFilled = i;
                }
            }

            boolean showBlankSlots = !this.sortAlphabetically && normalizedSearch.isEmpty();

            // Base visible size: only through the last real entry.
            int visibleSize = Math.max(0, lastFilled + 1);

            // Legacy-like hover expansion:
            // when hovering beyond the last filled slot, reveal blanks up to hovered slot.
            if (showBlankSlots && this.hoveredFolderOrderedSlot >= visibleSize) {
                visibleSize = Math.min(slots.size(), this.hoveredFolderOrderedSlot + 1);
            }

            for (int i = 0; i < visibleSize; i++) {
                ItemStack stack = slots.get(i);

                if (stack.isEmpty()) {
                    if (showBlankSlots) {
                        built.add(new DisplayEntry(ItemStack.EMPTY, i, 1, "__folder_empty__"));
                    }
                    continue;
                }

                String searchName = this.getSearchName(stack);
                if (!normalizedSearch.isEmpty() && !normalize(searchName).contains(normalizedSearch)) {
                    continue;
                }

                built.add(new DisplayEntry(stack.copy(), i, 1, searchName));
            }

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
    protected List<PageSurfaceRenderer.SurfaceEntry> toSurfaceEntries() {
        List<PageSurfaceRenderer.SurfaceEntry> entries = new ArrayList<>(this.displayEntries.size());

        for (DisplayEntry entry : this.displayEntries) {
            boolean placeholder = entry.absoluteIndex < 0 || "__folder_empty__".equals(entry.searchName);

            entries.add(new PageSurfaceRenderer.SurfaceEntry(
                    entry.absoluteIndex,
                    entry.stack.copy(),
                    placeholder,
                    entry.count,
                    entry.searchName,
                    Math.round(entry.x),
                    Math.round(entry.y)
            ));
        }

        return entries;
    }

    @Override
    protected void onSurfaceClicked(DisplayEntry entry, MouseButtonEvent event) {
        if (this.minecraft == null || this.minecraft.gameMode == null) {
            return;
        }

        if (entry == null) {
            return;
        }

        // Legacy-like copy behavior:
        // right click on a real page copies its symbol into the target via WriteSymbol.
        if (event.button() == 1 && entry.absoluteIndex >= 0 && !entry.stack.isEmpty() && this.menu.getCarried().isEmpty()) {
            Identifier symbolId = Page.getSymbol(entry.stack);
            if (symbolId != null) {
                int symbolIndex = 0;
                for (var symbol : PageSymbolRegistry.values()) {
                    if (symbol.id().equals(symbolId)) {
                        this.minecraft.gameMode.handleInventoryButtonClick(
                                this.menu.containerId,
                                WritingDeskMenu.BUTTON_WRITE_SYMBOL_START + symbolIndex
                        );
                        return;
                    }
                    symbolIndex++;
                }
            }
        }

        if (entry.absoluteIndex >= 0) {
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

        if (entry.absoluteIndex < 0) {
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
        ItemStack tabStack = this.menu.getTabStack(this.menu.getActiveTab());

        if (tabStack.is(myst.synthetic.MystcraftItems.FOLDER)
                && !this.sortAlphabetically
                && this.searchBox != null
                && this.searchBox.getValue().isEmpty()) {

            int totalSlots = myst.synthetic.item.ItemFolder.createInventory(tabStack).size();
            int rows = (totalSlots + PageSurfaceRenderer.COLUMNS - 1) / PageSurfaceRenderer.COLUMNS;
            int maxBottom = rows * PageSurfaceRenderer.PAGE_Y_STEP;
            return Math.max(0, maxBottom - SURFACE_HEIGHT);
        }

        int maxBottom = 0;
        for (DisplayEntry entry : this.displayEntries) {
            int bottom = Math.round(entry.y + PAGE_HEIGHT + 6);
            if (bottom > maxBottom) {
                maxBottom = bottom;
            }
        }
        return Math.max(0, maxBottom - SURFACE_HEIGHT);
    }

    @Override
    protected void scrollToMouse(int mouseY) {
        int maxScroll = this.getMaxScroll();
        if (maxScroll <= 0) {
            this.scroll = 0;
            return;
        }

        int trackTop = this.topPos + SCROLLBAR_Y;
        int trackBottom = trackTop + SCROLLBAR_HEIGHT - 15;
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
        int x = this.leftPos + SURFACE_OFFSET_X + SCROLLBAR_X;
        int y = this.topPos + SCROLLBAR_Y;
        return mouseX >= x && mouseX < x + SCROLLBAR_WIDTH
                && mouseY >= y && mouseY < y + SCROLLBAR_HEIGHT;
    }

    @Override
    protected DisplayEntry getHoveredEntry(int mouseX, int mouseY) {
        if (!this.isOverPageArea(mouseX, mouseY)) {
            return null;
        }

        int clipTop = this.topPos + SURFACE_Y;
        int clipBottom = clipTop + SURFACE_HEIGHT;

        for (DisplayEntry entry : this.displayEntries) {
            int drawX = this.leftPos + SURFACE_OFFSET_X + Math.round(entry.x);
            int drawY = this.topPos + SURFACE_Y + Math.round(entry.y) - this.scroll;

            if (drawY + PAGE_HEIGHT <= clipTop || drawY >= clipBottom) {
                continue;
            }

            if (mouseX >= drawX && mouseX < drawX + PAGE_WIDTH
                    && mouseY >= drawY && mouseY < drawY + PAGE_HEIGHT) {
                return entry;
            }
        }

        return null;
    }

    @Override
    protected void renderPageTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!this.isOverPageArea(mouseX, mouseY)) {
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

    private void renderPreviewTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        ItemStack hovered = ItemStack.EMPTY;

        if (this.targetHasSinglePagePreview()) {
            int left = this.leftPos + PREVIEW_X;
            int top = this.topPos + PREVIEW_Y;
            int drawX = left + 34;
            int drawY = top + PREVIEW_PAGE_Y;

            if (mouseX >= drawX && mouseX < drawX + PREVIEW_PAGE_WIDTH
                    && mouseY >= drawY && mouseY < drawY + PREVIEW_PAGE_HEIGHT) {
                hovered = this.menu.getTargetStack();
            }
        } else if (this.targetHasScrollablePreview()) {
            int hoveredIndex = this.getHoveredPreviewIndex(mouseX, mouseY);
            if (hoveredIndex >= 0) {
                List<ItemStack> entries = this.getPreviewEntries();
                if (hoveredIndex < entries.size()) {
                    hovered = entries.get(hoveredIndex);
                }
            }
        }

        if (hovered.isEmpty()) {
            return;
        }

        List<ClientTooltipComponent> tooltip = new ArrayList<>();

        for (var line : hovered.getTooltipLines(
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

        if (this.isOverLeftArrowTop(ix, iy)) {
            if (scrollY != 0) {
                this.shiftTabWindow(-1);
                return true;
            }
        }

        if (this.isOverLeftArrowBottom(ix, iy)) {
            if (scrollY != 0) {
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
        } else if (this.searchBox != null) {
            this.searchBox.setFocused(false);
        }

        if (this.titleBox != null && this.isOverTitleBox(mouseX, mouseY)) {
            this.setFocused(this.titleBox);
            this.titleBox.setFocused(true);
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

        if (this.handleSurfaceInsertClick(mouseX, mouseY)) {
            return true;
        }

        if (this.isOverScrollbar(mouseX, mouseY)) {
            this.draggingScrollbar = true;
            this.scrollToMouse(mouseY);
            return true;
        }

        if (this.isOverPageArea(mouseX, mouseY)) {
            DisplayEntry hovered = this.getHoveredEntry(mouseX, mouseY);
            this.onSurfaceClicked(hovered, event);
            return true;
        }

        if (this.minecraft != null && this.minecraft.gameMode != null) {
            if (this.isOverPreviewLeftArrow(mouseX, mouseY)) {
                if (this.previewScroll > 0) {
                    this.previewScroll--;
                }
                return true;
            }

            if (this.isOverPreviewRightArrow(mouseX, mouseY)) {
                int max = this.getPreviewMaxScroll();
                if (this.previewScroll < max) {
                    this.previewScroll++;
                }
                return true;
            }

            int previewIndex = this.getHoveredPreviewIndex(mouseX, mouseY);
            if (previewIndex >= 0) {
                if (this.menu.getCarried().isEmpty()) {
                    this.minecraft.gameMode.handleInventoryButtonClick(
                            this.menu.containerId,
                            WritingDeskMenu.BUTTON_PREVIEW_REMOVE_PAGE_START + previewIndex
                    );
                    return true;
                }

                if (this.menu.getCarried().is(myst.synthetic.MystcraftItems.PAGE)) {
                    this.minecraft.gameMode.handleInventoryButtonClick(
                            this.menu.containerId,
                            WritingDeskMenu.BUTTON_PREVIEW_INSERT_PAGE_START + previewIndex
                    );
                    return true;
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void removed() {
        this.pushTitleIfChanged();
        super.removed();
    }

    private boolean targetHasScrollablePreview() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return false;
        }

        ItemStack target = this.menu.getTargetStack();
        if (target.isEmpty()) {
            return false;
        }

        if (target.is(myst.synthetic.MystcraftItems.PAGE)) {
            return false;
        }

        // Portfolios are not useful in this strip; legacy behavior is better
        // approximated by excluding them here.
        if (target.is(myst.synthetic.MystcraftItems.PORTFOLIO)) {
            return false;
        }

        // Linking books will get their own preview mode later.
        if (target.is(myst.synthetic.MystcraftItems.LINKBOOK)) {
            return false;
        }

        return !this.getPreviewEntries().isEmpty();
    }

    private boolean targetHasSinglePagePreview() {
        return this.menu.getTargetStack().is(myst.synthetic.MystcraftItems.PAGE);
    }

    private int getPreviewPageCount() {
        return this.getPreviewEntries().size();
    }

    private boolean isOverPreviewLeftArrow(int mouseX, int mouseY) {
        int x = this.leftPos + PREVIEW_LEFT_ARROW_X;
        int y = this.topPos + PREVIEW_ARROW_Y;
        return mouseX >= x && mouseX < x + PREVIEW_ARROW_W
                && mouseY >= y && mouseY < y + PREVIEW_ARROW_H;
    }

    private boolean isOverPreviewRightArrow(int mouseX, int mouseY) {
        int x = this.leftPos + PREVIEW_RIGHT_ARROW_X;
        int y = this.topPos + PREVIEW_ARROW_Y;
        return mouseX >= x && mouseX < x + PREVIEW_ARROW_W
                && mouseY >= y && mouseY < y + PREVIEW_ARROW_H;
    }

    private int getHoveredPreviewIndex(int mouseX, int mouseY) {
        int slot = this.getPreviewSlotIndexFromMouse(mouseX, mouseY);
        if (slot < 0) {
            return -1;
        }

        int absolute = this.previewScroll + slot;
        List<ItemStack> entries = this.getPreviewEntries();

        if (absolute < 0 || absolute >= entries.size()) {
            return -1;
        }

        return absolute;
    }

    private enum TabVisualState {
        NORMAL,
        ACTIVE,
        INACTIVE
    }

    private boolean targetHasFolderPreview() {
        ItemStack target = this.menu.getTargetStack();
        return !target.isEmpty() && target.is(myst.synthetic.MystcraftItems.FOLDER);
    }

    private int getFolderPreviewLastFilledIndex(ItemStack folder) {
        if (folder.isEmpty() || !folder.is(myst.synthetic.MystcraftItems.FOLDER)) {
            return -1;
        }

        var slots = myst.synthetic.item.ItemFolder.createInventory(folder);
        int lastFilled = -1;
        for (int i = 0; i < slots.size(); i++) {
            if (!slots.get(i).isEmpty()) {
                lastFilled = i;
            }
        }
        return lastFilled;
    }

    private List<ItemStack> getPreviewEntries() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return List.of();
        }

        ItemStack target = this.menu.getTargetStack();
        if (target.isEmpty()) {
            return List.of();
        }

        if (target.is(myst.synthetic.MystcraftItems.FOLDER)) {
            var slots = myst.synthetic.item.ItemFolder.createInventory(target);
            List<ItemStack> entries = new ArrayList<>(slots.size());

            for (int i = 0; i < slots.size(); i++) {
                entries.add(slots.get(i).copy());
            }

            return entries;
        }

        return this.menu.getTargetPages(this.minecraft.player);
    }

    private int getPreviewVisibleCount() {
        if (this.targetHasFolderPreview()) {
            return 3;
        }
        return PREVIEW_VISIBLE_COUNT;
    }

    private int getPreviewSlotIndexFromMouse(int mouseX, int mouseY) {
        if (!this.targetHasScrollablePreview()) {
            return -1;
        }

        int clipLeft = this.leftPos + PREVIEW_X;
        int clipTop = this.topPos + PREVIEW_Y;
        int clipRight = clipLeft + PREVIEW_W;
        int clipBottom = clipTop + PREVIEW_H;

        if (mouseX < clipLeft || mouseX >= clipRight || mouseY < clipTop || mouseY >= clipBottom) {
            return -1;
        }

        int relX = mouseX - clipLeft;
        int relY = mouseY - clipTop;

        // Arrow overlays still take precedence.
        if (relX < PREVIEW_ARROW_W || relX >= PREVIEW_W - PREVIEW_ARROW_W) {
            return -1;
        }

        for (int i = 0; i < PREVIEW_VISIBLE_COUNT; i++) {
            int x = PREVIEW_PAGE_X + i * PREVIEW_PAGE_SPACING;

            if (relX >= x && relX < x + PREVIEW_PAGE_WIDTH
                    && relY >= PREVIEW_PAGE_Y && relY < PREVIEW_PAGE_Y + PREVIEW_PAGE_HEIGHT) {
                return i;
            }
        }

        return -1;
    }

    private int getPreviewMaxScroll() {
        ItemStack target = this.menu.getTargetStack();

        if (target.is(myst.synthetic.MystcraftItems.FOLDER)) {
            int lastFilled = this.getFolderPreviewLastFilledIndex(target);
            int hardMax = Math.max(0, myst.synthetic.component.FolderDataComponent.MAX_SLOTS - PREVIEW_VISIBLE_COUNT);

            // Legacy-like behavior:
            // allow scrolling until the last filled page is in the first visible slot,
            // giving up to 2 blank slots after it in the 3-page preview.
            return Math.max(0, Math.min(hardMax, lastFilled));
        }

        int size = this.getPreviewEntries().size();
        return Math.max(0, size - this.getPreviewVisibleCount());
    }

    private void clampPreviewScroll() {
        this.previewScroll = Mth.clamp(this.previewScroll, 0, this.getPreviewMaxScroll());
    }

    private int getStateU(int baseU, int width, TabVisualState state) {
        return switch (state) {
            case NORMAL -> baseU;
            case ACTIVE -> baseU + width;
            case INACTIVE -> baseU + width * 2;
        };
    }

    private TabVisualState getArrowState(boolean canScroll, boolean pointsToActiveTabOffscreen) {
        if (!canScroll) {
            return TabVisualState.INACTIVE;
        }
        if (pointsToActiveTabOffscreen) {
            return TabVisualState.ACTIVE;
        }
        return TabVisualState.NORMAL;
    }

    private TabVisualState getTabState(boolean active) {
        return active ? TabVisualState.ACTIVE : TabVisualState.NORMAL;
    }

    private void drawSearchFrame(GuiGraphics guiGraphics) {
        guiGraphics.fill(
                this.leftPos + SEARCH_BOX_X,
                this.topPos + SEARCH_BOX_Y,
                this.leftPos + SEARCH_BOX_X + SEARCH_BOX_W,
                this.topPos + SEARCH_BOX_Y + SEARCH_BOX_H,
                0xFFA0A0A0
        );
        guiGraphics.fill(
                this.leftPos + SEARCH_BOX_X + 1,
                this.topPos + SEARCH_BOX_Y + 1,
                this.leftPos + SEARCH_BOX_X + SEARCH_BOX_W - 1,
                this.topPos + SEARCH_BOX_Y + SEARCH_BOX_H - 1,
                0xFF000000
        );
    }

    private void drawTabNumber(GuiGraphics guiGraphics, int number, int x, int y) {
        int[] components = getDniNumberComponents(number);

        for (int component : components) {
            this.drawTabNumberComponent(guiGraphics, component, x, y);
        }
    }

    private int[] getDniNumberComponents(int num) {
        // Exact sheet ids from your mapping:
        // 2 = 0
        // 3 = 25
        // 57 = 1
        // 58 = 2
        // 59 = 3
        // 60 = 4
        // 61 = 5
        // 62 = 10
        // 63 = 15
        // 64 = 20

        if (num <= 0) {
            return new int[] { 2 };
        }

        if (num >= 25) {
            return new int[] { 3 };
        }

        int major = 0;
        if (num >= 20) {
            major = 64;
        } else if (num >= 15) {
            major = 63;
        } else if (num >= 10) {
            major = 62;
        } else if (num >= 5) {
            major = 61;
        }

        int remainder = num % 5;
        int minor = 0;
        if (remainder > 0) {
            minor = 56 + remainder; // 1->57, 2->58, 3->59, 4->60
        }

        if (major != 0 && minor != 0) {
            return new int[] { major, minor };
        }

        if (major != 0) {
            return new int[] { major };
        }

        return new int[] { minor };
    }

    private void drawTabNumberComponent(GuiGraphics guiGraphics, int componentIndex, int x, int y) {
        final int tileSize = 16;
        final int atlasColumns = 8;

        int index = componentIndex - 1;
        int u = (index % atlasColumns) * tileSize;
        int v = (index / atlasColumns) * tileSize;

        // Legacy draws these at 19px, but the sheet tiles are 16x16.
        float scale = 19.0F / 16.0F;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x, y);
        guiGraphics.pose().scale(scale, scale);

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/page/symbolcomponents.png"),
                0,
                0,
                u,
                v,
                16,
                16,
                atlasColumns * tileSize,
                atlasColumns * tileSize
        );

        guiGraphics.pose().popMatrix();
    }

    private void drawLeftTabStrip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int topSlot = this.menu.getFirstVisibleTab();
        int activeSlot = this.menu.getActiveTab();

        boolean canScrollUp = topSlot > 0;
        boolean canScrollDown = topSlot + TAB_COUNT < BlockEntityDesk.TAB_SLOT_COUNT;

        boolean activeAbove = activeSlot < topSlot;
        boolean activeBelow = activeSlot >= topSlot + TAB_COUNT;

        // Top arrow
        {
            TabVisualState state = getArrowState(canScrollUp, activeAbove);
            int u = getStateU(0, LEFT_ARROW_W, state);

            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    DESK_TEXTURE,
                    this.leftPos + LEFT_ARROW_TOP_X,
                    this.topPos + LEFT_ARROW_TOP_Y,
                    u,
                    203,
                    LEFT_ARROW_W,
                    LEFT_ARROW_H,
                    DESK_TEXTURE_W,
                    DESK_TEXTURE_H
            );
        }

        for (int i = 0; i < TAB_COUNT; i++) {
            int absoluteTab = topSlot + i;
            int x = this.leftPos + LEFT_TABS_X;
            int y = this.topPos + LEFT_TABS_Y + i * LEFT_TAB_STEP;

            int slotX = x + LEFT_TAB_SLOT_X;
            int slotY = y + LEFT_TAB_SLOT_Y;

            boolean hoveringTab =
                    mouseX >= x && mouseX < x + LEFT_TAB_W
                            && mouseY >= y && mouseY < y + LEFT_TAB_H;

            boolean active = absoluteTab == activeSlot;

            TabVisualState tabState = getTabState(active);
            int tabU = getStateU(0, LEFT_TAB_W, tabState);

            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    DESK_TEXTURE,
                    x,
                    y,
                    tabU,
                    166,
                    LEFT_TAB_W,
                    LEFT_TAB_H,
                    DESK_TEXTURE_W,
                    DESK_TEXTURE_H
            );

            ItemStack stack = this.menu.getVisibleTabStack(i);

            if (!stack.isEmpty()) {
                guiGraphics.renderItem(stack, slotX, slotY);
                guiGraphics.renderItemDecorations(this.font, stack, slotX, slotY);

                String name = stack.getHoverName().getString();
                int width = this.font.width(name) + 16;
                float scale = width > LEFT_TAB_W ? (float) LEFT_TAB_W / (float) width : 1.0F;

                guiGraphics.pose().pushMatrix();
                guiGraphics.pose().translate(x + 4, y + 25);
                guiGraphics.pose().scale(scale, scale);
                guiGraphics.drawString(
                        this.font,
                        name,
                        0,
                        0,
                        0xFF404040,
                        false
                );
                guiGraphics.pose().popMatrix();
            }

            this.drawTabNumber(guiGraphics, absoluteTab, x + 8, y + 3);

            if (hoveringTab && !stack.isEmpty() && this.minecraft != null && this.minecraft.player != null) {
                List<ClientTooltipComponent> tooltip = new ArrayList<>();

                for (var line : stack.getTooltipLines(
                        net.minecraft.world.item.Item.TooltipContext.EMPTY,
                        this.minecraft.player,
                        TooltipFlag.Default.NORMAL
                )) {
                    tooltip.add(new ClientTextTooltip(line.getVisualOrderText()));
                }

                this.pendingTabTooltip = tooltip;
                this.pendingTabTooltipX = mouseX;
                this.pendingTabTooltipY = mouseY;
            }
        }

        // Bottom arrow
        {
            TabVisualState state = getArrowState(canScrollDown, activeBelow);
            int u = getStateU(0, LEFT_ARROW_W, state);

            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    DESK_TEXTURE,
                    this.leftPos + LEFT_ARROW_BOTTOM_X,
                    this.topPos + LEFT_ARROW_BOTTOM_Y,
                    u,
                    212,
                    LEFT_ARROW_W,
                    LEFT_ARROW_H,
                    DESK_TEXTURE_W,
                    DESK_TEXTURE_H
            );
        }
    }

    private void drawSurfaceBackground(GuiGraphics guiGraphics) {
        int x = this.leftPos + SURFACE_OFFSET_X;
        int y = this.topPos + SURFACE_Y;
        guiGraphics.fill(x, y, x + SURFACE_PAGE_WIDTH, y + SURFACE_HEIGHT, 0xFF000000);
    }

    private void drawSurfaceEntries(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int clipLeft = this.leftPos + SURFACE_OFFSET_X;
        int clipTop = this.topPos + SURFACE_Y;
        int clipRight = clipLeft + SURFACE_PAGE_WIDTH;
        int clipBottom = clipTop + SURFACE_HEIGHT;

        guiGraphics.enableScissor(clipLeft, clipTop, clipRight, clipBottom);

        for (PageSurfaceRenderer.SurfaceEntry entry : this.toSurfaceEntries()) {
            int drawX = this.leftPos + SURFACE_OFFSET_X + entry.x();
            int drawY = this.topPos + SURFACE_Y + entry.y() - this.scroll;

            if (drawY + PAGE_HEIGHT < clipTop || drawY > clipBottom) {
                continue;
            }

            boolean hovered =
                    mouseX >= clipLeft && mouseX < clipRight
                            && mouseY >= clipTop && mouseY < clipBottom
                            && mouseX >= drawX && mouseX < drawX + PAGE_WIDTH
                            && mouseY >= drawY && mouseY < drawY + PAGE_HEIGHT;

            myst.synthetic.client.render.PageCardRenderer.drawPageCard(
                    guiGraphics,
                    drawX,
                    drawY,
                    entry.stack(),
                    entry.placeholder(),
                    hovered
            );

            if (entry.count() > 1) {
                guiGraphics.drawString(
                        this.font,
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

    private void drawSurfaceScrollbar(GuiGraphics guiGraphics) {
        int x = this.leftPos + SURFACE_OFFSET_X + SCROLLBAR_X;
        int y = this.topPos + SCROLLBAR_Y;

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
        float sliderPos = this.getMaxScroll() <= 0 ? 0.0F : this.scroll / (float) this.getMaxScroll();
        sliderPos = Mth.clamp(sliderPos, 0.0F, 1.0F);
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

    private void drawTitleFrame(GuiGraphics guiGraphics) {
        int x1 = this.leftPos + TITLE_BOX_X;
        int y1 = this.topPos + TITLE_BOX_Y;
        int x2 = x1 + TITLE_BOX_W;
        int y2 = y1 + TITLE_BOX_H;

        // Outer dark bevel
        guiGraphics.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0xFF5A5A5A);

        // Black text field
        guiGraphics.fill(x1, y1, x2, y2, 0xFF000000);
    }

    private void drawPreviewPage(GuiGraphics guiGraphics, int x, int y, ItemStack stack, boolean placeholder, boolean hovered) {
        float scaleX = PREVIEW_PAGE_WIDTH / (float) myst.synthetic.client.render.PageCardRenderer.CARD_WIDTH;
        float scaleY = PREVIEW_PAGE_HEIGHT / (float) myst.synthetic.client.render.PageCardRenderer.CARD_HEIGHT;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x, y);
        guiGraphics.pose().scale(scaleX, scaleY);

        myst.synthetic.client.render.PageCardRenderer.drawPageCard(
                guiGraphics,
                0,
                0,
                stack,
                placeholder,
                hovered
        );

        guiGraphics.pose().popMatrix();
    }

    private void drawTargetPreview(GuiGraphics guiGraphics) {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        ItemStack target = this.menu.getTargetStack();
        if (target.isEmpty()) {
            return;
        }

        this.clampPreviewScroll();

        int left = this.leftPos + PREVIEW_X;
        int top = this.topPos + PREVIEW_Y;
        int right = left + PREVIEW_W;
        int bottom = top + PREVIEW_H;

        // Legacy background
        guiGraphics.fill(left, top, right, bottom, 0xAA000000);

        // Single-page mode
        if (this.targetHasSinglePagePreview()) {
            guiGraphics.enableScissor(left + 1, top, right - 1, bottom);

            this.drawPreviewPage(
                    guiGraphics,
                    left + 34,
                    top + PREVIEW_PAGE_Y,
                    target,
                    false,
                    false
            );

            guiGraphics.disableScissor();
            return;
        }

        // Scrollable strip mode
        if (this.targetHasScrollablePreview()) {
            List<ItemStack> entries = this.getPreviewEntries();
            int start = this.previewScroll;
            int shown = Math.min(PREVIEW_VISIBLE_COUNT, Math.max(0, entries.size() - start));

            ItemStack targetStack = this.menu.getTargetStack();
            boolean folderPreview = targetStack.is(myst.synthetic.MystcraftItems.FOLDER);
            int lastFilled = folderPreview ? this.getFolderPreviewLastFilledIndex(targetStack) : -1;

            guiGraphics.enableScissor(left + 1, top, right - 1, bottom);

            for (int i = 0; i < PREVIEW_VISIBLE_COUNT; i++) {
                int absolute = start + i;
                if (absolute < 0 || absolute >= entries.size()) {
                    continue;
                }

                ItemStack entry = entries.get(absolute);
                int drawX = left + PREVIEW_PAGE_X + i * PREVIEW_PAGE_SPACING;
                int drawY = top + PREVIEW_PAGE_Y;

                if (folderPreview && entry.isEmpty()) {
                    if (absolute <= lastFilled) {
                        // Empty slot within the used folder range: dark placeholder page.
                        this.drawPreviewPage(
                                guiGraphics,
                                drawX,
                                drawY,
                                entry,
                                true,
                                false
                        );
                    }
                    // Trailing empty slots after the last filled page:
                    // draw nothing at all, but keep them logically clickable.
                    continue;
                }

                this.drawPreviewPage(
                        guiGraphics,
                        drawX,
                        drawY,
                        entry,
                        false,
                        false
                );
            }

            guiGraphics.disableScissor();

            boolean canLeft = this.previewScroll > 0;
            boolean canRight = this.previewScroll < this.getPreviewMaxScroll();

            int leftColor = canLeft ? 0xAA000000 : 0x33000000;
            int rightColor = canRight ? 0xAA000000 : 0x33000000;

            guiGraphics.fill(
                    left + PREVIEW_LEFT_ARROW_X - PREVIEW_X,
                    top,
                    left + PREVIEW_LEFT_ARROW_X - PREVIEW_X + PREVIEW_ARROW_W,
                    top + PREVIEW_ARROW_H,
                    leftColor
            );

            guiGraphics.fill(
                    left + PREVIEW_RIGHT_ARROW_X - PREVIEW_X,
                    top,
                    left + PREVIEW_RIGHT_ARROW_X - PREVIEW_X + PREVIEW_ARROW_W,
                    top + PREVIEW_ARROW_H,
                    rightColor
            );
        }
    }

    private void drawInkMeter(GuiGraphics guiGraphics) {
        int x1 = this.leftPos + INK_METER_X;
        int y1 = this.topPos + INK_METER_Y;
        int x2 = x1 + INK_METER_W;
        int y2 = y1 + INK_METER_H;

        // Legacy outer tank border/background
        guiGraphics.fill(x1, y1, x2, y2, 0x99000000);

        // Legacy inner tank gradient
        guiGraphics.fillGradient(
                x1 + 1,
                y1 + 1,
                x2 - 1,
                y2 - 1,
                0xFFCCCCEE,
                0xFF666699
        );

        int innerX1 = x1 + 1;
        int innerY1 = y1 + 1;
        int innerX2 = x2 - 1;
        int innerY2 = y2 - 1;

        int innerHeight = innerY2 - innerY1;
        int fill = this.menu.getInkFillScaled(innerHeight);

        if (fill <= 0) {
            return;
        }

        int fillTop = innerY2 - fill;
        int fillBottom = innerY2;

        guiGraphics.enableScissor(innerX1, fillTop, innerX2, fillBottom);

        // Animated fluid body
        renderInkFluidTexture(guiGraphics, innerX1, fillTop, innerX2 - innerX1, fillBottom - fillTop);

        // Tint to the real modern black ink colour
        guiGraphics.fill(innerX1, fillTop, innerX2, fillBottom, 0xCC191919);

        guiGraphics.disableScissor();
    }

    private void renderInkFluidTexture(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        int frame = (int) ((System.currentTimeMillis() / 120L) % 32L);
        int v = frame * 16;

        for (int drawX = 0; drawX < width; drawX += 16) {
            for (int drawY = 0; drawY < height; drawY += 16) {
                int w = Math.min(16, width - drawX);
                int h = Math.min(16, height - drawY);

                guiGraphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        FLUID_TEXTURE,
                        x + drawX,
                        y + drawY,
                        0,
                        v,
                        w,
                        h,
                        16,
                        512
                );
            }
        }
    }

    private boolean isMouseOverInkMeter(int mouseX, int mouseY) {
        return mouseX >= this.leftPos + INK_METER_X
                && mouseX < this.leftPos + INK_METER_X + INK_METER_W
                && mouseY >= this.topPos + INK_METER_Y
                && mouseY < this.topPos + INK_METER_Y + INK_METER_H;
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

    private boolean isOverLeftArrowTop(int mouseX, int mouseY) {
        return mouseX >= this.leftPos + LEFT_ARROW_TOP_X
                && mouseX < this.leftPos + LEFT_ARROW_TOP_X + LEFT_ARROW_W
                && mouseY >= this.topPos + LEFT_ARROW_TOP_Y
                && mouseY < this.topPos + LEFT_ARROW_TOP_Y + LEFT_ARROW_H;
    }

    private boolean isOverLeftArrowBottom(int mouseX, int mouseY) {
        return mouseX >= this.leftPos + LEFT_ARROW_BOTTOM_X
                && mouseX < this.leftPos + LEFT_ARROW_BOTTOM_X + LEFT_ARROW_W
                && mouseY >= this.topPos + LEFT_ARROW_BOTTOM_Y
                && mouseY < this.topPos + LEFT_ARROW_BOTTOM_Y + LEFT_ARROW_H;
    }

    private int getHoveredOrderedFolderSlot(int mouseX, int mouseY) {
        int relX = mouseX - (this.leftPos + SURFACE_OFFSET_X);
        int relY = mouseY - (this.topPos + SURFACE_Y);

        if (relX < 0 || relY < 0 || relX >= SURFACE_PAGE_WIDTH || relY >= SURFACE_HEIGHT) {
            return -1;
        }

        relY += this.scroll;

        int col = relX / PageSurfaceRenderer.PAGE_X_STEP;
        int row = relY / PageSurfaceRenderer.PAGE_Y_STEP;

        if (col < 0 || col >= PageSurfaceRenderer.COLUMNS || row < 0) {
            return -1;
        }

        int localX = relX % PageSurfaceRenderer.PAGE_X_STEP;
        int localY = relY % PageSurfaceRenderer.PAGE_Y_STEP;

        if (localX >= PAGE_WIDTH || localY >= PAGE_HEIGHT) {
            return -1;
        }

        return row * PageSurfaceRenderer.COLUMNS + col;
    }

    private boolean isOverPageArea(int mouseX, int mouseY) {
        int relX = mouseX - (this.leftPos + SURFACE_OFFSET_X);
        int relY = mouseY - (this.topPos + SURFACE_Y);

        return relX >= 0
                && relY >= 0
                && relX < SURFACE_PAGE_WIDTH
                && relY < SURFACE_HEIGHT;
    }

    private boolean handleArrowClick(int mouseX, int mouseY) {
        if (this.isOverLeftArrowTop(mouseX, mouseY)) {
            this.shiftTabWindow(-1);
            return true;
        }

        if (this.isOverLeftArrowBottom(mouseX, mouseY)) {
            this.shiftTabWindow(1);
            return true;
        }

        return false;
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

            int slotX = x + LEFT_TAB_SLOT_X;
            int slotY = y + LEFT_TAB_SLOT_Y;
            boolean overSlot =
                    mouseX >= slotX && mouseX < slotX + LEFT_TAB_SLOT_W
                            && mouseY >= slotY && mouseY < slotY + LEFT_TAB_SLOT_H;

            if (overSlot) {
                if (this.menu.getCarried().isEmpty()) {
                    this.minecraft.gameMode.handleInventoryButtonClick(
                            this.menu.containerId,
                            WritingDeskMenu.BUTTON_PICKUP_TAB_STACK_START + absoluteTab
                    );
                    return true;
                }

                if (DeskItemBehaviors.canBeDeskTabStorage(this.menu.getCarried())) {
                    this.minecraft.gameMode.handleInventoryButtonClick(
                            this.menu.containerId,
                            WritingDeskMenu.BUTTON_PLACE_TAB_STACK_START + absoluteTab
                    );
                    return true;
                }
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

    private boolean handleSurfaceInsertClick(int mouseX, int mouseY) {
        if (this.minecraft == null || this.minecraft.gameMode == null) {
            return false;
        }

        if (this.menu.getCarried().isEmpty() || !this.menu.getCarried().is(MystcraftItems.PAGE)) {
            return false;
        }

        if (!this.isOverPageArea(mouseX, mouseY)) {
            return false;
        }

        ItemStack activeStorage = this.menu.getTabStack(this.menu.getActiveTab());

        if (activeStorage.is(myst.synthetic.MystcraftItems.FOLDER)) {
            DisplayEntry hovered = this.getHoveredEntry(mouseX, mouseY);

            // A-Z/search mode:
            // clicking a shown page replaces that exact folder slot,
            // clicking empty area adds to the first free folder slot.
            if (this.sortAlphabetically || (this.searchBox != null && !this.searchBox.getValue().isEmpty())) {
                if (hovered != null && hovered.absoluteIndex >= 0) {
                    this.minecraft.gameMode.handleInventoryButtonClick(
                            this.menu.containerId,
                            WritingDeskMenu.BUTTON_PLACE_CARRIED_AT_START + hovered.absoluteIndex
                    );
                } else {
                    this.minecraft.gameMode.handleInventoryButtonClick(
                            this.menu.containerId,
                            WritingDeskMenu.BUTTON_ADD_CARRIED_TO_TAB_START + this.menu.getActiveTab()
                    );
                }
                return true;
            }

            int orderedSlot = this.getHoveredOrderedFolderSlot(mouseX, mouseY);
            if (orderedSlot >= 0) {
                this.minecraft.gameMode.handleInventoryButtonClick(
                        this.menu.containerId,
                        WritingDeskMenu.BUTTON_PLACE_CARRIED_AT_START + orderedSlot
                );
                return true;
            }

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