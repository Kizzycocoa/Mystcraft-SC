package myst.synthetic.block.entity;

import myst.synthetic.block.entity.DisplayContentType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class BlockEntityDisplayContainer extends BlockEntity implements Container {

	private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);

	protected BlockEntityDisplayContainer(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public abstract boolean canAcceptDisplayItem(ItemStack stack);

	public ItemStack getStoredItem() {
		return this.items.get(0);
	}

	public boolean hasStoredItem() {
		return !this.getStoredItem().isEmpty();
	}

	public DisplayContentType getContentType() {
		return DisplayItemRules.classify(this.getStoredItem());
	}

	public void setStoredItem(ItemStack stack) {
		if (stack.isEmpty()) {
			this.items.set(0, ItemStack.EMPTY);
		} else {
			ItemStack single = stack.copyWithCount(1);

			if (!this.canAcceptDisplayItem(single)) {
				return;
			}

			this.items.set(0, single);
		}

		this.setChangedAndSync();
	}

	public ItemStack takeStoredItem() {
		ItemStack stack = this.items.get(0);
		this.items.set(0, ItemStack.EMPTY);
		this.setChangedAndSync();
		return stack;
	}

	protected void setChangedAndSync() {
		this.setChanged();

		if (this.level != null) {
			BlockState state = this.getBlockState();
			this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_ALL);
		}
	}

	@Override
	public void loadAdditional(ValueInput input) {
		super.loadAdditional(input);

		this.items = NonNullList.withSize(1, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(input, this.items);

		ItemStack stack = this.items.get(0);
		if (!stack.isEmpty() && !this.canAcceptDisplayItem(stack)) {
			this.items.set(0, ItemStack.EMPTY);
		}
	}

	@Override
	public void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, this.items);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public net.minecraft.nbt.CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return this.saveWithoutMetadata(registries);
	}

	@Override
	public int getContainerSize() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return this.items.get(0).isEmpty();
	}

	@Override
	public ItemStack getItem(int slot) {
		return slot == 0 ? this.items.get(0) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		if (slot != 0) {
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
		if (slot != 0) {
			return ItemStack.EMPTY;
		}

		return ContainerHelper.takeItem(this.items, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (slot != 0) {
			return;
		}

		if (stack.isEmpty()) {
			this.items.set(0, ItemStack.EMPTY);
		} else {
			ItemStack single = stack.copyWithCount(1);

			if (!this.canAcceptDisplayItem(single)) {
				return;
			}

			this.items.set(0, single);
		}

		this.setChangedAndSync();
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return slot == 0 && this.canAcceptDisplayItem(stack);
	}

	@Override
	public int getMaxStackSize(ItemStack stack) {
		return 1;
	}

	@Override
	public void clearContent() {
		this.items.set(0, ItemStack.EMPTY);
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
}