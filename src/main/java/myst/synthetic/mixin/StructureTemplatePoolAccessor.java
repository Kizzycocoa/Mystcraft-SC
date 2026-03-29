package myst.synthetic.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(StructureTemplatePool.class)
public interface StructureTemplatePoolAccessor {

	@Accessor("templates")
	List<StructurePoolElement> mystcraft$getTemplates();

	@Accessor("rawTemplates")
	List<Pair<StructurePoolElement, Integer>> mystcraft$getRawTemplates();

	@Accessor("rawTemplates")
	@Mutable
	void mystcraft$setRawTemplates(List<Pair<StructurePoolElement, Integer>> rawTemplates);
}