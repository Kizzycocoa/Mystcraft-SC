package myst.synthetic.mixin.client;

import myst.synthetic.MystcraftSyntheticCodex;
import myst.synthetic.client.age.MystClientAgeRenderData;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CloudRenderer.class)
public abstract class CloudRendererMixin {

	@Unique
	private static boolean mystcraft$loggedCloudRenderCall = false;

	@Inject(method = "render", at = @At("HEAD"))
	private void mystcraft$debugCloudRenderCall(
			int color,
			CloudStatus mode,
			float cloudHeight,
			Vec3 cameraPos,
			float cloudPhase,
			CallbackInfo ci
	) {
		if (!mystcraft$loggedCloudRenderCall) {
			mystcraft$loggedCloudRenderCall = true;

			MystcraftSyntheticCodex.LOGGER.info(
					"[MystAgeRenderClient] CloudRenderer.render called: mode={}, vanillaCloudHeight={}, adjustedCloudHeight={}, cameraY={}, hasAgeCloudHeight={}",
					mode,
					cloudHeight,
					MystClientAgeRenderData.adjustCloudHeight(cloudHeight),
					cameraPos.y,
					MystClientAgeRenderData.hasCloudHeight()
			);
		}
	}

	@ModifyVariable(
			method = "render",
			at = @At("HEAD"),
			index = 3,
			argsOnly = true
	)
	private float mystcraft$applyAgeCloudHeight(float vanillaCloudHeight) {
		return MystClientAgeRenderData.adjustCloudHeight(vanillaCloudHeight);
	}
}