package myst.synthetic.menu;

import myst.synthetic.MystcraftMenus;
import myst.synthetic.MystcraftItems;
import myst.synthetic.block.entity.BlockEntityBookBinder;
import net.minecraft.core.BlockPos;
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

public class BookBinderMenu extends AbstractContainerMenu {

    public static final int DATA_CAN_CRAFT = 0;
    public static final int DATA_TITLE_LENGTH = 1;
    public static final int DATA_TITLE_CHARS_START = 2;
    public static final int DATA_POS_X = DATA_TITLE_CHARS_START + BlockEntityBookBinder.MAX_TITLE_LENGTH;
    public static final int DATA_POS_Y = DATA_POS_X + 1;
    public static final int DATA_POS_Z = DATA_POS_Y + 1;
    public static final int DATA_COUNT = DATA_POS_Z + 1;

    public static final int BUTTON_INSERT_AT_START = 1000;
    public static final int BUTTON_INSERT_SINGLE_AT_START = 2000;
    public static final int BUTTON_REMOVE_AT_START = 3000;

    private static final int SLOT_COVER = 0;
    private static final int SLOT_RESULT = 1;
    private static final int PLAYER_SLOT_START = 2;

    private final Container binderInventory;
    @Nullable
    private final BlockEntityBookBinder binderBlockEntity;
    private final ContainerData data;
    private final SimpleContainer resultContainer = new SimpleContainer(1);
    private final Inventory playerInventory;

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
        this.playerInventory = playerInventory;

        this.addSlot(new CoverSlot(this.binderInventory, SLOT_COVER, 8, 27));
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

    @Nullable
    private BlockEntityBookBinder getBinder() {
        if (this.binderBlockEntity != null) {
            return this.binderBlockEntity;
        }

        BlockPos pos = new BlockPos(this.data.get(DATA_POS_X), this.data.get(DATA_POS_Y), this.data.get(DATA_POS_Z));
        if (this.playerInventory.player.level().getBlockEntity(pos) instanceof BlockEntityBookBinder binder) {
            return binder;
        }

        return null;
    }

    public void updateCraftResult() {
        BlockEntityBookBinder binder = this.getBinder();
        if (binder == null) {
            this.resultContainer.setItem(0, ItemStack.EMPTY);
            return;
        }

        this.resultContainer.setItem(0, binder.getCraftedItemPreview());
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
        BlockEntityBookBinder binder = this.getBinder();
        return binder == null ? 0 : binder.getPageList().size();
    }

    public ItemStack getPageAt(int index) {
        BlockEntityBookBinder binder = this.getBinder();
        if (binder == null) {
            return ItemStack.EMPTY;
        }

        List<ItemStack> pages = binder.getPageList();
        if (index < 0 || index >= pages.size()) {
            return ItemStack.EMPTY;
        }

        return pages.get(index);
    }

    @Nullable
    public BlockEntityBookBinder getBinderBlockEntity() {
        return this.getBinder();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        BlockEntityBookBinder binder = this.getBinder();
        if (binder == null) {
            return false;
        }

        int pageCount = binder.getPageList().size();

        if (id >= BUTTON_REMOVE_AT_START && id < BUTTON_REMOVE_AT_START + pageCount) {
            int index = id - BUTTON_REMOVE_AT_START;
            if (!this.getCarried().isEmpty()) {
                return false;
            }

            ItemStack removed = binder.removePage(index);
            if (removed.isEmpty()) {
                return false;
            }

            this.setCarried(removed);
            this.updateCraftResult();
            return true;
        }

        if (id >= BUTTON_INSERT_SINGLE_AT_START && id < BUTTON_INSERT_SINGLE_AT_START + (pageCount + 1)) {
            int index = id - BUTTON_INSERT_SINGLE_AT_START;
            return this.tryInsertCarried(player, index, true);
        }

        if (id >= BUTTON_INSERT_AT_START && id < BUTTON_INSERT_AT_START + (pageCount + 1)) {
            int index = id - BUTTON_INSERT_AT_START;
            return this.tryInsertCarried(player, index, false);
        }

        return false;
    }

    private boolean tryInsertCarried(Player player, int index, boolean single) {
        BlockEntityBookBinder binder = this.getBinder();
        if (binder == null) {
            return false;
        }

        ItemStack carried = this.getCarried();
        if (carried.isEmpty()) {
            return false;
        }

        if (carried.is(MystcraftItems.FOLDER)) {
            binder.insertFromFolder(carried, index);
            this.updateCraftResult();
            return true;
        }

        if (single) {
            ItemStack one = carried.copyWithCount(1);
            ItemStack fail = binder.insertPage(one, index);
            if (!fail.isEmpty()) {
                return false;
            }

            carried.shrink(1);
            this.setCarried(carried.isEmpty() ? ItemStack.EMPTY : carried);
            this.updateCraftResult();
            return true;
        }

        ItemStack fail = binder.insertPage(carried.copy(), index);
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
            if (!this.moveItemStackTo(source, PLAYER_SLOT_START, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(source, copy);
            return copy;
        }

        if (index == SLOT_COVER) {
            if (!this.moveItemStackTo(source, PLAYER_SLOT_START, this.slots.size(), false)) {
                return ItemStack.EMPTY;
            }
            slot.setChanged();
            this.updateCraftResult();
            return copy;
        }

        BlockEntityBookBinder binder = this.getBinder();
        if (index >= PLAYER_SLOT_START && binder != null) {
            if (binder.isValidCover(source)) {
                if (!this.slots.get(SLOT_COVER).hasItem()) {
                    ItemStack one = source.copyWithCount(1);
                    this.slots.get(SLOT_COVER).set(one);
                    source.shrink(1);
                }
            } else if (source.is(MystcraftItems.PAGE) || source.is(Items.PAPER) || source.is(MystcraftItems.FOLDER)) {
                ItemStack fail = source.is(MystcraftItems.FOLDER)
                        ? binder.insertFromFolder(source, binder.getPageList().size())
                        : binder.insertPage(source.copy(), binder.getPageList().size());

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

        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        BlockEntityBookBinder binder = this.getBinder();
        return binder == null || binder.stillValid(player);
    }

    private final class CoverSlot extends Slot {
        private CoverSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            BlockEntityBookBinder binder = getBinder();
            return binder != null && binder.isValidCover(stack);
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

    private final class ResultSlot extends Slot {
        private ResultSlot(Player player, Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            BlockEntityBookBinder binder = getBinder();
            return binder != null && binder.canBuildItem();
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            BlockEntityBookBinder binder = getBinder();
            if (binder == null) {
                return;
            }

            ItemStack crafted = binder.buildBook(player);
            this.set(crafted.isEmpty() ? ItemStack.EMPTY : crafted.copy());
            updateCraftResult();
            super.onTake(player, stack);
        }
    }
}