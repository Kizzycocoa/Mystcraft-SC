package myst.synthetic.block.entity;

import myst.synthetic.MystcraftBlockEntities;
import myst.synthetic.MystcraftItems;
import myst.synthetic.item.ItemAgebook;
import myst.synthetic.item.ItemFolder;
import myst.synthetic.menu.BookBinderMenu;
import myst.synthetic.page.Page;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

public class BlockEntityBookBinder extends BlockEntity implements Container, MenuProvider {

	public static final int SLOT_COVER = 0;
	public static final int TOTAL_SLOTS = 1;
	public static final int MAX_TITLE_LENGTH = 21;

	private NonNullList<ItemStack> items = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);
	private final List<ItemStack> pages = new ArrayList<>();
	private String pendingTitle = "";

	private final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int index) {
			if (index == BookBinderMenu.DATA_CAN_CRAFT) {
				return canBuildItem() ? 1 : 0;
			}

			if (index == BookBinderMenu.DATA_TITLE_LENGTH) {
				return Math.min(pendingTitle.length(), MAX_TITLE_LENGTH);
			}

			int charIndex = index - BookBinderMenu.DATA_TITLE_CHARS_START;
			if (charIndex >= 0 && charIndex < MAX_TITLE_LENGTH && charIndex < pendingTitle.length()) {
				return pendingTitle.charAt(charIndex);
			}

			if (index == BookBinderMenu.DATA_POS_X) {
				return worldPosition.getX();
			}

			if (index == BookBinderMenu.DATA_POS_Y) {
				return worldPosition.getY();
			}

			if (index == BookBinderMenu.DATA_POS_Z) {
				return worldPosition.getZ();
			}

			return 0;
		}

		@Override
		public void set(int index, int value) {
		}

		@Override
		public int getCount() {
			return BookBinderMenu.DATA_COUNT;
		}
	};

	public BlockEntityBookBinder(BlockPos pos, BlockState state) {
		super(MystcraftBlockEntities.BOOK_BINDER, pos, state);
	}

	public ContainerData getDataAccess() {
		return this.dataAccess;
	}

	public String getPendingTitle() {
		return this.pendingTitle == null ? "" : this.pendingTitle;
	}

	public void setBookTitle(String name) {
		this.pendingTitle = name == null ? "" : name;
		if (this.pendingTitle.length() > MAX_TITLE_LENGTH) {
			this.pendingTitle = this.pendingTitle.substring(0, MAX_TITLE_LENGTH);
		}
		this.setChangedAndSync();
	}

	public boolean isValidCover(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}

		if (stack.is(Items.LEATHER)) {
			return true;
		}

		return stack.is(MystcraftItems.FOLDER) && ItemFolder.getFolderData(stack).isEmpty();
	}

	public List<ItemStack> getPageList() {
		List<ItemStack> copy = new ArrayList<>(this.pages.size());
		for (ItemStack stack : this.pages) {
			if (!stack.isEmpty() && stack.is(MystcraftItems.PAGE)) {
				copy.add(stack.copyWithCount(1));
			}
		}
		return copy;
	}

	private void setPageList(List<ItemStack> newPages) {
		this.pages.clear();

		for (ItemStack page : newPages) {
			if (!page.isEmpty() && page.is(MystcraftItems.PAGE)) {
				this.pages.add(page.copyWithCount(1));
			}
		}

		this.setChangedAndSync();
	}

	public ItemStack insertPage(ItemStack stack, int index) {
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}

		if (stack.is(Items.PAPER)) {
			ItemStack remaining = stack.copy();

			while (!remaining.isEmpty()) {
				ItemStack blankPage = Page.createPage();
				ItemStack fail = this.insertPage(blankPage, index);
				if (!fail.isEmpty()) {
					return remaining;
				}
				remaining.shrink(1);
				index++;
			}

			return ItemStack.EMPTY;
		}

		if (!stack.is(MystcraftItems.PAGE)) {
			return stack;
		}

		int insertAt = Math.max(0, Math.min(index, this.pages.size()));
		this.pages.add(insertAt, stack.copyWithCount(1));
		this.setChangedAndSync();
		return ItemStack.EMPTY;
	}

	public ItemStack insertFromFolder(ItemStack folder, int index) {
		if (!folder.is(MystcraftItems.FOLDER)) {
			return folder;
		}

		var slots = ItemFolder.createInventory(folder);

		boolean folderHasContent = false;
		for (ItemStack slot : slots) {
			if (!slot.isEmpty()) {
				folderHasContent = true;
				break;
			}
		}

		if (!folderHasContent) {
			for (int i = 0; i < this.pages.size() && i < slots.size(); i++) {
				slots.set(i, this.pages.get(i).copyWithCount(1));
			}
			ItemFolder.saveInventory(folder, slots);
			this.setPageList(List.of());
			return ItemStack.EMPTY;
		}

		for (int slot = 0; slot < slots.size(); slot++) {
			ItemStack page = slots.get(slot);
			if (page.isEmpty()) {
				continue;
			}

			ItemStack fail = this.insertPage(page.copyWithCount(1), index);
			if (fail.isEmpty()) {
				slots.set(slot, ItemStack.EMPTY);
				index++;
			}
		}

		ItemFolder.saveInventory(folder, slots);
		this.setChangedAndSync();
		return ItemStack.EMPTY;
	}

	public ItemStack removePage(int index) {
		if (index < 0 || index >= this.pages.size()) {
			return ItemStack.EMPTY;
		}

		ItemStack removed = this.pages.remove(index);
		this.setChangedAndSync();
		return removed;
	}

	public boolean canBuildItem() {
		ItemStack cover = this.items.get(SLOT_COVER);

		if (cover.isEmpty() || !isValidCover(cover)) {
			return false;
		}

		if (this.pages.isEmpty()) {
			return false;
		}

		if (!Page.isLinkPanel(this.pages.get(0))) {
			return false;
		}

		if (this.getPendingTitle().isBlank()) {
			return false;
		}

		for (int i = 1; i < this.pages.size(); i++) {
			if (Page.isLinkPanel(this.pages.get(i))) {
				return false;
			}
		}

		return true;
	}

	public ItemStack getCraftedItemPreview() {
		if (!this.canBuildItem()) {
			return ItemStack.EMPTY;
		}

		ItemStack preview = new ItemStack(MystcraftItems.AGEBOOK);
		ItemAgebook.create(preview, DummyPlayerNameHolder.INSTANCE, this.getPageList(), this.getPendingTitle());
		return preview;
	}

	public ItemStack buildBook(Player player) {
		if (!this.canBuildItem()) {
			return ItemStack.EMPTY;
		}

		ItemStack result = new ItemStack(MystcraftItems.AGEBOOK);
		ItemAgebook.create(result, player, this.getPageList(), this.getPendingTitle());

		this.setPageList(List.of());
		this.pendingTitle = "";

		ItemStack cover = this.items.get(SLOT_COVER);
		cover.shrink(1);
		if (cover.isEmpty()) {
			this.items.set(SLOT_COVER, ItemStack.EMPTY);
		}

		this.setChangedAndSync();
		return result;
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

		this.pages.clear();
		int pageCount = input.getInt("PageCount").orElse(0);
		for (int i = 0; i < pageCount; i++) {
			ItemStack stack = input.read("Page" + i, ItemStack.CODEC).orElse(ItemStack.EMPTY);
			if (!stack.isEmpty() && stack.is(MystcraftItems.PAGE)) {
				this.pages.add(stack.copyWithCount(1));
			}
		}

		this.pendingTitle = input.getString("PendingTitle").orElse("");
	}

	@Override
	public void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);

		ContainerHelper.saveAllItems(output, this.items);

		output.putInt("PageCount", this.pages.size());
		for (int i = 0; i < this.pages.size(); i++) {
			ItemStack stack = this.pages.get(i);
			if (!stack.isEmpty()) {
				output.store("Page" + i, ItemStack.CODEC, stack);
			}
		}

		output.putString("PendingTitle", this.getPendingTitle());
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("block.mystcraft-sc.blockbookbinder");
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
		return new BookBinderMenu(containerId, playerInventory, this, this.dataAccess);
	}

	@Override
	public int getContainerSize() {
		return TOTAL_SLOTS;
	}

	@Override
	public boolean isEmpty() {
		if (!this.items.get(SLOT_COVER).isEmpty()) {
			return false;
		}
		return this.pages.isEmpty();
	}

	@Override
	public ItemStack getItem(int slot) {
		return slot == SLOT_COVER ? this.items.get(SLOT_COVER) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		if (slot != SLOT_COVER) {
			return ItemStack.EMPTY;
		}

		ItemStack stack = ContainerHelper.removeItem(this.items, slot, amount);
		if (!stack.isEmpty()) {
			this.setChangedAndSync();
		}
		return stack;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return slot == SLOT_COVER ? ContainerHelper.takeItem(this.items, slot) : ItemStack.EMPTY;
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (slot != SLOT_COVER) {
			return;
		}

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
		return slot == SLOT_COVER && this.isValidCover(stack);
	}

	@Override
	public void clearContent() {
		this.items.clear();
		this.pages.clear();
		this.pendingTitle = "";
		this.setChangedAndSync();
	}

	/**
	 * Used only for preview generation where an author string is needed.
	 */
	private static final class DummyPlayerNameHolder extends PlayerNameOnlyStub {
		private static final DummyPlayerNameHolder INSTANCE = new DummyPlayerNameHolder();

		private DummyPlayerNameHolder() {
			super("Unknown Author");
		}
	}
}