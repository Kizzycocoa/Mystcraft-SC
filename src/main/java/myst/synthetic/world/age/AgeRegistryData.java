package myst.synthetic.world.age;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class AgeRegistryData extends SavedData {

    public static final String DATA_NAME = "mystcraft_sc_age_registry";

    public static final Codec<AgeEntry> AGE_ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("dimension_uid").forGetter(AgeEntry::dimensionUid),
            Codec.STRING.optionalFieldOf("target_uuid").forGetter(entry -> Optional.ofNullable(entry.targetUuid)),
            Codec.LONG.fieldOf("seed").forGetter(AgeEntry::seed),
            Codec.STRING.optionalFieldOf("display_name", "").forGetter(AgeEntry::displayName)
    ).apply(instance, (dimensionUid, targetUuid, seed, displayName) ->
            new AgeEntry(dimensionUid, targetUuid.orElse(null), seed, displayName)
    ));

    public static final Codec<AgeRegistryData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("next_dimension_index", 1).forGetter(AgeRegistryData::nextDimensionIndex),
            Codec.unboundedMap(Codec.STRING, AGE_ENTRY_CODEC)
                    .optionalFieldOf("ages", Map.of())
                    .forGetter(AgeRegistryData::ages)
    ).apply(instance, AgeRegistryData::new));

    public static final SavedDataType<AgeRegistryData> TYPE = new SavedDataType<>(
            DATA_NAME,
            AgeRegistryData::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private int nextDimensionIndex;
    private final Map<String, AgeEntry> ages;

    public AgeRegistryData() {
        this(1, new LinkedHashMap<>());
    }

    private AgeRegistryData(int nextDimensionIndex, Map<String, AgeEntry> ages) {
        this.nextDimensionIndex = Math.max(1, nextDimensionIndex);
        this.ages = new LinkedHashMap<>();
        if (ages != null && !ages.isEmpty()) {
            ages.forEach((key, value) -> {
                if (key != null && !key.isBlank() && value != null) {
                    this.ages.put(normalizeKey(key), value.normalized());
                }
            });
        }

        this.nextDimensionIndex = Math.max(this.nextDimensionIndex, computeNextIndexFloor());
    }

    public static AgeRegistryData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public int nextDimensionIndex() {
        return this.nextDimensionIndex;
    }

    public Map<String, AgeEntry> ages() {
        return Map.copyOf(this.ages);
    }

    public boolean containsDimension(String dimensionUid) {
        return this.ages.containsKey(normalizeKey(dimensionUid));
    }

    @Nullable
    public AgeEntry getByDimensionUid(String dimensionUid) {
        return this.ages.get(normalizeKey(dimensionUid));
    }

    @Nullable
    public AgeEntry getByTargetUuid(UUID targetUuid) {
        if (targetUuid == null) {
            return null;
        }

        String needle = targetUuid.toString();
        for (AgeEntry entry : this.ages.values()) {
            if (needle.equals(entry.targetUuid)) {
                return entry;
            }
        }

        return null;
    }

    public AgeEntry reserveNewAge(long seed, @Nullable String displayName) {
        String dimensionUid = AgeStoragePaths.buildDimensionUid(this.nextDimensionIndex++);
        UUID targetUuid = UUID.randomUUID();

        AgeEntry entry = new AgeEntry(
                dimensionUid,
                targetUuid.toString(),
                seed,
                displayName == null ? "" : displayName.trim()
        ).normalized();

        this.ages.put(normalizeKey(dimensionUid), entry);
        this.setDirty();
        return entry;
    }

    public AgeEntry reserveOrGetExisting(@Nullable UUID targetUuid, long seed, @Nullable String displayName) {
        if (targetUuid != null) {
            AgeEntry existing = this.getByTargetUuid(targetUuid);
            if (existing != null) {
                return existing;
            }
        }

        return reserveNewAge(seed, displayName);
    }

    public void put(AgeEntry entry) {
        Objects.requireNonNull(entry, "entry");
        this.ages.put(normalizeKey(entry.dimensionUid), entry.normalized());
        this.nextDimensionIndex = Math.max(this.nextDimensionIndex, computeNextIndexFloor());
        this.setDirty();
    }

    public void updateDisplayName(String dimensionUid, @Nullable String displayName) {
        AgeEntry existing = this.getByDimensionUid(dimensionUid);
        if (existing == null) {
            return;
        }

        this.ages.put(
                normalizeKey(dimensionUid),
                new AgeEntry(
                        existing.dimensionUid,
                        existing.targetUuid,
                        existing.seed,
                        displayName == null ? "" : displayName.trim()
                ).normalized()
        );
        this.setDirty();
    }

    public void bindTargetUuid(String dimensionUid, UUID targetUuid) {
        AgeEntry existing = this.getByDimensionUid(dimensionUid);
        if (existing == null) {
            return;
        }

        this.ages.put(
                normalizeKey(dimensionUid),
                new AgeEntry(
                        existing.dimensionUid,
                        targetUuid == null ? null : targetUuid.toString(),
                        existing.seed,
                        existing.displayName
                ).normalized()
        );
        this.setDirty();
    }

    private int computeNextIndexFloor() {
        int max = 1;

        for (AgeEntry entry : this.ages.values()) {
            String uid = entry.dimensionUid;
            if (uid == null) {
                continue;
            }

            String normalized = AgeStoragePaths.normalizeDimensionUid(uid);
            if (!normalized.startsWith(AgeStoragePaths.DIMENSION_PREFIX)) {
                continue;
            }

            String suffix = normalized.substring(AgeStoragePaths.DIMENSION_PREFIX.length());
            try {
                int parsed = Integer.parseInt(suffix);
                max = Math.max(max, parsed + 1);
            } catch (NumberFormatException ignored) {
            }
        }

        return max;
    }

    private static String normalizeKey(String dimensionUid) {
        return AgeStoragePaths.normalizeDimensionUid(dimensionUid);
    }

    public record AgeEntry(
            String dimensionUid,
            @Nullable String targetUuid,
            long seed,
            String displayName
    ) {
        public AgeEntry normalized() {
            return new AgeEntry(
                    AgeStoragePaths.normalizeDimensionUid(this.dimensionUid),
                    normalizeString(this.targetUuid),
                    this.seed,
                    this.displayName == null ? "" : this.displayName.trim()
            );
        }

        @Nullable
        public UUID targetUuidAsUuid() {
            if (this.targetUuid == null || this.targetUuid.isBlank()) {
                return null;
            }

            try {
                return UUID.fromString(this.targetUuid);
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }

        @Nullable
        private static String normalizeString(@Nullable String value) {
            if (value == null) {
                return null;
            }

            String trimmed = value.trim();
            return trimmed.isEmpty() ? null : trimmed;
        }
    }
}