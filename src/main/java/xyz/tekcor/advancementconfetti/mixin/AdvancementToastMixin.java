package xyz.tekcor.advancementconfetti.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import xyz.tekcor.advancementconfetti.client.AdvancementConfettiClient;

@Mixin(AdvancementToast.class)
public abstract class AdvancementToastMixin {

	@Shadow
	private boolean isChallengeAdvancement() {
		throw new UnsupportedOperationException();
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void advancementconfetti$onShown(AdvancementHolder advancement, CallbackInfo ci) {
		String title = advancement.value().display()
				.map(display -> display.getTitle().getString())
				.orElseGet(() -> advancement.id().toString());

		AdvancementConfettiClient.celebrate(this.isChallengeAdvancement(), title);
	}
}
