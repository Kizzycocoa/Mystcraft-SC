package myst.synthetic.item;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class LinkBookData {

	private LinkBookData() {}

	public static CompoundTag createLegacyStyleLinkTag(Level level, Player player) {
		CompoundTag tag = new CompoundTag();

		String dimensionId = extractDimensionId(level);
		String ageName = prettifyDimensionName(dimensionId);

		BlockPos pos = player.blockPosition();

		tag.putString("Author", player.getName().getString());
		// Destination identity
		tag.putString("AgeName", ageName);
		tag.putString("Dimension", dimensionId);

		// Destination coordinates
		tag.putInt("SpawnX", pos.getX());
		tag.putInt("SpawnY", pos.getY());
		tag.putInt("SpawnZ", pos.getZ());
		tag.putFloat("SpawnYaw", player.getYRot());

		// Book durability (legacy-compatible)
		tag.putFloat("damage", 0.0F);
		tag.putFloat("MaxHealth", 10.0F);

		// Extension containers (legacy Mystcraft behavior)
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

	private static String extractDimensionId(Level level) {
		String raw = level.dimension().toString();

		int slash = raw.lastIndexOf('/');
		int end = raw.lastIndexOf(']');

		if (slash >= 0 && end > slash) {
			return raw.substring(slash + 1, end).trim();
		}

		return raw;
	}

	private static String prettifyDimensionName(String dimensionId) {
		String path = dimensionId;

		int colon = path.indexOf(':');
		if (colon >= 0 && colon + 1 < path.length()) {
			path = path.substring(colon + 1);
		}

		path = path.replace('_', ' ').trim();

		if (path.isEmpty()) {
			return "Unknown Age";
		}

		String[] parts = path.split("\\s+");
		StringBuilder out = new StringBuilder();

		for (int i = 0; i < parts.length; i++) {
			String part = parts[i];
			if (part.isEmpty()) {
				continue;
			}

			if (out.length() > 0) {
				out.append(' ');
			}

			out.append(Character.toUpperCase(part.charAt(0)));
			if (part.length() > 1) {
				out.append(part.substring(1));
			}
		}

		return out.toString();
	}
}