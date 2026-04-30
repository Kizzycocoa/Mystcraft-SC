package myst.synthetic.mixin;

import myst.synthetic.villager.ArchivistPageTradeReroller;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(Villager.class)
public abstract class VillagerPageTradeRerollMixin {

	@Unique
	private final Map<Integer, Integer> mystcraft_sc$pageTradesToReroll = new LinkedHashMap<>();

	@Inject(method = "rewardTradeXp", at = @At("HEAD"))
	private void mystcraft_sc$markPageTradeForReroll(MerchantOffer offer, CallbackInfo ci) {
		Villager villager = (Villager) (Object) this;

		if (!ArchivistPageTradeReroller.isArchivist(villager)) {
			return;
		}

		int rank = ArchivistPageTradeReroller.getSoldPageRank(offer);
		if (rank <= 0) {
			return;
		}

		MerchantOffers offers = villager.getOffers();
		for (int i = 0; i < offers.size(); i++) {
			if (offers.get(i) == offer) {
				this.mystcraft_sc$pageTradesToReroll.put(i, rank);
				return;
			}
		}
	}

	@Inject(method = "restock", at = @At("TAIL"))
	private void mystcraft_sc$rerollMarkedPageTradesAfterRestock(CallbackInfo ci) {
		this.mystcraft_sc$rerollMarkedPageTrades();
	}

	@Inject(method = "catchUpDemand", at = @At("TAIL"))
	private void mystcraft_sc$rerollMarkedPageTradesAfterCatchup(CallbackInfo ci) {
		this.mystcraft_sc$rerollMarkedPageTrades();
	}

	@Unique
	private void mystcraft_sc$rerollMarkedPageTrades() {
		if (this.mystcraft_sc$pageTradesToReroll.isEmpty()) {
			return;
		}

		Villager villager = (Villager) (Object) this;

		if (!ArchivistPageTradeReroller.isArchivist(villager)) {
			this.mystcraft_sc$pageTradesToReroll.clear();
			return;
		}

		MerchantOffers offers = villager.getOffers();

		for (Map.Entry<Integer, Integer> entry : this.mystcraft_sc$pageTradesToReroll.entrySet()) {
			int slot = entry.getKey();
			int rank = entry.getValue();

			if (slot < 0 || slot >= offers.size()) {
				continue;
			}

			MerchantOffer currentOffer = offers.get(slot);
			MerchantOffer replacement = ArchivistPageTradeReroller.rerollPageTrade(
					villager,
					currentOffer,
					rank
			);

			offers.set(slot, replacement);
		}

		this.mystcraft_sc$pageTradesToReroll.clear();
	}
}