package myst.synthetic.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record WritingDeskTitlePayload(int containerId, String title) implements CustomPacketPayload {

    public static final Identifier PAYLOAD_ID = Identifier.fromNamespaceAndPath("mystcraft-sc", "writing_desk_title");
    public static final CustomPacketPayload.Type<WritingDeskTitlePayload> ID = new CustomPacketPayload.Type<>(PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, WritingDeskTitlePayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    WritingDeskTitlePayload::containerId,
                    ByteBufCodecs.STRING_UTF8,
                    WritingDeskTitlePayload::title,
                    WritingDeskTitlePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
