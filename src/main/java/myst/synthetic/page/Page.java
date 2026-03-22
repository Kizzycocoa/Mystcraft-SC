package myst.synthetic.page;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import myst.synthetic.MystcraftItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class Page {

    public static final String TAG_SYMBOL = "symbol";
    public static final String TAG_LINK_PANEL = "linkpanel";
    public static final String TAG_PROPERTIES = "properties";
    public static final String TAG_QUALITY = "Quality";

    private Page() {}

    private static CompoundTag getDataCopy(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return data.copyTag();
    }

    private static void updateData(ItemStack stack, java.util.function.Consumer<CompoundTag> editor) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, editor);
    }

    public static boolean isBlank(ItemStack page) {
        return !isLinkPanel(page) && getSymbol(page) == null;
    }

    public static boolean isLinkPanel(ItemStack page) {
        return getDataCopy(page).contains(TAG_LINK_PANEL);
    }

    public static void makeLinkPanel(ItemStack page) {
        updateData(page, tag -> {
            if (!tag.contains(TAG_LINK_PANEL)) {
                tag.put(TAG_LINK_PANEL, new CompoundTag());
            }
        });
    }

    public static void addLinkProperty(ItemStack page, String property) {
        updateData(page, tag -> {
            CompoundTag linkPanel = tag.contains(TAG_LINK_PANEL)
                    ? tag.getCompoundOrEmpty(TAG_LINK_PANEL)
                    : new CompoundTag();

            ListTag properties = linkPanel.contains(TAG_PROPERTIES)
                    ? linkPanel.getListOrEmpty(TAG_PROPERTIES)
                    : new ListTag();

            properties.add(StringTag.valueOf(property));
            linkPanel.put(TAG_PROPERTIES, properties);
            tag.put(TAG_LINK_PANEL, linkPanel);
        });
    }

    @Nullable
    public static Collection<String> getLinkProperties(ItemStack page) {
        CompoundTag tag = getDataCopy(page);
        if (!tag.contains(TAG_LINK_PANEL)) {
            return null;
        }

        CompoundTag linkPanel = tag.getCompoundOrEmpty(TAG_LINK_PANEL);
        if (!linkPanel.contains(TAG_PROPERTIES)) {
            return List.of();
        }

        ListTag list = linkPanel.getListOrEmpty(TAG_PROPERTIES);
        List<String> out = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            list.getString(i).ifPresent(out::add);
        }
        return out;
    }

    public static void setSymbol(ItemStack page, @Nullable Identifier symbol) {
        updateData(page, tag -> {
            if (symbol == null) {
                tag.remove(TAG_SYMBOL);
            } else {
                tag.putString(TAG_SYMBOL, symbol.toString());
            }
        });
    }

    @Nullable
    public static Identifier getSymbol(ItemStack page) {
        CompoundTag tag = getDataCopy(page);
        if (!tag.contains(TAG_SYMBOL)) {
            return null;
        }

        String symbol = tag.getString(TAG_SYMBOL).orElse("");
        if (symbol.isBlank()) {
            return null;
        }

        return Identifier.tryParse(symbol);
    }

    public static void setQuality(ItemStack page, String trait, int quality) {
        updateData(page, tag -> {
            CompoundTag qualityTag = tag.contains(TAG_QUALITY)
                    ? tag.getCompoundOrEmpty(TAG_QUALITY)
                    : new CompoundTag();
            qualityTag.putInt(trait, quality);
            tag.put(TAG_QUALITY, qualityTag);
        });
    }

    @Nullable
    public static Integer getQuality(ItemStack page, String trait) {
        CompoundTag tag = getDataCopy(page);
        if (!tag.contains(TAG_QUALITY)) {
            return null;
        }

        CompoundTag qualityTag = tag.getCompoundOrEmpty(TAG_QUALITY);
        return qualityTag.contains(trait) ? qualityTag.getInt(trait).orElse(0) : null;
    }

    public static int getTotalQuality(ItemStack page) {
        CompoundTag tag = getDataCopy(page);
        if (!tag.contains(TAG_QUALITY)) {
            return 0;
        }

        CompoundTag qualityTag = tag.getCompoundOrEmpty(TAG_QUALITY);
        int sum = 0;
        for (String key : qualityTag.keySet()) {
            sum += qualityTag.getInt(key).orElse(0);
        }
        return sum;
    }

    public static ItemStack createPage() {
        ItemStack page = new ItemStack(MystcraftItems.PAGE);
        CustomData.set(DataComponents.CUSTOM_DATA, page, new CompoundTag());
        return page;
    }

    public static ItemStack createLinkPage() {
        ItemStack page = createPage();
        makeLinkPanel(page);
        return page;
    }

    public static ItemStack createLinkPage(String property) {
        ItemStack page = createPage();
        makeLinkPanel(page);
        addLinkProperty(page, property);
        return page;
    }

    public static ItemStack createSymbolPage(Identifier symbol) {
        ItemStack page = createPage();
        setSymbol(page, symbol);
        return page;
    }
}