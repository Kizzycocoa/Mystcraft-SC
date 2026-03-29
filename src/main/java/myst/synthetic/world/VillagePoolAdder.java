package myst.synthetic.world;

import com.mojang.datafixers.util.Pair;
import myst.synthetic.mixin.StructureTemplatePoolAccessor;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.ArrayList;
import java.util.List;

public final class VillagePoolAdder {

    private VillagePoolAdder() {
    }

    public static void inject(MinecraftServer server) {
        var pools = server.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);

        addToPool(pools, "village/plains/houses", "mystcraft-sc:village/plains/houses/plains_atelier_1", 6);
        addToPool(pools, "village/plains/houses", "mystcraft-sc:village/plains/houses/plains_atelier_2", 6);
        addToPool(pools, "village/plains/houses", "mystcraft-sc:village/plains/houses/plains_legacy_atelier", 1);

        addToPool(pools, "village/desert/houses", "mystcraft-sc:village/desert/houses/desert_atelier_1", 6);
        addToPool(pools, "village/desert/houses", "mystcraft-sc:village/desert/houses/desert_atelier_2", 6);

        addToPool(pools, "village/savanna/houses", "mystcraft-sc:village/savanna/houses/savanna_atelier_1", 6);
        addToPool(pools, "village/savanna/houses", "mystcraft-sc:village/savanna/houses/savanna_atelier_2", 6);

        addToPool(pools, "village/snowy/houses", "mystcraft-sc:village/snowy/houses/snowy_atelier_1", 6);
        addToPool(pools, "village/snowy/houses", "mystcraft-sc:village/snowy/houses/snowy_atelier_2", 6);

        addToPool(pools, "village/taiga/houses", "mystcraft-sc:village/taiga/houses/taiga_atelier_1", 6);
        addToPool(pools, "village/taiga/houses", "mystcraft-sc:village/taiga/houses/taiga_atelier_2", 6);
    }

    private static void addToPool(
            net.minecraft.core.HolderLookup.RegistryLookup<StructureTemplatePool> pools,
            String poolPath,
            String structureId,
            int weight
    ) {
        var poolKey = net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.TEMPLATE_POOL,
                net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", poolPath)
        );

        var poolHolder = pools.get(poolKey).orElse(null);
        if (poolHolder == null) {
            return;
        }

        StructureTemplatePool pool = poolHolder.value();

        StructurePoolElement element = SinglePoolElement.legacy(structureId)
                .apply(StructureTemplatePool.Projection.RIGID);

        StructureTemplatePoolAccessor accessor = (StructureTemplatePoolAccessor) pool;

        for (int i = 0; i < weight; i++) {
            accessor.mystcraft$getTemplates().add(element);
        }

        var rawTemplates = new java.util.ArrayList<>(accessor.mystcraft$getRawTemplates());
        rawTemplates.add(Pair.of(element, weight));
        accessor.mystcraft$setRawTemplates(rawTemplates);
    }
}