package myst.synthetic.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BookBinderTitlePayload(int containerId, String title) implements CustomPacketPayload {

    public static final Identifier PAYLOAD_ID = Identifier.fromNamespaceAndPath("mystcraft-sc", "book_binder_title");
    public static final Type<BookBinderTitlePayload> ID = new Type<>(PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, BookBinderTitlePayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    BookBinderTitlePayload::containerId,
                    ByteBufCodecs.STRING_UTF8,
                    BookBinderTitlePayload::title,
                    BookBinderTitlePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}