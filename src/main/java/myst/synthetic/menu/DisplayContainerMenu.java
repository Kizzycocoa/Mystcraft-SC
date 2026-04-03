package myst.synthetic.menu;

import myst.synthetic.MystcraftItems;
import myst.synthetic.MystcraftMenus;
import myst.synthetic.block.entity.BlockEntityDisplayContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DisplayContainerMenu extends AbstractContainerMenu {

    private final Container container;
    private final BlockPos blockPos;

    public DisplayContainerMenu(int containerId, Inventory playerInventory, Container container, BlockPos blockPos) {
        super(MystcraftMenus.DISPLAY_CONTAINER, containerId);
        this.container = container;
        this.blockPos = blockPos;

        checkContainerSize(container, 1);
        container.startOpen(playerInventory.player);

        // Single-slot mode display slot
        this.addSlot(new ConditionalDisplaySlot(container, 0, 80, 35, false));

        // Linkbook/descriptive-book mode display slot
        this.addSlot(new ConditionalDisplaySlot(container, 0, 41, 21, true));

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
    }

    public DisplayContainerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(1), BlockPos.ZERO);
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public ItemStack getDisplayStack() {
        return this.container.getItem(0);
    }

    public boolean isLinkBookMode() {
        ItemStack stack = this.getDisplayStack();
        return stack.is(MystcraftItems.LINKBOOK) || stack.is(MystcraftItems.AGEBOOK);
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

        // Either display slot -> move into player inventory
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
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    private final class ConditionalDisplaySlot extends Slot {
        private final boolean linkBookVariant;

        private ConditionalDisplaySlot(Container container, int slot, int x, int y, boolean linkBookVariant) {
            super(container, slot, x, y);
            this.linkBookVariant = linkBookVariant;
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
            return isLinkBookMode() == this.linkBookVariant;
        }
    }

    private final class ConditionalPlayerSlot extends Slot {
        private ConditionalPlayerSlot(Inventory inventory, int slot, int x, int y) {
            super(inventory, slot, x, y);
        }

        @Override
        public boolean isActive() {
            return !isLinkBookMode();
        }
    }
}