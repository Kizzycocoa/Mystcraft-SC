package myst.synthetic.client.gui;

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

public abstract class PageBrowserScreen<T extends net.minecraft.world.inventory.AbstractContainerMenu>
        extends AbstractContainerScreen<T> {

    protected static final Identifier NOTEBOOK_TEXTURE =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/notebook.png");

    protected static final Identifier DESK_TEXTURE =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/writingdesk.png");

    protected static final int SURFACE_WIDTH = 176;
    protected static final int SURFACE_HEIGHT = 132;
    protected static final int BUTTON_SIZE = 18;
    protected static final int INVENTORY_HEIGHT = 80;
    protected static final int TOTAL_HEIGHT = SURFACE_HEIGHT + BUTTON_SIZE + INVENTORY_HEIGHT + 1;

    protected static final int SEARCH_X = (BUTTON_SIZE + 2) * 2;
    protected static final int SEARCH_Y = 0;
    protected static final int SEARCH_WIDTH = SURFACE_WIDTH - SEARCH_X;
    protected static final int SEARCH_HEIGHT = BUTTON_SIZE;

    protected static final int PAGE_SURFACE_X = 0;
    protected static final int PAGE_SURFACE_Y = BUTTON_SIZE + 1;
    protected static final int PAGE_SURFACE_WIDTH = SURFACE_WIDTH;
    protected static final int PAGE_SURFACE_HEIGHT = SURFACE_HEIGHT - BUTTON_SIZE;

    protected static final int SCROLLBAR_RESERVED_WIDTH = 20;
    protected static final int PAGE_DRAW_WIDTH = PAGE_SURFACE_WIDTH - SCROLLBAR_RESERVED_WIDTH;

    protected static final float PAGE_WIDTH = 30.0f;
    protected static final float PAGE_HEIGHT = 40.0f;
    protected static final float PAGE_X_STEP = PAGE_WIDTH + 1.0f;
    protected static final float PAGE_Y_STEP = PAGE_HEIGHT + 1.0f;

    protected static final Comparator<DisplayEntry> ENTRY_COMPARATOR =
            Comparator.comparing((DisplayEntry entry) -> normalize(entry.searchName))
                    .thenComparingInt(entry -> entry.absoluteIndex);

    protected Button sortButton;
    protected Button allButton;
    protected EditBox searchBox;

    protected boolean sortAlphabetically = true;
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
                Button.builder(this.sortButtonText(), button -> {
                            this.sortAlphabetically = !this.sortAlphabetically;
                            button.setMessage(this.sortButtonText());
                            this.rebuildDisplayEntries();
                        })
                        .pos(this.leftPos, this.topPos)
                        .size(BUTTON_SIZE, BUTTON_SIZE)
                        .build()
        );

        this.allButton = this.addRenderableWidget(
                Button.builder(this.allButtonText(), button -> {
                            this.showAllSymbols = !this.showAllSymbols;
                            button.setMessage(this.allButtonText());
                            this.rebuildDisplayEntries();
                        })
                        .pos(this.leftPos + BUTTON_SIZE, this.topPos)
                        .size(BUTTON_SIZE, BUTTON_SIZE)
                        .build()
        );

        this.searchBox = new EditBox(
                this.font,
                this.leftPos + SEARCH_X,
                this.topPos + SEARCH_Y,
                SEARCH_WIDTH,
                SEARCH_HEIGHT,
                Component.translatable("screen.mystcraft-sc.page_browser.search")
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
                NOTEBOOK_TEXTURE,
                this.leftPos,
                this.topPos,
                0,
                0,
                SURFACE_WIDTH,
                SURFACE_HEIGHT,
                256,
                256
        );

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

        this.drawPageSurface(guiGraphics, mouseX, mouseY);
        this.drawScrollbar(guiGraphics);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
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

        if (this.searchBox != null && this.searchBox.mouseClicked(event, doubleClick)) {
            return true;
        }

        if (this.isOverScrollbar(mouseX, mouseY)) {
            this.draggingScrollbar = true;
            this.scrollToMouse(mouseY);
            return true;
        }

        DisplayEntry hovered = this.getHoveredEntry(mouseX, mouseY);
        if (hovered != null) {
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

        if (this.searchBox != null) {
            this.searchBox.render(guiGraphics, mouseX, mouseY, partialTick);
        }

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

    protected Component sortButtonText() {
        return this.sortAlphabetically ? Component.literal("[AZ]") : Component.literal("AZ");
    }

    protected Component allButtonText() {
        return this.showAllSymbols ? Component.literal("[ALL]") : Component.literal("ALL");
    }

    protected void drawPageSurface(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int surfaceLeft = this.leftPos + PAGE_SURFACE_X;
        int surfaceTop = this.topPos + PAGE_SURFACE_Y;

        guiGraphics.fill(
                surfaceLeft,
                surfaceTop,
                surfaceLeft + PAGE_DRAW_WIDTH,
                surfaceTop + PAGE_SURFACE_HEIGHT,
                0xAA000000
        );

        for (DisplayEntry entry : this.displayEntries) {
            int drawX = surfaceLeft + Math.round(entry.x);
            int drawY = surfaceTop + Math.round(entry.y) - this.scroll;

            if (drawY + PAGE_HEIGHT < surfaceTop || drawY > surfaceTop + PAGE_SURFACE_HEIGHT) {
                continue;
            }

            guiGraphics.renderItem(entry.stack, drawX + 7, drawY + 8);

            if (entry.count > 1) {
                guiGraphics.drawString(this.font, Integer.toString(entry.count), drawX + 1, drawY + 31, 0xFFFFFF, false);
            }

            if (entry.absoluteIndex < 0) {
                guiGraphics.fill(drawX, drawY, drawX + (int) PAGE_WIDTH, drawY + (int) PAGE_HEIGHT, 0x60000000);
            }

            if (mouseX >= drawX && mouseX < drawX + PAGE_WIDTH && mouseY >= drawY && mouseY < drawY + PAGE_HEIGHT) {
                guiGraphics.fill(drawX, drawY, drawX + (int) PAGE_WIDTH, drawY + (int) PAGE_HEIGHT, 0x40FFFFFF);
            }
        }
    }

    protected void drawScrollbar(GuiGraphics guiGraphics) {
        int trackX = this.leftPos + PAGE_DRAW_WIDTH;
        int trackY = this.topPos + PAGE_SURFACE_Y;
        int trackHeight = PAGE_SURFACE_HEIGHT;

        guiGraphics.fill(trackX + 8, trackY, trackX + 12, trackY + trackHeight, 0xFF241A12);

        int maxScroll = this.getMaxScroll();
        int knobTravel = trackHeight - 16;
        int knobOffset = maxScroll <= 0 ? 0 : (this.scroll * knobTravel) / maxScroll;
        int knobY = trackY + knobOffset;

        guiGraphics.fill(trackX + 6, knobY, trackX + 14, knobY + 16, 0xFF8C7A5B);
        guiGraphics.fill(trackX + 7, knobY + 1, trackX + 13, knobY + 15, 0xFFB49B75);
    }

    protected int getMaxScroll() {
        int maxBottom = 0;
        for (DisplayEntry entry : this.displayEntries) {
            int bottom = Math.round(entry.y + PAGE_HEIGHT + 6);
            if (bottom > maxBottom) {
                maxBottom = bottom;
            }
        }
        return Math.max(0, maxBottom - PAGE_SURFACE_HEIGHT);
    }

    protected void scrollToMouse(int mouseY) {
        int maxScroll = this.getMaxScroll();
        if (maxScroll <= 0) {
            this.scroll = 0;
            return;
        }

        int trackTop = this.topPos + PAGE_SURFACE_Y;
        int trackBottom = trackTop + PAGE_SURFACE_HEIGHT - 16;
        int clampedMouse = Math.max(trackTop, Math.min(mouseY - 8, trackBottom));

        int knobTravel = trackBottom - trackTop;
        if (knobTravel <= 0) {
            return;
        }

        this.scroll = ((clampedMouse - trackTop) * maxScroll) / knobTravel;
    }

    protected boolean isOverScrollbar(int mouseX, int mouseY) {
        int x = this.leftPos + PAGE_DRAW_WIDTH + 6;
        int y = this.topPos + PAGE_SURFACE_Y;
        return mouseX >= x && mouseX < x + 8 && mouseY >= y && mouseY < y + PAGE_SURFACE_HEIGHT;
    }

    protected DisplayEntry getHoveredEntry(int mouseX, int mouseY) {
        int surfaceLeft = this.leftPos + PAGE_SURFACE_X;
        int surfaceTop = this.topPos + PAGE_SURFACE_Y;

        for (DisplayEntry entry : this.displayEntries) {
            int drawX = surfaceLeft + Math.round(entry.x);
            int drawY = surfaceTop + Math.round(entry.y) - this.scroll;

            if (mouseX >= drawX && mouseX < drawX + PAGE_WIDTH && mouseY >= drawY && mouseY < drawY + PAGE_HEIGHT) {
                return entry;
            }
        }

        return null;
    }

    protected void renderPageTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
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

    protected void arrangeLinear(List<DisplayEntry> entries) {
        float x = 0.0f;
        float y = 0.0f;

        for (DisplayEntry entry : entries) {
            entry.x = x;
            entry.y = y;

            x += PAGE_X_STEP;
            if (x + PAGE_X_STEP > PAGE_DRAW_WIDTH) {
                x = 0.0f;
                y += PAGE_Y_STEP;
            }
        }
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