package myst.synthetic.villager;

import myst.synthetic.MystcraftVillagerProfessions;
import myst.synthetic.page.Page;
import myst.synthetic.page.symbol.PageSymbol;
import myst.synthetic.page.symbol.PageSymbolRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.List;

public final class ArchivistPageTradeReroller {

    private ArchivistPageTradeReroller() {
    }

    public static boolean isArchivist(Villager villager) {
        return villager.getVillagerData().profession().is(MystcraftVillagerProfessions.ARCHIVIST_KEY);
    }

    public static boolean isUsedPageTrade(MerchantOffer offer) {
        return offer.getUses() > 0
                && getSoldPageSymbol(offer) != null;
    }

    public static int getSoldPageRank(MerchantOffer offer) {
        PageSymbol symbol = getSoldPageSymbol(offer);
        return symbol == null ? 0 : symbol.cardRank();
    }

    public static MerchantOffer rerollPageTrade(Villager villager, MerchantOffer currentOffer, int rank) {
        RandomSource random = villager.getRandom();
        ArchivistTradeTheme theme = ArchivistTradeTheme.fromVillager(villager);

        boolean useTheme = random.nextFloat() < 0.60F;

        ItemStack page = useTheme
                ? pickThemePage(rank, theme, random)
                : pickCommonPage(rank, random);

        if (page.isEmpty()) {
            page = useTheme
                    ? pickCommonPage(rank, random)
                    : pickThemePage(rank, theme, random);
        }

        if (page.isEmpty()) {
            return currentOffer;
        }

        MerchantOffer replacement = new MerchantOffer(
                currentOffer.getItemCostA(),
                currentOffer.getItemCostB(),
                page,
                currentOffer.getUses(),
                currentOffer.getMaxUses(),
                currentOffer.getXp(),
                currentOffer.getPriceMultiplier(),
                currentOffer.getDemand()
        );

        replacement.setSpecialPriceDiff(currentOffer.getSpecialPriceDiff());
        return replacement;
    }

    private static ItemStack pickCommonPage(int rank, RandomSource random) {
        return pickPage(rank, List.of("trade", "common"), List.of(), random);
    }

    private static ItemStack pickThemePage(int rank, ArchivistTradeTheme theme, RandomSource random) {
        return pickPage(rank, List.of("trade"), List.of(theme.biomeTag(), theme.temperatureTag()), random);
    }

    private static ItemStack pickPage(
            int rank,
            List<String> requiredTags,
            List<String> anyTags,
            RandomSource random
    ) {
        List<PageSymbol> matches = PageSymbolRegistry.values().stream()
                .filter(symbol -> symbol.cardRank() == rank)
                .filter(symbol -> hasAllTags(symbol, requiredTags))
                .filter(symbol -> anyTags.isEmpty() || hasAnyTag(symbol, anyTags))
                .toList();

        if (matches.isEmpty()) {
            return ItemStack.EMPTY;
        }

        PageSymbol picked = matches.get(random.nextInt(matches.size()));
        return Page.createSymbolPage(picked.id());
    }

    private static boolean hasAllTags(PageSymbol symbol, List<String> tags) {
        for (String tag : tags) {
            if (!symbol.lootTags().contains(tag)) {
                return false;
            }
        }

        return true;
    }

    private static boolean hasAnyTag(PageSymbol symbol, List<String> tags) {
        for (String tag : tags) {
            if (symbol.lootTags().contains(tag)) {
                return true;
            }
        }

        return false;
    }

    private static PageSymbol getSoldPageSymbol(MerchantOffer offer) {
        ItemStack result = offer.getResult();
        if (!Page.isSymbolPage(result)) {
            return null;
        }

        Identifier symbolId = Page.getSymbol(result);
        if (symbolId == null) {
            return null;
        }

        return PageSymbolRegistry.get(symbolId);
    }
}