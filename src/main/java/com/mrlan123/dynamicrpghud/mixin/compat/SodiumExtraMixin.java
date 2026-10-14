package com.mrlan123.dynamicrpghud.mixin.compat;

import com.mrlan123.dynamicrpghud.ExternalHud;
import com.mrlan123.dynamicrpghud.SupportedMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sodium Extra: {@code SodiumExtraClientMod.onHudRender(GuiGraphics, DeltaTracker)} draws the FPS / coordinates overlay.
 * Applied only when the mod is installed (see CompatMixinPlugin); the target is matched by name
 * at runtime, so this mod needs nothing of it at build time. If the target is missing the hook
 * is simply skipped.
 */
@Mixin(targets = "me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod", remap = false)
public abstract class SodiumExtraMixin {

    @Inject(method = {"onHudRender"}, at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void dynamicrpghud$begin(CallbackInfo ci) {
        if (!ExternalHud.begin(SupportedMod.SODIUM_EXTRA)) {
            ci.cancel();
        }
    }

    @Inject(method = {"onHudRender"}, at = @At("RETURN"), remap = false, require = 0)
    private static void dynamicrpghud$end(CallbackInfo ci) {
        ExternalHud.end();
    }
}
