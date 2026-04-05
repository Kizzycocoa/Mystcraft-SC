package myst.synthetic;

import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

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
            /*
             * IN TIME:
             * 1 common page
             * costs 8 emeralds
             * 5 trade stock
             * 3 villager experience
             * 0.05 price multiplier
             * Do this trade twice
             *
             * factories.add(pageForEmeralds(CommonPagePools::randomPage, 8, 5, 3, PRICE_MULTIPLIER));
             * factories.add(pageForEmeralds(CommonPagePools::randomPage, 8, 5, 3, PRICE_MULTIPLIER));
             */

            // FOR NOW: villager buys 24 paper and gives 1 emerald
            factories.add(emeraldForItems(
                    Items.PAPER,
                    24,
                    1,
                    16,
                    2,
                    PRICE_MULTIPLIER
            ));
        });
    }

    private static void registerLevel2() {
        TradeOfferHelper.registerVillagerOffers(MystcraftVillagerProfessions.ARCHIVIST_KEY, 2, factories -> {
            /*
             * IN TIME:
             * 1 uncommon page
             * costs 16 emeralds
             * 5 trade stock
             * 5 villager experience
             * 0.05 price multiplier
             *
             * factories.add(pageForEmeralds(UncommonPagePools::randomPage, 16, 5, 5, PRICE_MULTIPLIER));
             */

            factories.add(itemsForEmeralds(
                    MystcraftItems.VIAL,
                    1,
                    5,
                    5,
                    5,
                    PRICE_MULTIPLIER
            ));
        });
    }

    private static void registerLevel3() {
        TradeOfferHelper.registerVillagerOffers(MystcraftVillagerProfessions.ARCHIVIST_KEY, 3, factories -> {
            /*
             * IN TIME:
             * 1 rare page
             * costs 25 emeralds
             * 5 trade stock
             * 8 villager experience
             * 0.05 price multiplier
             *
             * factories.add(pageForEmeralds(RarePagePools::randomPage, 25, 5, 8, PRICE_MULTIPLIER));
             */

            factories.add(itemsForEmeralds(
                    MystcraftBlocks.CRYSTAL.asItem(),
                    2,
                    16,
                    10,
                    8,
                    PRICE_MULTIPLIER
            ));
        });
    }

    private static void registerLevel4() {
        TradeOfferHelper.registerVillagerOffers(MystcraftVillagerProfessions.ARCHIVIST_KEY, 4, factories -> {
            /*
             * IN TIME:
             * 1 legendary page
             * costs 35 emeralds
             * 5 trade stock
             * 10 villager experience
             * 0.05 price multiplier
             *
             * factories.add(pageForEmeralds(LegendaryPagePools::randomPage, 35, 5, 10, PRICE_MULTIPLIER));
             */

            factories.add(itemsForEmeralds(
                    MystcraftItems.UNLINKEDBOOK,
                    1,
                    32,
                    2,
                    12,
                    PRICE_MULTIPLIER
            ));
        });
    }

    private static void registerLevel5() {
        TradeOfferHelper.registerVillagerOffers(MystcraftVillagerProfessions.ARCHIVIST_KEY, 5, factories -> {

            factories.add(itemsForEmeralds(
                    MystcraftItems.BAHRO_LEATHER,
                    1,
                    45,
                    2,
                    10,
                    PRICE_MULTIPLIER
            ));

            factories.add(itemsForEmeralds(
                    MystcraftItems.BOOSTER,
                    1,
                    20,
                    5,
                    20,
                    PRICE_MULTIPLIER
            ));
        });
    }

    private static VillagerTrades.ItemListing itemsForEmeralds(
            Item item,
            int itemCount,
            int emeraldCost,
            int maxUses,
            int villagerXp,
            float priceMultiplier
    ) {
        return (entity, random, level) -> new MerchantOffer(
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
        return (entity, random, level) -> new MerchantOffer(
                new ItemCost(item, itemCount),
                new ItemStack(Items.EMERALD, emeraldCount),
                maxUses,
                villagerXp,
                priceMultiplier
        );
    }

    /*
    private static VillagerTrades.ItemListing pageForEmeralds(
            java.util.function.Supplier<ItemStack> pageSupplier,
            int emeraldCost,
            int maxUses,
            int villagerXp,
            float priceMultiplier
    ) {
        return (entity, random, level) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, emeraldCost),
                pageSupplier.get(),
                maxUses,
                villagerXp,
                priceMultiplier
        );
    }
    */
}