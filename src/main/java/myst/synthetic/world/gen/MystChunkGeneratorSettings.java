package myst.synthetic.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import myst.synthetic.world.terrain.BedrockProfile;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;

import java.util.Locale;

public record MystChunkGeneratorSettings(
        long seed,
        Holder<Biome> biome,
        String biomeId,
        int groundLevel,
        int seaLevel,
        BedrockProfile bedrockProfile
) {

    private static final Codec<BedrockProfile> BEDROCK_PROFILE_CODEC = Codec.STRING.xmap(
            MystChunkGeneratorSettings::parseBedrockProfile,
            MystChunkGeneratorSettings::serializeBedrockProfile
    );

    public static final Codec<MystChunkGeneratorSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.optionalFieldOf("seed", 0L).forGetter(MystChunkGeneratorSettings::seed),
            Biome.CODEC.fieldOf("biome").forGetter(MystChunkGeneratorSettings::biome),
            Codec.STRING.optionalFieldOf("biome_id", "minecraft:plains").forGetter(MystChunkGeneratorSettings::biomeId),
            Codec.INT.optionalFieldOf("ground_level", 64).forGetter(MystChunkGeneratorSettings::groundLevel),
            Codec.INT.optionalFieldOf("sea_level", 63).forGetter(MystChunkGeneratorSettings::seaLevel),
            BEDROCK_PROFILE_CODEC.optionalFieldOf("bedrock_profile", BedrockProfile.VANILLA_FLOOR)
                    .forGetter(MystChunkGeneratorSettings::bedrockProfile)
    ).apply(instance, MystChunkGeneratorSettings::new));

    public MystChunkGeneratorSettings {
        biomeId = normalizeBiomeId(biomeId);
        groundLevel = Math.max(1, groundLevel);
        seaLevel = Math.max(groundLevel, seaLevel);
        bedrockProfile = bedrockProfile == null ? BedrockProfile.VANILLA_FLOOR : bedrockProfile;
    }

    public static MystChunkGeneratorSettings create(
            long seed,
            Holder<Biome> biome,
            Identifier biomeId,
            int groundLevel,
            int seaLevel,
            BedrockProfile bedrockProfile
    ) {
        return new MystChunkGeneratorSettings(
                seed,
                biome,
                biomeId == null ? "minecraft:plains" : biomeId.toString(),
                groundLevel,
                seaLevel,
                bedrockProfile
        );
    }

    public Identifier biomeIdentifier() {
        return Identifier.tryParse(this.biomeId) != null
                ? Identifier.tryParse(this.biomeId)
                : Identifier.fromNamespaceAndPath("minecraft", "plains");
    }

    private static BedrockProfile parseBedrockProfile(String value) {
        if (value == null || value.isBlank()) {
            return BedrockProfile.VANILLA_FLOOR;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        try {
            return BedrockProfile.valueOf(normalized);
        } catch (IllegalArgumentException ignored) {
            return BedrockProfile.VANILLA_FLOOR;
        }
    }

    private static String serializeBedrockProfile(BedrockProfile profile) {
        return profile == null
                ? "vanilla_floor"
                : profile.name().toLowerCase(Locale.ROOT);
    }

    private static String normalizeBiomeId(String biomeId) {
        if (biomeId == null || biomeId.isBlank()) {
            return "minecraft:plains";
        }

        Identifier parsed = Identifier.tryParse(biomeId.trim());
        return parsed == null ? "minecraft:plains" : parsed.toString();
    }
}