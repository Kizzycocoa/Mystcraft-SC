package myst.synthetic.api.linking;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Modernized legacy Mystcraft link description.
 *
 * Intentionally keeps legacy method names where practical so later ports
 * of Star Fissure, linking books, and /tpx remain recognisably Mystcraft.
 */
public interface ILinkInfo {

    @NotNull
    String getDisplayName();

    void setDisplayName(@NotNull String displayName);

    /**
     * Legacy name retained.
     *
     * In old Mystcraft this was an integer dimension id.
     * In the modern port this is a namespaced dimension id string,
     * e.g. "minecraft:overworld" or "mystcraft-sc:age_1".
     */
    @Nullable
    String getDimensionUID();

    void setDimensionUID(@Nullable String uid);

    @Nullable
    UUID getTargetUUID();

    void setTargetUUID(@Nullable UUID uuid);

    @Nullable
    BlockPos getSpawn();

    void setSpawn(@Nullable BlockPos spawn);

    float getSpawnYaw();

    void setSpawnYaw(float spawnYaw);

    boolean getFlag(String flag);

    void setFlag(String flag, boolean value);

    @Nullable
    String getProperty(String property);

    void setProperty(String property, @Nullable String value);

    @NotNull
    CompoundTag getTagCompound();

    @NotNull
    ILinkInfo clone();
}