package com.mrlan123.dynamicrpghud.mixin;

import com.mrlan123.dynamicrpghud.HudAnimationState;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Minecraft 1.21.6+ has no global GUI translucency, so the "smooth fade-out" multiplies the colour
 * argument of the draw calls that HUD elements (vanilla and other mods') use: sprites, text and
 * filled rectangles. Only the overloads that every shorter overload ends up in are touched, so the
 * alpha is applied once. If a target is not found the injection is skipped silently and the element
 * simply disappears at the end of the fade instead.
 */
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsFadeMixin {

    @ModifyVariable(
            method = "blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 4, require = 0)
    private int dynamicrpghud$fadeSprite(int color) {
        return HudAnimationState.applyAlpha(color);
    }

    @ModifyVariable(
            method = "blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIIIIIII)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 8, require = 0)
    private int dynamicrpghud$fadeSliced(int color) {
        return HudAnimationState.applyAlpha(color);
    }

    @ModifyVariable(
            method = "drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 2, require = 0)
    private int dynamicrpghud$fadeText(int color) {
        return HudAnimationState.applyTextAlpha(color);
    }

    @ModifyVariable(
            method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 2, require = 0)
    private int dynamicrpghud$fadeComponent(int color) {
        return HudAnimationState.applyTextAlpha(color);
    }

    @ModifyVariable(
            method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 2, require = 0)
    private int dynamicrpghud$fadeSequence(int color) {
        return HudAnimationState.applyTextAlpha(color);
    }

    @ModifyVariable(
            method = "fill(Lcom/mojang/blaze3d/pipeline/RenderPipeline;IIIII)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 4, require = 0)
    private int dynamicrpghud$fadeFill(int color) {
        return HudAnimationState.applyAlpha(color);
    }
}
