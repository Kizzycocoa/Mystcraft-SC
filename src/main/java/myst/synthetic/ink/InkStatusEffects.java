package myst.synthetic.ink;

import myst.synthetic.MystcraftSyntheticCodex;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class InkStatusEffects {

    private InkStatusEffects() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (!player.isAlive()) {
                    continue;
                }

                if (InkExposure.isSubmergedInInk(player)) {
                    applyInkSlowness(player);
                }
            }
        });

        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
            if (!(entity instanceof ServerPlayer player)) {
                return;
            }

            if (blocked || damageTaken <= 0.0F) {
                return;
            }

            if (!source.is(DamageTypes.DROWN)) {
                return;
            }

            if (!InkExposure.isSubmergedInInk(player)) {
                return;
            }

            player.addEffect(new MobEffectInstance(
                    MobEffects.POISON,
                    15 * 20,
                    0,
                    false,
                    true,
                    true
            ));
        });

        MystcraftSyntheticCodex.LOGGER.info("Registered ink immersion effects.");
    }

    private static void applyInkSlowness(ServerPlayer player) {
        MobEffectInstance existing = player.getEffect(MobEffects.SLOWNESS);

        if (existing != null && existing.getAmplifier() >= 1 && existing.getDuration() >= 19 * 20) {
            return;
        }

        player.addEffect(new MobEffectInstance(
                MobEffects.SLOWNESS,
                20 * 20,
                1,
                false,
                true,
                true
        ));
    }
}