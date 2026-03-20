package myst.synthetic.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class LinkBookData {

	private LinkBookData() {}

	public static CompoundTag createLegacyStyleLinkTag(Level level, Player player) {
		CompoundTag tag = new CompoundTag();

		String dimensionId = level.dimension().toString();
		BlockPos pos = player.blockPosition();

		tag.putString("TargetUUID", dimensionId.toString());
		tag.putString("DisplayName", dimensionId.toString());
		tag.putString("DimensionName", dimensionId.toString());
		tag.putString("Dimension", dimensionId.toString());

		tag.putInt("SpawnX", pos.getX());
		tag.putInt("SpawnY", pos.getY());
		tag.putInt("SpawnZ", pos.getZ());
		tag.putFloat("SpawnYaw", player.getYRot());

		tag.put("Flags", new CompoundTag());
		tag.put("Props", new CompoundTag());

		return tag;
	}

	public static void copyPanelFlags(CompoundTag fromTag, CompoundTag toTag) {
		if (fromTag == null) {
			return;
		}

		if (fromTag.contains("Flags")) {
			CompoundTag flags = fromTag.getCompound("Flags").orElse(new CompoundTag());
			toTag.put("Flags", flags);
		}

		if (fromTag.contains("Props")) {
			CompoundTag props = fromTag.getCompound("Props").orElse(new CompoundTag());
			toTag.put("Props", props);
		}
	}
}