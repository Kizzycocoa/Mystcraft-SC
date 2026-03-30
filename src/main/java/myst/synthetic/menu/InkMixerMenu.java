package myst.synthetic.menu;

import myst.synthetic.MystcraftMenus;
import myst.synthetic.block.entity.BlockEntityInkMixer;
import myst.synthetic.ink.InkMixerInkSource;
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
import net.minecraft.world.item.ItemStack;

public class InkMixerMenu extends AbstractContainerMenu {

    public static final int DATA_HAS_INK = 0;
    public static final int DATA_PROPERTY_COUNT = 1;

    private static final int SLOT_INK_INPUT = 0;
    private static final int SLOT_PAPER = 1;
    private static final int SLOT_CONTAINER_OUTPUT = 2;
    private static final int SLOT_RESULT = 39;

    private static final int PLAYER_INV_START = 3;
    private static final int PLAYER_INV_END = 30;
    private static final int HOTBAR_START = 30;
    private static final int HOTBAR_END = 39;

    private final Container inkMixerInventory;
    @Nullable
    private final BlockEntityInkMixer inkMixerBlockEntity;
    private final ContainerData data;
    private final SimpleContainer resultContainer = new SimpleContainer(1);
    private static final int BUTTON_MIX_ONE = 0;
    private static final int BUTTON_MIX_STACK = 1;

    public InkMixerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(3), null, new SimpleContainerData(2));
    }

    public InkMixerMenu(int containerId, Inventory playerInventory, BlockEntityInkMixer blockEntity, ContainerData data) {
        this(containerId, playerInventory, blockEntity, blockEntity, data);
    }

    private InkMixerMenu(
            int containerId,
            Inventory playerInventory,
            Container inventory,
            @Nullable BlockEntityInkMixer blockEntity,
            ContainerData data
    ) {
        super(MystcraftMenus.INK_MIXER, containerId);

        checkContainerSize(inventory, 3);
        checkContainerDataCount(data, 2);

        this.inkMixerInventory = inventory;
        this.inkMixerBlockEntity = blockEntity;
        this.data = data;

        this.addSlot(new InkInputSlot(this.inkMixerInventory, SLOT_INK_INPUT, 8, 27));
        this.addSlot(new PaperSlot(this.inkMixerInventory, SLOT_PAPER, 8, 48));
        this.addSlot(new OutputSlot(this.inkMixerInventory, SLOT_CONTAINER_OUTPUT, 152, 27));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 99 + row * 18));
            }
        }

        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            this.addSlot(new Slot(playerInventory, hotbarSlot, 8 + hotbarSlot * 18, 157));
        }

        this.addSlot(new ResultSlot(this.resultContainer, 0, 152, 48));

        this.addDataSlots(this.data);

        this.updateResultSlot();
    }

    public boolean hasInk() {
        return this.data.get(DATA_HAS_INK) != 0;
    }

    public int getStoredPropertyCount() {
        return this.data.get(DATA_PROPERTY_COUNT);
    }

    private boolean consumeHeldIngredient(Player player, boolean wholeStack) {
        if (this.inkMixerBlockEntity == null) {
            return false;
        }

        ItemStack carried = this.getCarried();
        if (carried.isEmpty()) {
            return false;
        }

        if (!this.inkMixerBlockEntity.canConsumeIngredient(carried)) {
            return false;
        }

        int amount = wholeStack ? carried.getCount() : 1;
        if (!this.inkMixerBlockEntity.consumeIngredient(carried, amount)) {
            return false;
        }

        carried.shrink(amount);
        this.setCarried(carried.isEmpty() ? ItemStack.EMPTY : carried);

        this.updateResultSlot();
        this.broadcastChanges();
        return true;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return switch (id) {
            case BUTTON_MIX_ONE -> this.consumeHeldIngredient(player, false);
            case BUTTON_MIX_STACK -> this.consumeHeldIngredient(player, true);
            default -> false;
        };
    }

    private void updateResultSlot() {
        if (this.inkMixerBlockEntity != null) {
            this.resultContainer.setItem(0, this.inkMixerBlockEntity.getPreviewStack());
        }
    }

    @Override
    public void broadcastChanges() {
        if (this.inkMixerBlockEntity != null) {
            this.updateResultSlot();
        }

        super.broadcastChanges();
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);

        if (this.inkMixerBlockEntity != null) {
            this.updateResultSlot();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inkMixerInventory.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copied = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        copied = stack.copy();

        if (index == SLOT_RESULT) {
            if (!this.moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, copied);
        } else if (index == SLOT_INK_INPUT || index == SLOT_PAPER || index == SLOT_CONTAINER_OUTPUT) {
            if (!this.moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (InkMixerInkSource.isValidInkSource(stack)) {
            if (!this.moveItemStackTo(stack, SLOT_INK_INPUT, SLOT_INK_INPUT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(Items.PAPER)) {
            if (!this.moveItemStackTo(stack, SLOT_PAPER, SLOT_PAPER + 1, false)) {
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

        if (stack.getCount() == copied.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return copied;
    }

    private final class InkInputSlot extends Slot {
        private InkInputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return InkMixerInkSource.isValidInkSource(stack);
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

    private static final class OutputSlot extends Slot {
        private OutputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }

    private final class ResultSlot extends Slot {
        private ResultSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return inkMixerBlockEntity != null && inkMixerBlockEntity.canCraftPreview();
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            if (inkMixerBlockEntity != null) {
                inkMixerBlockEntity.finishCraft(stack);
                updateResultSlot();
            }

            super.onTake(player, stack);
        }
    }
}