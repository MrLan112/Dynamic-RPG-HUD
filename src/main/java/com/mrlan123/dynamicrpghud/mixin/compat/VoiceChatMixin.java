package com.mrlan123.dynamicrpghud.mixin.compat;

import com.mrlan123.dynamicrpghud.ExternalHud;
import com.mrlan123.dynamicrpghud.SupportedMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Simple Voice Chat: {@code RenderEvents.onRenderHUD(GuiGraphics, float)} draws the microphone / group icons.
 * Applied only when the mod is installed (see CompatMixinPlugin); the target is matched by name
 * at runtime, so this mod needs nothing of it at build time. If the target is missing the hook
 * is simply skipped.
 */
@Mixin(targets = "de.maxhenkel.voicechat.voice.client.RenderEvents", remap = false)
public abstract class VoiceChatMixin {

    @Inject(method = {"onRenderHUD"}, at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void dynamicrpghud$begin(CallbackInfo ci) {
        if (!ExternalHud.begin(SupportedMod.VOICE_CHAT)) {
            ci.cancel();
        }
    }

    @Inject(method = {"onRenderHUD"}, at = @At("RETURN"), remap = false, require = 0)
    private static void dynamicrpghud$end(CallbackInfo ci) {
        ExternalHud.end();
    }
}
