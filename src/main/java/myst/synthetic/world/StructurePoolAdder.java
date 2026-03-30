package myst.synthetic.world;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import myst.synthetic.mixin.StructureTemplatePoolAccessor;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.ArrayList;
import java.util.List;

public final class StructurePoolAdder {

    private StructurePoolAdder() {
    }

    public static void inject(MinecraftServer server) {
        HolderLookup.RegistryLookup<StructureTemplatePool> pools =
                server.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);

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

        addToPool(pools, "trial_chambers/hallway", "mystcraft-sc:trial_chambers/hallway/mystcraft_book", 1);
    }

    private static void addToPool(
            HolderLookup.RegistryLookup<StructureTemplatePool> pools,
            String poolPath,
            String structureId,
            int weight
    ) {
        ResourceKey<StructureTemplatePool> poolKey = ResourceKey.create(
                Registries.TEMPLATE_POOL,
                Identifier.fromNamespaceAndPath("minecraft", poolPath)
        );

        var poolHolder = pools.get(poolKey).orElse(null);
        if (poolHolder == null) {
            return;
        }

        StructureTemplatePool pool = poolHolder.value();

        StructurePoolElement element = SinglePoolElement.legacy(structureId)
                .apply(StructureTemplatePool.Projection.RIGID);

        StructureTemplatePoolAccessor accessor = (StructureTemplatePoolAccessor) pool;

        ObjectArrayList<StructurePoolElement> newTemplates =
                new ObjectArrayList<>(accessor.mystcraft$getTemplates());
        for (int i = 0; i < weight; i++) {
            newTemplates.add(element);
        }
        accessor.mystcraft$setTemplates(newTemplates);

        List<Pair<StructurePoolElement, Integer>> newRawTemplates =
                new ArrayList<>(accessor.mystcraft$getRawTemplates());
        newRawTemplates.add(Pair.of(element, weight));
        accessor.mystcraft$setRawTemplates(newRawTemplates);
    }
}