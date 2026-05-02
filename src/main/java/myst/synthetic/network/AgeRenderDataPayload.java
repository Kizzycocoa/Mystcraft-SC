package myst.synthetic.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record AgeRenderDataPayload(
        String dimensionId,
        boolean hasCloudHeight,
        float cloudHeight
) implements CustomPacketPayload {

    public static final Identifier PAYLOAD_ID = Identifier.fromNamespaceAndPath("mystcraft-sc", "age_render_data");
    public static final CustomPacketPayload.Type<AgeRenderDataPayload> ID = new CustomPacketPayload.Type<>(PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, AgeRenderDataPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    AgeRenderDataPayload::dimensionId,
                    ByteBufCodecs.BOOL,
                    AgeRenderDataPayload::hasCloudHeight,
                    ByteBufCodecs.FLOAT,
                    AgeRenderDataPayload::cloudHeight,
                    AgeRenderDataPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}