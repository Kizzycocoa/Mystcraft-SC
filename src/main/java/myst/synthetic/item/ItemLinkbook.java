package myst.synthetic.item;

import myst.synthetic.LinkBookClientBridge;
import myst.synthetic.MystcraftEntities;
import myst.synthetic.MystcraftItems;
import myst.synthetic.entity.EntityLinkbook;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ItemLinkbook extends Item {

	public ItemLinkbook(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (level.isClientSide()) {
			LinkBookClientBridge.open(stack.copy());
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	public boolean overrideOtherStackedOnMe(
			ItemStack bookStack,
			ItemStack incomingStack,
			Slot slot,
			ClickAction clickAction,
			Player player,
			SlotAccess carriedAccess
	) {
		if (clickAction != ClickAction.SECONDARY) {
			return false;
		}

		if (!bookStack.is(MystcraftItems.LINKBOOK)) {
			return false;
		}

		if (!incomingStack.is(MystcraftItems.BOOKMARK)) {
			return false;
		}

		ItemStack incomingSingle = incomingStack.copyWithCount(1);
		ItemStack existingBookmark = BookBookmarkUtil.getBookmark(bookStack);

		if (!existingBookmark.isEmpty() && BookBookmarkUtil.sameBookmark(existingBookmark, incomingSingle)) {
			return true;
		}

		BookBookmarkUtil.setBookmark(bookStack, incomingSingle);
		slot.setChanged();

		if (existingBookmark.isEmpty()) {
			if (incomingStack.getCount() == 1) {
				carriedAccess.set(ItemStack.EMPTY);
			} else {
				ItemStack reduced = incomingStack.copy();
				reduced.shrink(1);
				carriedAccess.set(reduced);
			}
			return true;
		}

		if (incomingStack.getCount() == 1) {
			carriedAccess.set(existingBookmark);
			return true;
		}

		ItemStack reduced = incomingStack.copy();
		reduced.shrink(1);
		carriedAccess.set(reduced);

		if (!player.getInventory().add(existingBookmark)) {
			player.drop(existingBookmark, false);
		}

		return true;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		Level level = context.getLevel();
		ItemStack stack = context.getItemInHand();

		if (player == null || !player.isShiftKeyDown()) {
			return InteractionResult.PASS;
		}

		BlockPos placePos = context.getClickedPos().relative(context.getClickedFace());
		Vec3 spawnPos = Vec3.atBottomCenterOf(placePos);

		AABB bounds = MystcraftEntities.LINKBOOK.getDimensions().makeBoundingBox(
				spawnPos.x,
				spawnPos.y,
				spawnPos.z
		);

		if (!level.noCollision(bounds)) {
			return InteractionResult.FAIL;
		}

		if (!level.isClientSide()) {
			EntityLinkbook entity = new EntityLinkbook(MystcraftEntities.LINKBOOK, level);
			entity.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
			entity.setYRot(player.getYRot());
			entity.setXRot(0.0F);
			entity.setYHeadRot(player.getYRot());
			entity.setYBodyRot(player.getYRot());
			entity.setBook(stack.copyWithCount(1));
			level.addFreshEntity(entity);

			if (!player.getAbilities().instabuild) {
				stack.shrink(1);
			}
		}

		return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
	}

	public static void setHealth(ItemStack book, float health) {
		if (book.isEmpty()) {
			return;
		}

		CompoundTag tag = getOrCreateBookTag(book);
		tag.putFloat("damage", getMaxHealth(book) - health);
		book.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static float getHealth(ItemStack book) {
		float health = getMaxHealth(book);

		if (book.isEmpty()) {
			return health;
		}

		CompoundTag tag = getBookTag(book);
		if (tag == null) {
			return health;
		}

		float damage = tag.getFloat("damage").orElse(0.0F);
		return health - damage;
	}

	public static float getMaxHealth(ItemStack book) {
		float health = 10.0F;

		if (book.isEmpty()) {
			return health;
		}

		CompoundTag tag = getOrCreateBookTag(book);
		if (!tag.contains("MaxHealth")) {
			tag.putFloat("MaxHealth", health);
			book.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
			return health;
		}

		return tag.getFloat("MaxHealth").orElse(health);
	}

	private static CompoundTag getBookTag(ItemStack book) {
		CustomData customData = book.get(DataComponents.CUSTOM_DATA);
		return customData != null ? customData.copyTag() : null;
	}

	private static CompoundTag getOrCreateBookTag(ItemStack book) {
		CompoundTag tag = getBookTag(book);
		return tag != null ? tag : new CompoundTag();
	}
}