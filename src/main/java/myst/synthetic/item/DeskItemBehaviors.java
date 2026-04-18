package myst.synthetic.item;

import myst.synthetic.MystcraftItems;
import myst.synthetic.component.FolderDataComponent;
import myst.synthetic.component.PortfolioDataComponent;
import myst.synthetic.linking.LinkController;
import myst.synthetic.linking.LinkOptions;
import myst.synthetic.page.Page;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class DeskItemBehaviors {

    private DeskItemBehaviors() {
    }

    public static boolean canBeDeskTarget(ItemStack stack) {
        if (stack.isEmpty() || stack.getCount() != 1) {
            return false;
        }

        return stack.is(MystcraftItems.PAGE)
                || stack.is(MystcraftItems.FOLDER)
                || stack.is(MystcraftItems.PORTFOLIO)
                || stack.is(MystcraftItems.LINKBOOK)
                || stack.is(MystcraftItems.AGEBOOK)
                || stack.is(Items.WRITABLE_BOOK)
                || stack.is(Items.WRITTEN_BOOK);
    }

    public static boolean canBeDeskTabStorage(ItemStack stack) {
        if (stack.isEmpty() || stack.getCount() != 1) {
            return false;
        }

        return stack.is(MystcraftItems.FOLDER)
                || stack.is(MystcraftItems.PORTFOLIO)
                || stack.is(MystcraftItems.PAGE)
                || stack.is(MystcraftItems.LINKBOOK)
                || stack.is(MystcraftItems.AGEBOOK)
                || stack.is(Items.WRITABLE_BOOK)
                || stack.is(Items.WRITTEN_BOOK);
    }

    public static boolean canAcceptPaper(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        return stack.is(MystcraftItems.PAGE)
                || stack.is(MystcraftItems.FOLDER)
                || stack.is(MystcraftItems.PORTFOLIO);
    }

    public static boolean canAcceptWrittenSymbol(ItemStack stack) {
        return canBeDeskTarget(stack) && !stack.is(MystcraftItems.LINKBOOK) && !stack.is(MystcraftItems.AGEBOOK);
    }

    public static String getDisplayName(Player player, ItemStack stack) {
        Component custom = stack.get(DataComponents.CUSTOM_NAME);
        if (custom != null) {
            return custom.getString();
        }
        return "";
    }

    public static void setDisplayName(Player player, ItemStack stack, String title) {
        if (stack.isEmpty()) {
            return;
        }

        if (title == null || title.isBlank()) {
            stack.remove(DataComponents.CUSTOM_NAME);
        } else {
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(title.trim()));
        }

        if (stack.is(MystcraftItems.FOLDER)) {
            ItemFolder.syncFolderStackState(stack);
        }
    }

    public static List<ItemStack> getPages(Player player, ItemStack stack) {
        List<ItemStack> out = new ArrayList<>();

        if (stack.isEmpty()) {
            return out;
        }

        if (stack.is(MystcraftItems.PAGE)) {
            out.add(stack.copyWithCount(1));
            return out;
        }

        if (stack.is(MystcraftItems.FOLDER)) {
            NonNullList<ItemStack> slots = ItemFolder.createInventory(stack);
            for (ItemStack entry : slots) {
                if (!entry.isEmpty() && entry.is(MystcraftItems.PAGE)) {
                    out.add(entry.copyWithCount(1));
                }
            }
            return out;
        }

        if (stack.is(MystcraftItems.PORTFOLIO)) {
            out.addAll(ItemPortfolio.getData(stack).pagesCopy());
            return out;
        }

        return out;
    }

    public static ItemStack insertPage(Player player, ItemStack storage, ItemStack page, int index) {
        if (storage.isEmpty() || page.isEmpty() || !page.is(MystcraftItems.PAGE)) {
            return page;
        }

        ItemStack single = page.copyWithCount(1);

        if (storage.is(MystcraftItems.PAGE)) {
            if (Page.isBlank(storage)) {
                storage.applyComponents(single.getComponentsPatch());
                return ItemStack.EMPTY;
            }
            return page;
        }

        if (storage.is(MystcraftItems.FOLDER)) {
            NonNullList<ItemStack> slots = ItemFolder.createInventory(storage);
            int target = Math.max(0, Math.min(index, FolderDataComponent.MAX_SLOTS - 1));
            ItemStack existing = slots.get(target);
            slots.set(target, single);
            ItemFolder.saveInventory(storage, slots);
            return existing.isEmpty() ? ItemStack.EMPTY : existing;
        }

        if (storage.is(MystcraftItems.PORTFOLIO)) {
            PortfolioDataComponent data = ItemPortfolio.getData(storage);
            List<ItemStack> pages = data.pagesCopy();
            int target = Math.max(0, Math.min(index, pages.size()));
            pages.add(target, single);
            ItemPortfolio.setData(storage, new PortfolioDataComponent(pages));
            return ItemStack.EMPTY;
        }

        return page;
    }

    public static ItemStack addPage(Player player, ItemStack storage, ItemStack page) {
        if (storage.isEmpty() || page.isEmpty() || !page.is(MystcraftItems.PAGE)) {
            return page;
        }

        if (storage.is(MystcraftItems.PORTFOLIO)) {
            ItemPortfolio.setData(storage, ItemPortfolio.getData(storage).add(page));
            return ItemStack.EMPTY;
        }

        if (storage.is(MystcraftItems.FOLDER)) {
            NonNullList<ItemStack> slots = ItemFolder.createInventory(storage);
            for (int i = 0; i < slots.size(); i++) {
                if (slots.get(i).isEmpty()) {
                    slots.set(i, page.copyWithCount(1));
                    ItemFolder.saveInventory(storage, slots);
                    return ItemStack.EMPTY;
                }
            }
        }

        return insertPage(player, storage, page, getPages(player, storage).size());
    }

    public static ItemStack removePage(Player player, ItemStack storage, int index) {
        if (storage.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (storage.is(MystcraftItems.PAGE)) {
            if (index != 0) {
                return ItemStack.EMPTY;
            }
            ItemStack extracted = storage.copyWithCount(1);
            Page.clearPageData(storage);
            storage.remove(DataComponents.CUSTOM_NAME);
            return extracted;
        }

        if (storage.is(MystcraftItems.FOLDER)) {
            NonNullList<ItemStack> slots = ItemFolder.createInventory(storage);
            if (index < 0 || index >= slots.size()) {
                return ItemStack.EMPTY;
            }

            ItemStack entry = slots.get(index);
            if (entry.isEmpty()) {
                return ItemStack.EMPTY;
            }

            slots.set(index, ItemStack.EMPTY);
            ItemFolder.saveInventory(storage, slots);
            return entry;
        }

        if (storage.is(MystcraftItems.PORTFOLIO)) {
            PortfolioDataComponent data = ItemPortfolio.getData(storage);
            ItemStack page = data.getPage(index);
            if (page.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemPortfolio.setData(storage, data.removeAt(index));
            return page;
        }

        return ItemStack.EMPTY;
    }

    public static ItemStack removeMatchingPage(Player player, ItemStack storage, ItemStack page) {
        if (storage.isEmpty() || page.isEmpty() || !page.is(MystcraftItems.PAGE)) {
            return ItemStack.EMPTY;
        }

        List<ItemStack> pages = getPages(player, storage);
        for (int i = 0; i < pages.size(); i++) {
            if (ItemStack.isSameItemSameComponents(pages.get(i), page)) {
                return removePage(player, storage, i);
            }
        }

        return ItemStack.EMPTY;
    }

    public static boolean writeSymbol(Player player, ItemStack target, Identifier symbol) {
        if (target.isEmpty()) {
            return false;
        }

        // Direct write only applies to a literal page already in the target slot.
        if (target.is(MystcraftItems.PAGE)) {
            Page.setSymbol(target, symbol);
            return true;
        }

        // Folder/portfolio/book acceptor cases must be handled by the desk block entity,
        // because that path is what consumes paper + ink correctly.
        return false;
    }

    public static boolean activateLink(Level level, Player player, ItemStack stack) {
        if (level.isClientSide() || stack.isEmpty() || !stack.is(MystcraftItems.LINKBOOK)) {
            return false;
        }

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return false;
        }

        CompoundTag tag = customData.copyTag();
        LinkOptions info = new LinkOptions(tag);
        String targetDimension = info.getDimensionUID();
        if (targetDimension == null || targetDimension.isBlank()) {
            return false;
        }

        String current = player.level().dimension().toString();
        if (current.equals(targetDimension)) {
            return false;
        }

        return LinkController.travelEntity(level, player, info);
    }
}
