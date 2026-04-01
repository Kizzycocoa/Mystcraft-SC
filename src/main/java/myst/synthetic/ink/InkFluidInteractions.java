package myst.synthetic.ink;

import myst.synthetic.MystcraftFluids;
import myst.synthetic.MystcraftItems;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class InkFluidInteractions {

    private InkFluidInteractions() {
    }

    public static void initialize() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            ItemStack held = player.getItemInHand(hand);

            if (!held.is(Items.GLASS_BOTTLE)) {
                return InteractionResult.PASS;
            }

            BlockHitResult hit = raycastForSourceFluid(level, player);

            if (hit.getType() != HitResult.Type.BLOCK) {
                return InteractionResult.PASS;
            }

            BlockPos pos = hit.getBlockPos();
            FluidState fluidState = level.getFluidState(pos);

            if (!fluidState.is(MystcraftFluids.BLACK_INK) || !fluidState.isSource()) {
                return InteractionResult.PASS;
            }

            if (!level.isClientSide()) {
                ItemStack vial = new ItemStack(MystcraftItems.VIAL);

                if (!player.getAbilities().instabuild) {
                    held.shrink(1);

                    if (held.isEmpty()) {
                        player.setItemInHand(hand, vial);
                    } else if (!player.getInventory().add(vial)) {
                        player.drop(vial, false);
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
            }

            return InteractionResult.SUCCESS;
        });
    }

    private static BlockHitResult raycastForSourceFluid(Level level, Player player) {
        float partialTick = 1.0F;
        Vec3 eyePos = player.getEyePosition(partialTick);
        Vec3 look = player.getViewVector(partialTick);
        double reach = player.blockInteractionRange();
        Vec3 end = eyePos.add(look.x * reach, look.y * reach, look.z * reach);

        ClipContext context = new ClipContext(
                eyePos,
                end,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.SOURCE_ONLY,
                player
        );

        return level.clip(context);
    }
}