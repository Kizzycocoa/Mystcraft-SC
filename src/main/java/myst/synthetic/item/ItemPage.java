package myst.synthetic.item;

import myst.synthetic.page.Page;
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

        Identifier symbol = Page.getSymbol(stack);
        if (symbol != null) {
            return Component.translatable("item.mystcraft-sc.page.symbol", symbol.toString());
        }

        return Component.translatable("item.mystcraft-sc.page.blank");
    }
}