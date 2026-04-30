package myst.synthetic.item;

import myst.synthetic.MystcraftItems;
import myst.synthetic.component.FolderDataComponent;
import myst.synthetic.page.Page;
import myst.synthetic.page.symbol.PageSymbol;
import myst.synthetic.page.symbol.PageSymbolRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class ItemBooster extends Item {

	private static final int RANK_6_CHANCE = 15;

	public ItemBooster(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack boosterStack = player.getItemInHand(hand);

		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.SUCCESS;
		}

		ItemStack folder = generateBoosterFolder(serverLevel.getRandom());

		if (!player.getAbilities().instabuild) {
			boosterStack.shrink(1);
		}

		if (boosterStack.isEmpty()) {
			player.setItemInHand(hand, folder);
		} else if (!player.getInventory().add(folder)) {
			player.drop(folder, false);
		}

		return InteractionResult.CONSUME;
	}

	private static ItemStack generateBoosterFolder(RandomSource random) {
		ItemStack folder = new ItemStack(MystcraftItems.FOLDER);

		NonNullList<ItemStack> slots = NonNullList.withSize(FolderDataComponent.MAX_SLOTS, ItemStack.EMPTY);

		int slot = 0;

		boolean includeRank6 = random.nextInt(RANK_6_CHANCE) == 0;

		int rank1Count = includeRank6 ? 6 : 7;

		slot = addRankPages(slots, slot, random, 1, rank1Count);

		if (includeRank6) {
			slot = addRankPages(slots, slot, random, 6, 1);

			/*
			 * If no rank 6 pages currently exist, avoid leaving the booster one page short.
			 */
			if (countFilled(slots) < 7) {
				slot = addRankPages(slots, slot, random, 1, 1);
			}
		}

		slot = addRankPages(slots, slot, random, 2, 5);
		slot = addRankPages(slots, slot, random, 3, 4);
		slot = addRankPages(slots, slot, random, 4, 2);
		addRankPages(slots, slot, random, 5, 1);

		ItemFolder.saveInventory(folder, slots);
		return folder;
	}

	private static int addRankPages(
			NonNullList<ItemStack> slots,
			int slot,
			RandomSource random,
			int rank,
			int count
	) {
		for (int i = 0; i < count && slot < slots.size(); i++) {
			ItemStack page = pickRankPage(random, rank);

			if (page.isEmpty()) {
				continue;
			}

			slots.set(slot, page.copyWithCount(1));
			slot++;
		}

		return slot;
	}

	private static ItemStack pickRankPage(RandomSource random, int rank) {
		List<PageSymbol> matches = PageSymbolRegistry.values().stream()
				.filter(symbol -> symbol.cardRank() == rank)
				.toList();

		if (matches.isEmpty()) {
			return ItemStack.EMPTY;
		}

		PageSymbol picked = matches.get(random.nextInt(matches.size()));
		return Page.createSymbolPage(picked.id());
	}

	private static int countFilled(NonNullList<ItemStack> slots) {
		int count = 0;

		for (ItemStack stack : slots) {
			if (!stack.isEmpty()) {
				count++;
			}
		}

		return count;
	}
}