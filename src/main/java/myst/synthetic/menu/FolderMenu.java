package myst.synthetic.menu;

import myst.synthetic.MystcraftItems;
import myst.synthetic.MystcraftMenus;
import myst.synthetic.component.FolderDataComponent;
import myst.synthetic.item.ItemFolder;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FolderMenu extends AbstractContainerMenu {

    private static final int FOLDER_SLOT_COUNT = FolderDataComponent.MAX_SLOTS;
    private static final int PLAYER_INV_START = FOLDER_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Inventory playerInventory;
    private final SimpleContainer folderInventory;
    private final int hostSlot;

    public FolderMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, playerInventory.getSelectedSlot());
    }

    public FolderMenu(int containerId, Inventory playerInventory, int hostSlot) {
        super(MystcraftMenus.FOLDER, containerId);

        this.playerInventory = playerInventory;
        this.hostSlot = hostSlot;
        this.folderInventory = new SimpleContainer(FOLDER_SLOT_COUNT);

        NonNullList<ItemStack> contents = ItemFolder.createInventory(this.getHostStack());
        for (int i = 0; i < FOLDER_SLOT_COUNT; i++) {
            this.folderInventory.setItem(i, contents.get(i));
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int slot = column + row * 9;
                this.addSlot(new FolderSlot(this.folderInventory, slot, 8 + column * 18, 18 + row * 18));
            }
        }

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
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);

        if (container == this.folderInventory) {
            this.saveBackToHost();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.hostSlot < 0 || this.hostSlot >= 9) {
            return false;
        }

        return this.getHostStack().is(MystcraftItems.FOLDER);
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (this.isBlockedClick(slotId, button, clickType)) {
            return;
        }

        super.clicked(slotId, button, clickType, player);
        this.saveBackToHost();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index < FOLDER_SLOT_COUNT) {
            if (!this.moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (ItemFolder.canStore(stack)) {
                if (!this.moveItemStackTo(stack, 0, FOLDER_SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= PLAYER_INV_START && index < PLAYER_INV_END) {
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
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        this.saveBackToHost();
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public void removed(Player player) {
        ItemFolder.syncFolderStackState(this.getHostStack());
        this.saveBackToHost();
        super.removed(player);
    }

    public int getStoredCount() {
        int count = 0;

        for (int i = 0; i < FOLDER_SLOT_COUNT; i++) {
            if (!this.folderInventory.getItem(i).isEmpty()) {
                count++;
            }
        }

        return count;
    }

    private ItemStack getHostStack() {
        return this.playerInventory.getItem(this.hostSlot);
    }

    private void saveBackToHost() {
        ItemStack host = this.getHostStack();
        if (!host.is(MystcraftItems.FOLDER)) {
            return;
        }

        NonNullList<ItemStack> slots = NonNullList.withSize(FOLDER_SLOT_COUNT, ItemStack.EMPTY);
        for (int i = 0; i < FOLDER_SLOT_COUNT; i++) {
            slots.set(i, this.folderInventory.getItem(i).copy());
        }

        ItemFolder.saveInventory(host, slots);
        ItemFolder.syncFolderStackState(host);
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

    private static final class FolderSlot extends Slot {
        private FolderSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return ItemFolder.canStore(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return 1;
        }
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