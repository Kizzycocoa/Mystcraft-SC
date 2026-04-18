package myst.synthetic.block.entity;

import myst.synthetic.MystcraftBlockEntities;
import myst.synthetic.item.DeskItemBehaviors;
import myst.synthetic.ink.InkMixerInkSource;
import myst.synthetic.menu.WritingDeskMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.util.Mth;

public class BlockEntityDesk extends BlockEntity implements Container, MenuProvider {

    public static final int SLOT_TARGET = 0;
    public static final int SLOT_PAPER = 1;
    public static final int SLOT_INK_INPUT = 2;
    public static final int SLOT_OUTPUT = 3;
    public static final int SLOT_TAB_START = 4;
    public static final int TAB_SLOT_COUNT = 25;
    public static final int TOTAL_SLOTS = SLOT_TAB_START + TAB_SLOT_COUNT;
    public static final int INK_TANK_CAPACITY = 1000;
    public static final int INK_COST_PER_WRITE = 50;

    private NonNullList<ItemStack> items = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);

    private int inkAmount = 0;
    private int activeTab = 0;
    private int firstVisibleTab = 0;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case WritingDeskMenu.DATA_INK_AMOUNT -> inkAmount;
                case WritingDeskMenu.DATA_ACTIVE_TAB -> activeTab;
                case WritingDeskMenu.DATA_FIRST_TAB -> firstVisibleTab;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case WritingDeskMenu.DATA_INK_AMOUNT -> inkAmount = Mth.clamp(value, 0, INK_TANK_CAPACITY);
                case WritingDeskMenu.DATA_ACTIVE_TAB -> activeTab = clampTab(value);
                case WritingDeskMenu.DATA_FIRST_TAB -> firstVisibleTab = clampFirstTab(value);
            }
        }

        @Override
        public int getCount() {
            return WritingDeskMenu.DATA_COUNT;
        }
    };

    public BlockEntityDesk(BlockPos pos, BlockState state) {
        super(MystcraftBlockEntities.WRITING_DESK, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityDesk blockEntity) {
        if (level.isClientSide()) {
            return;
        }

        blockEntity.tryConsumeInkContainer();
    }

    public ContainerData getDataAccess() {
        return this.dataAccess;
    }

    public boolean hasInk() {
        return this.inkAmount > 0;
    }

    public boolean hasEnoughInk() {
        return this.inkAmount >= INK_COST_PER_WRITE;
    }

    public int getInkAmount() {
        return this.inkAmount;
    }

    public int getActiveTab() {
        return this.activeTab;
    }

    public void setActiveTab(int activeTab) {
        this.activeTab = clampTab(activeTab);
        this.setChangedAndSync();
    }

    public int getFirstVisibleTab() {
        return this.firstVisibleTab;
    }

    public void setFirstVisibleTab(int firstVisibleTab) {
        this.firstVisibleTab = clampFirstTab(firstVisibleTab);
        this.setChangedAndSync();
    }

    public ItemStack getTargetStack() {
        return this.items.get(SLOT_TARGET);
    }

    public ItemStack getActiveTabStack() {
        return this.getTabStack(this.activeTab);
    }

    public ItemStack getTabStack(int tabIndex) {
        if (tabIndex < 0 || tabIndex >= TAB_SLOT_COUNT) {
            return ItemStack.EMPTY;
        }
        return this.items.get(SLOT_TAB_START + tabIndex);
    }

    public boolean writeSymbol(Player player, net.minecraft.resources.Identifier symbol) {
        ItemStack target = this.getTargetStack();

        // If the desk target slot is empty, use one paper to create a blank page there.
        if (target.isEmpty()) {
            ItemStack paper = this.items.get(SLOT_PAPER);
            if (!paper.isEmpty() && paper.is(net.minecraft.world.item.Items.PAPER)) {
                ItemStack created = myst.synthetic.page.Page.createPage();
                this.items.set(SLOT_TARGET, created);
                paper.shrink(1);
                if (paper.isEmpty()) {
                    this.items.set(SLOT_PAPER, ItemStack.EMPTY);
                }
                target = created;
            }
        }

        if (target.isEmpty() || !this.hasEnoughInk()) {
            return false;
        }

        // Writable target path: page already exists and can be written directly.
        boolean changed = DeskItemBehaviors.writeSymbol(player, target, symbol);
        if (changed) {
            this.inkAmount = Math.max(0, this.inkAmount - INK_COST_PER_WRITE);
            this.setChangedAndSync();
            return true;
        }

        // Acceptor path: create a fresh page from paper and insert it into folder/portfolio/etc.
        ItemStack paper = this.items.get(SLOT_PAPER);
        if (!paper.isEmpty() && paper.is(net.minecraft.world.item.Items.PAPER) && DeskItemBehaviors.canAcceptPaper(target)) {
            ItemStack newPage = myst.synthetic.page.Page.createPage();
            myst.synthetic.page.Page.setSymbol(newPage, symbol);

            ItemStack returned = DeskItemBehaviors.addPage(player, target, newPage);
            if (returned.isEmpty()) {
                paper.shrink(1);
                if (paper.isEmpty()) {
                    this.items.set(SLOT_PAPER, ItemStack.EMPTY);
                }

                this.inkAmount = Math.max(0, this.inkAmount - INK_COST_PER_WRITE);
                this.setChangedAndSync();
                return true;
            }
        }

        return false;
    }

    public ItemStack removePageFromActiveTab(Player player, int index) {
        ItemStack active = this.getActiveTabStack();
        if (active.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack removed = DeskItemBehaviors.removePage(player, active, index);
        if (!removed.isEmpty()) {
            this.setChangedAndSync();
        }
        return removed;
    }

    public ItemStack insertPageIntoActiveTab(Player player, ItemStack page, int index) {
        ItemStack active = this.getActiveTabStack();
        if (active.isEmpty()) {
            return page;
        }

        ItemStack result = DeskItemBehaviors.insertPage(player, active, page, index);
        if (!ItemStack.isSameItemSameComponents(result, page) || result.getCount() != page.getCount()) {
            this.setChangedAndSync();
        }
        return result;
    }

    public ItemStack addPageToActiveTab(Player player, ItemStack page) {
        ItemStack active = this.getActiveTabStack();
        if (active.isEmpty()) {
            return page;
        }

        ItemStack result = DeskItemBehaviors.addPage(player, active, page);
        if (!ItemStack.isSameItemSameComponents(result, page) || result.getCount() != page.getCount()) {
            this.setChangedAndSync();
        }
        return result;
    }

    public void setTargetTitle(Player player, String title) {
        ItemStack target = this.getTargetStack();
        if (target.isEmpty()) {
            return;
        }

        DeskItemBehaviors.setDisplayName(player, target, title);
        this.setChangedAndSync();
    }

    public boolean activateTargetLink(Player player) {
        boolean result = DeskItemBehaviors.activateLink(player.level(), player, this.getTargetStack());
        if (result) {
            this.setChangedAndSync();
        }
        return result;
    }

    private void tryConsumeInkContainer() {
        // Legacy behavior: only refill when empty.
        if (this.inkAmount > 0) {
            return;
        }

        ItemStack input = this.items.get(SLOT_INK_INPUT);
        if (input.isEmpty() || !InkMixerInkSource.isValidInkSource(input)) {
            return;
        }

        ItemStack remaining = InkMixerInkSource.getRemainingContainer(input);
        if (!this.canOutput(remaining)) {
            return;
        }

        input.shrink(1);
        if (input.isEmpty()) {
            this.items.set(SLOT_INK_INPUT, ItemStack.EMPTY);
        }

        this.putOutput(remaining);
        this.inkAmount = INK_TANK_CAPACITY;
        this.setChangedAndSync();
    }

    private boolean canOutput(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }

        ItemStack output = this.items.get(SLOT_OUTPUT);
        if (output.isEmpty()) {
            return true;
        }

        if (!ItemStack.isSameItemSameComponents(output, stack)) {
            return false;
        }

        return output.getCount() + stack.getCount() <= output.getMaxStackSize();
    }

    private void putOutput(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        ItemStack output = this.items.get(SLOT_OUTPUT);
        if (output.isEmpty()) {
            this.items.set(SLOT_OUTPUT, stack.copy());
            return;
        }

        output.grow(stack.getCount());
    }

    private static int clampTab(int tab) {
        return Math.max(0, Math.min(TAB_SLOT_COUNT - 1, tab));
    }

    private static int clampFirstTab(int tab) {
        return Math.max(0, Math.min(TAB_SLOT_COUNT - WritingDeskMenu.VISIBLE_TAB_COUNT, tab));
    }

    private void setChangedAndSync() {
        this.setChanged();
        if (this.level != null) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.inkAmount = input.getIntOr("InkAmount", 0);
        this.activeTab = clampTab(input.getInt("ActiveTab").orElse(0));
        this.firstVisibleTab = clampFirstTab(input.getInt("FirstVisibleTab").orElse(0));
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("InkAmount", this.inkAmount);
        output.putInt("ActiveTab", this.activeTab);
        output.putInt("FirstVisibleTab", this.firstVisibleTab);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.mystcraft-sc.writing_desk");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new WritingDeskMenu(containerId, playerInventory, this, this.dataAccess);
    }

    @Override
    public int getContainerSize() {
        return TOTAL_SLOTS;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(this.items, slot, amount);
        if (!stack.isEmpty()) {
            this.setChangedAndSync();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > this.getMaxStackSize(stack)) {
            stack.setCount(this.getMaxStackSize(stack));
        }
        this.setChangedAndSync();
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level == null) {
            return false;
        }
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(
                this.worldPosition.getX() + 0.5D,
                this.worldPosition.getY() + 0.5D,
                this.worldPosition.getZ() + 0.5D
        ) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_TARGET -> DeskItemBehaviors.canBeDeskTarget(stack);
            case SLOT_PAPER -> stack.is(net.minecraft.world.item.Items.PAPER);
            case SLOT_INK_INPUT -> InkMixerInkSource.isValidInkSource(stack);
            case SLOT_OUTPUT -> false;
            default -> slot >= SLOT_TAB_START && slot < TOTAL_SLOTS && DeskItemBehaviors.canBeDeskTabStorage(stack);
        };
    }

    @Override
    public void clearContent() {
        this.items.clear();
        this.setChangedAndSync();
    }
}
