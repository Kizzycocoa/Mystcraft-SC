package myst.synthetic.ink;

import myst.synthetic.MystcraftFluids;
import myst.synthetic.MystcraftItems;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;

public final class InkFluidInteractions {

    private InkFluidInteractions() {
    }

    public static void initialize() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            ItemStack held = player.getItemInHand(hand);

            if (!held.is(Items.GLASS_BOTTLE)) {
                return InteractionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            FluidState fluidState = level.getFluidState(pos);

            if (!fluidState.is(MystcraftFluids.BLACK_INK) || !fluidState.isSource()) {
                return InteractionResult.PASS;
            }

            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }

            ItemStack filled = new ItemStack(MystcraftItems.VIAL);

            if (!player.getAbilities().instabuild) {
                held.shrink(1);

                if (held.isEmpty()) {
                    player.setItemInHand(hand, filled);
                } else if (!player.getInventory().add(filled)) {
                    player.drop(filled, false);
                }
            }

            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
            level.playSound(
                    null,
                    pos,
                    SoundEvents.BOTTLE_FILL,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
            );

            return InteractionResult.CONSUME;
        });
    }
}