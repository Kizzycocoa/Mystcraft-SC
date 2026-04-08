package myst.synthetic.client.gui;

import myst.synthetic.menu.FolderMenu;
import myst.synthetic.page.Page;
import myst.synthetic.page.symbol.PageSymbol;
import myst.synthetic.page.symbol.PageSymbolRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
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
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import myst.synthetic.client.gui.widget.LegacyTinyToggleButton;

public class FolderScreen extends AbstractContainerScreen<FolderMenu> {

    private static final Identifier DESK_TEXTURE =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/writingdesk.png");

    private static final int SURFACE_WIDTH = 176;
    private static final int SURFACE_HEIGHT = 132;
    private static final int BUTTON_SIZE = 18;
    private static final int INVENTORY_HEIGHT = 80;
    private static final int TOTAL_HEIGHT = SURFACE_HEIGHT + BUTTON_SIZE + INVENTORY_HEIGHT + 1;

    private LegacyTinyToggleButton sortButton;
    private LegacyTinyToggleButton allButton;
    private EditBox searchBox;

    private boolean draggingScrollbar = false;
    private int scroll = 0;

    private boolean sortAlphabetically = true;
    private boolean showAllSymbols = false;

    private List<PageSurfaceRenderer.SurfaceEntry> entries = List.of();

