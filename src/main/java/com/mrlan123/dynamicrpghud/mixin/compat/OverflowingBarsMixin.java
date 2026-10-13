package com.mrlan123.dynamicrpghud.mixin.compat;

import com.mrlan123.dynamicrpghud.HudPart;
import com.mrlan123.dynamicrpghud.HudPose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Overflowing Bars. Through Puzzles Lib it draws its bars from a "before layer" event that runs
 * BEFORE the vanilla HUD methods (so the wrappers around those never see them). The three layer
 * handlers are wrapped here with the matching vanilla element, so its health, armor and
 * toughness bars move, scale and hide with Health / Armor in the editor. Nothing is cancelled:
 * Overflowing Bars still runs its own layout bookkeeping.
 */
@Mixin(targets = "fuzs.overflowingbars.client.handler.GuiLayerHandler", remap = false)
public abstract class OverflowingBarsMixin {

    @Inject(method = "onRenderPlayerHealth", at = @At("HEAD"), remap = false, require = 0)
    private static void dynamicrpghud$healthBegin(CallbackInfoReturnable<Object> cir) {
        HudPose.beginPart(HudPart.HEALTH);
    }

    @Inject(method = "onRenderPlayerHealth", at = @At("RETURN"), remap = false, require = 0)
    private static void dynamicrpghud$healthEnd(CallbackInfoReturnable<Object> cir) {
        HudPose.end();
    }

    @Inject(method = "onRenderArmorLevel", at = @At("HEAD"), remap = false, require = 0)
    private static void dynamicrpghud$armorBegin(CallbackInfoReturnable<Object> cir) {
        HudPose.beginPart(HudPart.ARMOR);
    }

    @Inject(method = "onRenderArmorLevel", at = @At("RETURN"), remap = false, require = 0)
    private static void dynamicrpghud$armorEnd(CallbackInfoReturnable<Object> cir) {
        HudPose.end();
    }

    @Inject(method = "onRenderToughnessLevel", at = @At("HEAD"), remap = false, require = 0)
    private static void dynamicrpghud$toughnessBegin(CallbackInfoReturnable<Object> cir) {
        HudPose.beginPart(HudPart.ARMOR);
    }

    @Inject(method = "onRenderToughnessLevel", at = @At("RETURN"), remap = false, require = 0)
    private static void dynamicrpghud$toughnessEnd(CallbackInfoReturnable<Object> cir) {
        HudPose.end();
    }
}
