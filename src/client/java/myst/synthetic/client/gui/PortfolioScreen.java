package myst.synthetic.client.gui;

import myst.synthetic.menu.PortfolioMenu;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class PortfolioScreen extends PageBrowserScreen<PortfolioMenu> {

    public PortfolioScreen(PortfolioMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void buildOwnedEntries(List<DisplayEntry> built, String normalizedSearch) {
        for (int i = 0; i < this.menu.getStoredCount(); i++) {
            ItemStack stack = this.menu.getPortfolioData().getPage(i);
            if (stack.isEmpty()) {
                continue;
            }

            String searchName = this.getSearchName(stack);
            if (!normalizedSearch.isEmpty() && !normalize(searchName).contains(normalizedSearch)) {
                continue;
            }

            DisplayEntry existing = null;
            for (DisplayEntry entry : built) {
                if (entry.absoluteIndex >= 0 && ItemStack.isSameItemSameComponents(entry.stack, stack)) {
                    existing = entry;
                    break;
                }
            }

            if (existing != null) {
                existing.count++;
            } else {
                built.add(new DisplayEntry(stack.copy(), i, 1, searchName));
            }
        }
    }

    @Override
    protected void onSurfaceClicked(DisplayEntry entry, MouseButtonEvent event) {
        if (entry.absoluteIndex < 0 || this.minecraft == null || this.minecraft.gameMode == null) {
            return;
        }

        this.minecraft.gameMode.handleInventoryButtonClick(
                this.menu.containerId,
                PortfolioMenu.BUTTON_TAKE_ABSOLUTE_START + entry.absoluteIndex
        );
    }

    @Override
    protected boolean allowShowAllSymbols() {
        return true;
    }
}