    public FolderScreen(FolderMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = SURFACE_WIDTH;
        this.imageHeight = TOTAL_HEIGHT;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 140;
        this.titleLabelX = 8;
        this.titleLabelY = 140;
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
                            this.sortAlphabetically = true;
                            this.scroll = 0;
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
                        }
                )
        );

        this.searchBox = new EditBox(
                this.font,
                this.leftPos + 41,
                this.topPos + 1,
                134,
                16,
                Component.translatable("screen.mystcraft-sc.page_browser.search")
        );
        this.searchBox.setBordered(false);
        this.searchBox.setMaxLength(64);
        this.searchBox.setTextColor(0xFFE0E0E0);
        this.searchBox.setTextColorUneditable(0xFF707070);
        this.searchBox.setResponder(text -> this.scroll = 0);
        this.addRenderableWidget(this.searchBox);
        this.searchBox.setCanLoseFocus(true);
    }

    private void drawLegacySearchBox(GuiGraphics guiGraphics) {
        int x1 = this.leftPos + 40;
        int y1 = this.topPos;
        int x2 = this.leftPos + 176;
        int y2 = this.topPos + 18;

        boolean focused = this.searchBox != null && this.searchBox.isFocused();

        int outer = focused ? 0xFFC0C0C0 : 0xFFA0A0A0;
        int inner = 0xFF000000;

        guiGraphics.fill(x1, y1, x2, y2, outer);
        guiGraphics.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, inner);
    }

    @Override
    public void containerTick() {
        super.containerTick();
    }

    private void renderSearchGhostText(GuiGraphics guiGraphics) {
        if (this.searchBox == null) {
            return;
        }

        if (!this.searchBox.getValue().isEmpty()) {
            return;
        }

        guiGraphics.drawString(
                this.font,
                "Search...",
                this.searchBox.getX() + 1,
                this.searchBox.getY() + 4,
                0xFF707070,
                false
        );
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        this.rebuildEntries(mouseX, mouseY);

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

        this.drawLegacySearchBox(guiGraphics);

        PageSurfaceRenderer.drawSurfaceBackground(guiGraphics, this.leftPos, this.topPos);
        PageSurfaceRenderer.drawEntries(
                guiGraphics,
                this.font,
                this.leftPos,
                this.topPos,
                mouseX,
                mouseY,
                this.scroll,
                this.entries
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
        if (this.searchBox != null && this.searchBox.isFocused() && this.searchBox.keyPressed(input)) {
            return true;
        }

        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (this.searchBox != null && this.searchBox.isFocused() && this.searchBox.charTyped(input)) {
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
            PageSurfaceRenderer.SurfaceEntry hoveredEntry = PageSurfaceRenderer.getHoveredEntry(
                    this.leftPos,
                    this.topPos,
                    mouseX,
                    mouseY,
                    this.scroll,
                    this.entries
            );

            if (hoveredEntry != null && this.minecraft != null && this.minecraft.gameMode != null) {
                if (hoveredEntry.slotIndex() >= 0 && !hoveredEntry.stack().isEmpty()) {
                    if (this.isDefaultOrderedView() && this.menu.canPreviewPlace()) {
                        this.minecraft.gameMode.handleInventoryButtonClick(
                                this.menu.containerId,
                                FolderMenu.BUTTON_SWAP_ORDERED_START + hoveredEntry.slotIndex()
                        );
                    } else {
                        this.minecraft.gameMode.handleInventoryButtonClick(
                                this.menu.containerId,
                                FolderMenu.BUTTON_TAKE_ORDERED_START + hoveredEntry.slotIndex()
                        );
                    }
                    return true;
                }
            }

            if (this.isDefaultOrderedView() && this.minecraft != null && this.minecraft.gameMode != null && this.menu.canPreviewPlace()) {
                int hoveredSlot = PageSurfaceRenderer.getHoveredOrderedSlot(
                        this.leftPos,
                        this.topPos,
                        mouseX,
                        mouseY,
                        this.scroll
                );

                if (hoveredSlot >= 0) {
                    this.minecraft.gameMode.handleInventoryButtonClick(
                            this.menu.containerId,
                            FolderMenu.BUTTON_PLACE_ORDERED_START + hoveredSlot
                    );
                    return true;
                }
            }

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

    private void rebuildEntries(int mouseX, int mouseY) {
        if (this.isDefaultOrderedView()) {
            this.rebuildOrderedEntries(mouseX, mouseY);
            return;
        }

        this.rebuildCollectionEntries();
    }

    private void rebuildOrderedEntries(int mouseX, int mouseY) {
        List<PageSurfaceRenderer.SurfaceEntry> built = new ArrayList<>();

        int lastIndex = this.menu.findLastMeaningfulIndex();

        int hoveredPreviewSlot = -1;
        if (this.menu.canPreviewPlace()
                && PageSurfaceRenderer.isOverPageArea(this.leftPos, this.topPos, mouseX, mouseY)) {
            hoveredPreviewSlot = PageSurfaceRenderer.getHoveredOrderedSlot(
                    this.leftPos,
                    this.topPos,
                    mouseX,
                    mouseY,
                    this.scroll
            );
        }

        if (hoveredPreviewSlot >= 0) {
            lastIndex = Math.max(lastIndex, hoveredPreviewSlot);
        }

        if (lastIndex < 0) {
            this.entries = List.of();
            this.scroll = 0;
            return;
        }

        for (int i = 0; i <= lastIndex; i++) {
            int x = (i % PageSurfaceRenderer.COLUMNS) * PageSurfaceRenderer.PAGE_X_STEP;
            int y = (i / PageSurfaceRenderer.COLUMNS) * PageSurfaceRenderer.PAGE_Y_STEP;

            ItemStack stack = this.menu.getOrderedItem(i);
            boolean placeholder = stack.isEmpty();

            built.add(new PageSurfaceRenderer.SurfaceEntry(
                    i,
                    stack.copy(),
                    placeholder,
                    stack.isEmpty() ? 0 : 1,
                    this.getSearchName(stack),
                    x,
                    y
            ));
        }

        this.entries = built;
        this.scroll = Math.max(0, Math.min(this.scroll, this.getMaxScroll()));
    }

    private void rebuildCollectionEntries() {
        List<AggregatedEntry> built = new ArrayList<>();
        String normalizedSearch = normalize(this.searchBox == null ? "" : this.searchBox.getValue());

        for (int slotIndex = 0; slotIndex < FolderMenu.FOLDER_SLOT_COUNT; slotIndex++) {
            ItemStack stack = this.menu.getOrderedItem(slotIndex);
            if (stack.isEmpty()) {
                continue;
            }

            String searchName = this.getSearchName(stack);
            if (!this.matchesSearch(normalizedSearch, searchName)) {
                continue;
            }

            AggregatedEntry existing = null;
            for (AggregatedEntry entry : built) {
                if (ItemStack.isSameItemSameComponents(entry.stack, stack)) {
                    existing = entry;
                    break;
                }
            }

            if (existing != null) {
                existing.count++;
            } else {
                built.add(new AggregatedEntry(slotIndex, stack.copyWithCount(1), 1, searchName, false));
            }
        }

        if (this.showAllSymbols) {
            for (PageSymbol symbol : PageSymbolRegistry.values()) {
                ItemStack virtualPage = Page.createSymbolPage(symbol.id());
                String searchName = symbol.displayName().getString();

                if (!this.matchesSearch(normalizedSearch, searchName)) {
                    continue;
                }

                boolean found = false;
                for (AggregatedEntry entry : built) {
                    if (ItemStack.isSameItemSameComponents(entry.stack, virtualPage)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    built.add(new AggregatedEntry(-1, virtualPage, 0, searchName, true));
                }
            }
        }

        if (this.sortAlphabetically) {
            built.sort(
                    Comparator.comparing((AggregatedEntry entry) -> normalize(entry.searchName))
                            .thenComparingInt(entry -> entry.slotIndex < 0 ? Integer.MAX_VALUE : entry.slotIndex)
            );
        }

        List<PageSurfaceRenderer.SurfaceEntry> rendered = new ArrayList<>();
        for (int i = 0; i < built.size(); i++) {
            AggregatedEntry entry = built.get(i);

            int x = (i % PageSurfaceRenderer.COLUMNS) * PageSurfaceRenderer.PAGE_X_STEP;
            int y = (i / PageSurfaceRenderer.COLUMNS) * PageSurfaceRenderer.PAGE_Y_STEP;

            rendered.add(new PageSurfaceRenderer.SurfaceEntry(
                    entry.slotIndex,
                    entry.stack.copy(),
                    entry.placeholder,
                    entry.count,
                    entry.searchName,
                    x,
                    y
            ));
        }

        this.entries = rendered;
        this.scroll = Math.max(0, Math.min(this.scroll, this.getMaxScroll()));
    }

    private int getMaxScroll() {
        return PageSurfaceRenderer.getMaxScroll(this.entries);
    }

    private boolean isOverScrollbar(int mouseX, int mouseY) {
        int x = this.leftPos + PageSurfaceRenderer.SCROLLBAR_X;
        int y = this.topPos + PageSurfaceRenderer.SCROLLBAR_Y;
        return mouseX >= x && mouseX < x + PageSurfaceRenderer.SCROLLBAR_WIDTH
                && mouseY >= y && mouseY < y + PageSurfaceRenderer.SCROLLBAR_HEIGHT;
    }

    private boolean isMouseOverSearchBox(int mouseX, int mouseY) {
        return mouseX >= this.leftPos + 40
                && mouseX < this.leftPos + 176
                && mouseY >= this.topPos
                && mouseY < this.topPos + 18;
    }

    private void scrollToMouse(int mouseY) {
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

    private void renderPageTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!PageSurfaceRenderer.isOverPageArea(this.leftPos, this.topPos, mouseX, mouseY)) {
            return;
        }

        PageSurfaceRenderer.SurfaceEntry hoveredEntry = PageSurfaceRenderer.getHoveredEntry(
                this.leftPos,
                this.topPos,
                mouseX,
                mouseY,
                this.scroll,
                this.entries
        );

        if (hoveredEntry == null || hoveredEntry.stack().isEmpty() || this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        List<ClientTooltipComponent> tooltip = new ArrayList<>();
        for (var line : hoveredEntry.stack().getTooltipLines(
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

    private boolean isDefaultOrderedView() {
        return !this.showAllSymbols && normalize(this.searchBox == null ? "" : this.searchBox.getValue()).isEmpty();
    }

    private String getSearchName(ItemStack stack) {
        if (stack.isEmpty()) {
            return "";
        }

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

    private boolean matchesSearch(String normalizedSearch, String searchName) {
        return normalizedSearch.isEmpty() || normalize(searchName).contains(normalizedSearch);
    }

    private static String normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private static final class AggregatedEntry {
        private final int slotIndex;
        private final ItemStack stack;
        private int count;
        private final String searchName;
        private final boolean placeholder;

        private AggregatedEntry(int slotIndex, ItemStack stack, int count, String searchName, boolean placeholder) {
            this.slotIndex = slotIndex;
            this.stack = stack;
            this.count = count;
            this.searchName = searchName;
            this.placeholder = placeholder;
        }
    }
}