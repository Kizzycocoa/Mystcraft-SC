package myst.synthetic.mixin.client;

import myst.synthetic.client.render.BookstandItemSpecialRendererUnbaked;
import myst.synthetic.client.render.SlantBoardItemSpecialRendererUnbaked;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpecialModelRenderers.class)
public abstract class SpecialModelRenderersMixin {

	@Inject(method = "bootstrap", at = @At("TAIL"))
	private static void mystcraft$registerSpecialRenderers(CallbackInfo ci) {
		SpecialModelRenderers.ID_MAPPER.put(
				Identifier.fromNamespaceAndPath("mystcraft-sc", "slant_board"),
				SlantBoardItemSpecialRendererUnbaked.MAP_CODEC
		);

		SpecialModelRenderers.ID_MAPPER.put(
				Identifier.fromNamespaceAndPath("mystcraft-sc", "bookstand"),
				BookstandItemSpecialRendererUnbaked.MAP_CODEC
		);
	}
}