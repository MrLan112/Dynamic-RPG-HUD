package com.mrlan123.dynamicrpghud.mixin.compat;

import com.mrlan123.dynamicrpghud.ExternalHud;
import com.mrlan123.dynamicrpghud.SupportedMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MiniHUD: {@code RenderHandler.onRenderGameOverlayPost(...)} draws the info lines.
 * Applied only when the mod is installed (see CompatMixinPlugin); the target is matched by name
 * at runtime, so this mod needs nothing of it at build time. If the target is missing the hook
 * is simply skipped.
 */
@Mixin(targets = "fi.dy.masa.minihud.event.RenderHandler", remap = false)
public abstract class MiniHudMixin {

    @Inject(method = {"onRenderGameOverlayPost"}, at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void dynamicrpghud$begin(CallbackInfo ci) {
        if (!ExternalHud.begin(SupportedMod.MINIHUD)) {
            ci.cancel();
        }
    }

    @Inject(method = {"onRenderGameOverlayPost"}, at = @At("RETURN"), remap = false, require = 0)
    private static void dynamicrpghud$end(CallbackInfo ci) {
        ExternalHud.end();
    }
}
