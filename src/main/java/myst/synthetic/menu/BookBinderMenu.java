package myst.synthetic.menu;

import myst.synthetic.MystcraftMenus;
import myst.synthetic.MystcraftItems;
import myst.synthetic.block.entity.BlockEntityBookBinder;
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

public class BookBinderMenu extends AbstractContainerMenu {

    public static final int DATA_CAN_CRAFT = 0;
    public static final int DATA_TITLE_LENGTH = 1;
    public static final int DATA_TITLE_CHARS_START = 2;
    public static final int DATA_COUNT = DATA_TITLE_CHARS_START + BlockEntityBookBinder.MAX_TITLE_LENGTH;

    public static final int BUTTON_INSERT_AT_START = 1000;
    public static final int BUTTON_INSERT_SINGLE_AT_START = 2000;
    public static final int BUTTON_REMOVE_AT_START = 3000;

    private static final int SLOT_COVER = 0;
    private static final int SLOT_RESULT = 28;

    private final Container binderInventory;
    @Nullable
    private final BlockEntityBookBinder binderBlockEntity;
    private final ContainerData data;
    private final SimpleContainer resultContainer = new SimpleContainer(1);

    public BookBinderMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(BlockEntityBookBinder.TOTAL_SLOTS), null, new SimpleContainerData(DATA_COUNT));
    }

    public BookBinderMenu(int containerId, Inventory playerInventory, BlockEntityBookBinder blockEntity, ContainerData data) {
        this(containerId, playerInventory, blockEntity, blockEntity, data);
    }

    private BookBinderMenu(
            int containerId,
            Inventory playerInventory,
            Container inventory,
            @Nullable BlockEntityBookBinder blockEntity,
            ContainerData data
    ) {
        super(MystcraftMenus.BOOK_BINDER, containerId);

        checkContainerSize(inventory, BlockEntityBookBinder.TOTAL_SLOTS);
        checkContainerDataCount(data, DATA_COUNT);

        this.binderInventory = inventory;
        this.binderBlockEntity = blockEntity;
        this.data = data;

        this.addSlot(new CoverSlot(this.binderInventory, SLOT_COVER, 8, 27));

        for (int i = 0; i < BlockEntityBookBinder.PAGE_SLOT_COUNT; i++) {
            this.addSlot(new HiddenPageSlot(this.binderInventory, BlockEntityBookBinder.SLOT_PAGE_START + i));
        }

        this.addSlot(new ResultSlot(playerInventory.player, this.resultContainer, 0, 152, 27));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 99 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInventory, column, 8 + column * 18, 157));
        }

        this.addDataSlots(this.data);
        this.updateCraftResult();
    }

    public void updateCraftResult() {
        if (this.binderBlockEntity == null) {
            this.resultContainer.setItem(0, ItemStack.EMPTY);
            return;
        }

        this.resultContainer.setItem(0, this.binderBlockEntity.getCraftedItemPreview());
        this.broadcastChanges();
    }

    public String getSyncedTitle() {
        int length = Math.max(0, Math.min(this.data.get(DATA_TITLE_LENGTH), BlockEntityBookBinder.MAX_TITLE_LENGTH));
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int code = this.data.get(DATA_TITLE_CHARS_START + i);
            if (code > 0) {
                builder.append((char) code);
            }
        }

        return builder.toString();
    }

    public boolean canCraft() {
        return this.data.get(DATA_CAN_CRAFT) != 0;
    }

    public int getPageCount() {
        int count = 0;

        for (int i = 0; i < BlockEntityBookBinder.PAGE_SLOT_COUNT; i++) {
            ItemStack stack = this.binderInventory.getItem(BlockEntityBookBinder.SLOT_PAGE_START + i);
            if (!stack.isEmpty()) {
                count++;
            }
        }

        return count;
    }

    public ItemStack getPageAt(int index) {
        if (index < 0 || index >= BlockEntityBookBinder.PAGE_SLOT_COUNT) {
            return ItemStack.EMPTY;
        }
        return this.binderInventory.getItem(BlockEntityBookBinder.SLOT_PAGE_START + index);
    }

    @Nullable
    public BlockEntityBookBinder getBinderBlockEntity() {
        return this.binderBlockEntity;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (this.binderBlockEntity == null) {
            return false;
        }

        if (id >= BUTTON_REMOVE_AT_START && id < BUTTON_REMOVE_AT_START + BlockEntityBookBinder.PAGE_SLOT_COUNT) {
            int index = id - BUTTON_REMOVE_AT_START;
            if (!this.getCarried().isEmpty()) {
                return false;
            }

            ItemStack removed = this.binderBlockEntity.removePage(index);
            if (removed.isEmpty()) {
                return false;
            }

            this.setCarried(removed);
            this.updateCraftResult();
            return true;
        }

        if (id >= BUTTON_INSERT_SINGLE_AT_START && id < BUTTON_INSERT_SINGLE_AT_START + (BlockEntityBookBinder.PAGE_SLOT_COUNT + 1)) {
            int index = id - BUTTON_INSERT_SINGLE_AT_START;
            return this.tryInsertCarried(player, index, true);
        }

        if (id >= BUTTON_INSERT_AT_START && id < BUTTON_INSERT_AT_START + (BlockEntityBookBinder.PAGE_SLOT_COUNT + 1)) {
            int index = id - BUTTON_INSERT_AT_START;
            return this.tryInsertCarried(player, index, false);
        }

        return false;
    }

    private boolean tryInsertCarried(Player player, int index, boolean single) {
        if (this.binderBlockEntity == null) {
            return false;
        }

        ItemStack carried = this.getCarried();
        if (carried.isEmpty()) {
            return false;
        }

        if (carried.is(MystcraftItems.FOLDER)) {
            this.binderBlockEntity.insertFromFolder(carried, index);
            this.updateCraftResult();
            return true;
        }

        if (single) {
            ItemStack one = carried.copyWithCount(1);
            ItemStack fail = this.binderBlockEntity.insertPage(one, index);
            if (!fail.isEmpty()) {
                return false;
            }

            carried.shrink(1);
            if (carried.isEmpty()) {
                this.setCarried(ItemStack.EMPTY);
            } else {
                this.setCarried(carried);
            }

            this.updateCraftResult();
            return true;
        }

        ItemStack fail = this.binderBlockEntity.insertPage(carried.copy(), index);
        if (fail.getCount() == carried.getCount() && ItemStack.isSameItemSameComponents(fail, carried)) {
            return false;
        }

        this.setCarried(fail);
        this.updateCraftResult();
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);

        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();

        if (index == SLOT_RESULT) {
            if (!this.moveItemStackTo(source, 29, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(source, copy);
            return copy;
        }

        if (index == SLOT_COVER) {
            if (!this.moveItemStackTo(source, 29, this.slots.size(), false)) {
                return ItemStack.EMPTY;
            }
            slot.setChanged();
            this.updateCraftResult();
            return copy;
        }

        if (index >= 29) {
            if (this.binderBlockEntity != null) {
                if (this.binderBlockEntity.isValidCover(source)) {
                    if (!this.slots.get(SLOT_COVER).hasItem()) {
                        ItemStack one = source.copyWithCount(1);
                        this.slots.get(SLOT_COVER).set(one);
                        source.shrink(1);
                    }
                } else if (source.is(MystcraftItems.PAGE) || source.is(Items.PAPER) || source.is(MystcraftItems.FOLDER)) {
                    ItemStack fail = source.is(MystcraftItems.FOLDER)
                            ? this.binderBlockEntity.insertFromFolder(source, this.binderBlockEntity.getPageList().size())
                            : this.binderBlockEntity.insertPage(source.copy(), this.binderBlockEntity.getPageList().size());

                    if (!(fail.getCount() == source.getCount() && ItemStack.isSameItemSameComponents(fail, source))) {
                        slot.set(fail);
                    }
                } else {
                    return ItemStack.EMPTY;
                }

                if (source.isEmpty()) {
                    slot.set(ItemStack.EMPTY);
                } else {
                    slot.setChanged();
                }

                this.updateCraftResult();
                return copy;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.binderBlockEntity == null || this.binderBlockEntity.stillValid(player);
    }

    private final class CoverSlot extends Slot {
        private CoverSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return binderBlockEntity != null && binderBlockEntity.isValidCover(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 64;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            updateCraftResult();
        }
    }

    private final class HiddenPageSlot extends Slot {
        private HiddenPageSlot(Container container, int slot) {
            super(container, slot, -10000, -10000);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }

    private final class ResultSlot extends Slot {
        private final Player player;

        private ResultSlot(Player player, Container container, int slot, int x, int y) {
            super(container, slot, x, y);
            this.player = player;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return binderBlockEntity != null && binderBlockEntity.canBuildItem();
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            if (binderBlockEntity == null) {
                return;
            }

            ItemStack crafted = binderBlockEntity.buildBook(player);
            if (!crafted.isEmpty()) {
                this.set(crafted.copy());
            } else {
                this.set(ItemStack.EMPTY);
            }

            updateCraftResult();
            super.onTake(player, stack);
        }
    }
}