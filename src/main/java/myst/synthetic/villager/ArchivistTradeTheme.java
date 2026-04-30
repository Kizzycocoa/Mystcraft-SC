package myst.synthetic.villager;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerType;

import java.util.List;

public enum ArchivistTradeTheme {
    PLAINS("plains", "neutral", List.of("baby_blue", "grey", "pink")),
    SNOWY("snowy", "cold", List.of("white", "cyan", "silver")),
    TAIGA("taiga", "cold", List.of("teal", "blue", "navy")),
    DESERT("desert", "hot", List.of("yellow", "red", "maroon")),
    SAVANNA("savanna", "hot", List.of("orange", "citrus", "lime")),
    SWAMP("swamp", "neutral", List.of("purple", "magenta", "dark_grey", "black")),
    JUNGLE("jungle", "hot", List.of("olive", "dark_green", "green"));

    private final String biomeTag;
    private final String temperatureTag;
    private final List<String> crystalColors;

    ArchivistTradeTheme(String biomeTag, String temperatureTag, List<String> crystalColors) {
        this.biomeTag = biomeTag;
        this.temperatureTag = temperatureTag;
        this.crystalColors = crystalColors;
    }

    public String biomeTag() {
        return biomeTag;
    }

    public String temperatureTag() {
        return temperatureTag;
    }

    public List<String> crystalColors() {
        return crystalColors;
    }

    public static ArchivistTradeTheme fromVillager(Villager villager) {
        return villager.getVillagerData().type().unwrapKey()
                .map(ResourceKey::identifier)
                .map(id -> switch (id.getPath()) {
                    case "desert" -> DESERT;
                    case "savanna" -> SAVANNA;
                    case "taiga" -> TAIGA;
                    case "snow" -> SNOWY;
                    case "swamp" -> SWAMP;
                    case "jungle" -> JUNGLE;
                    default -> PLAINS;
                })
                .orElse(PLAINS);
    }
}