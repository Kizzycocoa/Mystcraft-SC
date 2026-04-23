package myst.synthetic.item;

import myst.synthetic.MystcraftItems;
import myst.synthetic.api.hook.LinkPropertyAPI;
import myst.synthetic.block.entity.PlayerNameOnlyStub;
import myst.synthetic.component.AgebookDataComponent;
import myst.synthetic.component.MystcraftDataComponents;
import myst.synthetic.linking.LinkOptions;
import myst.synthetic.page.Page;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemAgebook extends ItemLinkbook {

	public ItemAgebook(Properties properties) {
		super(properties);
	}

	public static void create(ItemStack agebook, Player player, List<ItemStack> pages, String pendingTitle) {
		createInternal(agebook, player.getName().getString(), pages, pendingTitle);
	}

	public static void create(ItemStack agebook, PlayerNameOnlyStub player, List<ItemStack> pages, String pendingTitle) {
		createInternal(agebook, player.getAuthorName(), pages, pendingTitle);
	}

	private static void createInternal(ItemStack agebook, String author, List<ItemStack> pages, String pendingTitle) {
		if (!agebook.is(MystcraftItems.AGEBOOK)) {
			return;
		}

		List<ItemStack> cleanedPages = new ArrayList<>();
		for (ItemStack page : pages) {
			if (page == null || page.isEmpty()) {
				continue;
			}
			cleanedPages.add(page.copyWithCount(1));
		}

		String cleanAuthor = author == null ? "" : author.trim();
		String title = pendingTitle == null ? "" : pendingTitle.trim();

		List<String> authors = cleanAuthor.isBlank() ? List.of() : List.of(cleanAuthor);

		agebook.set(
				MystcraftDataComponents.AGEBOOK_DATA,
				new AgebookDataComponent(
						cleanedPages,
						authors,
						title,
						null,
						null,
						null
				)
		);

		if (!title.isBlank()) {
			agebook.set(DataComponents.CUSTOM_NAME, Component.literal(title));
		} else {
			agebook.remove(DataComponents.CUSTOM_NAME);
		}

		CompoundTag tag = new CompoundTag();
		tag.putString("Author", cleanAuthor);
		tag.putString("AgeName", title);
		tag.putString("DisplayName", title);
		tag.putFloat("damage", 0.0F);
		tag.putFloat("MaxHealth", 10.0F);
		tag.put("Flags", new CompoundTag());
		tag.put("Props", new CompoundTag());

		if (!authors.isEmpty()) {
			ListTag authorList = new ListTag();
			for (String entry : authors) {
				authorList.add(StringTag.valueOf(entry));
			}
			tag.put("Authors", authorList);
		}

		if (!cleanedPages.isEmpty() && Page.isLinkPanel(cleanedPages.get(0))) {
			applyLinkPanel(cleanedPages.get(0), tag);
		}

		agebook.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static void bindToGeneratedAge(
			ItemStack agebook,
			String dimensionUid,
			UUID targetUuid,
			long seed,
			@Nullable String ageName
	) {
		if (!agebook.is(MystcraftItems.AGEBOOK)) {
			return;
		}

		AgebookDataComponent current = getData(agebook);
		String resolvedName = ageName == null ? current.displayName() : ageName.trim();

		agebook.set(
				MystcraftDataComponents.AGEBOOK_DATA,
				new AgebookDataComponent(
						current.pagesCopy(),
						current.authorsCopy(),
						resolvedName,
						dimensionUid,
						targetUuid == null ? null : targetUuid.toString(),
						seed
				)
		);

		CompoundTag tag = getOrCreateBookTag(agebook);
		LinkOptions.setDimensionUID(tag, dimensionUid);
		LinkOptions.setUUID(tag, targetUuid);
		LinkOptions.setProperty(tag, "Seed", Long.toString(seed));

		if (!resolvedName.isBlank()) {
			tag.putString("AgeName", resolvedName);
			tag.putString("DisplayName", resolvedName);
			agebook.set(DataComponents.CUSTOM_NAME, Component.literal(resolvedName));
		}

		agebook.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static AgebookDataComponent getData(ItemStack stack) {
		return stack.getOrDefault(MystcraftDataComponents.AGEBOOK_DATA, AgebookDataComponent.EMPTY);
	}

	public static List<ItemStack> getPages(ItemStack stack) {
		return getData(stack).pagesCopy();
	}

	public static List<String> getAuthors(ItemStack stack) {
		return getData(stack).authorsCopy();
	}

	public static boolean isNewAgebook(ItemStack stack) {
		if (!stack.is(MystcraftItems.AGEBOOK)) {
			return false;
		}

		AgebookDataComponent data = getData(stack);
		if (data.isBound()) {
			return false;
		}

		return !data.pages().isEmpty() && Page.isLinkPanel(data.pages().get(0));
	}

	private static void applyLinkPanel(ItemStack panel, CompoundTag tag) {
		for (String property : Page.getLinkProperties(panel)) {
			LinkOptions.setFlag(tag, property, true);
		}

		if (!LinkOptions.getFlag(tag, LinkPropertyAPI.FLAG_GENERATE_PLATFORM)) {
			LinkOptions.setFlag(tag, LinkPropertyAPI.FLAG_GENERATE_PLATFORM, true);
		}
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