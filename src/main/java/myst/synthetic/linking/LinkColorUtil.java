package myst.synthetic.linking;

import java.util.Random;
import myst.synthetic.item.BookBookmarkUtil;
import myst.synthetic.item.BookmarkColorUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

public final class LinkColorUtil {

    private LinkColorUtil() {
    }

    public static int getPortalColor(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0xFFFFFFFF;
        }

        ItemStack bookmark = BookBookmarkUtil.getBookmark(stack);
        if (!bookmark.isEmpty()) {
            return 0xFF000000 | BookmarkColorUtil.getColor(bookmark);
        }

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return 0xFFFFFFFF;
        }

        return getPortalColor(customData.copyTag());
    }

    public static int getPortalColor(@Nullable CompoundTag tag) {
        if (tag == null) {
            return 0xFFFFFFFF;
        }

        String seedString = getBestSeedString(tag);
        Random random = new Random(seedString.hashCode());

        int color = 0xFF000000;
        color |= random.nextInt(256);
        color |= random.nextInt(256) << 8;
        color |= random.nextInt(256) << 16;
        return color;
    }

    private static String getBestSeedString(CompoundTag tag) {
        if (tag.contains("DisplayName")) {
            String value = tag.getString("DisplayName").orElse("");
            if (!value.isBlank()) {
                return value;
            }
        }

        if (tag.contains("AgeName")) {
            String value = tag.getString("AgeName").orElse("");
            if (!value.isBlank()) {
                return value;
            }
        }

        if (tag.contains("agename")) {
            String value = tag.getString("agename").orElse("");
            if (!value.isBlank()) {
                return value;
            }
        }

        if (tag.contains("Dimension")) {
            String value = tag.getString("Dimension").orElse("");
            if (!value.isBlank()) {
                return value;
            }
        }

        return "???";
    }
}