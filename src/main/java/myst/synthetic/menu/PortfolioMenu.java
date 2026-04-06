package myst.synthetic.menu;

import myst.synthetic.MystcraftItems;
import myst.synthetic.MystcraftMenus;
import myst.synthetic.component.FolderDataComponent;
import myst.synthetic.component.PortfolioDataComponent;
import myst.synthetic.item.ItemFolder;
import myst.synthetic.item.ItemPortfolio;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PortfolioMenu extends AbstractContainerMenu {

    public static final int BUTTON_PREV_PAGE = 1;
    public static final int BUTTON_NEXT_PAGE = 2;
    public static final int BUTTON_TAKE_VISIBLE_START = 100;

    public static final int GRID_COLUMNS = 7;
    public static final int GRID_ROWS = 4;
    public static final int PAGES_PER_SCREEN = GRID_COLUMNS * GRID_ROWS;

    private static final int DATA_SCREEN_PAGE = 0;
    private static final int DATA_COUNT = 1;

    private static final int PLAYER_INV_START = 0;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Inventory playerInventory;
    private final int hostSlot;
    private final ContainerData data;

    public PortfolioMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, playerInventory.getSelectedSlot(), new SimpleContainerData(DATA_COUNT));
    }

    public PortfolioMenu(int containerId, Inventory playerInventory, int hostSlot) {
        this(containerId, playerInventory, hostSlot, new SimpleContainerData(DATA_COUNT));
    }

    private PortfolioMenu(int containerId, Inventory playerInventory, int hostSlot, ContainerData data) {
        super(MystcraftMenus.PORTFOLIO, containerId);

        this.playerInventory = playerInventory;
        this.hostSlot = hostSlot;
        this.data = data;

        checkContainerDataCount(data, DATA_COUNT);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int slot = column + row * 9 + 9;
                this.addSlot(new LockedPlayerSlot(
                        playerInventory,
                        slot,
                        8 + column * 18,
                        84 + row * 18,
                        false
                ));
            }
        }

        for (int column = 0; column < 9; column++) {
            boolean locked = column == this.hostSlot;
            this.addSlot(new LockedPlayerSlot(
                    playerInventory,
                    column,
                    8 + column * 18,
                    142,
                    locked
            ));
        }

        this.addDataSlots(this.data);
        this.clampScreenPage();
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.hostSlot < 0 || this.hostSlot >= 9) {
            return false;
        }

        return this.getHostStack().is(MystcraftItems.PORTFOLIO);
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (this.isBlockedClick(slotId, button, clickType)) {
            return;
        }

        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (stack.is(MystcraftItems.PAGE)) {
            this.absorbPageStack(stack);
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            this.broadcastFullState(player);
            return original;
        }

        if (stack.is(MystcraftItems.FOLDER)) {
            if (this.absorbFolderStack(stack)) {
                slot.setChanged();
                this.broadcastFullState(player);
                return original;
            }
            return ItemStack.EMPTY;
        }

        if (index >= PLAYER_INV_START && index < PLAYER_INV_END) {
            if (!this.moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= HOTBAR_START && index < HOTBAR_END) {
            if (!this.moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_END, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_PREV_PAGE) {
            this.data.set(DATA_SCREEN_PAGE, this.getScreenPage() - 1);
            this.clampScreenPage();
            return true;
        }

        if (id == BUTTON_NEXT_PAGE) {
            this.data.set(DATA_SCREEN_PAGE, this.getScreenPage() + 1);
            this.clampScreenPage();
            return true;
        }

        if (id >= BUTTON_TAKE_VISIBLE_START && id < BUTTON_TAKE_VISIBLE_START + PAGES_PER_SCREEN) {
            int localIndex = id - BUTTON_TAKE_VISIBLE_START;
            int absoluteIndex = this.getVisibleStartIndex() + localIndex;

            ItemStack removed = this.removePageAt(absoluteIndex);
            if (removed.isEmpty()) {
                return false;
            }

            if (!player.getInventory().add(removed)) {
                player.drop(removed, false);
            }

            this.clampScreenPage();
            this.broadcastFullState(player);
            return true;
        }

        return false;
    }

    public ItemStack getPortfolioStack() {
        return this.getHostStack();
    }

    public PortfolioDataComponent getPortfolioData() {
        return ItemPortfolio.getData(this.getHostStack());
    }

    public int getStoredCount() {
        return this.getPortfolioData().size();
    }

    public int getScreenPage() {
        return this.data.get(DATA_SCREEN_PAGE);
    }

    public int getScreenCount() {
        int stored = this.getStoredCount();
        return Math.max(1, (stored + PAGES_PER_SCREEN - 1) / PAGES_PER_SCREEN);
    }

    public int getVisibleStartIndex() {
        return this.getScreenPage() * PAGES_PER_SCREEN;
    }

    public int getVisibleCount() {
        int remaining = this.getStoredCount() - this.getVisibleStartIndex();
        return Math.max(0, Math.min(PAGES_PER_SCREEN, remaining));
    }

    public ItemStack getVisiblePage(int localIndex) {
        if (localIndex < 0 || localIndex >= PAGES_PER_SCREEN) {
            return ItemStack.EMPTY;
        }

        int absoluteIndex = this.getVisibleStartIndex() + localIndex;
        return this.getPortfolioData().getPage(absoluteIndex);
    }

    private ItemStack getHostStack() {
        return this.playerInventory.getItem(this.hostSlot);
    }

    private void clampScreenPage() {
        int maxPage = this.getScreenCount() - 1;
        int clamped = Math.max(0, Math.min(this.getScreenPage(), maxPage));
        this.data.set(DATA_SCREEN_PAGE, clamped);
    }

    private void absorbPageStack(ItemStack stack) {
        if (!stack.is(MystcraftItems.PAGE)) {
            return;
        }

        int count = stack.getCount();
        if (count <= 0) {
            return;
        }

        PortfolioDataComponent data = this.getPortfolioData();
        ItemStack single = stack.copyWithCount(1);

        for (int i = 0; i < count; i++) {
            data = data.add(single);
        }

        ItemPortfolio.setData(this.getHostStack(), data);
        stack.setCount(0);
        this.clampScreenPage();
    }

    private boolean absorbFolderStack(ItemStack folder) {
        if (!folder.is(MystcraftItems.FOLDER)) {
            return false;
        }

        PortfolioDataComponent portfolioData = this.getPortfolioData();
        FolderDataComponent folderData = ItemFolder.getFolderData(folder);

        boolean changed = false;

        for (ItemStack stack : folderData.toSlotList()) {
            if (!stack.isEmpty() && stack.is(MystcraftItems.PAGE)) {
                portfolioData = portfolioData.add(stack);
                changed = true;
            }
        }

        if (!changed) {
            return false;
        }

        ItemPortfolio.setData(this.getHostStack(), portfolioData);
        ItemFolder.saveInventory(
                folder,
                NonNullList.withSize(FolderDataComponent.MAX_SLOTS, ItemStack.EMPTY)
        );

        this.clampScreenPage();
        return true;
    }

    private ItemStack removePageAt(int index) {
        PortfolioDataComponent data = this.getPortfolioData();
        ItemStack page = data.getPage(index);

        if (page.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemPortfolio.setData(this.getHostStack(), data.removeAt(index));
        return page;
    }

    private void broadcastFullState(Player player) {
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        this.broadcastChanges();
    }

    private boolean isBlockedClick(int slotId, int button, ClickType clickType) {
        if (clickType == ClickType.SWAP && button == this.hostSlot) {
            return true;
        }

        if (slotId < 0 || slotId >= this.slots.size()) {
            return false;
        }

        Slot slot = this.slots.get(slotId);
        if (slot.container != this.playerInventory) {
            return false;
        }

        return slot.getContainerSlot() == this.hostSlot;
    }

    private static final class LockedPlayerSlot extends Slot {
        private final boolean locked;

        private LockedPlayerSlot(Container container, int slot, int x, int y, boolean locked) {
            super(container, slot, x, y);
            this.locked = locked;
        }

        @Override
        public boolean mayPickup(Player player) {
            return !this.locked;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !this.locked;
        }
    }
}