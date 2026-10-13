package com.mrlan123.dynamicrpghud.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mrlan123.dynamicrpghud.HudAnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * While an element is fading out, every {@code setShaderColor} call (the alpha argument) is
 * multiplied by the element's fade. Vanilla and other mods reset the colour to white inside their
 * drawing code, which used to make some elements pop back to fully opaque and then vanish at once
 * instead of fading. Optional: if the target is not found the old behaviour stays.
 */
@Mixin(RenderSystem.class)
public abstract class RenderSystemFadeMixin {

    @ModifyVariable(method = "setShaderColor", at = @At("HEAD"), argsOnly = true, ordinal = 3, require = 0)
    private static float dynamicrpghud$fadeShaderColor(float alpha) {
        float fade = HudAnimationState.fadeAlpha;
        return fade < 1.0f ? alpha * fade : alpha;
    }
}
