package myst.synthetic.client.gui;

import myst.synthetic.menu.PortfolioMenu;
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

public class PortfolioScreen extends AbstractContainerScreen<PortfolioMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/notebook.png");

    private static final int GRID_COLUMNS = 7;
    private static final int GRID_ROWS_VISIBLE = 4;
    private static final int GRID_X = 25;
    private static final int GRID_Y = 19;
    private static final int CELL_SIZE = 18;
    private static final int ENTRIES_VISIBLE = GRID_COLUMNS * GRID_ROWS_VISIBLE;

    private static final int SCROLL_TRACK_X = 153;
    private static final int SCROLL_TRACK_Y = 17;
    private static final int SCROLL_TRACK_HEIGHT = 72;
    private static final int SCROLL_KNOB_HEIGHT = 14;

    private static final Comparator<DisplayEntry> ENTRY_COMPARATOR =
            Comparator.comparing((DisplayEntry entry) -> normalize(entry.searchName))
                    .thenComparingInt(entry -> entry.absoluteIndex);

    private Button sortButton;
    private Button allButton;
    private EditBox searchBox;

    private boolean draggingScrollbar = false;
    private boolean sortAlphabetically = true;
    private boolean showAllSymbols = false;
    private int scrollRow = 0;

    private List<DisplayEntry> displayEntries = List.of();

    public PortfolioScreen(PortfolioMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 72;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();

        this.sortButton = this.addRenderableWidget(
                Button.builder(this.sortButtonText(), button -> {
                            this.sortAlphabetically = !this.sortAlphabetically;
                            button.setMessage(this.sortButtonText());
                            this.rebuildDisplayEntries();
                        })
                        .pos(this.leftPos + 8, this.topPos + 0)
                        .size(18, 18)
                        .build()
        );

        this.allButton = this.addRenderableWidget(
                Button.builder(this.allButtonText(), button -> {
                            this.showAllSymbols = !this.showAllSymbols;
                            button.setMessage(this.allButtonText());
                            this.rebuildDisplayEntries();
                        })
                        .pos(this.leftPos + 28, this.topPos + 0)
                        .size(26, 18)
                        .build()
        );

        this.searchBox = new EditBox(
                this.font,
                this.leftPos + 56,
                this.topPos + 1,
                90,
                16,
                Component.translatable("screen.mystcraft-sc.portfolio.search")
        );
        this.searchBox.setBordered(false);
        this.searchBox.setMaxLength(64);
        this.searchBox.setResponder(text -> this.rebuildDisplayEntries());
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

        this.drawPortfolioGrid(guiGraphics, mouseX, mouseY);
        this.drawScrollbar(guiGraphics);
        this.drawPlayerInventoryFrames(guiGraphics);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x4A3927, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x4A3927, false);

        String countText = Integer.toString(this.menu.getStoredCount());
        int countWidth = this.font.width(countText);
        guiGraphics.drawString(this.font, countText, this.imageWidth - 8 - countWidth, 6, 0x4A3927, false);

        if (this.displayEntries.isEmpty()) {
            Component empty = Component.translatable("screen.mystcraft-sc.portfolio.empty");
            int width = this.font.width(empty);
            guiGraphics.drawString(
                    this.font,
                    empty,
                    (this.imageWidth - width) / 2,
                    50,
                    0x4A3927,
                    false
            );
        }
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0) {
            this.scrollRow = Math.max(0, this.scrollRow - 1);
            return true;
        }

        if (scrollY < 0) {
            this.scrollRow = Math.min(this.getMaxScrollRow(), this.scrollRow + 1);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int mouseX = (int) event.x();
        int mouseY = (int) event.y();

        if (this.searchBox != null && this.searchBox.mouseClicked(event, doubleClick)) {
            return true;
        }

        if (this.isOverScrollbar(mouseX, mouseY)) {
            this.draggingScrollbar = true;
            this.scrollToMouse(mouseY);
            return true;
        }

        if (this.minecraft != null && this.minecraft.gameMode != null) {
            int localIndex = this.getHoveredEntryIndex(mouseX, mouseY);
            if (localIndex >= 0) {
                DisplayEntry entry = this.getVisibleEntry(localIndex);
                if (entry != null && entry.absoluteIndex >= 0) {
                    this.minecraft.gameMode.handleInventoryButtonClick(
                            this.menu.containerId,
                            PortfolioMenu.BUTTON_TAKE_ABSOLUTE_START + entry.absoluteIndex
                    );
                    return true;
                }
            }
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

        if (this.searchBox != null) {
            this.searchBox.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);

        int hovered = this.getHoveredEntryIndex(mouseX, mouseY);

        if (hovered >= 0 && this.minecraft != null && this.minecraft.player != null) {
            DisplayEntry entry = this.getVisibleEntry(hovered);

            if (entry != null && !entry.stack.isEmpty()) {
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
                            Component.translatable("screen.mystcraft-sc.portfolio.copies", entry.count).getVisualOrderText()
                    ));
                } else if (entry.absoluteIndex < 0) {
                    tooltip.add(new ClientTextTooltip(
                            Component.translatable("screen.mystcraft-sc.portfolio.preview_only").getVisualOrderText()
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
        }
    }

    private void rebuildDisplayEntries() {
        List<DisplayEntry> built = new ArrayList<>();
        String search = this.searchBox == null ? "" : normalize(this.searchBox.getValue());

        for (int i = 0; i < this.menu.getStoredCount(); i++) {
            ItemStack stack = this.menu.getPortfolioData().getPage(i);
            if (stack.isEmpty()) {
                continue;
            }

            String searchName = this.getSearchName(stack);
            if (!search.isEmpty() && !normalize(searchName).contains(search)) {
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

        if (this.showAllSymbols) {
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

        this.displayEntries = built;
        this.scrollRow = Math.max(0, Math.min(this.scrollRow, this.getMaxScrollRow()));
    }

    private String getSearchName(ItemStack stack) {
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

    private Component sortButtonText() {
        return this.sortAlphabetically ? Component.literal("[AZ]") : Component.literal("AZ");
    }

    private Component allButtonText() {
        return this.showAllSymbols ? Component.literal("[ALL]") : Component.literal("ALL");
    }

    private int getTotalRows() {
        return Math.max(1, (this.displayEntries.size() + GRID_COLUMNS - 1) / GRID_COLUMNS);
    }

    private int getMaxScrollRow() {
        return Math.max(0, this.getTotalRows() - GRID_ROWS_VISIBLE);
    }

    private int getVisibleStartIndex() {
        return this.scrollRow * GRID_COLUMNS;
    }

    private DisplayEntry getVisibleEntry(int localIndex) {
        int absolute = this.getVisibleStartIndex() + localIndex;
        if (absolute < 0 || absolute >= this.displayEntries.size()) {
            return null;
        }
        return this.displayEntries.get(absolute);
    }

    private void scrollToMouse(int mouseY) {
        int maxScroll = this.getMaxScrollRow();
        if (maxScroll <= 0) {
            this.scrollRow = 0;
            return;
        }

        int trackTop = this.topPos + SCROLL_TRACK_Y + 14;
        int trackBottom = this.topPos + SCROLL_TRACK_Y + SCROLL_TRACK_HEIGHT - SCROLL_KNOB_HEIGHT;
        int clampedMouse = Math.max(trackTop, Math.min(mouseY - (SCROLL_KNOB_HEIGHT / 2), trackBottom));

        int knobTravel = trackBottom - trackTop;
        if (knobTravel <= 0) {
            return;
        }

        this.scrollRow = ((clampedMouse - trackTop) * maxScroll) / knobTravel;
    }

    private void drawPortfolioGrid(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (int row = 0; row < GRID_ROWS_VISIBLE; row++) {
            for (int column = 0; column < GRID_COLUMNS; column++) {
                int localIndex = column + row * GRID_COLUMNS;
                int x = this.leftPos + GRID_X + column * CELL_SIZE;
                int y = this.topPos + GRID_Y + row * CELL_SIZE;

                this.drawSlot(guiGraphics, x, y);

                DisplayEntry entry = this.getVisibleEntry(localIndex);
                if (entry != null && !entry.stack.isEmpty()) {
                    guiGraphics.renderItem(entry.stack, x + 1, y + 1);

                    String countText = entry.count > 1 ? Integer.toString(entry.count) : null;
                    guiGraphics.renderItemDecorations(this.font, entry.stack, x + 1, y + 1, countText);

                    if (entry.absoluteIndex < 0) {
                        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0x60000000);
                    }
                }

                if (mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE) {
                    guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0x80FFFFFF);
                }
            }
        }
    }

    private void drawScrollbar(GuiGraphics guiGraphics) {
        int trackX = this.leftPos + SCROLL_TRACK_X;
        int trackY = this.topPos + SCROLL_TRACK_Y;

        guiGraphics.fill(trackX + 5, trackY + 14, trackX + 9, trackY + SCROLL_TRACK_HEIGHT, 0xFF241A12);

        int maxScroll = this.getMaxScrollRow();
        int knobTravel = SCROLL_TRACK_HEIGHT - 28 - SCROLL_KNOB_HEIGHT;
        int knobOffset = maxScroll <= 0 ? 0 : (this.scrollRow * knobTravel) / maxScroll;
        int knobY = trackY + 14 + knobOffset;

        guiGraphics.fill(trackX + 3, knobY, trackX + 11, knobY + SCROLL_KNOB_HEIGHT, 0xFF8C7A5B);
        guiGraphics.fill(trackX + 4, knobY + 1, trackX + 10, knobY + SCROLL_KNOB_HEIGHT - 1, 0xFFB49B75);
    }

    private void drawPlayerInventoryFrames(GuiGraphics guiGraphics) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.drawSlot(guiGraphics, this.leftPos + 7 + column * 18, this.topPos + 83 + row * 18);
            }
        }

        for (int column = 0; column < 9; column++) {
            this.drawSlot(guiGraphics, this.leftPos + 7 + column * 18, this.topPos + 141);
        }
    }

    private void drawSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + 18, y + 18, 0xFF8C7A5B);
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF241A12);
        guiGraphics.fill(x + 2, y + 2, x + 16, y + 16, 0xFFB49B75);
    }

    private boolean isOverScrollbar(int mouseX, int mouseY) {
        int x = this.leftPos + SCROLL_TRACK_X + 3;
        int y = this.topPos + SCROLL_TRACK_Y + 14;
        return mouseX >= x && mouseX < x + 8 && mouseY >= y && mouseY < y + (SCROLL_TRACK_HEIGHT - 14);
    }

    private int getHoveredEntryIndex(int mouseX, int mouseY) {
        for (int row = 0; row < GRID_ROWS_VISIBLE; row++) {
            for (int column = 0; column < GRID_COLUMNS; column++) {
                int localIndex = column + row * GRID_COLUMNS;
                int x = this.leftPos + GRID_X + column * CELL_SIZE;
                int y = this.topPos + GRID_Y + row * CELL_SIZE;

                if (mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE) {
                    return localIndex;
                }
            }
        }

        return -1;
    }

    private static String normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT).trim();
    }

    private static final class DisplayEntry {
        private final ItemStack stack;
        private final int absoluteIndex;
        private int count;
        private final String searchName;

        private DisplayEntry(ItemStack stack, int absoluteIndex, int count, String searchName) {
            this.stack = stack;
            this.absoluteIndex = absoluteIndex;
            this.count = count;
            this.searchName = searchName;
        }
    }
}