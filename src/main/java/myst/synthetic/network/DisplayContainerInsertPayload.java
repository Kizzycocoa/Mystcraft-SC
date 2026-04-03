package myst.synthetic.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DisplayContainerInsertPayload(BlockPos pos, int playerSlot) implements CustomPacketPayload {

    public static final Identifier PAYLOAD_ID = Identifier.fromNamespaceAndPath("mystcraft-sc", "insert_display_container");
    public static final CustomPacketPayload.Type<DisplayContainerInsertPayload> ID = new CustomPacketPayload.Type<>(PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, DisplayContainerInsertPayload> CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    DisplayContainerInsertPayload::pos,
                    ByteBufCodecs.INT,
                    DisplayContainerInsertPayload::playerSlot,
                    DisplayContainerInsertPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}