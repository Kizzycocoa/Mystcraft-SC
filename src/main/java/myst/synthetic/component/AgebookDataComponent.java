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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public record AgebookDataComponent(
        List<ItemStack> pages,
        List<String> authors,
        String displayName,
        @Nullable String dimensionUid,
        @Nullable String targetUuid,
        @Nullable Long seed
) implements TooltipProvider {

    public static final AgebookDataComponent EMPTY = new AgebookDataComponent(List.of(), List.of(), "", null, null, null);

    public static final Codec<AgebookDataComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.listOf().optionalFieldOf("pages", List.of()).forGetter(AgebookDataComponent::pages),
            Codec.STRING.listOf().optionalFieldOf("authors", List.of()).forGetter(AgebookDataComponent::authors),
            Codec.STRING.optionalFieldOf("display_name", "").forGetter(AgebookDataComponent::displayName),
            Codec.STRING.optionalFieldOf("dimension_uid").forGetter(component -> Optional.ofNullable(component.dimensionUid())),
            Codec.STRING.optionalFieldOf("target_uuid").forGetter(component -> Optional.ofNullable(component.targetUuid())),
            Codec.LONG.optionalFieldOf("seed").forGetter(component -> Optional.ofNullable(component.seed()))
    ).apply(instance, (pages, authors, displayName, dimensionUid, targetUuid, seed) ->
            new AgebookDataComponent(
                    pages,
                    authors,
                    displayName,
                    dimensionUid.orElse(null),
                    targetUuid.orElse(null),
                    seed.orElse(null)
            )
    ));

    public AgebookDataComponent {
        pages = sanitizePages(pages);
        authors = sanitizeAuthors(authors);
        displayName = displayName == null ? "" : displayName.trim();
        dimensionUid = normalizeString(dimensionUid);
        targetUuid = normalizeString(targetUuid);
    }

    public List<ItemStack> pagesCopy() {
        List<ItemStack> out = new ArrayList<>();
        for (ItemStack stack : this.pages) {
            out.add(stack.copy());
        }
        return out;
    }

    public List<String> authorsCopy() {
        return List.copyOf(this.authors);
    }

    public int size() {
        return this.pages.size();
    }

    public boolean isBound() {
        return this.dimensionUid != null && !this.dimensionUid.isBlank();
    }

    public ItemStack getPage(int index) {
        if (index < 0 || index >= this.pages.size()) {
            return ItemStack.EMPTY;
        }
        return this.pages.get(index).copy();
    }

    public AgebookDataComponent withBinding(@Nullable String dimensionUid, @Nullable String targetUuid, @Nullable Long seed) {
        return new AgebookDataComponent(this.pages, this.authors, this.displayName, dimensionUid, targetUuid, seed);
    }

    public AgebookDataComponent withDisplayName(String displayName) {
        return new AgebookDataComponent(this.pages, this.authors, displayName, this.dimensionUid, this.targetUuid, this.seed);
    }

    public AgebookDataComponent withAuthors(List<String> authors) {
        return new AgebookDataComponent(this.pages, authors, this.displayName, this.dimensionUid, this.targetUuid, this.seed);
    }

    private static List<ItemStack> sanitizePages(List<ItemStack> source) {
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
        return List.copyOf(cleaned);
    }

    private static List<String> sanitizeAuthors(List<String> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<String> cleaned = new LinkedHashSet<>();
        for (String author : source) {
            if (author != null) {
                String trimmed = author.trim();
                if (!trimmed.isBlank()) {
                    cleaned.add(trimmed);
                }
            }
        }

        return List.copyOf(cleaned);
    }

    @Nullable
    private static String normalizeString(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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

        if (!this.authors.isEmpty()) {
            if (this.authors.size() == 1) {
                textConsumer.accept(
                        Component.literal("Author: " + this.authors.get(0))
                                .withStyle(ChatFormatting.DARK_GRAY)
                );
            } else {
                textConsumer.accept(
                        Component.literal("Authors: " + String.join(", ", this.authors))
                                .withStyle(ChatFormatting.DARK_GRAY)
                );
            }
        }

        if (!this.displayName.isBlank()) {
            textConsumer.accept(
                    Component.literal("Age: " + this.displayName)
                            .withStyle(ChatFormatting.DARK_AQUA)
            );
        }

        if (this.dimensionUid != null) {
            textConsumer.accept(
                    Component.literal("Dimension: " + this.dimensionUid)
                            .withStyle(ChatFormatting.BLUE)
            );
        }

        if (this.targetUuid != null) {
            textConsumer.accept(
                    Component.literal("UUID: " + this.targetUuid)
                            .withStyle(ChatFormatting.DARK_BLUE)
            );
        }

        if (this.seed != null) {
            textConsumer.accept(
                    Component.literal("Seed: " + this.seed)
                            .withStyle(ChatFormatting.DARK_GREEN)
            );
        }
    }
}