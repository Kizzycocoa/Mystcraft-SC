package myst.synthetic.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record LinkBookBookmarkExtractPayload(boolean mainHand) implements CustomPacketPayload {

    private static final Identifier PAYLOAD_ID = Identifier.fromNamespaceAndPath("mystcraft-sc", "linkbook_bookmark_extract");
    public static final CustomPacketPayload.Type<LinkBookBookmarkExtractPayload> ID = new CustomPacketPayload.Type<>(PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, LinkBookBookmarkExtractPayload> CODEC =
            StreamCodec.composite(
                    net.minecraft.network.codec.ByteBufCodecs.BOOL,
                    LinkBookBookmarkExtractPayload::mainHand,
                    LinkBookBookmarkExtractPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}