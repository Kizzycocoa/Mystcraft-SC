package myst.synthetic.client.gui;

import myst.synthetic.menu.FolderMenu;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class FolderScreen extends PageBrowserScreen<FolderMenu> {

    public FolderScreen(FolderMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void buildOwnedEntries(List<DisplayEntry> built, String normalizedSearch) {
        List<ItemStack> pages = this.menu.getBrowserItems();

        for (int i = 0; i < pages.size(); i++) {
            ItemStack stack = pages.get(i);

            if (!stack.isEmpty()) {
                String searchName = this.getSearchName(stack);
                if (!normalizedSearch.isEmpty() && !normalize(searchName).contains(normalizedSearch)) {
                    continue;
                }
                built.add(new DisplayEntry(stack.copy(), i, 1, searchName));
            } else {
                built.add(new DisplayEntry(ItemStack.EMPTY, i, 0, ""));
            }
        }

        this.arrangeLinear(built);
    }

    @Override
    protected void onSurfaceClicked(DisplayEntry entry, MouseButtonEvent event) {
        if (entry.absoluteIndex < 0 || this.minecraft == null || this.minecraft.gameMode == null) {
            return;
        }

        this.minecraft.gameMode.handleInventoryButtonClick(
                this.menu.containerId,
                FolderMenu.BUTTON_TAKE_ORDERED_START + entry.absoluteIndex
        );
    }

    @Override
    protected boolean allowShowAllSymbols() {
        return false;
    }
}