package myst.synthetic.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import myst.synthetic.MystcraftItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public record PortfolioDataComponent(List<ItemStack> pages) implements TooltipProvider {

    public static final PortfolioDataComponent EMPTY =
            new PortfolioDataComponent(List.of());

    public static final Codec<PortfolioDataComponent> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    ItemStack.CODEC.listOf()
                            .optionalFieldOf("pages", List.of())
                            .forGetter(PortfolioDataComponent::pages)
            ).apply(instance, PortfolioDataComponent::new));

    public PortfolioDataComponent {
        pages = sanitize(pages);
    }

    public boolean isEmpty() {
        return this.pages.isEmpty();
    }

    public int size() {
        return this.pages.size();
    }

    public List<ItemStack> pagesCopy() {
        List<ItemStack> copy = new ArrayList<>();

        for (ItemStack stack : this.pages) {
            copy.add(stack.copy());
        }

        return copy;
    }

    public PortfolioDataComponent add(ItemStack stack) {
        if (!stack.is(MystcraftItems.PAGE)) {
            return this;
        }

        List<ItemStack> newPages = this.pagesCopy();
        newPages.add(stack.copyWithCount(1));

        return new PortfolioDataComponent(newPages);
    }

    public PortfolioDataComponent remove(ItemStack stack) {
        List<ItemStack> newPages = this.pagesCopy();

        for (int i = 0; i < newPages.size(); i++) {
            if (ItemStack.isSameItemSameComponents(newPages.get(i), stack)) {
                newPages.remove(i);
                break;
            }
        }

        return new PortfolioDataComponent(newPages);
    }

    public PortfolioDataComponent removeAt(int index) {
        if (index < 0 || index >= this.pages.size()) {
            return this;
        }

        List<ItemStack> newPages = this.pagesCopy();
        newPages.remove(index);
        return new PortfolioDataComponent(newPages);
    }

    public ItemStack getPage(int index) {
        if (index < 0 || index >= this.pages.size()) {
            return ItemStack.EMPTY;
        }

        return this.pages.get(index).copy();
    }

    public ItemStack getLastPage() {
        if (this.pages.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return this.pages.get(this.pages.size() - 1).copy();
    }

    private static List<ItemStack> sanitize(List<ItemStack> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }

        List<ItemStack> cleaned = new ArrayList<>();

        for (ItemStack stack : source) {
            if (!stack.is(MystcraftItems.PAGE)) {
                continue;
            }

            cleaned.add(stack.copyWithCount(1));
        }

        return cleaned;
    }

    @Override
    public void addToTooltip(
            Item.TooltipContext ctx,
            Consumer<Component> tooltip,
            net.minecraft.world.item.TooltipFlag flag,
            DataComponentGetter components
    ) {
        if (this.pages.isEmpty()) {
            tooltip.accept(
                    Component.translatable("tooltip.mystcraft-sc.portfolio.empty")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            return;
        }

        tooltip.accept(
                Component.translatable("tooltip.mystcraft-sc.portfolio.count", this.pages.size())
                        .withStyle(ChatFormatting.GRAY)
        );

        int previewCount = Math.min(5, this.pages.size());
        for (int i = 0; i < previewCount; i++) {
            tooltip.accept(this.pages.get(i).getHoverName());
        }
    }
}