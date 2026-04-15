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

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void removed() {
        this.pushTitleIfChanged();
        super.removed();
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

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/page/symbolcomponents.png"),
                x,
                y,
                u,
                v,
                16,
                16,
                atlasColumns * tileSize,
                atlasColumns * tileSize
        );
    }

    private void drawLeftTabStrip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int topSlot = this.menu.getFirstVisibleTab();
        int activeSlot = this.menu.getActiveTab();

        boolean canScrollUp = topSlot > 0;
        boolean canScrollDown = topSlot + TAB_COUNT < BlockEntityDesk.TAB_SLOT_COUNT;
        boolean activeAbove = activeSlot < topSlot;
        boolean activeBelow = activeSlot >= topSlot + TAB_COUNT;

        // Top arrow
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                DESK_TEXTURE,
                this.leftPos + LEFT_ARROW_TOP_X,
                this.topPos + LEFT_ARROW_TOP_Y,
                0,
                203,
                LEFT_ARROW_W,
                LEFT_ARROW_H,
                DESK_TEXTURE_W,
                DESK_TEXTURE_H
        );

        // Legacy-style tinting:
        // greyed when unavailable, blue when active tab is above current window
        if (!canScrollUp) {
            guiGraphics.fill(
                    this.leftPos + LEFT_ARROW_TOP_X,
                    this.topPos + LEFT_ARROW_TOP_Y,
                    this.leftPos + LEFT_ARROW_TOP_X + LEFT_ARROW_W,
                    this.topPos + LEFT_ARROW_TOP_Y + LEFT_ARROW_H,
                    0xA0000000
            );
        } else if (activeAbove) {
            guiGraphics.fill(
                    this.leftPos + LEFT_ARROW_TOP_X,
                    this.topPos + LEFT_ARROW_TOP_Y,
                    this.leftPos + LEFT_ARROW_TOP_X + LEFT_ARROW_W,
                    this.topPos + LEFT_ARROW_TOP_Y + LEFT_ARROW_H,
                    0x606080FF
            );
        }

        for (int i = 0; i < TAB_COUNT; i++) {
            int absoluteTab = topSlot + i;
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

            if (absoluteTab == activeSlot) {
                guiGraphics.fill(
                        x,
                        y,
                        x + LEFT_TAB_W,
                        y + LEFT_TAB_H,
                        0x604060FF
                );
            }

            ItemStack stack = this.menu.getVisibleTabStack(i);
            int slotX = x + LEFT_TAB_SLOT_X;
            int slotY = y + LEFT_TAB_SLOT_Y;
            boolean hoveringSlot =
                    mouseX >= slotX && mouseX < slotX + LEFT_TAB_SLOT_W
                            && mouseY >= slotY && mouseY < slotY + LEFT_TAB_SLOT_H;

            if (hoveringSlot) {
                guiGraphics.fill(
                        slotX,
                        slotY,
                        slotX + LEFT_TAB_SLOT_W,
                        slotY + LEFT_TAB_SLOT_H,
                        0x60FFFFFF
                );
            }

            // Legacy D'ni-style tab number position
            this.drawTabNumber(guiGraphics, absoluteTab, x + 8, y + 3);

            if (!stack.isEmpty()) {
                guiGraphics.renderItem(stack, slotX, slotY);
                guiGraphics.renderItemDecorations(this.font, stack, slotX, slotY);

                String name = stack.getHoverName().getString();
                float scale = 1.0F;
                int width = this.font.width(name) + 16;
                if (width > LEFT_TAB_W) {
                    scale = (float) LEFT_TAB_W / (float) width;
                }

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

        // Bottom arrow
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                DESK_TEXTURE,
                this.leftPos + LEFT_ARROW_BOTTOM_X,
                this.topPos + LEFT_ARROW_BOTTOM_Y,
                0,
                212,
                LEFT_ARROW_W,
                LEFT_ARROW_H,
                DESK_TEXTURE_W,
                DESK_TEXTURE_H
        );

        if (!canScrollDown) {
            guiGraphics.fill(
                    this.leftPos + LEFT_ARROW_BOTTOM_X,
                    this.topPos + LEFT_ARROW_BOTTOM_Y,
                    this.leftPos + LEFT_ARROW_BOTTOM_X + LEFT_ARROW_W,
                    this.topPos + LEFT_ARROW_BOTTOM_Y + LEFT_ARROW_H,
                    0xA0000000
            );
        } else if (activeBelow) {
            guiGraphics.fill(
                    this.leftPos + LEFT_ARROW_BOTTOM_X,
                    this.topPos + LEFT_ARROW_BOTTOM_Y,
                    this.leftPos + LEFT_ARROW_BOTTOM_X + LEFT_ARROW_W,
                    this.topPos + LEFT_ARROW_BOTTOM_Y + LEFT_ARROW_H,
                    0x606080FF
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

    private void drawTargetPreview(GuiGraphics guiGraphics) {
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

        int drawX = clipLeft + 4;
        int drawY = clipTop + 4;

        int shown = Math.min(4, pages.size());
        for (int i = 0; i < shown; i++) {
            ItemStack page = pages.get(i);
            if (page.isEmpty()) {
                continue;
            }

            myst.synthetic.client.render.PageCardRenderer.drawPageCard(
                    guiGraphics,
                    drawX + i * 24,
                    drawY,
                    page,
                    false,
                    false
            );
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