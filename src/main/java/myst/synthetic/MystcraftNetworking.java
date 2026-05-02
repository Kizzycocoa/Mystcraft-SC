package myst.synthetic;

import myst.synthetic.block.entity.BlockEntityDisplayContainer;
import myst.synthetic.linking.LinkController;
import myst.synthetic.linking.LinkOptions;
import myst.synthetic.menu.BookBinderMenu;
import myst.synthetic.menu.WritingDeskMenu;
import myst.synthetic.network.BookBinderTitlePayload;
import myst.synthetic.network.DisplayContainerExtractPayload;
import myst.synthetic.network.DisplayContainerInsertPayload;
import myst.synthetic.network.LinkBookBookmarkExtractPayload;
import myst.synthetic.network.LinkBookUsePayload;
import myst.synthetic.network.WritingDeskTitlePayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import myst.synthetic.item.BookBookmarkUtil;
import myst.synthetic.world.dimension.AgeDimensionManager;
import net.minecraft.server.level.ServerLevel;
import myst.synthetic.network.DisplayContainerUseLinkPayload;
import myst.synthetic.block.entity.BlockEntityDisplayContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.network.chat.Component;
import myst.synthetic.world.dimension.PendingAgeTeleportManager;
import myst.synthetic.network.AgeRenderDataPayload;

public final class MystcraftNetworking {

    private MystcraftNetworking() {
    }

    public static void initialize() {
        PayloadTypeRegistry.playC2S().register(LinkBookUsePayload.ID, LinkBookUsePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(DisplayContainerExtractPayload.ID, DisplayContainerExtractPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(DisplayContainerInsertPayload.ID, DisplayContainerInsertPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(LinkBookBookmarkExtractPayload.ID, LinkBookBookmarkExtractPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(WritingDeskTitlePayload.ID, WritingDeskTitlePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(BookBinderTitlePayload.ID, BookBinderTitlePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(DisplayContainerUseLinkPayload.ID, DisplayContainerUseLinkPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AgeRenderDataPayload.ID, AgeRenderDataPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(LinkBookUsePayload.ID, (payload, context) -> {
            var player = context.player();

            context.server().execute(() -> {
                InteractionHand hand = payload.mainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                ItemStack stack = player.getItemInHand(hand);

                if (!stack.is(MystcraftItems.LINKBOOK) && !stack.is(MystcraftItems.AGEBOOK)) {
                    return;
                }

                if (stack.is(MystcraftItems.AGEBOOK)) {
                    PendingAgeTeleportManager.queue(player, stack);
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

                if (currentDimension.equals(targetDimension)) {
                    return;
                }

                LinkController.travelEntity(player.level(), player, info);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(LinkBookBookmarkExtractPayload.ID, (payload, context) -> {
            var player = context.player();

            context.server().execute(() -> {
                InteractionHand hand = payload.mainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                ItemStack stack = player.getItemInHand(hand);

                if (!stack.is(MystcraftItems.LINKBOOK) && !stack.is(MystcraftItems.AGEBOOK)) {
                    return;
                }

                ItemStack removed = BookBookmarkUtil.removeBookmark(stack);
                if (removed.isEmpty()) {
                    return;
                }

                if (!player.getInventory().add(removed)) {
                    player.drop(removed, false);
                }

                player.inventoryMenu.broadcastChanges();
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(DisplayContainerUseLinkPayload.ID, (payload, context) -> {
            var player = context.player();

            context.server().execute(() -> {
                BlockPos pos = payload.pos();

                if (!player.level().isLoaded(pos)) {
                    return;
                }

                BlockEntity blockEntity = player.level().getBlockEntity(pos);
                if (!(blockEntity instanceof BlockEntityDisplayContainer displayContainer)) {
                    return;
                }

                ItemStack stack = displayContainer.getStoredItem();
                if (!stack.is(MystcraftItems.LINKBOOK) && !stack.is(MystcraftItems.AGEBOOK)) {
                    return;
                }

                if (stack.is(MystcraftItems.AGEBOOK)) {
                    PendingAgeTeleportManager.queue(player, stack);
                    displayContainer.setChanged();
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
                if (currentDimension.equals(targetDimension)) {
                    return;
                }

                LinkController.travelEntity(player.level(), player, info);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(WritingDeskTitlePayload.ID, (payload, context) -> {
            var player = context.player();

            context.server().execute(() -> {
                if (!(player.containerMenu instanceof WritingDeskMenu menu)) {
                    return;
                }

                if (menu.containerId != payload.containerId()) {
                    return;
                }

                var desk = menu.getDeskBlockEntity();
                if (desk == null || !desk.stillValid(player)) {
                    return;
                }

                desk.setTargetTitle(player, payload.title());
                player.containerMenu.broadcastChanges();
                player.inventoryMenu.broadcastChanges();
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(BookBinderTitlePayload.ID, (payload, context) -> {
            var player = context.player();

            context.server().execute(() -> {
                if (!(player.containerMenu instanceof BookBinderMenu menu)) {
                    return;
                }

                if (menu.containerId != payload.containerId()) {
                    return;
                }

                var binder = menu.getBinderBlockEntity();
                if (binder == null || !binder.stillValid(player)) {
                    return;
                }

                binder.setBookTitle(payload.title());
                menu.updateCraftResult();
                player.containerMenu.broadcastChanges();
                player.inventoryMenu.broadcastChanges();
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(DisplayContainerExtractPayload.ID, (payload, context) -> {
            var player = context.player();

            context.server().execute(() -> {
                if (!(player.level().getBlockEntity(payload.pos()) instanceof BlockEntityDisplayContainer blockEntity)) {
                    return;
                }

                if (!player.blockPosition().closerThan(payload.pos(), 8.0D)) {
                    return;
                }

                if (!blockEntity.hasStoredItem()) {
                    return;
                }

                ItemStack removed = blockEntity.takeStoredItem();
                if (removed.isEmpty()) {
                    return;
                }

                if (player.getMainHandItem().isEmpty()) {
                    player.setItemInHand(InteractionHand.MAIN_HAND, removed);
                } else if (!player.addItem(removed)) {
                    player.drop(removed, false);
                }

                player.inventoryMenu.broadcastChanges();
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(DisplayContainerInsertPayload.ID, (payload, context) -> {
            var player = context.player();

            context.server().execute(() -> {
                if (!(player.level().getBlockEntity(payload.pos()) instanceof BlockEntityDisplayContainer blockEntity)) {
                    return;
                }

                if (!player.blockPosition().closerThan(payload.pos(), 8.0D)) {
                    return;
                }

                int slot = payload.playerSlot();
                if (slot < 0 || slot >= Inventory.getSelectionSize() + 27) {
                    return;
                }

                Inventory inventory = player.getInventory();
                ItemStack clicked = inventory.getItem(slot);

                if (clicked.isEmpty()) {
                    return;
                }

                if (!blockEntity.canAcceptDisplayItem(clicked)) {
                    return;
                }

                ItemStack previous = ItemStack.EMPTY;
                if (blockEntity.hasStoredItem()) {
                    previous = blockEntity.takeStoredItem();
                }

                blockEntity.setStoredItem(clicked.copyWithCount(1));

                if (!player.getAbilities().instabuild) {
                    clicked.shrink(1);
                    inventory.setItem(slot, clicked);
                }

                if (!previous.isEmpty()) {
                    if (!player.addItem(previous)) {
                        player.drop(previous, false);
                    }
                }

                player.inventoryMenu.broadcastChanges();
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