package myst.synthetic.item;

import java.util.Collection;
import org.jetbrains.annotations.Nullable;

import myst.synthetic.page.Page;
import net.minecraft.ChatFormatting;
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

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            net.minecraft.world.item.component.TooltipDisplay tooltipDisplay,
            java.util.function.Consumer<Component> textConsumer,
            net.minecraft.world.item.TooltipFlag flag
    ) {
        Identifier symbol = Page.getSymbol(stack);
        if (symbol != null) {
            textConsumer.accept(
                    Component.translatable("tooltip.mystcraft-sc.page.symbol", symbol.toString())
                            .withStyle(ChatFormatting.GRAY)
            );
        }

        if (Page.isLinkPanel(stack)) {
            textConsumer.accept(
                    Component.translatable("tooltip.mystcraft-sc.page.linkpanel")
                            .withStyle(ChatFormatting.AQUA)
            );

            @Nullable Collection<String> properties = Page.getLinkProperties(stack);
            if (properties != null) {
                for (String property : properties) {
                    textConsumer.accept(Component.literal(" - " + property).withStyle(ChatFormatting.DARK_AQUA));
                }
            }
        }

        Integer quality = Page.getTotalQuality(stack);
        if (quality != null && quality > 0) {
            textConsumer.accept(
                    Component.translatable("tooltip.mystcraft-sc.page.quality", quality)
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
    }
}