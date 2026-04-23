package myst.synthetic.world.age;

import myst.synthetic.world.biome.layout.BiomeLayoutKind;
import myst.synthetic.world.terrain.BedrockProfile;
import myst.synthetic.world.terrain.TerrainKind;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record AgeSpec(
        long seed,
        String displayName,
        @Nullable String dimensionUid,
        BiomeLayoutKind biomeLayout,
        TerrainKind terrain,
        Identifier resolvedBiome,
        int resolvedGroundLevel,
        BedrockProfile bedrockProfile,
        List<Identifier> ignoredSymbols
) {

    public AgeSpec {
        displayName = displayName == null ? "" : displayName.trim();
        biomeLayout = Objects.requireNonNullElse(biomeLayout, BiomeLayoutKind.SINGLE_BIOME);
        terrain = Objects.requireNonNullElse(terrain, TerrainKind.FLAT);
        resolvedBiome = resolvedBiome == null
                ? Identifier.fromNamespaceAndPath("minecraft", "plains")
                : resolvedBiome;
        resolvedGroundLevel = Math.max(1, resolvedGroundLevel);
        bedrockProfile = Objects.requireNonNullElse(bedrockProfile, BedrockProfile.VANILLA_FLOOR);
        ignoredSymbols = ignoredSymbols == null ? List.of() : List.copyOf(ignoredSymbols);
    }

    public static AgeSpec createDefault(long seed, String displayName) {
        return new AgeSpec(
                seed,
                displayName,
                null,
                BiomeLayoutKind.SINGLE_BIOME,
                TerrainKind.FLAT,
                Identifier.fromNamespaceAndPath("minecraft", "plains"),
                64,
                BedrockProfile.VANILLA_FLOOR,
                List.of()
        );
    }

    public AgeSpec withDimensionUid(@Nullable String dimensionUid) {
        return new AgeSpec(
                this.seed,
                this.displayName,
                dimensionUid,
                this.biomeLayout,
                this.terrain,
                this.resolvedBiome,
                this.resolvedGroundLevel,
                this.bedrockProfile,
                this.ignoredSymbols
        );
    }

    public AgeSpec withResolvedBiome(Identifier biomeId) {
        return new AgeSpec(
                this.seed,
                this.displayName,
                this.dimensionUid,
                this.biomeLayout,
                this.terrain,
                biomeId,
                this.resolvedGroundLevel,
                this.bedrockProfile,
                this.ignoredSymbols
        );
    }

    public AgeSpec withGroundLevel(int groundLevel) {
        return new AgeSpec(
                this.seed,
                this.displayName,
                this.dimensionUid,
                this.biomeLayout,
                this.terrain,
                this.resolvedBiome,
                groundLevel,
                this.bedrockProfile,
                this.ignoredSymbols
        );
    }

    public AgeSpec withBedrockProfile(BedrockProfile bedrockProfile) {
        return new AgeSpec(
                this.seed,
                this.displayName,
                this.dimensionUid,
                this.biomeLayout,
                this.terrain,
                this.resolvedBiome,
                this.resolvedGroundLevel,
                bedrockProfile,
                this.ignoredSymbols
        );
    }

    public AgeSpec withIgnoredSymbols(List<Identifier> ignoredSymbols) {
        return new AgeSpec(
                this.seed,
                this.displayName,
                this.dimensionUid,
                this.biomeLayout,
                this.terrain,
                this.resolvedBiome,
                this.resolvedGroundLevel,
                this.bedrockProfile,
                ignoredSymbols
        );
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private long seed;
        private String displayName;
        private String dimensionUid;
        private BiomeLayoutKind biomeLayout;
        private TerrainKind terrain;
        private Identifier resolvedBiome;
        private int resolvedGroundLevel;
        private BedrockProfile bedrockProfile;
        private final List<Identifier> ignoredSymbols = new ArrayList<>();

        public Builder(long seed, String displayName) {
            this.seed = seed;
            this.displayName = displayName == null ? "" : displayName.trim();
            this.dimensionUid = null;
            this.biomeLayout = BiomeLayoutKind.SINGLE_BIOME;
            this.terrain = TerrainKind.FLAT;
            this.resolvedBiome = Identifier.fromNamespaceAndPath("minecraft", "plains");
            this.resolvedGroundLevel = 64;
            this.bedrockProfile = BedrockProfile.VANILLA_FLOOR;
        }

        public Builder(AgeSpec spec) {
            this.seed = spec.seed;
            this.displayName = spec.displayName;
            this.dimensionUid = spec.dimensionUid;
            this.biomeLayout = spec.biomeLayout;
            this.terrain = spec.terrain;
            this.resolvedBiome = spec.resolvedBiome;
            this.resolvedGroundLevel = spec.resolvedGroundLevel;
            this.bedrockProfile = spec.bedrockProfile;
            this.ignoredSymbols.addAll(spec.ignoredSymbols);
        }

        public Builder seed(long seed) {
            this.seed = seed;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName == null ? "" : displayName.trim();
            return this;
        }

        public Builder dimensionUid(@Nullable String dimensionUid) {
            this.dimensionUid = dimensionUid;
            return this;
        }

        public Builder biomeLayout(BiomeLayoutKind biomeLayout) {
            this.biomeLayout = biomeLayout;
            return this;
        }

        public Builder terrain(TerrainKind terrain) {
            this.terrain = terrain;
            return this;
        }

        public Builder resolvedBiome(Identifier resolvedBiome) {
            this.resolvedBiome = resolvedBiome;
            return this;
        }

        public Builder resolvedGroundLevel(int resolvedGroundLevel) {
            this.resolvedGroundLevel = resolvedGroundLevel;
            return this;
        }

        public Builder bedrockProfile(BedrockProfile bedrockProfile) {
            this.bedrockProfile = bedrockProfile;
            return this;
        }

        public Builder addIgnoredSymbol(Identifier symbolId) {
            if (symbolId != null) {
                this.ignoredSymbols.add(symbolId);
            }
            return this;
        }

        public Builder ignoredSymbols(List<Identifier> ignoredSymbols) {
            this.ignoredSymbols.clear();
            if (ignoredSymbols != null) {
                this.ignoredSymbols.addAll(ignoredSymbols);
            }
            return this;
        }

        public AgeSpec build() {
            return new AgeSpec(
                    this.seed,
                    this.displayName,
                    this.dimensionUid,
                    this.biomeLayout,
                    this.terrain,
                    this.resolvedBiome,
                    this.resolvedGroundLevel,
                    this.bedrockProfile,
                    this.ignoredSymbols
            );
        }
    }
}