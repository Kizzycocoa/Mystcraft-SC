package myst.synthetic;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

public final class MystcraftVillagerProfessions {

    public static final Identifier ARCHIVIST_ID =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "archivist");

    public static final VillagerProfession ARCHIVIST = Registry.register(
            BuiltInRegistries.VILLAGER_PROFESSION,
            ARCHIVIST_ID,
            new VillagerProfession(
                    Component.translatable("entity.mystcraft-sc.villager.archivist"),
                    holder -> holder.is(MystcraftPoiTypes.ARCHIVIST_DESK_KEY),
                    holder -> holder.is(MystcraftPoiTypes.ARCHIVIST_DESK_KEY),
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    SoundEvents.VILLAGER_WORK_LIBRARIAN
            )
    );

    private MystcraftVillagerProfessions() {
    }

    public static void initialize() {
    }
}