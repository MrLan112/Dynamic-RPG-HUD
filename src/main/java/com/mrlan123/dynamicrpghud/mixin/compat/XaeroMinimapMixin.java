package com.mrlan123.dynamicrpghud.mixin.compat;

import com.mrlan123.dynamicrpghud.ExternalHud;
import com.mrlan123.dynamicrpghud.SupportedMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Xaero's Minimap: {@code MinimapRenderer.render(MinimapSession, ModuleRenderContext, GuiGraphics, float)} (descriptor taken from XaeroPlus' mixins; given for both Mojang and intermediary names).
 * Applied only when the mod is installed (see CompatMixinPlugin); the target is matched by name
 * at runtime, so this mod needs nothing of it at build time. If the target is missing the hook
 * is simply skipped.
 */
@Mixin(targets = "xaero.hud.minimap.module.MinimapRenderer", remap = false)
public abstract class XaeroMinimapMixin {

    @Inject(method = {"render(Lxaero/hud/minimap/module/MinimapSession;Lxaero/hud/render/module/ModuleRenderContext;Lnet/minecraft/client/gui/GuiGraphics;F)V", "render(Lxaero/hud/minimap/module/MinimapSession;Lxaero/hud/render/module/ModuleRenderContext;Lnet/minecraft/class_332;F)V"}, at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void dynamicrpghud$begin(CallbackInfo ci) {
        if (!ExternalHud.begin(SupportedMod.XAERO_MINIMAP)) {
            ci.cancel();
        }
    }

    @Inject(method = {"render(Lxaero/hud/minimap/module/MinimapSession;Lxaero/hud/render/module/ModuleRenderContext;Lnet/minecraft/client/gui/GuiGraphics;F)V", "render(Lxaero/hud/minimap/module/MinimapSession;Lxaero/hud/render/module/ModuleRenderContext;Lnet/minecraft/class_332;F)V"}, at = @At("RETURN"), remap = false, require = 0)
    private static void dynamicrpghud$end(CallbackInfo ci) {
        ExternalHud.end();
    }
}
