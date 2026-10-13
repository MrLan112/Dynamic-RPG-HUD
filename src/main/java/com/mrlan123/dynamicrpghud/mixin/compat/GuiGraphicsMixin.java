package com.mrlan123.dynamicrpghud.mixin.compat;

import com.mrlan123.dynamicrpghud.FpsDisplayHook;
import com.mrlan123.dynamicrpghud.HudCapture;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Reports the corners of what is drawn to {@link HudCapture} (only does anything while another
 * mod's HUD callback is running). Every injection is optional ({@code require = 0}): a missing
 * one only makes the measured box smaller, it never breaks the game.
 */
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {

    @Inject(method = "fill(Lnet/minecraft/client/renderer/RenderType;IIIIII)V", at = @At("HEAD"), require = 0)
    private void dynamicrpghud$fill(RenderType type, int x1, int y1, int x2, int y2, int z, int color, CallbackInfo ci) {
        if (HudCapture.active()) {
            HudCapture.rect((GuiGraphics) (Object) this, x1, y1, x2, y2);
        }
    }

    @Inject(method = "innerBlit(Lnet/minecraft/resources/ResourceLocation;IIIIIFFFF)V", at = @At("HEAD"), require = 0)
    private void dynamicrpghud$blit(ResourceLocation tex, int x1, int x2, int y1, int y2, int z,
                                    float u0, float u1, float v0, float v1, CallbackInfo ci) {
        if (HudCapture.active()) {
            HudCapture.rect((GuiGraphics) (Object) this, x1, y1, x2, y2);
        }
    }

    @Inject(method = "innerBlit(Lnet/minecraft/resources/ResourceLocation;IIIIIFFFFFFFF)V", at = @At("HEAD"), require = 0)
    private void dynamicrpghud$blitTinted(ResourceLocation tex, int x1, int x2, int y1, int y2, int z,
                                          float u0, float u1, float v0, float v1,
                                          float r, float g, float b, float a, CallbackInfo ci) {
        if (HudCapture.active()) {
            HudCapture.rect((GuiGraphics) (Object) this, x1, y1, x2, y2);
        }
    }

    @Inject(method = "drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I", at = @At("HEAD"),
            cancellable = true, require = 0)
    private void dynamicrpghud$text(Font font, String text, int x, int y, int color, boolean shadow,
                                    CallbackInfoReturnable<Integer> cir) {
        if (!FpsDisplayHook.begin(text)) { // FPS - Display's counter, hidden by the player
            cir.setReturnValue(0);
            return;
        }
        if (HudCapture.active() && text != null) {
            HudCapture.rect((GuiGraphics) (Object) this, x, y, x + font.width(text), y + 9);
        }
    }

    @Inject(method = "drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I", at = @At("RETURN"), require = 0)
    private void dynamicrpghud$textEnd(Font font, String text, int x, int y, int color, boolean shadow,
                                       CallbackInfoReturnable<Integer> cir) {
        FpsDisplayHook.end();
    }

    @Inject(method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)I",
            at = @At("HEAD"), require = 0)
    private void dynamicrpghud$textSeq(Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow,
                                       CallbackInfoReturnable<Integer> cir) {
        if (HudCapture.active()) {
            HudCapture.rect((GuiGraphics) (Object) this, x, y, x + font.width(text), y + 9);
        }
    }

    @Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;"
            + "Lnet/minecraft/world/item/ItemStack;IIII)V", at = @At("HEAD"), require = 0)
    private void dynamicrpghud$item(LivingEntity entity, Level level, ItemStack stack, int x, int y, int seed, int z,
                                    CallbackInfo ci) {
        if (HudCapture.active()) {
            HudCapture.rect((GuiGraphics) (Object) this, x, y, x + 16, y + 16);
        }
    }
}
