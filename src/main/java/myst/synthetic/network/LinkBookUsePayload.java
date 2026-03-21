package myst.synthetic.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record LinkBookUsePayload(boolean mainHand) implements CustomPacketPayload {

    public static final Identifier PAYLOAD_ID = Identifier.fromNamespaceAndPath("mystcraft-sc", "use_linkbook");
    public static final CustomPacketPayload.Type<LinkBookUsePayload> ID = new CustomPacketPayload.Type<>(PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, LinkBookUsePayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    LinkBookUsePayload::mainHand,
                    LinkBookUsePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}