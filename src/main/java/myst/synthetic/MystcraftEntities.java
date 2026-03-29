package myst.synthetic;

import myst.synthetic.entity.EntityLinkbook;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public final class MystcraftEntities {

    public static final ResourceKey<EntityType<?>> LINKBOOK_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath("mystcraft-sc", "linkbook")
    );

    public static final EntityType<EntityLinkbook> LINKBOOK = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            LINKBOOK_KEY,
            EntityType.Builder.<EntityLinkbook>of(EntityLinkbook::new, MobCategory.MISC)
                    .sized(0.25F, 0.2F)
                    .clientTrackingRange(8)
                    .updateInterval(10)
                    .build(LINKBOOK_KEY)
    );

    private MystcraftEntities() {
    }

    public static void initialize() {
        FabricDefaultAttributeRegistry.register(LINKBOOK, EntityLinkbook.createAttributes());
    }
}