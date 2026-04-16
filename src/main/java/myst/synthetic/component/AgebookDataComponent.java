package myst.synthetic.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public record AgebookDataComponent(
        List<ItemStack> pages,
        String author
) implements TooltipProvider {

    public static final AgebookDataComponent EMPTY = new AgebookDataComponent(List.of(), "");

    public static final Codec<AgebookDataComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.listOf().optionalFieldOf("pages", List.of()).forGetter(AgebookDataComponent::pages),
            Codec.STRING.optionalFieldOf("author", "").forGetter(AgebookDataComponent::author)
    ).apply(instance, AgebookDataComponent::new));

    public AgebookDataComponent {
        pages = sanitize(pages);
        author = author == null ? "" : author;
    }

    public List<ItemStack> pagesCopy() {
        List<ItemStack> out = new ArrayList<>();
        for (ItemStack stack : this.pages) {
            out.add(stack.copy());
        }
        return out;
    }

    public int size() {
        return this.pages.size();
    }

    public ItemStack getPage(int index) {
        if (index < 0 || index >= this.pages.size()) {
            return ItemStack.EMPTY;
        }
        return this.pages.get(index).copy();
    }

    private static List<ItemStack> sanitize(List<ItemStack> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }

        List<ItemStack> cleaned = new ArrayList<>();
        for (ItemStack stack : source) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            cleaned.add(stack.copyWithCount(1));
        }
        return cleaned;
    }

    @Override
    public void addToTooltip(
            Item.TooltipContext tooltipContext,
            Consumer<Component> textConsumer,
            TooltipFlag tooltipFlag,
            DataComponentGetter components
    ) {
        textConsumer.accept(
                Component.literal("Pages: " + this.pages.size())
                        .withStyle(ChatFormatting.GRAY)
        );

        if (!this.author.isBlank()) {
            textConsumer.accept(
                    Component.literal("Author: " + this.author)
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
    }
}