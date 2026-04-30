package myst.synthetic;

import myst.synthetic.block.BlockCrystal;
import myst.synthetic.block.property.CrystalColor;
import myst.synthetic.page.Page;
import myst.synthetic.page.symbol.PageSymbol;
import myst.synthetic.page.symbol.PageSymbolRegistry;
import myst.synthetic.villager.ArchivistTradeTheme;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.List;

public final class MystcraftVillagerTrades {

    private static final float PRICE_MULTIPLIER = 0.05f;

    private MystcraftVillagerTrades() {
    }

    public static void initialize() {
        registerLevel1();
        registerLevel2();
        registerLevel3();
        registerLevel4();
        registerLevel5();
    }

    private static void registerLevel1() {
        TradeOfferHelper.registerVillagerOffers(MystcraftVillagerProfessions.ARCHIVIST_KEY, 1, factories -> {
            factories.add(emeraldForItems(Items.PAPER, 24, 1, 16, 2, PRICE_MULTIPLIER));

            factories.add(pageTradeCommon(1, 4, 5, 3));
            factories.add(pageTradeTheme(1, 4, 5, 3));
        });
    }

    private static void registerLevel2() {
        TradeOfferHelper.registerVillagerOffers(MystcraftVillagerProfessions.ARCHIVIST_KEY, 2, factories -> {
            factories.add(itemsForEmeralds(MystcraftItems.VIAL, 1, 5, 5, 5, PRICE_MULTIPLIER));

            factories.add(pageTradeCommon(2, 8, 5, 5));
            factories.add(pageTradeTheme(2, 8, 5, 5));
        });
    }

    private static void registerLevel3() {
        TradeOfferHelper.registerVillagerOffers(MystcraftVillagerProfessions.ARCHIVIST_KEY, 3, factories -> {
            factories.add(randomCrystalTrade(10, 10, 8));

            factories.add(pageTradeCommon(3, 16, 5, 8));
            factories.add(pageTradeTheme(3, 16, 5, 8));
        });
    }

    private static void registerLevel4() {
        TradeOfferHelper.registerVillagerOffers(MystcraftVillagerProfessions.ARCHIVIST_KEY, 4, factories -> {
            factories.add(itemsForEmeralds(MystcraftItems.BOOSTER, 1, 20, 5, 20, PRICE_MULTIPLIER));

            factories.add(pageTradeThemeOrCommon(4, 24, 5, 10));
        });
    }

    private static void registerLevel5() {
        TradeOfferHelper.registerVillagerOffers(MystcraftVillagerProfessions.ARCHIVIST_KEY, 5, factories -> {
            factories.add(pageTradeThemeOrCommon(5, 32, 5, 12));
            factories.add(level5SpecialTrade());
        });
    }

    private static VillagerTrades.ItemListing pageTradeCommon(
            int rank,
            int emeraldCost,
            int maxUses,
            int villagerXp
    ) {
        return (entity, random, level) -> {
            ItemStack page = pickPage(rank, List.of("trade", "common"), List.of());
            if (page.isEmpty()) {
                return null;
            }

            return new MerchantOffer(
                    new ItemCost(Items.EMERALD, emeraldCost),
                    page,
                    maxUses,
                    villagerXp,
                    PRICE_MULTIPLIER
            );
        };
    }

    private static VillagerTrades.ItemListing pageTradeTheme(
            int rank,
            int emeraldCost,
            int maxUses,
            int villagerXp
    ) {
        return (serverLevel, entity, random) -> {
            ArchivistTradeTheme theme = themeFor(entity);
            ItemStack page = pickPage(rank, List.of("trade"), List.of(theme.biomeTag(), theme.temperatureTag()));
            if (page.isEmpty()) {
                return null;
            }

            return new MerchantOffer(
                    new ItemCost(Items.EMERALD, emeraldCost),
                    page,
                    maxUses,
                    villagerXp,
                    PRICE_MULTIPLIER
            );
        };
    }

    private static VillagerTrades.ItemListing pageTradeThemeOrCommon(
            int rank,
            int emeraldCost,
            int maxUses,
            int villagerXp
    ) {
        return (serverLevel, entity, random) -> {
            ArchivistTradeTheme theme = themeFor(entity);
            ItemStack page = pickPage(rank, List.of("trade"), List.of("common", theme.biomeTag(), theme.temperatureTag()));
            if (page.isEmpty()) {
                return null;
            }

            return new MerchantOffer(
                    new ItemCost(Items.EMERALD, emeraldCost),
                    page,
                    maxUses,
                    villagerXp,
                    PRICE_MULTIPLIER
            );
        };
    }

