package myst.synthetic.mixin.client;

import myst.synthetic.client.age.MystClientAgeRenderData;
import net.minecraft.client.renderer.CloudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CloudRenderer.class)
public abstract class CloudRendererMixin {

	@ModifyVariable(
			method = "renderClouds",
			at = @At("HEAD"),
			ordinal = 0,
			argsOnly = true
	)
	private float mystcraft$applyAgeCloudHeight(float vanillaCloudHeight) {
		return MystClientAgeRenderData.adjustCloudHeight(vanillaCloudHeight);
	}
}