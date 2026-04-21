package myst.synthetic.page;

import myst.synthetic.MystcraftItems;
import myst.synthetic.component.MystcraftDataComponents;
import myst.synthetic.component.PageDataComponent;
import myst.synthetic.page.symbol.PageSymbol;
import myst.synthetic.page.symbol.PageSymbolRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class Page {

    private Page() {
    }

    public static PageDataComponent getPageData(ItemStack stack) {
        return stack.getOrDefault(MystcraftDataComponents.PAGE_DATA, PageDataComponent.EMPTY);
    }

    public static void setPageData(ItemStack stack, PageDataComponent data) {
        stack.set(MystcraftDataComponents.PAGE_DATA, data == null ? PageDataComponent.EMPTY : data);
    }

    public static boolean isBlank(ItemStack page) {
        return getPageData(page).isBlank();
    }

    public static boolean isSymbolPage(ItemStack page) {
        return getPageData(page).isSymbolPage();
    }

    public static boolean isLinkPanel(ItemStack page) {
        return getPageData(page).isLinkPanel();
    }

    public static void clearPageData(ItemStack page) {
        setPageData(page, PageDataComponent.EMPTY);
    }

    public static void makeLinkPanel(ItemStack page) {
        setPageData(page, getPageData(page).asLinkPanel());
    }

    public static void clearLinkPanel(ItemStack page) {
        setPageData(page, getPageData(page).clearLinkPanel());
    }

    public static void addLinkProperty(ItemStack page, String property) {
        setPageData(page, getPageData(page).addLinkProperty(property));
    }

    public static void setLinkProperties(ItemStack page, Collection<String> properties) {
        setPageData(page, getPageData(page).withLinkProperties(List.copyOf(properties)));
    }

    public static boolean hasLinkProperty(ItemStack page, String property) {
        return getLinkProperties(page).contains(property);
    }

    public static List<String> getLinkProperties(ItemStack page) {
        return getPageData(page).linkProperties();
    }

    public static void setSymbol(ItemStack page, @Nullable Identifier symbol) {
        PageDataComponent data = getPageData(page).withSymbol(symbol);

        if (symbol != null) {
            PageSymbol pageSymbol = PageSymbolRegistry.get(symbol);
            data = data.withValue(pageSymbol == null ? null : pageSymbol.value());
        } else {
            data = data.withValue(null);
        }

        setPageData(page, data);
    }

    @Nullable
    public static Identifier getSymbol(ItemStack page) {
        return getPageData(page).getSymbolIdentifier();
    }

    public static void clearSymbol(ItemStack page) {
        setPageData(page, getPageData(page).withoutSymbol().withValue(null));
    }

    public static void setQuality(ItemStack page, String trait, int quality) {
        setPageData(page, getPageData(page).withQuality(trait, quality));
    }

    @Nullable
    public static Integer getQuality(ItemStack page, String trait) {
        return getPageData(page).getQuality(trait);
    }

    public static Map<String, Integer> getQualityMap(ItemStack page) {
        return getPageData(page).quality();
    }

    public static int getTotalQuality(ItemStack page) {
        return getPageData(page).getTotalQuality();
    }

    public static void setValue(ItemStack page, @Nullable PageValue value) {
        setPageData(page, getPageData(page).withValue(value));
    }

    @Nullable
    public static PageValue getValue(ItemStack page) {
        PageDataComponent data = getPageData(page);
        if (data.value() != null) {
            return data.value();
        }

        Identifier symbolId = data.getSymbolIdentifier();
        if (symbolId == null) {
            return null;
        }

        PageSymbol symbol = PageSymbolRegistry.get(symbolId);
        return symbol == null ? null : symbol.value();
    }

    @Nullable
    public static Float getScalarValue(ItemStack page) {
        PageValue value = getValue(page);
        return value == null ? null : value.scalarOrNull();
    }

    @Nullable
    public static List<Float> getVectorValue(ItemStack page) {
        PageValue value = getValue(page);
        return value == null ? null : value.vectorOrNull();
    }

    @Nullable
    public static String getTextValue(ItemStack page) {
        PageValue value = getValue(page);
        return value == null ? null : value.textOrNull();
    }

    public static ItemStack createPage() {
        ItemStack page = new ItemStack(MystcraftItems.PAGE);
        setPageData(page, PageDataComponent.EMPTY);
        return page;
    }

    public static ItemStack createLinkPage() {
        ItemStack page = createPage();
        makeLinkPanel(page);
        return page;
    }

    public static ItemStack createLinkPage(String property) {
        ItemStack page = createPage();
        addLinkProperty(page, property);
        return page;
    }

    public static ItemStack createLinkPage(Collection<String> properties) {
        ItemStack page = createPage();
        setLinkProperties(page, properties);
        return page;
    }

    public static ItemStack createSymbolPage(Identifier symbol) {
        ItemStack page = createPage();
        setSymbol(page, symbol);
        return page;
    }
}