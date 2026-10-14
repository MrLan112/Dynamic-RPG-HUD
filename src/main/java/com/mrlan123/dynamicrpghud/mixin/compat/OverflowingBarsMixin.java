package com.mrlan123.dynamicrpghud.mixin.compat;

import com.mrlan123.dynamicrpghud.HudPart;
import com.mrlan123.dynamicrpghud.HudPose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Overflowing Bars (1.21.9+ builds). Through Puzzles Lib it REPLACES the vanilla health and armor
 * HUD elements with its own layer handlers, so the wrappers around the vanilla methods never see
 * its bars. In these versions the three handlers return void (in the 1.21.1 build they return an
 * event result), hence plain {@code CallbackInfo}. Each handler is wrapped with the matching
 * vanilla element, so the bars move, scale and hide with Health / Armor in the editor. Nothing is
 * cancelled: Overflowing Bars still runs its own layout bookkeeping.
 * Targets are matched by name only (the mod's classes carry no Minecraft names we could rely on).
 */
@Mixin(targets = "fuzs.overflowingbars.client.handler.GuiLayerHandler", remap = false)
public abstract class OverflowingBarsMixin {

    @Inject(method = "onRenderPlayerHealth", at = @At("HEAD"), remap = false, require = 0)
    private static void dynamicrpghud$healthBegin(CallbackInfo ci) {
        HudPose.beginPart(HudPart.HEALTH);
    }

    @Inject(method = "onRenderPlayerHealth", at = @At("RETURN"), remap = false, require = 0)
    private static void dynamicrpghud$healthEnd(CallbackInfo ci) {
        HudPose.end();
    }

    @Inject(method = "onRenderArmorLevel", at = @At("HEAD"), remap = false, require = 0)
    private static void dynamicrpghud$armorBegin(CallbackInfo ci) {
        HudPose.beginPart(HudPart.ARMOR);
    }

    @Inject(method = "onRenderArmorLevel", at = @At("RETURN"), remap = false, require = 0)
    private static void dynamicrpghud$armorEnd(CallbackInfo ci) {
        HudPose.end();
    }

    @Inject(method = "onRenderToughnessLevel", at = @At("HEAD"), remap = false, require = 0)
    private static void dynamicrpghud$toughnessBegin(CallbackInfo ci) {
        HudPose.beginPart(HudPart.ARMOR);
    }

    @Inject(method = "onRenderToughnessLevel", at = @At("RETURN"), remap = false, require = 0)
    private static void dynamicrpghud$toughnessEnd(CallbackInfo ci) {
        HudPose.end();
    }
}
