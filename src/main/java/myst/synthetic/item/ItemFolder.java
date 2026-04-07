package myst.synthetic.item;

import myst.synthetic.MystcraftItems;
import myst.synthetic.component.FolderDataComponent;
import myst.synthetic.component.MystcraftDataComponents;
import myst.synthetic.menu.FolderMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class ItemFolder extends Item {

	private static final int EMPTY_STACK_SIZE = 32;
	private static final int SINGLE_STACK_SIZE = 1;

	private static final Identifier FILLED_FOLDER_MODEL =
			Identifier.fromNamespaceAndPath("mystcraft-sc", "folder_filled");

	public ItemFolder(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (hand != InteractionHand.MAIN_HAND) {
			return InteractionResult.PASS;
		}

		ItemStack stack = player.getItemInHand(hand);

		if (stack.getCount() != 1) {
			return InteractionResult.PASS;
		}

		if (!level.isClientSide()) {
			int hostSlot = player.getInventory().getSelectedSlot();

			player.openMenu(new SimpleMenuProvider(
					(containerId, playerInventory, ignored) -> new FolderMenu(containerId, playerInventory, hostSlot),
					stack.getHoverName()
			));
		}

		return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
	}

	public static boolean canStore(ItemStack stack) {
		return !stack.isEmpty() && (stack.is(MystcraftItems.PAGE) || stack.is(Items.PAPER));
	}

	public static FolderDataComponent getFolderData(ItemStack stack) {
		return stack.getOrDefault(MystcraftDataComponents.FOLDER_DATA, FolderDataComponent.EMPTY);
	}

	public static NonNullList<ItemStack> createInventory(ItemStack stack) {
		return getFolderData(stack).toSlotList();
	}

	public static void saveInventory(ItemStack stack, NonNullList<ItemStack> slots) {
		FolderDataComponent data = FolderDataComponent.fromSlotList(slots);
		stack.set(MystcraftDataComponents.FOLDER_DATA, data);
		syncFolderStackState(stack);
	}

	public static void syncFolderStackState(ItemStack stack) {
		if (!stack.is(MystcraftItems.FOLDER)) {
			return;
		}

		FolderDataComponent data = getFolderData(stack);
		boolean filled = !data.isEmpty();
		boolean singleStack = filled || stack.has(DataComponents.CUSTOM_NAME);

		stack.set(
				DataComponents.MAX_STACK_SIZE,
				singleStack ? SINGLE_STACK_SIZE : EMPTY_STACK_SIZE
		);

		if (filled) {
			stack.set(DataComponents.ITEM_MODEL, FILLED_FOLDER_MODEL);
		} else {
			stack.remove(DataComponents.ITEM_MODEL);
		}
	}
}