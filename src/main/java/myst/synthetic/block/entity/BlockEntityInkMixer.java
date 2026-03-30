package myst.synthetic.block.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import myst.synthetic.MystcraftBlockEntities;
import myst.synthetic.menu.InkMixerMenu;
import myst.synthetic.page.Page;
import myst.synthetic.ink.InkMixerInkSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import myst.synthetic.ink.InkMixerVisuals;

import myst.synthetic.ink.InkMixerEffects;
import myst.synthetic.ink.InkMixerEffects.ConsumeAction;

public class BlockEntityInkMixer extends BlockEntity implements Container, MenuProvider {

	public static final int SLOT_INK_INPUT = 0;
	public static final int SLOT_PAPER = 1;
	public static final int SLOT_CONTAINER_OUTPUT = 2;

	private NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);

	private boolean hasInk = false;
	private final HashMap<String, Float> inkProbabilities = new HashMap<>();
	private long nextSeed = new Random().nextLong();

	private final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int index) {
			int mixedColor = getBasinMixedColor();
			return switch (index) {
				case 0 -> hasInk ? 1 : 0;
				case 1 -> inkProbabilities.size();
				case 2 -> (mixedColor >> 16) & 0xFF;
				case 3 -> (mixedColor >> 8) & 0xFF;
				case 4 -> mixedColor & 0xFF;
				case 5 -> getBasinOverlayAlpha();
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
			if (index == 0) {
				hasInk = value != 0;
			}
		}

		@Override
		public int getCount() {
			return 6;
		}
	};

	public BlockEntityInkMixer(BlockPos pos, BlockState state) {
		super(MystcraftBlockEntities.INK_MIXER, pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityInkMixer blockEntity) {
		if (level.isClientSide()) {
			return;
		}

		blockEntity.tryConsumeInkContainer();
	}

	public ContainerData getDataAccess() {
		return this.dataAccess;
	}

	public boolean hasInk() {
		return this.hasInk;
	}

	public int getStoredPropertyCount() {
		return this.inkProbabilities.size();
	}

	public List<Map.Entry<String, Float>> getSortedProperties() {
		List<Map.Entry<String, Float>> entries = new ArrayList<>(this.inkProbabilities.entrySet());
		entries.sort(Map.Entry.<String, Float>comparingByValue(Comparator.reverseOrder()));
		return entries;
	}

	public boolean canConsumeIngredient(ItemStack stack) {
		return this.hasInk && InkMixerEffects.canConsumeIngredient(stack);
	}

	public boolean consumeIngredient(ItemStack stack, int amount) {
		if (!this.hasInk || stack.isEmpty() || amount <= 0) {
			return false;
		}

		ConsumeAction action = InkMixerEffects.getConsumeAction(stack);
		if (action == ConsumeAction.NONE) {
			return false;
		}

		if (action == ConsumeAction.RESET_TO_PLAIN_INK) {
			this.inkProbabilities.clear();
			this.setChangedAndSync();
			return true;
		}

		ItemStack single = stack.copyWithCount(1);
		Map<String, Float> effects = InkMixerEffects.getItemEffects(single);
		if (effects.isEmpty()) {
			return false;
		}

		for (int i = 0; i < amount; i++) {
			InkMixerEffects.blendInto(this.inkProbabilities, effects);
		}

		this.setChangedAndSync();
		return true;
	}

	public int getBasinMixedColor() {
		if (!this.hasInk) {
			return 0x000000;
		}

		if (this.inkProbabilities.isEmpty()) {
			return 0x101018;
		}

		return InkMixerVisuals.getMixedColor(this.inkProbabilities);
	}

	public int getBasinOverlayAlpha() {
		if (!this.hasInk || this.inkProbabilities.isEmpty()) {
			return 0;
		}

		return InkMixerVisuals.getOverlayAlpha(this.inkProbabilities);
	}

	public boolean canCraftPreview() {
		ItemStack paper = this.items.get(SLOT_PAPER);
		return this.hasInk && paper.is(Items.PAPER) && !paper.isEmpty();
	}

	public ItemStack getPreviewStack() {
		if (!canCraftPreview()) {
			return ItemStack.EMPTY;
		}
		return Page.createLinkPage();
	}

	public void finishCraft(ItemStack stackTaken) {
		if (!canCraftPreview()) {
			return;
		}

		Random random = new Random(this.nextSeed);

		for (Entry<String, Float> entry : this.inkProbabilities.entrySet()) {
			if (random.nextFloat() < entry.getValue()) {
				Page.addLinkProperty(stackTaken, entry.getKey());
			}
		}

		this.nextSeed = random.nextLong();
		this.hasInk = false;
		this.inkProbabilities.clear();

		ItemStack paper = this.items.get(SLOT_PAPER);
		paper.shrink(1);
		if (paper.isEmpty()) {
			this.items.set(SLOT_PAPER, ItemStack.EMPTY);
		}

		this.setChangedAndSync();
	}

	private void tryConsumeInkContainer() {
		if (this.hasInk) {
			return;
		}

		ItemStack input = this.items.get(SLOT_INK_INPUT);
		if (input.isEmpty()) {
			return;
		}

		if (!InkMixerInkSource.isValidInkSource(input)) {
			return;
		}

		ItemStack remaining = InkMixerInkSource.getRemainingContainer(input);
		if (!canOutput(remaining)) {
			return;
		}

		input.shrink(1);
		if (input.isEmpty()) {
			this.items.set(SLOT_INK_INPUT, ItemStack.EMPTY);
		}

		putOutput(remaining);
		this.hasInk = true;
		this.setChangedAndSync();
	}

	private boolean canOutput(ItemStack stack) {
		if (stack.isEmpty()) {
			return true;
		}

		ItemStack output = this.items.get(SLOT_CONTAINER_OUTPUT);
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

		ItemStack output = this.items.get(SLOT_CONTAINER_OUTPUT);
		if (output.isEmpty()) {
			this.items.set(SLOT_CONTAINER_OUTPUT, stack.copy());
			return;
		}

		output.grow(stack.getCount());
	}

	private void setChangedAndSync() {
		this.setChanged();

		if (this.level != null) {
			BlockState state = this.getBlockState();
			this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
		}
	}

	public long getNextSeed() {
		return this.nextSeed;
	}

	@Override
	public void loadAdditional(ValueInput input) {
		super.loadAdditional(input);

		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(input, this.items);

		this.hasInk = input.getBooleanOr("HasInk", false);
		this.nextSeed = input.getLong("NextSeed").orElse(new Random().nextLong());

		this.inkProbabilities.clear();
		int probabilityCount = input.getInt("ProbabilityCount").orElse(0);

		for (int i = 0; i < probabilityCount; i++) {
			String key = input.getString("ProbabilityKey" + i).orElse("");
			float value = input.getFloatOr("ProbabilityValue" + i, 0.0F);

			if (!key.isEmpty()) {
				this.inkProbabilities.put(key, value);
			}
		}
	}

	@Override
	public void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);

		ContainerHelper.saveAllItems(output, this.items);

		output.putBoolean("HasInk", this.hasInk);
		output.putLong("NextSeed", this.nextSeed);

		output.putInt("ProbabilityCount", this.inkProbabilities.size());

		int index = 0;
		for (Entry<String, Float> entry : this.inkProbabilities.entrySet()) {
			output.putString("ProbabilityKey" + index, entry.getKey());
			output.putFloat("ProbabilityValue" + index, entry.getValue());
			index++;
		}
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("container.mystcraft-sc.ink_mixer");
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
		return new InkMixerMenu(containerId, playerInventory, this, this.dataAccess);
	}

	@Override
	public int getContainerSize() {
		return 3;
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
		if (stack.getCount() > this.getMaxStackSize(stack)) {
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
			case SLOT_INK_INPUT -> InkMixerInkSource.isValidInkSource(stack);
			case SLOT_PAPER -> stack.is(Items.PAPER);
			case SLOT_CONTAINER_OUTPUT -> false;
			default -> false;
		};
	}

	@Override
	public void clearContent() {
		this.items.clear();
		this.setChangedAndSync();
	}
}