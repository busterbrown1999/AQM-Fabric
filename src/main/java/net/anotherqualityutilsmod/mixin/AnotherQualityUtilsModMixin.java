package net.anotherqualityutilsmod.mixin;

import net.anotherqualityutilsmod.AnotherQualityUtilsMod;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class AnotherQualityUtilsModMixin {
	@Inject(at = @At("HEAD"), method = "init()V")
	private void init(CallbackInfo info) {
		AnotherQualityUtilsMod.LOGGER.info("Init AQUM");
	}
}
