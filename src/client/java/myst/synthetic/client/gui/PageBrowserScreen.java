package myst.synthetic.client.gui;

import myst.synthetic.client.gui.widget.LegacyTinyToggleButton;
import myst.synthetic.page.Page;
import myst.synthetic.page.symbol.PageSymbol;
import myst.synthetic.page.symbol.PageSymbolRegistry;
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
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public abstract class PageBrowserScreen<T extends net.minecraft.world.inventory.AbstractContainerMenu>
        extends AbstractContainerScreen<T> {

    protected static final Identifier DESK_TEXTURE =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/writingdesk.png");

    protected static final int SURFACE_WIDTH = 176;
    protected static final int SURFACE_HEIGHT = 132;
    protected static final int BUTTON_SIZE = 18;
    protected static final int INVENTORY_HEIGHT = 80;
    protected static final int TOTAL_HEIGHT = SURFACE_HEIGHT + BUTTON_SIZE + INVENTORY_HEIGHT + 1;

    protected static final int SEARCH_FRAME_X = 40;
    protected static final int SEARCH_FRAME_Y = 0;
    protected static final int SEARCH_FRAME_WIDTH = 136;
    protected static final int SEARCH_FRAME_HEIGHT = 18;

    protected static final int LEGACY_SEARCH_TEXT_X_INSET = 4;
    protected static final int LEGACY_SEARCH_TEXT_Y_OFFSET = 5;

    protected static final Comparator<DisplayEntry> ENTRY_COMPARATOR =
            Comparator.comparing((DisplayEntry entry) -> normalize(entry.searchName))
                    .thenComparingInt(entry -> entry.absoluteIndex < 0 ? Integer.MAX_VALUE : entry.absoluteIndex);

    protected LegacyTinyToggleButton sortButton;
    protected LegacyTinyToggleButton allButton;
    protected EditBox searchBox;

    protected boolean sortAlphabetically = false;
    protected boolean showAllSymbols = false;
    protected boolean draggingScrollbar = false;
    protected int scroll = 0;

    protected List<DisplayEntry> displayEntries = List.of();

    protected PageBrowserScreen(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = SURFACE_WIDTH;
        this.imageHeight = TOTAL_HEIGHT;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = SURFACE_HEIGHT + BUTTON_SIZE + 7;
        this.titleLabelX = 8;
        this.titleLabelY = SURFACE_HEIGHT + BUTTON_SIZE + 7;
    }

    @Override
    protected void init() {
        super.init();

        this.sortButton = this.addRenderableWidget(
                new LegacyTinyToggleButton(
                        this.leftPos,
                        this.topPos,
                        18,
                        18,
                        "AZ",
                        this.font,
                        () -> this.sortAlphabetically,
                        button -> {
                            this.sortAlphabetically = !this.sortAlphabetically;
                            this.scroll = 0;
                            this.rebuildDisplayEntries();
                        }
                )
        );

        this.allButton = this.addRenderableWidget(
                new LegacyTinyToggleButton(
                        this.leftPos + 18,
                        this.topPos,
                        18,
                        18,
                        "ALL",
                        this.font,
                        () -> this.showAllSymbols,
                        button -> {
                            this.showAllSymbols = !this.showAllSymbols;
                            this.scroll = 0;
                            this.rebuildDisplayEntries();
                        }
                )
        );

        this.searchBox = new EditBox(
                this.font,
                this.leftPos + SEARCH_FRAME_X + LEGACY_SEARCH_TEXT_X_INSET,
                this.topPos + SEARCH_FRAME_Y + 5,
                SEARCH_FRAME_WIDTH - (LEGACY_SEARCH_TEXT_X_INSET * 2),
                SEARCH_FRAME_HEIGHT,
                Component.translatable("screen.mystcraft-sc.page_browser.search")
        );
        this.searchBox.setBordered(false);
        this.searchBox.setMaxLength(64);
        this.searchBox.setTextColor(0xFFE0E0E0);
        this.searchBox.setTextColorUneditable(0xFF707070);
        this.searchBox.setResponder(text -> {
            this.scroll = 0;
            this.rebuildDisplayEntries();
        });
        this.searchBox.setCanLoseFocus(true);
        this.addRenderableWidget(this.searchBox);

        this.rebuildDisplayEntries();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.rebuildDisplayEntries();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        this.drawLegacySearchBox(guiGraphics);

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                DESK_TEXTURE,
                this.leftPos,
                this.topPos + SURFACE_HEIGHT + 1,
                0,
                82,
                SURFACE_WIDTH,
                INVENTORY_HEIGHT,
                256,
                256
        );

        PageSurfaceRenderer.drawSurfaceBackground(guiGraphics, this.leftPos, this.topPos);
        PageSurfaceRenderer.drawEntries(
                guiGraphics,
                this.font,
                this.leftPos,
                this.topPos,
                mouseX,
                mouseY,
                this.scroll,
                this.toSurfaceEntries()
        );

        PageSurfaceRenderer.drawScrollbar(
                guiGraphics,
                this.leftPos,
                this.topPos,
                this.scroll,
                this.getMaxScroll()
        );
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
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

        return super.charTyped(input);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0) {
            this.scroll = Math.max(0, this.scroll - 8);
            return true;
        }

        if (scrollY < 0) {
            this.scroll = Math.min(this.getMaxScroll(), this.scroll + 8);
            return true;
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

        if (this.isOverScrollbar(mouseX, mouseY)) {
            this.draggingScrollbar = true;
            this.scrollToMouse(mouseY);
            return true;
        }

        if (PageSurfaceRenderer.isOverPageArea(this.leftPos, this.topPos, mouseX, mouseY)) {
            DisplayEntry hovered = this.getHoveredEntry(mouseX, mouseY);
            this.onSurfaceClicked(hovered, event);
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (this.draggingScrollbar) {
            this.scrollToMouse((int) event.y());
            return true;
        }

        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.draggingScrollbar = false;
        return super.mouseReleased(event);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        this.renderSearchGhostText(guiGraphics);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
        this.renderPageTooltip(guiGraphics, mouseX, mouseY);
    }

    protected void rebuildDisplayEntries() {
        List<DisplayEntry> built = new ArrayList<>();
        String search = this.searchBox == null ? "" : normalize(this.searchBox.getValue());

        this.buildOwnedEntries(built, search);

        if (this.showAllSymbols && this.allowShowAllSymbols()) {
            for (PageSymbol symbol : PageSymbolRegistry.values()) {
                ItemStack virtualPage = Page.createSymbolPage(symbol.id());
                String searchName = symbol.displayName().getString();

                if (!search.isEmpty() && !normalize(searchName).contains(search)) {
                    continue;
                }

                boolean found = false;
                for (DisplayEntry entry : built) {
                    if (ItemStack.isSameItemSameComponents(entry.stack, virtualPage)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    built.add(new DisplayEntry(virtualPage, -1, 0, searchName));
                }
            }
        }

        if (this.sortAlphabetically) {
            built.sort(ENTRY_COMPARATOR);
        }

        this.arrangeLinear(built);

        this.displayEntries = built;
        this.scroll = Math.max(0, Math.min(this.scroll, this.getMaxScroll()));
    }

    protected abstract void buildOwnedEntries(List<DisplayEntry> built, String normalizedSearch);

    protected abstract void onSurfaceClicked(DisplayEntry entry, MouseButtonEvent event);

    protected abstract boolean allowShowAllSymbols();

    protected String getSearchName(ItemStack stack) {
        Identifier symbolId = Page.getSymbol(stack);
        if (symbolId != null) {
            PageSymbol symbol = PageSymbolRegistry.get(symbolId);
            if (symbol != null) {
                return symbol.displayName().getString();
            }
            return symbolId.getPath();
        }

        return stack.getHoverName().getString();
    }

    protected void arrangeLinear(List<DisplayEntry> entries) {
        float x = 0.0f;
        float y = 0.0f;

        for (DisplayEntry entry : entries) {
            entry.x = x;
            entry.y = y;

            x += PageSurfaceRenderer.PAGE_X_STEP;
            if (x + PageSurfaceRenderer.PAGE_X_STEP > PageSurfaceRenderer.SURFACE_PAGE_WIDTH) {
                x = 0.0f;
                y += PageSurfaceRenderer.PAGE_Y_STEP;
            }
        }
    }

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

    protected void scrollToMouse(int mouseY) {
        int maxScroll = this.getMaxScroll();
        if (maxScroll <= 0) {
            this.scroll = 0;
            return;
        }

        int trackTop = this.topPos + PageSurfaceRenderer.SCROLLBAR_Y;
        int trackBottom = trackTop + PageSurfaceRenderer.SCROLLBAR_HEIGHT - 18;
        int clampedMouse = Math.max(trackTop, Math.min(mouseY - 9, trackBottom));

        int knobTravel = trackBottom - trackTop;
        if (knobTravel <= 0) {
            return;
        }

        this.scroll = ((clampedMouse - trackTop) * maxScroll) / knobTravel;
    }

    protected boolean isOverScrollbar(int mouseX, int mouseY) {
        int x = this.leftPos + PageSurfaceRenderer.SCROLLBAR_X;
        int y = this.topPos + PageSurfaceRenderer.SCROLLBAR_Y;
        return mouseX >= x && mouseX < x + PageSurfaceRenderer.SCROLLBAR_WIDTH
                && mouseY >= y && mouseY < y + PageSurfaceRenderer.SCROLLBAR_HEIGHT;
    }

    protected boolean isMouseOverSearchBox(int mouseX, int mouseY) {
        return mouseX >= this.leftPos + SEARCH_FRAME_X
                && mouseX < this.leftPos + SEARCH_FRAME_X + SEARCH_FRAME_WIDTH
                && mouseY >= this.topPos + SEARCH_FRAME_Y
                && mouseY < this.topPos + SEARCH_FRAME_Y + SEARCH_FRAME_HEIGHT;
    }

    protected DisplayEntry getHoveredEntry(int mouseX, int mouseY) {
        if (!PageSurfaceRenderer.isOverPageArea(this.leftPos, this.topPos, mouseX, mouseY)) {
            return null;
        }

        int clipTop = this.topPos + PageSurfaceRenderer.SURFACE_Y;
        int clipBottom = clipTop + PageSurfaceRenderer.SURFACE_HEIGHT;

        for (DisplayEntry entry : this.displayEntries) {
            int drawX = this.leftPos + PageSurfaceRenderer.SURFACE_X + Math.round(entry.x);
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

    protected void renderPageTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!PageSurfaceRenderer.isOverPageArea(this.leftPos, this.topPos, mouseX, mouseY)) {
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

    protected void drawLegacySearchBox(GuiGraphics guiGraphics) {
        int x1 = this.leftPos + SEARCH_FRAME_X;
        int y1 = this.topPos + SEARCH_FRAME_Y;
        int x2 = x1 + SEARCH_FRAME_WIDTH;
        int y2 = y1 + SEARCH_FRAME_HEIGHT;

        boolean focused = this.searchBox != null && this.searchBox.isFocused();

        int outer = focused ? 0xFFC0C0C0 : 0xFFA0A0A0;
        int inner = 0xFF000000;

        guiGraphics.fill(x1, y1, x2, y2, outer);
        guiGraphics.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, inner);
    }

    protected void renderSearchGhostText(GuiGraphics guiGraphics) {
        if (this.searchBox == null || !this.searchBox.getValue().isEmpty()) {
            return;
        }

        guiGraphics.drawString(
                this.font,
                "Search...",
                this.leftPos + SEARCH_FRAME_X + LEGACY_SEARCH_TEXT_X_INSET,
                this.topPos + SEARCH_FRAME_Y + LEGACY_SEARCH_TEXT_Y_OFFSET,
                0xFF707070,
                false
        );
    }

    protected List<PageSurfaceRenderer.SurfaceEntry> toSurfaceEntries() {
        List<PageSurfaceRenderer.SurfaceEntry> entries = new ArrayList<>(this.displayEntries.size());

        for (DisplayEntry entry : this.displayEntries) {
            entries.add(new PageSurfaceRenderer.SurfaceEntry(
                    entry.absoluteIndex,
                    entry.stack.copy(),
                    entry.absoluteIndex < 0,
                    entry.count,
                    entry.searchName,
                    Math.round(entry.x),
                    Math.round(entry.y)
            ));
        }

        return entries;
    }

    protected static String normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT).trim();
    }

    protected static final class DisplayEntry {
        public final ItemStack stack;
        public final int absoluteIndex;
        public int count;
        public final String searchName;
        public float x;
        public float y;

        public DisplayEntry(ItemStack stack, int absoluteIndex, int count, String searchName) {
            this.stack = stack;
            this.absoluteIndex = absoluteIndex;
            this.count = count;
            this.searchName = searchName;
        }
    }
}