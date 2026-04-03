package myst.synthetic.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DisplayContainerExtractPayload(BlockPos pos) implements CustomPacketPayload {

    public static final Identifier PAYLOAD_ID = Identifier.fromNamespaceAndPath("mystcraft-sc", "extract_display_container");
    public static final CustomPacketPayload.Type<DisplayContainerExtractPayload> ID = new CustomPacketPayload.Type<>(PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, DisplayContainerExtractPayload> CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    DisplayContainerExtractPayload::pos,
                    DisplayContainerExtractPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}