package myst.synthetic.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public record PageDataComponent(
        String symbolId,
        List<String> linkProperties,
        Map<String, Integer> quality
) implements TooltipProvider {

    public static final PageDataComponent EMPTY = new PageDataComponent(null, List.of(), Map.of());

    public static final Codec<PageDataComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("symbol", "").forGetter(component ->
                    component.symbolId() == null ? "" : component.symbolId()
            ),
            Codec.STRING.listOf().optionalFieldOf("link_properties", List.of()).forGetter(PageDataComponent::linkProperties),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("quality", Map.of()).forGetter(PageDataComponent::quality)
    ).apply(instance, (symbolId, linkProperties, quality) -> {
        String normalizedSymbol = (symbolId == null || symbolId.isBlank()) ? null : symbolId;

        List<String> cleanedProperties = sanitizeProperties(linkProperties);
        Map<String, Integer> cleanedQuality = sanitizeQuality(quality);

        if (!cleanedProperties.isEmpty()) {
            normalizedSymbol = null;
        }

        return new PageDataComponent(normalizedSymbol, cleanedProperties, cleanedQuality);
    }));

    public PageDataComponent {
        linkProperties = sanitizeProperties(linkProperties);
        quality = sanitizeQuality(quality);

        if (!linkProperties.isEmpty()) {
            symbolId = null;
        }
    }

    public boolean isBlank() {
        return !isLinkPanel() && !isSymbolPage();
    }

    public boolean isSymbolPage() {
        return symbolId != null && !symbolId.isBlank();
    }

    public boolean isLinkPanel() {
        return !linkProperties.isEmpty();
    }

    public Identifier getSymbolIdentifier() {
        if (!isSymbolPage()) {
            return null;
        }

        return Identifier.tryParse(symbolId);
    }

    public PageDataComponent withSymbol(Identifier symbol) {
        if (symbol == null) {
            return new PageDataComponent(null, this.linkProperties, this.quality);
        }

        return new PageDataComponent(symbol.toString(), List.of(), this.quality);
    }

    public PageDataComponent withoutSymbol() {
        return new PageDataComponent(null, this.linkProperties, this.quality);
    }

    public PageDataComponent asLinkPanel() {
        return new PageDataComponent(null, this.linkProperties, this.quality);
    }

    public PageDataComponent withLinkProperties(List<String> properties) {
        return new PageDataComponent(null, properties, this.quality);
    }

    public PageDataComponent addLinkProperty(String property) {
        if (property == null || property.isBlank()) {
            return this;
        }

        List<String> newProperties = new ArrayList<>(this.linkProperties);
        if (!newProperties.contains(property)) {
            newProperties.add(property);
        }

        return new PageDataComponent(null, newProperties, this.quality);
    }

    public PageDataComponent clearLinkProperties() {
        return new PageDataComponent(this.symbolId, List.of(), this.quality);
    }

    public PageDataComponent withQuality(String trait, int value) {
        if (trait == null || trait.isBlank()) {
            return this;
        }

        LinkedHashMap<String, Integer> map = new LinkedHashMap<>(this.quality);
        map.put(trait, value);
        return new PageDataComponent(this.symbolId, this.linkProperties, map);
    }

    public Integer getQuality(String trait) {
        if (trait == null || trait.isBlank()) {
            return null;
        }
        return this.quality.get(trait);
    }

    public int getTotalQuality() {
        int sum = 0;
        for (int value : this.quality.values()) {
            sum += value;
        }
        return sum;
    }

    @Override
    public void addToTooltip(
            Item.TooltipContext tooltipContext,
            Consumer<Component> textConsumer,
            TooltipFlag type,
            DataComponentGetter components
    ) {
        if (isLinkPanel()) {
            textConsumer.accept(Component.translatable("tooltip.mystcraft-sc.page.kind.link_panel")
                    .withStyle(ChatFormatting.GRAY));

            textConsumer.accept(Component.translatable(
                    "tooltip.mystcraft-sc.page.link_properties.count",
                    this.linkProperties.size()
            ).withStyle(ChatFormatting.GRAY));

            for (String property : this.linkProperties) {
                textConsumer.accept(Component.literal(" - " + property)
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        } else if (isSymbolPage()) {
            textConsumer.accept(Component.translatable("tooltip.mystcraft-sc.page.kind.symbol")
                    .withStyle(ChatFormatting.GRAY));
            textConsumer.accept(Component.translatable("tooltip.mystcraft-sc.page.symbol_id", this.symbolId)
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            textConsumer.accept(Component.translatable("tooltip.mystcraft-sc.page.kind.blank")
                    .withStyle(ChatFormatting.GRAY));
        }

        int totalQuality = getTotalQuality();
        if (totalQuality != 0) {
            textConsumer.accept(Component.translatable("tooltip.mystcraft-sc.page.quality_total", totalQuality)
                    .withStyle(ChatFormatting.BLUE));
        }
    }

    private static List<String> sanitizeProperties(List<String> properties) {
        if (properties == null || properties.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String property : properties) {
            if (property != null && !property.isBlank()) {
                unique.add(property);
            }
        }

        return List.copyOf(unique);
    }

    private static Map<String, Integer> sanitizeQuality(Map<String, Integer> quality) {
        if (quality == null || quality.isEmpty()) {
            return Map.of();
        }

        LinkedHashMap<String, Integer> cleaned = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : quality.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();

            if (key != null && !key.isBlank() && value != null) {
                cleaned.put(key, value);
            }
        }

        return Map.copyOf(cleaned);
    }
}