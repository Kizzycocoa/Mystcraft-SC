package myst.synthetic;

import myst.synthetic.network.LinkBookUsePayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import myst.synthetic.linking.LinkController;
import myst.synthetic.linking.LinkOptions;
import net.minecraft.world.phys.Vec3;

public final class MystcraftNetworking {

    private MystcraftNetworking() {
    }

    public static void initialize() {
        PayloadTypeRegistry.playC2S().register(LinkBookUsePayload.ID, LinkBookUsePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(LinkBookUsePayload.ID, (payload, context) -> {
            var player = context.player();

            context.server().execute(() -> {
                InteractionHand hand = payload.mainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                ItemStack stack = player.getItemInHand(hand);

                if (!stack.is(MystcraftItems.LINKBOOK)) {
                    return;
                }

                CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                if (customData == null) {
                    return;
                }

                CompoundTag tag = customData.copyTag();
                LinkOptions info = new LinkOptions(tag);

                String targetDimension = info.getDimensionUID();
                if (targetDimension == null || targetDimension.isBlank()) {
                    return;
                }

                String currentDimension = extractDimensionId(player.level().dimension().toString());

                // Only link if the target dimension is different from the current one.
                if (currentDimension.equals(targetDimension)) {
                    return;
                }

                LinkController.travelEntity(player.level(), player, info);
            });
        });
    }

    private static String extractDimensionId(String raw) {
        int slash = raw.lastIndexOf('/');
        int end = raw.lastIndexOf(']');

        if (slash >= 0 && end > slash) {
            return raw.substring(slash + 1, end).trim();
        }

        return raw;
    }
}