package myst.synthetic.mixin.client;

import myst.synthetic.client.age.MystClientAgeRenderData;
import net.minecraft.client.renderer.SkyRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {

	@ModifyVariable(
			method = "renderSunMoonAndStars",
			at = @At("HEAD"),
			ordinal = 1,
			argsOnly = true
	)
	private float mystcraft$applyMoonDirection(float vanillaMoonAngle) {
		return MystClientAgeRenderData.adjustMoonAngle(vanillaMoonAngle);
	}
}