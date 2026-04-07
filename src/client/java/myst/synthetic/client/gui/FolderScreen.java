package myst.synthetic.client.gui;

import myst.synthetic.menu.FolderMenu;
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
import java.util.List;

public class FolderScreen extends AbstractContainerScreen<FolderMenu> {

    private static final Identifier NOTEBOOK_TEXTURE =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/notebook.png");

    private static final Identifier DESK_TEXTURE =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/writingdesk.png");

    private static final int SURFACE_WIDTH = 176;
    private static final int SURFACE_HEIGHT = 132;
    private static final int BUTTON_SIZE = 18;
    private static final int INVENTORY_HEIGHT = 80;
    private static final int TOTAL_HEIGHT = SURFACE_HEIGHT + BUTTON_SIZE + INVENTORY_HEIGHT + 1;

    private Button sortButton;
    private Button allButton;
    private EditBox searchBox;

    private boolean draggingScrollbar = false;
    private int scroll = 0;

    private List<PageSurfaceRenderer.SurfaceEntry> entries = List.of();

    public FolderScreen(FolderMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = SURFACE_WIDTH;
        this.imageHeight = TOTAL_HEIGHT;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 158;
        this.titleLabelX = 8;
        this.titleLabelY = 158;
    }

    @Override
    protected void init() {
        super.init();

        this.sortButton = this.addRenderableWidget(
                Button.builder(Component.literal("AZ"), button -> this.rebuildEntries())
                        .pos(this.leftPos, this.topPos)
                        .size(18, 18)
                        .build()
        );

        this.allButton = this.addRenderableWidget(
                Button.builder(Component.literal("ALL"), button -> {})
                        .pos(this.leftPos + 18, this.topPos)
                        .size(18, 18)
                        .build()
        );
        this.allButton.active = false;

        this.searchBox = new EditBox(
                this.font,
                this.leftPos + 40,
                this.topPos,
                136,
                18,
                Component.translatable("screen.mystcraft-sc.page_browser.search")
        );
        this.searchBox.setBordered(false);
        this.searchBox.setEditable(false);
        this.addRenderableWidget(this.searchBox);

        this.rebuildEntries();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.rebuildEntries();
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

        PageSurfaceRenderer.drawEntries(
                guiGraphics,
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

        int hoveredSlot = PageSurfaceRenderer.getHoveredSlot(this.entries, this.leftPos, this.topPos, mouseX, mouseY, this.scroll);
        if (hoveredSlot >= 0 && this.minecraft != null && this.minecraft.gameMode != null) {
            ItemStack stack = this.menu.getOrderedItem(hoveredSlot);

            if (!stack.isEmpty()) {
                this.minecraft.gameMode.handleInventoryButtonClick(
                        this.menu.containerId,
                        FolderMenu.BUTTON_TAKE_ORDERED_START + hoveredSlot
                );
                return true;
            }

            if (this.menu.canPreviewPlace()) {
                this.minecraft.gameMode.handleInventoryButtonClick(
                        this.menu.containerId,
                        FolderMenu.BUTTON_PLACE_ORDERED_START + hoveredSlot
                );
                return true;
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
        this.renderPageTooltip(guiGraphics, mouseX, mouseY);
    }

    private void rebuildEntries() {
        List<PageSurfaceRenderer.SurfaceEntry> built = new ArrayList<>();

        int lastIndex = this.menu.findLastMeaningfulIndex();
        if (this.menu.canPreviewPlace()) {
            lastIndex = Math.max(lastIndex, this.menu.findNextPreviewIndex());
        }

        if (lastIndex < 0) {
            this.entries = List.of();
            this.scroll = 0;
            return;
        }

        int x = 0;
        int y = 0;

        for (int i = 0; i <= lastIndex; i++) {
            ItemStack stack = this.menu.getOrderedItem(i);
            boolean placeholder = stack.isEmpty();

            built.add(new PageSurfaceRenderer.SurfaceEntry(
                    i,
                    stack.copy(),
                    placeholder,
                    x,
                    y
            ));

            x += PageSurfaceRenderer.PAGE_X_STEP;
            if (x + PageSurfaceRenderer.PAGE_WIDTH > PageSurfaceRenderer.SURFACE_WIDTH) {
                x = 0;
                y += PageSurfaceRenderer.PAGE_Y_STEP;
            }
        }

        this.entries = built;
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

    private void scrollToMouse(int mouseY) {
        int maxScroll = this.getMaxScroll();
        if (maxScroll <= 0) {
            this.scroll = 0;
            return;
        }

        int trackTop = this.topPos + PageSurfaceRenderer.SCROLLBAR_Y;
        int trackBottom = trackTop + PageSurfaceRenderer.SCROLLBAR_HEIGHT - 16;
        int clampedMouse = Math.max(trackTop, Math.min(mouseY - 8, trackBottom));

        int knobTravel = trackBottom - trackTop;
        if (knobTravel <= 0) {
            return;
        }

        this.scroll = ((clampedMouse - trackTop) * maxScroll) / knobTravel;
    }

    private void renderPageTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int hoveredSlot = PageSurfaceRenderer.getHoveredSlot(this.entries, this.leftPos, this.topPos, mouseX, mouseY, this.scroll);
        if (hoveredSlot < 0 || this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        ItemStack stack = this.menu.getOrderedItem(hoveredSlot);
        if (stack.isEmpty()) {
            return;
        }

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