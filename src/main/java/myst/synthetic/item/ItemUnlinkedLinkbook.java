package myst.synthetic.item;

import myst.synthetic.MystcraftItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;

public class ItemUnlinkedLinkbook extends Item {

	public ItemUnlinkedLinkbook(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack inHand = player.getItemInHand(hand);

		if (inHand.getCount() > 1) {
			return InteractionResult.PASS;
		}

		ItemStack linkedBook = new ItemStack(MystcraftItems.LINKBOOK);

		CompoundTag newTag = LinkBookData.createLegacyStyleLinkTag(level, player);

		CustomData oldData = inHand.get(DataComponents.CUSTOM_DATA);
		CompoundTag oldTag = oldData != null ? oldData.copyTag() : null;
		if (oldTag != null) {
			LinkBookData.copyPanelFlags(oldTag, newTag);
		}

		linkedBook.set(DataComponents.CUSTOM_DATA, CustomData.of(newTag));
		player.setItemInHand(hand, linkedBook);

		return InteractionResult.SUCCESS;
	}
}