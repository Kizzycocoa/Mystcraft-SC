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
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.List;

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

		String title = pendingTitle == null ? "" : pendingTitle.trim();

		agebook.set(MystcraftDataComponents.AGEBOOK_DATA, new AgebookDataComponent(cleanedPages, author));

		if (!title.isBlank()) {
			agebook.set(DataComponents.CUSTOM_NAME, Component.literal(title));
		} else {
			agebook.remove(DataComponents.CUSTOM_NAME);
		}

		CompoundTag tag = new CompoundTag();
		tag.putString("Author", author);
		tag.putString("AgeName", title);
		tag.putString("DisplayName", title);
		tag.putFloat("damage", 0.0F);
		tag.putFloat("MaxHealth", 10.0F);
		tag.put("Flags", new CompoundTag());
		tag.put("Props", new CompoundTag());

		if (!cleanedPages.isEmpty() && Page.isLinkPanel(cleanedPages.get(0))) {
			applyLinkPanel(cleanedPages.get(0), tag);
		}

		agebook.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static AgebookDataComponent getData(ItemStack stack) {
		return stack.getOrDefault(MystcraftDataComponents.AGEBOOK_DATA, AgebookDataComponent.EMPTY);
	}

	private static void applyLinkPanel(ItemStack panel, CompoundTag tag) {
		for (String property : Page.getLinkProperties(panel)) {
			LinkOptions.setFlag(tag, property, true);
		}

		if (!LinkOptions.getFlag(tag, LinkPropertyAPI.FLAG_GENERATE_PLATFORM)) {
			LinkOptions.setFlag(tag, LinkPropertyAPI.FLAG_GENERATE_PLATFORM, true);
		}
	}
}