package myst.synthetic.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public record FolderDataComponent(
        Map<Integer, ItemStack> pages
) implements TooltipProvider {

    public static final int MAX_SLOTS = 27;

    public static final FolderDataComponent EMPTY = new FolderDataComponent(Map.of());

    public static final Codec<FolderDataComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.INT, ItemStack.CODEC)
                    .optionalFieldOf("pages", Map.of())
                    .forGetter(FolderDataComponent::pages)
    ).apply(instance, FolderDataComponent::new));

    public FolderDataComponent {
        pages = sanitize(pages);
    }

    public boolean isEmpty() {
        return this.pages.isEmpty();
    }

    public int getStoredCount() {
        return this.pages.size();
    }

    public ItemStack getItem(int slot) {
        ItemStack stack = this.pages.get(slot);
        return stack == null ? ItemStack.EMPTY : stack.copy();
    }

    public NonNullList<ItemStack> toSlotList() {
        NonNullList<ItemStack> slots = NonNullList.withSize(MAX_SLOTS, ItemStack.EMPTY);

        for (Map.Entry<Integer, ItemStack> entry : this.pages.entrySet()) {
            int slot = entry.getKey();
            if (slot >= 0 && slot < MAX_SLOTS) {
                slots.set(slot, entry.getValue().copy());
            }
        }

        return slots;
    }

    public static FolderDataComponent fromSlotList(NonNullList<ItemStack> slots) {
        LinkedHashMap<Integer, ItemStack> pages = new LinkedHashMap<>();

        for (int i = 0; i < Math.min(MAX_SLOTS, slots.size()); i++) {
            ItemStack stack = slots.get(i);
            if (isValidFolderEntry(stack)) {
                pages.put(i, stack.copyWithCount(1));
            }
        }

        return pages.isEmpty() ? EMPTY : new FolderDataComponent(pages);
    }

    @Override
    public void addToTooltip(
            Item.TooltipContext tooltipContext,
            Consumer<Component> textConsumer,
            TooltipFlag type,
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

    private static Map<Integer, ItemStack> sanitize(Map<Integer, ItemStack> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }

        LinkedHashMap<Integer, ItemStack> cleaned = new LinkedHashMap<>();

        source.entrySet().stream()
                .filter(entry -> entry.getKey() != null)
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    int slot = entry.getKey();
                    ItemStack stack = entry.getValue();

                    if (slot < 0 || slot >= MAX_SLOTS) {
                        return;
                    }

                    if (!isValidFolderEntry(stack)) {
                        return;
                    }

                    cleaned.put(slot, stack.copyWithCount(1));
                });

        return cleaned;
    }

    private static boolean isValidFolderEntry(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        return stack.is(MystcraftItems.PAGE) || stack.is(Items.PAPER);
    }
}