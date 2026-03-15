package myst.synthetic.linking;

import myst.synthetic.api.linking.ILinkInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Modernized version of the old Mystcraft LinkOptions container.
 *
 * Still stores its payload in CompoundTag so it remains easy to serialize later.
 * This also keeps the port visually and structurally close to legacy Mystcraft.
 */
public class LinkOptions implements ILinkInfo {

    private CompoundTag data;

    public LinkOptions(@Nullable CompoundTag data) {
        this.data = data != null ? data.copy() : new CompoundTag();
    }

    @Override
    public @NotNull CompoundTag getTagCompound() {
        return this.data.copy();
    }

    @Override
    public @NotNull ILinkInfo clone() {
        return new LinkOptions(this.data);
    }

    @Override
    public @NotNull String getDisplayName() {
        return getDisplayName(this.data);
    }

    @Override
    public void setDisplayName(@NotNull String displayName) {
        this.data = setDisplayName(this.data, displayName);
    }

    @Override
    public @Nullable String getDimensionUID() {
        return getDimensionUID(this.data);
    }

    @Override
    public void setDimensionUID(@Nullable String uid) {
        this.data = setDimensionUID(this.data, uid);
    }

    @Override
    public @Nullable UUID getTargetUUID() {
        return getUUID(this.data);
    }

    @Override
    public void setTargetUUID(@Nullable UUID uuid) {
        this.data = setUUID(this.data, uuid);
    }

    @Override
    public @Nullable BlockPos getSpawn() {
        return getSpawn(this.data);
    }

    @Override
    public void setSpawn(@Nullable BlockPos spawn) {
        this.data = setSpawn(this.data, spawn);
    }

    @Override
    public float getSpawnYaw() {
        return getSpawnYaw(this.data);
    }

    @Override
    public void setSpawnYaw(float spawnYaw) {
        this.data = setSpawnYaw(this.data, spawnYaw);
    }

    @Override
    public boolean getFlag(String flag) {
        return getFlag(this.data, flag);
    }

    @Override
    public void setFlag(String flag, boolean value) {
        this.data = setFlag(this.data, flag, value);
    }

    @Override
    public @Nullable String getProperty(String property) {
        return getProperty(this.data, property);
    }

    @Override
    public void setProperty(String property, @Nullable String value) {
        this.data = setProperty(this.data, property, value);
    }

    public static CompoundTag setDisplayName(@Nullable CompoundTag tag, @NotNull String name) {
        CompoundTag out = ensureTag(tag);
        out.putString("DisplayName", name);
        return out;
    }

    public static @NotNull String getDisplayName(@Nullable CompoundTag tag) {
        if (tag != null && tag.contains("DisplayName")) {
            return tag.getString("DisplayName").orElse("???");
        }
        if (tag != null && tag.contains("agename")) {
            return tag.getString("agename").orElse("???");
        }
        return "???";
    }

    public static CompoundTag setFlag(@Nullable CompoundTag tag, String flag, boolean value) {
        CompoundTag out = ensureTag(tag);
        getFlagCompound(out).putBoolean(flag, value);
        return out;
    }

    public static boolean getFlag(@Nullable CompoundTag tag, String flag) {
        return tag != null && getFlagCompound(tag).getBoolean(flag).orElse(false);
    }

    public static CompoundTag setProperty(@Nullable CompoundTag tag, String property, @Nullable String value) {
        CompoundTag out = ensureTag(tag);
        CompoundTag props = getPropertyCompound(out);

        if (value == null) {
            props.remove(property);
        } else {
            props.putString(property, value);
        }

        return out;
    }

    public static @Nullable String getProperty(@Nullable CompoundTag tag, String property) {
        if (tag != null && getPropertyCompound(tag).contains(property)) {
            return getPropertyCompound(tag).getString(property).orElse(null);
        }
        return null;
    }

    public static CompoundTag setDimensionUID(@Nullable CompoundTag tag, @Nullable String uid) {
        CompoundTag out = ensureTag(tag);

        if (uid == null || uid.isBlank()) {
            out.remove("Dimension");
        } else {
            out.putString("Dimension", uid);
        }

        return out;
    }

    public static @Nullable String getDimensionUID(@Nullable CompoundTag tag) {
        if (tag != null && tag.contains("Dimension")) {
            String value = tag.getString("Dimension").orElse("");
            if (!value.isBlank()) {
                return value;
            }
        }

        // TODO: Legacy integer dimension compatibility importer.
        // Old Mystcraft also looked for "AgeUID"/int ids.
        // This port has intentionally moved to namespaced dimension ids.
        return null;
    }

    public static CompoundTag setUUID(@Nullable CompoundTag tag, @Nullable UUID uuid) {
        CompoundTag out = ensureTag(tag);

        if (uuid == null) {
            out.remove("TargetUUID");
        } else {
            out.putString("TargetUUID", uuid.toString());
        }

        return out;
    }

    public static @Nullable UUID getUUID(@Nullable CompoundTag tag) {
        if (tag != null && tag.contains("TargetUUID")) {
            String value = tag.getString("TargetUUID").orElse("");
            if (!value.isBlank()) {
                try {
                    return UUID.fromString(value);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return null;
    }

    public static CompoundTag setSpawn(@Nullable CompoundTag tag, @Nullable BlockPos pos) {
        CompoundTag out = ensureTag(tag);

        if (pos == null) {
            out.remove("SpawnX");
            out.remove("SpawnY");
            out.remove("SpawnZ");
        } else {
            out.putInt("SpawnX", pos.getX());
            out.putInt("SpawnY", pos.getY());
            out.putInt("SpawnZ", pos.getZ());
        }

        return out;
    }

    public static @Nullable BlockPos getSpawn(@Nullable CompoundTag tag) {
        if (tag != null && tag.contains("SpawnX") && tag.contains("SpawnY") && tag.contains("SpawnZ")) {
            return new BlockPos(
                    tag.getInt("SpawnX").orElse(0),
                    tag.getInt("SpawnY").orElse(64),
                    tag.getInt("SpawnZ").orElse(0)
            );
        }
        return null;
    }

    public static CompoundTag setSpawnYaw(@Nullable CompoundTag tag, float yaw) {
        CompoundTag out = ensureTag(tag);
        out.putFloat("SpawnYaw", yaw);
        return out;
    }

    public static float getSpawnYaw(@Nullable CompoundTag tag) {
        if (tag != null && tag.contains("SpawnYaw")) {
            return tag.getFloat("SpawnYaw").orElse(180.0F);
        }
        return 180.0F;
    }

    private static CompoundTag ensureTag(@Nullable CompoundTag tag) {
        return tag == null ? new CompoundTag() : tag;
    }

    private static CompoundTag getFlagCompound(CompoundTag tag) {
        if (!tag.contains("Flags")) {
            tag.put("Flags", new CompoundTag());
        }
        return tag.getCompound("Flags").orElseGet(CompoundTag::new);
    }

    private static CompoundTag getPropertyCompound(CompoundTag tag) {
        if (!tag.contains("Props")) {
            tag.put("Props", new CompoundTag());
        }
        return tag.getCompound("Props").orElseGet(CompoundTag::new);
    }
}