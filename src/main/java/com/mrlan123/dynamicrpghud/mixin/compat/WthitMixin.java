package com.mrlan123.dynamicrpghud.mixin.compat;

import com.mrlan123.dynamicrpghud.ExternalHud;
import com.mrlan123.dynamicrpghud.SupportedMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * WTHIT: {@code TooltipRenderer.renderUncached(GuiGraphics, DeltaTracker)} draws the tooltip itself (not the optional FPS counter that {@code render} also draws) (read from WTHIT's 1.21.1 source).
 * Applied only when the mod is installed (see CompatMixinPlugin); the target is matched by name
 * at runtime, so this mod needs nothing of it at build time. If the target is missing the hook
 * is simply skipped.
 */
@Mixin(targets = "mcp.mobius.waila.gui.hud.TooltipRenderer", remap = false)
public abstract class WthitMixin {

    @Inject(method = {"renderUncached"}, at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void dynamicrpghud$begin(CallbackInfo ci) {
        if (!ExternalHud.begin(SupportedMod.WTHIT)) {
            ci.cancel();
        }
    }

    @Inject(method = {"renderUncached"}, at = @At("RETURN"), remap = false, require = 0)
    private static void dynamicrpghud$end(CallbackInfo ci) {
        ExternalHud.end();
    }
}
