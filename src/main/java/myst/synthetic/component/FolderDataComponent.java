package myst.synthetic.component;

import com.mojang.serialization.Codec;
import myst.synthetic.MystcraftItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public record FolderDataComponent(
        NonNullList<ItemStack> pages
) implements TooltipProvider {

    public static final int MAX_SLOTS = 27;

    public static final FolderDataComponent EMPTY = new FolderDataComponent(
            NonNullList.withSize(MAX_SLOTS, ItemStack.EMPTY)
    );

    public static final Codec<FolderDataComponent> CODEC =
            ItemStack.CODEC.listOf().xmap(
                    FolderDataComponent::fromList,
                    FolderDataComponent::toList
            );

    public FolderDataComponent {
        pages = sanitize(pages);
    }

    public boolean isEmpty() {
        for (ItemStack stack : this.pages) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public int getStoredCount() {
        int count = 0;

        for (ItemStack stack : this.pages) {
            if (!stack.isEmpty()) {
                count++;
            }
        }

        return count;
    }

    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= MAX_SLOTS) {
            return ItemStack.EMPTY;
        }

        return this.pages.get(slot).copy();
    }

    public NonNullList<ItemStack> toSlotList() {
        NonNullList<ItemStack> slots = NonNullList.withSize(MAX_SLOTS, ItemStack.EMPTY);

        for (int i = 0; i < MAX_SLOTS; i++) {
            slots.set(i, this.pages.get(i).copy());
        }

        return slots;
    }

    public static FolderDataComponent fromSlotList(NonNullList<ItemStack> slots) {
        NonNullList<ItemStack> cleaned = NonNullList.withSize(MAX_SLOTS, ItemStack.EMPTY);

        for (int i = 0; i < Math.min(MAX_SLOTS, slots.size()); i++) {
            ItemStack stack = slots.get(i);

            if (isValidFolderEntry(stack)) {
                cleaned.set(i, stack.copyWithCount(1));
            }
        }

        return new FolderDataComponent(cleaned);
    }

    private static FolderDataComponent fromList(List<ItemStack> list) {
        NonNullList<ItemStack> slots = NonNullList.withSize(MAX_SLOTS, ItemStack.EMPTY);

        for (int i = 0; i < Math.min(list.size(), MAX_SLOTS); i++) {
            ItemStack stack = list.get(i);

            if (isValidFolderEntry(stack)) {
                slots.set(i, stack.copyWithCount(1));
            }
        }

        return new FolderDataComponent(slots);
    }

    private List<ItemStack> toList() {
        List<ItemStack> out = new ArrayList<>(MAX_SLOTS);

        for (int i = 0; i < MAX_SLOTS; i++) {
            out.add(this.pages.get(i).copy());
        }

        return out;
    }

    @Override
    public void addToTooltip(
            Item.TooltipContext tooltipContext,
            Consumer<Component> textConsumer,
            TooltipFlag tooltipFlag,
            DataComponentGetter components
    ) {
        if (this.isEmpty()) {
            textConsumer.accept(
                    Component.translatable("tooltip.mystcraft-sc.folder.empty")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            return;
        }

        textConsumer.accept(
                Component.translatable("tooltip.mystcraft-sc.folder.page_count", this.getStoredCount())
                        .withStyle(ChatFormatting.GRAY)
        );
    }

    private static NonNullList<ItemStack> sanitize(NonNullList<ItemStack> source) {
        NonNullList<ItemStack> cleaned = NonNullList.withSize(MAX_SLOTS, ItemStack.EMPTY);

        if (source == null) {
            return cleaned;
        }

        for (int i = 0; i < Math.min(source.size(), MAX_SLOTS); i++) {
            ItemStack stack = source.get(i);

            if (isValidFolderEntry(stack)) {
                cleaned.set(i, stack.copyWithCount(1));
            }
        }

        return cleaned;
    }

    private static boolean isValidFolderEntry(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        return stack.is(MystcraftItems.PAGE) || stack.is(Items.PAPER);
    }
}