    private static ItemStack pickPage(int rank, List<String> requiredTags, List<String> anyTags) {
        List<PageSymbol> matches = PageSymbolRegistry.values().stream()
                .filter(symbol -> symbol.cardRank() == rank)
                .filter(symbol -> hasAllTags(symbol, requiredTags))
                .filter(symbol -> anyTags.isEmpty() || hasAnyTag(symbol, anyTags))
                .toList();

        if (matches.isEmpty()) {
            return ItemStack.EMPTY;
        }

        PageSymbol picked = matches.get(net.minecraft.util.RandomSource.create().nextInt(matches.size()));
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

    private static VillagerTrades.ItemListing randomCrystalTrade(
            int emeraldCost,
            int maxUses,
            int villagerXp
    ) {
        return (serverLevel, entity, random) -> {
            ArchivistTradeTheme theme = themeFor(entity);
            List<String> colors = theme.crystalColors();

            String colorName = colors.get(random.nextInt(colors.size()));
            CrystalColor color = CrystalColor.valueOf(colorName.toUpperCase(java.util.Locale.ROOT));

            ItemStack stack = new ItemStack(MystcraftBlocks.CRYSTAL);
            stack.set(
                    DataComponents.BLOCK_STATE,
                    BlockItemStateProperties.EMPTY.with(BlockCrystal.COLOR, color)
            );

            return new MerchantOffer(
                    new ItemCost(Items.EMERALD, emeraldCost),
                    stack,
                    maxUses,
                    villagerXp,
                    PRICE_MULTIPLIER
            );
        };
    }

    private static VillagerTrades.ItemListing level5SpecialTrade() {
        return (serverLevel, entity, random) -> {
            ArchivistTradeTheme theme = themeFor(entity);

            return switch (theme) {
                case PLAINS -> itemsForEmeralds(
                        // Replace this with MystcraftItems.MYST_ICONOGRAPHY_BANNER_PATTERN once that item exists in MystcraftItems.
                        MystcraftItems.MYST_ICONOGRAPHY_BANNER_PATTERN,
                        1,
                        16,
                        3,
                        12,
                        PRICE_MULTIPLIER
                ).getOffer(serverLevel, entity, random);

                case SNOWY -> itemsForEmeralds(
                        MystcraftItems.MYST_POETRY_BANNER_PATTERN,
                        1,
                        16,
                        3,
                        12,
                        PRICE_MULTIPLIER
                ).getOffer(serverLevel, entity, random);

                case TAIGA -> itemsForEmeralds(
                        MystcraftItems.UNLINKEDBOOK,
                        1,
                        24,
                        2,
                        12,
                        PRICE_MULTIPLIER
                ).getOffer(serverLevel, entity, random);

                case DESERT -> itemsForEmeralds(
                        MystcraftItems.BOOKMARK,
                        4 + random.nextInt(3),
                        8,
                        3,
                        12,
                        PRICE_MULTIPLIER
                ).getOffer(serverLevel, entity, random);

                case SAVANNA -> itemsForEmeralds(
                        MystcraftItems.MYST_NUMEROLOGY_BANNER_PATTERN,
                        1,
                        16,
                        3,
                        12,
                        PRICE_MULTIPLIER
                ).getOffer(serverLevel, entity, random);

                case SWAMP -> itemsForEmeralds(
                        MystcraftItems.BAHRO_LEATHER,
                        1,
                        45,
                        2,
                        12,
                        PRICE_MULTIPLIER
                ).getOffer(serverLevel, entity, random);

                case JUNGLE -> itemsForEmeralds(
                        MystcraftItems.GLASSES,
                        1,
                        16,
                        3,
                        12,
                        PRICE_MULTIPLIER
                ).getOffer(serverLevel, entity, random);
            };
        };
    }

    private static ArchivistTradeTheme themeFor(Entity entity) {
        if (entity instanceof Villager villager) {
            return ArchivistTradeTheme.fromVillager(villager);
        }

        return ArchivistTradeTheme.PLAINS;
    }

    private static VillagerTrades.ItemListing itemsForEmeralds(
            Item item,
            int itemCount,
            int emeraldCost,
            int maxUses,
            int villagerXp,
            float priceMultiplier
    ) {
        return (serverLevel, entity, random) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, emeraldCost),
                new ItemStack(item, itemCount),
                maxUses,
                villagerXp,
                priceMultiplier
        );
    }

    private static VillagerTrades.ItemListing emeraldForItems(
            Item item,
            int itemCount,
            int emeraldCount,
            int maxUses,
            int villagerXp,
            float priceMultiplier
    ) {
        return (serverLevel, entity, random) -> new MerchantOffer(
                new ItemCost(item, itemCount),
                new ItemStack(Items.EMERALD, emeraldCount),
                maxUses,
                villagerXp,
                priceMultiplier
        );
    }
}