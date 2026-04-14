package myst.synthetic.menu;

import myst.synthetic.MystcraftMenus;
import myst.synthetic.block.entity.BlockEntityDesk;
import myst.synthetic.item.DeskItemBehaviors;
import myst.synthetic.page.symbol.PageSymbol;
import myst.synthetic.page.symbol.PageSymbolRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WritingDeskMenu extends AbstractContainerMenu {

    public static final int DATA_HAS_INK = 0;
    public static final int DATA_ACTIVE_TAB = 1;
    public static final int DATA_FIRST_TAB = 2;
    public static final int DATA_COUNT = 3;

    public static final int VISIBLE_TAB_COUNT = 4;

    public static final int BUTTON_SELECT_TAB_START = 0;
    public static final int BUTTON_SET_FIRST_TAB_START = 100;
    public static final int BUTTON_ADD_CARRIED_TO_TAB_START = 200;
    public static final int BUTTON_REMOVE_ACTIVE_PAGE_START = 1000;
    public static final int BUTTON_REMOVE_ACTIVE_TO_INVENTORY_START = 2000;
    public static final int BUTTON_PLACE_CARRIED_AT_START = 3000;
    public static final int BUTTON_WRITE_SYMBOL_START = 10000;
    public static final int BUTTON_USE_LINK = 20000;

    private static final int DESK_TAB_MENU_START = 0;
    private static final int DESK_TAB_MENU_END = BlockEntityDesk.TAB_SLOT_COUNT;
    private static final int TARGET_MENU_SLOT = DESK_TAB_MENU_END;
    private static final int PAPER_MENU_SLOT = TARGET_MENU_SLOT + 1;
    private static final int INK_MENU_SLOT = TARGET_MENU_SLOT + 2;
    private static final int OUTPUT_MENU_SLOT = TARGET_MENU_SLOT + 3;

    private static final int SLOT_TARGET = BlockEntityDesk.SLOT_TARGET;
    private static final int SLOT_PAPER = BlockEntityDesk.SLOT_PAPER;
    private static final int SLOT_INK_INPUT = BlockEntityDesk.SLOT_INK_INPUT;
    private static final int SLOT_OUTPUT = BlockEntityDesk.SLOT_OUTPUT;
    private static final int SLOT_TAB_START = BlockEntityDesk.SLOT_TAB_START;
    private static final int SLOT_TAB_END = BlockEntityDesk.TOTAL_SLOTS;

    private static final int PLAYER_INV_START = OUTPUT_MENU_SLOT + 1;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container deskInventory;
    @Nullable
    private final BlockEntityDesk deskBlockEntity;
    private final ContainerData data;

    public WritingDeskMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(BlockEntityDesk.TOTAL_SLOTS), null, new SimpleContainerData(DATA_COUNT));
    }

    public WritingDeskMenu(int containerId, Inventory playerInventory, BlockEntityDesk blockEntity, ContainerData data) {
        this(containerId, playerInventory, blockEntity, blockEntity, data);
    }

    private WritingDeskMenu(int containerId, Inventory playerInventory, Container inventory, @Nullable BlockEntityDesk blockEntity, ContainerData data) {
        super(MystcraftMenus.WRITING_DESK, containerId);

        checkContainerSize(inventory, BlockEntityDesk.TOTAL_SLOTS);
        checkContainerDataCount(data, DATA_COUNT);

        this.deskInventory = inventory;
        this.deskBlockEntity = blockEntity;
        this.data = data;

        for (int i = SLOT_TAB_START; i < SLOT_TAB_END; i++) {
            this.addSlot(new HiddenDeskSlot(this.deskInventory, i));
        }

        this.addSlot(new TargetSlot(this.deskInventory, SLOT_TARGET, 188, 80));
        this.addSlot(new PaperSlot(this.deskInventory, SLOT_PAPER, 188, 28));
        this.addSlot(new InkInputSlot(this.deskInventory, SLOT_INK_INPUT, 332, 28));
        this.addSlot(new OutputSlot(this.deskInventory, SLOT_OUTPUT, 332, 80));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 188 + column * 18, 104 + row * 18));
            }
        }

        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            this.addSlot(new Slot(playerInventory, hotbarSlot, 188 + hotbarSlot * 18, 162));
        }

        this.addDataSlots(this.data);
    }

    public boolean hasInk() {
        return this.data.get(DATA_HAS_INK) != 0;
    }

    public int getActiveTab() {
        return this.data.get(DATA_ACTIVE_TAB);
    }

    public int getFirstVisibleTab() {
        return this.data.get(DATA_FIRST_TAB);
    }

    public ItemStack getTargetStack() {
        return this.deskInventory.getItem(SLOT_TARGET);
    }

    public ItemStack getTabStack(int absoluteTab) {
        if (absoluteTab < 0 || absoluteTab >= BlockEntityDesk.TAB_SLOT_COUNT) {
            return ItemStack.EMPTY;
        }
        return this.deskInventory.getItem(SLOT_TAB_START + absoluteTab);
    }

    public ItemStack getVisibleTabStack(int visibleIndex) {
        return this.getTabStack(this.getFirstVisibleTab() + visibleIndex);
    }

    public List<ItemStack> getActiveTabPages(Player player) {
        return DeskItemBehaviors.getPages(player, this.getTabStack(this.getActiveTab()));
    }

    public boolean canUseLink() {
        return this.getTargetStack().is(myst.synthetic.MystcraftItems.LINKBOOK);
    }


    public @Nullable BlockEntityDesk getDeskBlockEntity() {
        return this.deskBlockEntity;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= BUTTON_SELECT_TAB_START && id < BUTTON_SET_FIRST_TAB_START) {
            int tab = id - BUTTON_SELECT_TAB_START;
            this.setActiveTab(tab);
            return true;
        }

        if (id >= BUTTON_SET_FIRST_TAB_START && id < BUTTON_ADD_CARRIED_TO_TAB_START) {
            int first = id - BUTTON_SET_FIRST_TAB_START;
            this.setFirstVisibleTab(first);
            return true;
        }

        if (id >= BUTTON_ADD_CARRIED_TO_TAB_START && id < BUTTON_REMOVE_ACTIVE_PAGE_START) {
            int tab = id - BUTTON_ADD_CARRIED_TO_TAB_START;
            ItemStack carried = this.getCarried();
            if (carried.isEmpty() || !carried.is(myst.synthetic.MystcraftItems.PAGE) || this.deskBlockEntity == null) {
                return false;
            }

            this.deskBlockEntity.setActiveTab(tab);
            ItemStack single = carried.copyWithCount(1);
            ItemStack returned = this.deskBlockEntity.addPageToActiveTab(player, single);
            if (!returned.isEmpty()) {
                return false;
            }

            carried.shrink(1);
            this.setCarried(carried.isEmpty() ? ItemStack.EMPTY : carried);
            this.broadcastChanges();
            return true;
        }

        if (id >= BUTTON_REMOVE_ACTIVE_PAGE_START && id < BUTTON_REMOVE_ACTIVE_TO_INVENTORY_START) {
            int index = id - BUTTON_REMOVE_ACTIVE_PAGE_START;
            if (!this.getCarried().isEmpty() || this.deskBlockEntity == null) {
                return false;
            }

            ItemStack removed = this.deskBlockEntity.removePageFromActiveTab(player, index);
            if (removed.isEmpty()) {
                return false;
            }

            this.setCarried(removed);
            this.broadcastChanges();
            return true;
        }

        if (id >= BUTTON_REMOVE_ACTIVE_TO_INVENTORY_START && id < BUTTON_PLACE_CARRIED_AT_START) {
            int index = id - BUTTON_REMOVE_ACTIVE_TO_INVENTORY_START;
            if (this.deskBlockEntity == null) {
                return false;
            }

            ItemStack removed = this.deskBlockEntity.removePageFromActiveTab(player, index);
            if (removed.isEmpty()) {
                return false;
            }

            if (!this.moveSingleToPlayerInventory(removed)) {
                this.deskBlockEntity.insertPageIntoActiveTab(player, removed, index);
                return false;
            }

            this.broadcastChanges();
            return true;
        }

        if (id >= BUTTON_PLACE_CARRIED_AT_START && id < BUTTON_WRITE_SYMBOL_START) {
            int index = id - BUTTON_PLACE_CARRIED_AT_START;
            ItemStack carried = this.getCarried();
            if (carried.isEmpty() || !carried.is(myst.synthetic.MystcraftItems.PAGE) || this.deskBlockEntity == null) {
                return false;
            }

            ItemStack single = carried.copyWithCount(1);
            ItemStack returned = this.deskBlockEntity.insertPageIntoActiveTab(player, single, index);
            if (!returned.isEmpty() && ItemStack.isSameItemSameComponents(returned, single)) {
                return false;
            }

            carried.shrink(1);
            if (!returned.isEmpty()) {
                if (carried.isEmpty()) {
                    carried = returned;
                } else if (ItemStack.isSameItemSameComponents(carried, returned)
                        && carried.getCount() + returned.getCount() <= carried.getMaxStackSize()) {
                    carried.grow(returned.getCount());
                } else {
                    player.getInventory().placeItemBackInInventory(returned);
                }
            }

            this.setCarried(carried.isEmpty() ? ItemStack.EMPTY : carried);
            this.broadcastChanges();
            return true;
        }

        if (id >= BUTTON_WRITE_SYMBOL_START && id < BUTTON_USE_LINK) {
            int symbolIndex = id - BUTTON_WRITE_SYMBOL_START;
            List<PageSymbol> values = List.copyOf(PageSymbolRegistry.values());
            if (symbolIndex < 0 || symbolIndex >= values.size() || this.deskBlockEntity == null) {
                return false;
            }

            boolean changed = this.deskBlockEntity.writeSymbol(player, values.get(symbolIndex).id());
            if (changed) {
                this.broadcastChanges();
            }
            return changed;
        }

        if (id == BUTTON_USE_LINK) {
            if (this.deskBlockEntity == null) {
                return false;
            }
            boolean used = this.deskBlockEntity.activateTargetLink(player);
            if (used) {
                this.broadcastChanges();
            }
            return used;
        }

        return false;
    }

    public void setActiveTab(int tab) {
        this.data.set(DATA_ACTIVE_TAB, tab);
        if (this.deskBlockEntity != null) {
            this.deskBlockEntity.setActiveTab(tab);
        }
    }

    public void setFirstVisibleTab(int firstTab) {
        this.data.set(DATA_FIRST_TAB, firstTab);
        if (this.deskBlockEntity != null) {
            this.deskBlockEntity.setFirstVisibleTab(firstTab);
        }
    }

    private boolean moveSingleToPlayerInventory(ItemStack stack) {
        ItemStack moving = stack.copy();
        if (!this.moveItemStackTo(moving, PLAYER_INV_START, HOTBAR_END, true)) {
            return false;
        }
        return moving.isEmpty();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.deskInventory.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index >= DESK_TAB_MENU_START && index <= OUTPUT_MENU_SLOT) {
            if (!this.moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (DeskItemBehaviors.canBeDeskTarget(stack)) {
            if (!this.moveItemStackTo(stack, TARGET_MENU_SLOT, TARGET_MENU_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(Items.PAPER)) {
            if (!this.moveItemStackTo(stack, PAPER_MENU_SLOT, PAPER_MENU_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (myst.synthetic.ink.InkMixerInkSource.isValidInkSource(stack)) {
            if (!this.moveItemStackTo(stack, INK_MENU_SLOT, INK_MENU_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (DeskItemBehaviors.canBeDeskTabStorage(stack)) {
            if (!this.moveItemStackTo(stack, DESK_TAB_MENU_START, DESK_TAB_MENU_END, false)) {
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

    private static final class HiddenDeskSlot extends Slot {
        private HiddenDeskSlot(Container container, int slot) {
            super(container, slot, -10000, -10000);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return DeskItemBehaviors.canBeDeskTabStorage(stack);
        }
    }

    private static final class TargetSlot extends Slot {
        private TargetSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return DeskItemBehaviors.canBeDeskTarget(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    private static final class PaperSlot extends Slot {
        private PaperSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(Items.PAPER);
        }
    }

    private static final class InkInputSlot extends Slot {
        private InkInputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return myst.synthetic.ink.InkMixerInkSource.isValidInkSource(stack);
        }
    }

    private static final class OutputSlot extends Slot {
        private OutputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
