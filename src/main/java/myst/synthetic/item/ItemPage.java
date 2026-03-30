package myst.synthetic.item;

import myst.synthetic.page.Page;
import myst.synthetic.page.symbol.PageSymbol;
import myst.synthetic.page.symbol.PageSymbolRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemPage extends Item {

    public ItemPage(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        if (Page.isLinkPanel(stack)) {
            return Component.translatable("item.mystcraft-sc.page.panel");
        }

        Identifier symbolId = Page.getSymbol(stack);
        if (symbolId != null) {
            PageSymbol symbol = PageSymbolRegistry.get(symbolId);

            if (symbol != null) {
                return Component.translatable(
                        "item.mystcraft-sc.page.symbol_named",
                        Component.translatable(symbol.translationKey())
                );
            }

            return Component.translatable("item.mystcraft-sc.page.symbol", symbolId.toString());
        }

        return Component.translatable("item.mystcraft-sc.page.blank");
    }
}