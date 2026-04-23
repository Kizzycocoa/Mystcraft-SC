package myst.synthetic.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DisplayContainerUseLinkPayload(BlockPos pos) implements CustomPacketPayload {

    public static final Identifier PAYLOAD_ID = Identifier.fromNamespaceAndPath("mystcraft-sc", "use_display_container_link");
    public static final CustomPacketPayload.Type<DisplayContainerUseLinkPayload> ID = new CustomPacketPayload.Type<>(PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, DisplayContainerUseLinkPayload> CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    DisplayContainerUseLinkPayload::pos,
                    DisplayContainerUseLinkPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}