package myst.synthetic.menu;

import myst.synthetic.MystcraftItems;
import myst.synthetic.MystcraftMenus;
import myst.synthetic.block.entity.BlockEntityDisplayContainer;
import myst.synthetic.linking.LinkController;
import myst.synthetic.linking.LinkOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import myst.synthetic.item.BookBookmarkUtil;
import myst.synthetic.item.ItemAgebook;
import myst.synthetic.world.dimension.AgeDimensionManager;
import net.minecraft.server.level.ServerLevel;

public class DisplayContainerMenu extends AbstractContainerMenu {

    public static final int BUTTON_PREV_PAGE = 1;
    public static final int BUTTON_NEXT_PAGE = 2;
    public static final int BUTTON_TAKE_BOOK = 3;
    public static final int BUTTON_USE_LINKBOOK = 4;
    public static final int BUTTON_EXTRACT_BOOKMARK = 5;
    public static final int BUTTON_PAGE_JUMP_RANGE_START = 100;

    private static final int DATA_CURRENT_PAGE = 0;
    private static final int DATA_COUNT = 1;

    private final Container container;
    private final BlockPos blockPos;
    private final ContainerData data;

    public DisplayContainerMenu(int containerId, Inventory playerInventory, Container container, BlockPos blockPos) {
        this(containerId, playerInventory, container, blockPos, new SimpleContainerData(DATA_COUNT));
    }

