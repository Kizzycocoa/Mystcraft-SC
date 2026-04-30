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

import java.util.ArrayList;
import java.util.List;

@Mixin(Villager.class)
public abstract class VillagerPageTradeRerollMixin {

	@Unique
	private final List<PageTradeSlot> mystcraft_sc$pageTradesToReroll = new ArrayList<>();

	@Inject(method = "restock", at = @At("HEAD"))
	private void mystcraft_sc$captureUsedPageTrades(CallbackInfo ci) {
		Villager villager = (Villager) (Object) this;
		this.mystcraft_sc$pageTradesToReroll.clear();

		if (!ArchivistPageTradeReroller.isArchivist(villager)) {
			return;
		}

		MerchantOffers offers = villager.getOffers();
		for (int i = 0; i < offers.size(); i++) {
			MerchantOffer offer = offers.get(i);
			if (!ArchivistPageTradeReroller.isUsedPageTrade(offer)) {
				continue;
			}

			int rank = ArchivistPageTradeReroller.getSoldPageRank(offer);
			if (rank <= 0) {
				continue;
			}

			this.mystcraft_sc$pageTradesToReroll.add(new PageTradeSlot(i, rank));
		}
	}

	@Inject(method = "restock", at = @At("TAIL"))
	private void mystcraft_sc$rerollUsedPageTrades(CallbackInfo ci) {
		if (this.mystcraft_sc$pageTradesToReroll.isEmpty()) {
			return;
		}

		Villager villager = (Villager) (Object) this;
		MerchantOffers offers = villager.getOffers();

		for (PageTradeSlot slot : this.mystcraft_sc$pageTradesToReroll) {
			if (slot.index() < 0 || slot.index() >= offers.size()) {
				continue;
			}

			MerchantOffer currentOffer = offers.get(slot.index());
			MerchantOffer replacement = ArchivistPageTradeReroller.rerollPageTrade(
					villager,
					currentOffer,
					slot.rank()
			);

			offers.set(slot.index(), replacement);
		}

		this.mystcraft_sc$pageTradesToReroll.clear();
	}

	@Unique
	private record PageTradeSlot(int index, int rank) {
	}
}