package myst.synthetic.item;

import java.util.List;
import java.util.function.Consumer;

import myst.synthetic.page.Page;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

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

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag flag) {
        if (Page.isLinkPanel(stack)) {
            List<String> properties = Page.getLinkProperties(stack);

            textConsumer.accept(Component.translatable("tooltip.mystcraft-sc.page.kind.link_panel").withStyle(ChatFormatting.GRAY));

            if (properties.isEmpty()) {
                textConsumer.accept(Component.translatable("tooltip.mystcraft-sc.page.link_properties.none").withStyle(ChatFormatting.DARK_GRAY));
            } else {
                textConsumer.accept(Component.translatable("tooltip.mystcraft-sc.page.link_properties.count", properties.size()).withStyle(ChatFormatting.GRAY));

                for (String property : properties) {
                    textConsumer.accept(Component.literal(" - " + property).withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        } else {
            Identifier symbol = Page.getSymbol(stack);
            if (symbol != null) {
                textConsumer.accept(Component.translatable("tooltip.mystcraft-sc.page.kind.symbol").withStyle(ChatFormatting.GRAY));
                textConsumer.accept(Component.translatable("tooltip.mystcraft-sc.page.symbol_id", symbol.toString()).withStyle(ChatFormatting.DARK_GRAY));
            } else {
                textConsumer.accept(Component.translatable("tooltip.mystcraft-sc.page.kind.blank").withStyle(ChatFormatting.GRAY));
            }
        }

        int totalQuality = Page.getTotalQuality(stack);
        if (totalQuality != 0) {
            textConsumer.accept(Component.translatable("tooltip.mystcraft-sc.page.quality_total", totalQuality).withStyle(ChatFormatting.BLUE));
        }
    }
}