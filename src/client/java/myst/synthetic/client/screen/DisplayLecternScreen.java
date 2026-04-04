package myst.synthetic.client.screen;

import java.util.Objects;
import myst.synthetic.client.gui.SingleSlotScreen;
import myst.synthetic.menu.DisplayContainerMenu;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

public class DisplayLecternScreen extends BookViewScreen implements MenuAccess<DisplayContainerMenu> {

    private static final Component TAKE_BOOK_LABEL = Component.translatable("lectern.take_book");

    private final DisplayContainerMenu menu;
    private final Inventory playerInventory;
    private final Component screenTitle;

    private final ContainerListener listener = new ContainerListener() {
        @Override
        public void slotChanged(AbstractContainerMenu abstractContainerMenu, int slot, ItemStack itemStack) {
            DisplayLecternScreen.this.bookChanged();
        }

        @Override
        public void dataChanged(AbstractContainerMenu abstractContainerMenu, int index, int value) {
            if (index == 0) {
                DisplayLecternScreen.this.pageChanged();
            }
        }
    };

    public DisplayLecternScreen(DisplayContainerMenu menu, Inventory inventory, Component title) {
        super(EMPTY_ACCESS);
        this.menu = menu;
        this.playerInventory = inventory;
        this.screenTitle = title;
    }

    @Override
    public DisplayContainerMenu getMenu() {
        return this.menu;
    }

    @Override
    protected void init() {
        super.init();
        this.menu.addSlotListener(this.listener);
        this.bookChanged();
        this.pageChanged();
    }

    @Override
    public void onClose() {
        this.minecraft.player.closeContainer();
        super.onClose();
    }

    @Override
    public void removed() {
        super.removed();
        this.menu.removeSlotListener(this.listener);
    }

    @Override
    protected void createMenuControls() {
        int y = this.menuControlsTop();
        int center = this.width / 2;

        this.addRenderableWidget(
                Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                        .pos(center - 98 - 2, y)
                        .width(98)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(TAKE_BOOK_LABEL, button -> this.sendButtonClick(DisplayContainerMenu.BUTTON_TAKE_BOOK))
                        .pos(center + 2, y)
                        .width(98)
                        .build()
        );
    }

    @Override
    protected void pageBack() {
        this.sendButtonClick(DisplayContainerMenu.BUTTON_PREV_PAGE);
    }

    @Override
    protected void pageForward() {
        this.sendButtonClick(DisplayContainerMenu.BUTTON_NEXT_PAGE);
    }

    @Override
    protected boolean forcePage(int page) {
        if (page != this.menu.getPage()) {
            this.sendButtonClick(DisplayContainerMenu.BUTTON_PAGE_JUMP_RANGE_START + page);
            return true;
        }

        return false;
    }

    private void sendButtonClick(int id) {
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
    }

    void bookChanged() {
        if (!this.menu.isLecternBookMode()) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.setScreen(new SingleSlotScreen(this.menu, this.playerInventory, this.screenTitle));
            }
            return;
        }

        ItemStack itemStack = this.menu.getBook();
        this.setBookAccess(
                Objects.requireNonNullElse(BookAccess.fromItem(itemStack), EMPTY_ACCESS)
        );
    }

    void pageChanged() {
        this.setPage(this.menu.getPage());
    }

    @Override
    protected void closeContainerOnServer() {
        this.minecraft.player.closeContainer();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}