    private DisplayContainerMenu(int containerId, Inventory playerInventory, Container container, BlockPos blockPos, ContainerData data) {
        super(MystcraftMenus.DISPLAY_CONTAINER, containerId);
        this.container = container;
        this.blockPos = blockPos;
        this.data = data;

        checkContainerSize(container, 1);
        checkContainerDataCount(data, DATA_COUNT);

        container.startOpen(playerInventory.player);

        this.addSlot(new ConditionalDisplaySlot(container, 0, 80, 35, DisplaySlotMode.SINGLE));
        this.addSlot(new ConditionalDisplaySlot(container, 0, 41, 21, DisplaySlotMode.LINKBOOK));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new ConditionalPlayerSlot(
                        playerInventory,
                        column + row * 9 + 9,
                        8 + column * 18,
                        84 + row * 18
                ));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlot(new ConditionalPlayerSlot(
                    playerInventory,
                    column,
                    8 + column * 18,
                    142
            ));
        }

        this.addDataSlots(this.data);
        this.clampCurrentPage();
    }

    public DisplayContainerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(1), BlockPos.ZERO, new SimpleContainerData(DATA_COUNT));
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public ItemStack getDisplayStack() {
        return this.container.getItem(0);
    }

    public ItemStack getBook() {
        return this.container.getItem(0);
    }

    public boolean isLinkBookMode() {
        ItemStack stack = this.getDisplayStack();
        return stack.is(MystcraftItems.LINKBOOK) || stack.is(MystcraftItems.AGEBOOK);
    }

    public boolean isWritableBookMode() {
        return this.getDisplayStack().is(Items.WRITABLE_BOOK);
    }

    public boolean isWrittenBookMode() {
        return this.getDisplayStack().is(Items.WRITTEN_BOOK);
    }

    public boolean isLecternBookMode() {
        return this.isWritableBookMode() || this.isWrittenBookMode();
    }

    public int getPage() {
        return this.data.get(DATA_CURRENT_PAGE);
    }

    public int getCurrentPage() {
        return this.data.get(DATA_CURRENT_PAGE);
    }

    public int getPageCount() {
        ItemStack stack = this.getDisplayStack();

        if (stack.isEmpty()) {
            return 1;
        }

        if (stack.is(Items.WRITTEN_BOOK)) {
            WrittenBookContent content = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
            if (content == null) {
                return 1;
            }

            return Math.max(1, content.getPages(false).size());
        }

        if (stack.is(Items.WRITABLE_BOOK)) {
            WritableBookContent content = stack.get(DataComponents.WRITABLE_BOOK_CONTENT);
            if (content == null) {
                return 1;
            }

            return Math.max(1, (int) content.getPages(false).count());
        }

        return 1;
    }

    private void clampCurrentPage() {
        int maxPage = this.getPageCount() - 1;
        int clamped = Math.max(0, Math.min(this.getCurrentPage(), maxPage));
        this.data.set(DATA_CURRENT_PAGE, clamped);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= BUTTON_PAGE_JUMP_RANGE_START) {
            int page = id - BUTTON_PAGE_JUMP_RANGE_START;
            this.setData(DATA_CURRENT_PAGE, page);
            return true;
        }

        switch (id) {
            case BUTTON_PREV_PAGE -> {
                if (!this.isLecternBookMode()) {
                    return false;
                }

                int current = this.getCurrentPage();
                this.setData(DATA_CURRENT_PAGE, current - 1);
                return true;
            }

            case BUTTON_NEXT_PAGE -> {
                if (!this.isLecternBookMode()) {
                    return false;
                }

                int current = this.getCurrentPage();
                this.setData(DATA_CURRENT_PAGE, current + 1);
                return true;
            }

            case BUTTON_TAKE_BOOK -> {
                ItemStack carried = this.getCarried();
                if (!carried.isEmpty()) {
                    this.setCarried(ItemStack.EMPTY);

                    if (!player.addItem(carried)) {
                        player.drop(carried, false);
                    }
                }

                ItemStack removed;

                if (this.container instanceof BlockEntityDisplayContainer displayContainer) {
                    removed = displayContainer.takeStoredItem();
                } else {
                    removed = this.container.removeItemNoUpdate(0);
                    this.container.setChanged();
                }

                if (removed.isEmpty()) {
                    return false;
                }

                this.setData(DATA_CURRENT_PAGE, 0);

                if (!player.getInventory().add(removed)) {
                    player.drop(removed, false);
                }

                player.getInventory().setChanged();
                player.inventoryMenu.broadcastChanges();
                this.broadcastChanges();

                return true;
            }

            case BUTTON_USE_LINKBOOK -> {
                if (!this.isLinkBookMode()) {
                    return false;
                }

                ItemStack stack = this.getDisplayStack();
                if (!stack.is(MystcraftItems.LINKBOOK) && !stack.is(MystcraftItems.AGEBOOK)) {
                    return false;
                }

                if (stack.is(MystcraftItems.AGEBOOK)) {
                    CustomData initialCustomData = stack.get(DataComponents.CUSTOM_DATA);
                    CompoundTag initialTag = initialCustomData == null ? new CompoundTag() : initialCustomData.copyTag();
                    LinkOptions initialInfo = new LinkOptions(initialTag);

                    String initialTargetDimension = initialInfo.getDimensionUID();
                    if (initialTargetDimension == null || initialTargetDimension.isBlank()) {
                        if (player.level().isClientSide()) {
                            return false;
                        }

                        ServerLevel created = new AgeDimensionManager().getOrCreateAgeLevel(player.getServer(), stack);
                        if (created == null) {
                            return false;
                        }
                    }
                }

                CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                if (customData == null) {
                    return false;
                }

                CompoundTag tag = customData.copyTag();
                LinkOptions info = new LinkOptions(tag);

                String targetDimension = info.getDimensionUID();
                if (targetDimension == null || targetDimension.isBlank()) {
                    return false;
                }

                String currentDimension = extractDimensionId(player.level().dimension().toString());
                if (currentDimension.equals(targetDimension)) {
                    return false;
                }

                ItemStack carried = this.getCarried();
                if (!carried.isEmpty()) {
                    this.setCarried(ItemStack.EMPTY);

                    if (!player.addItem(carried)) {
                        player.drop(carried, false);
                    }
                }

                this.container.setChanged();
                this.broadcastChanges();

                LinkController.travelEntity(player.level(), player, info);
                return true;
            }

            case BUTTON_EXTRACT_BOOKMARK -> {
                if (!this.isLinkBookMode()) {
                    return false;
                }

                ItemStack stack = this.getDisplayStack();
                if (stack.isEmpty()) {
                    return false;
                }

                ItemStack removed = BookBookmarkUtil.removeBookmark(stack);
                if (removed.isEmpty()) {
                    return false;
                }

                this.container.setChanged();
                this.broadcastChanges();

                if (!player.getInventory().add(removed)) {
                    player.drop(removed, false);
                }

                player.getInventory().setChanged();
                player.inventoryMenu.broadcastChanges();
                return true;
            }

            default -> {
                return false;
            }
        }
    }

    @Override
    public void setData(int id, int value) {
        super.setData(id, value);
        this.clampCurrentPage();
        this.broadcastChanges();
    }

    @Override
    public void broadcastChanges() {
        this.clampCurrentPage();
        super.broadcastChanges();
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        this.clampCurrentPage();
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.container instanceof BlockEntityDisplayContainer displayContainer) {
            return displayContainer.stillValid(player);
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (!slot.isActive() || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        result = stack.copy();

        if (index == 0 || index == 1) {
            if (!this.moveItemStackTo(stack, 2, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            Slot activeDisplaySlot = this.slots.get(this.isLinkBookMode() ? 1 : 0);

            if (!activeDisplaySlot.mayPlace(stack) || activeDisplaySlot.hasItem()) {
                return ItemStack.EMPTY;
            }

            ItemStack single = stack.copyWithCount(1);
            activeDisplaySlot.set(single);
            stack.shrink(1);
            this.setData(DATA_CURRENT_PAGE, 0);
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        this.broadcastChanges();
        return result;
    }

    @Override
    public void removed(Player player) {
        if (!player.level().isClientSide()) {
            ItemStack carried = this.getCarried();
            if (!carried.isEmpty()) {
                this.setCarried(ItemStack.EMPTY);

                if (!player.addItem(carried)) {
                    player.drop(carried, false);
                }
            }
        }

        super.removed(player);
        this.container.stopOpen(player);
    }

    private static String extractDimensionId(String raw) {
        int slash = raw.lastIndexOf('/');
        int end = raw.lastIndexOf(']');

        if (slash >= 0 && end > slash) {
            return raw.substring(slash + 1, end).trim();
        }

        return raw;
    }

    private enum DisplaySlotMode {
        SINGLE,
        LINKBOOK
    }

    private final class ConditionalDisplaySlot extends Slot {
        private final DisplaySlotMode mode;

        private ConditionalDisplaySlot(Container container, int slot, int x, int y, DisplaySlotMode mode) {
            super(container, slot, x, y);
            this.mode = mode;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (container instanceof BlockEntityDisplayContainer displayContainer) {
                return displayContainer.canAcceptDisplayItem(stack);
            }
            return false;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public boolean isActive() {
            if (isLecternBookMode()) {
                return false;
            }

            return switch (this.mode) {
                case SINGLE -> !isLinkBookMode();
                case LINKBOOK -> isLinkBookMode();
            };
        }
    }

    private final class ConditionalPlayerSlot extends Slot {
        private ConditionalPlayerSlot(Inventory inventory, int slot, int x, int y) {
            super(inventory, slot, x, y);
        }

        @Override
        public boolean isActive() {
            return !isLinkBookMode() && !isLecternBookMode();
        }
    }
}